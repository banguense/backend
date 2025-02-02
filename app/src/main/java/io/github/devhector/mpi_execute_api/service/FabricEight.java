package io.github.devhector.mpi_execute_api.service;

import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.MakefileRequest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.fabric8.kubernetes.api.model.Pod;

public class FabricEight implements KubernetesClient {
  private static final Logger logger = LoggerFactory.getLogger(FabricEight.class);

  private final KubernetesClientBuilder clientBuilder;
  private final JobBuilder jobBuilder;

  public FabricEight(KubernetesClientBuilder clientBuilder, JobBuilder jobBuilder) {
    this.clientBuilder = clientBuilder;
    this.jobBuilder = jobBuilder;
  }

  @Override
  public void createJob(JobRequest request) {
    try (io.fabric8.kubernetes.client.KubernetesClient client = clientBuilder.build()) {
      Job job = jobBuilder.withNewMetadata().withName(request.getUuid()).withNamespace("default").endMetadata()
          .withNewSpec().withNewTemplate().withNewSpec().addNewContainer().withName("busybox").withImage("busybox")
          .withCommand("echo", "Hello world!").endContainer().withRestartPolicy("Never").endSpec().endTemplate()
          .endSpec().build();

      client.batch().v1().jobs().resource(job).create();
    }
  }

  @Override
  public String makefileRunner(MakefileRequest request) {

    try (io.fabric8.kubernetes.client.KubernetesClient client = clientBuilder.build()) {
      String uuid = request.getUuid().substring(0, 5);
      String podName = "master-" + uuid;
      String imageName = "localhost:32000/alpine-mpi:v0.2";
      final String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");

      validate(request);

      List<String> podNames = createWorkerPods(request, client, uuid, imageName, namespace);

      logger.info("waiting worker pods to be running");
      podNames.forEach(name -> client.pods()
          .inNamespace(namespace)
          .withName(name)
          .waitUntilCondition(
              pod -> pod != null && "Running".equalsIgnoreCase(pod.getStatus().getPhase()),
              60L,
              TimeUnit.SECONDS));

      List<String> hostAddresses = getHostsFrom(client, namespace, podNames);
      logger.info("list of hosts: " + hostAddresses.toString());

      Pod pod = createMasterPod(request, client, uuid, podName, imageName, namespace, hostAddresses);

      logger.info("Master Pod is ready now");
      final LogWatch lw = client.pods().inNamespace(namespace).withName(pod.getMetadata().getName())
          .watchLog(System.out);

      client.pods().inNamespace(namespace).withName(podName).waitUntilCondition(
          masterPod -> masterPod != null &&
              "Succeeded".equalsIgnoreCase(masterPod.getStatus().getPhase()) ||
              "Failed".equalsIgnoreCase(masterPod.getStatus().getPhase()),
          10L,
          TimeUnit.SECONDS);

      String log = client.pods().inNamespace(namespace).withName(podName).getLog();
      lw.close();
      logger.info("Closing Master Pod log watch");

      logger.info("Deleting all pods");
      podNames.forEach(name -> client.pods().inNamespace(namespace).withName(name).delete());
      client.pods().inNamespace(namespace).withName(podName).delete();

      return filter(log);
    } catch (Exception e) {
      e.printStackTrace();
      return e.getMessage().toString();
    }
  }

  @Override
  public String run(JobRequest request) {
    try (io.fabric8.kubernetes.client.KubernetesClient client = clientBuilder.build()) {
      String uuid = request.getUuid().substring(0, 5);
      String podName = "master-" + uuid;
      String imageName = "localhost:32000/alpine-mpi:v0.2";
      final String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");

      validate(request);

      List<String> podNames = createWorkerPods(request, client, uuid, imageName, namespace);

      logger.info("waiting worker pods to be running");
      podNames.forEach(name -> client.pods()
          .inNamespace(namespace)
          .withName(name)
          .waitUntilCondition(
              pod -> pod != null && "Running".equalsIgnoreCase(pod.getStatus().getPhase()),
              60L,
              TimeUnit.SECONDS));

      List<String> hostAddresses = getHostsFrom(client, namespace, podNames);
      logger.info("list of hosts: " + hostAddresses.toString());

      Pod pod = createMasterPod(request, client, uuid, podName, imageName, namespace, hostAddresses);

      logger.info("Master Pod is ready now");
      final LogWatch lw = client.pods().inNamespace(namespace).withName(pod.getMetadata().getName())
          .watchLog(System.out);

      client.pods().inNamespace(namespace).withName(podName).waitUntilCondition(
          masterPod -> masterPod != null &&
              "Succeeded".equalsIgnoreCase(masterPod.getStatus().getPhase()) ||
              "Failed".equalsIgnoreCase(masterPod.getStatus().getPhase()),
          10L,
          TimeUnit.SECONDS);

      String log = client.pods().inNamespace(namespace).withName(podName).getLog();
      lw.close();
      logger.info("Closing Master Pod log watch");

      logger.info("Deleting all pods");
      podNames.forEach(name -> client.pods().inNamespace(namespace).withName(name).delete());
      client.pods().inNamespace(namespace).withName(podName).delete();

      return filter(log);
    } catch (Exception e) {
      e.printStackTrace();
      return e.getMessage().toString();
    }
  }

  private String filter(String input) {
    return input.lines()
        .filter(line -> !line.startsWith("Warning: Permanently added"))
        .reduce((a, b) -> a + "\n" + b)
        .orElse("");
  }

  private Pod createMasterPod(JobRequest request, io.fabric8.kubernetes.client.KubernetesClient client, String uuid,
      String podName, String imageName, final String namespace, List<String> hostAddresses) {
    Pod pod = client.pods().inNamespace(namespace).resource(
        new PodBuilder()
            .withNewMetadata()
            .withName(podName)
            .withNamespace(namespace)
            .addToLabels("mpi", uuid)
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName(podName)
            .withImage(imageName)
            .withCommand("sh", "-c",
                command(request.getCode(), request.getNumberOfProcess(), "/shared-nfs/" +
                    podName, String.join(",", hostAddresses)))
            .addNewPort()
            .withContainerPort(22)
            .endPort()
            .addNewVolumeMount()
            .withName("nfs-volume")
            .withMountPath("/shared-nfs")
            .endVolumeMount()
            .endContainer()
            .withRestartPolicy("Never")
            .addNewVolume()
            .withName("nfs-volume")
            .withNewPersistentVolumeClaim()
            .withClaimName("nfs-pvc")
            .endPersistentVolumeClaim()
            .endVolume()
            .endSpec()
            .build())
        .create();
    return pod;
  }

  private Pod createMasterPod(MakefileRequest request, io.fabric8.kubernetes.client.KubernetesClient client,
      String uuid,
      String podName, String imageName, final String namespace, List<String> hostAddresses) {
    Pod pod = client.pods().inNamespace(namespace).resource(
        new PodBuilder()
            .withNewMetadata()
            .withName(podName)
            .withNamespace(namespace)
            .addToLabels("mpi", uuid)
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName(podName)
            .withImage(imageName)
            .withCommand("sh", "-c",
                command(request.getCode(), request.getMakefile(), "/shared-nfs/" +
                    podName, String.join(",", hostAddresses)))
            .addNewPort()
            .withContainerPort(22)
            .endPort()
            .addNewVolumeMount()
            .withName("nfs-volume")
            .withMountPath("/shared-nfs")
            .endVolumeMount()
            .endContainer()
            .withRestartPolicy("Never")
            .addNewVolume()
            .withName("nfs-volume")
            .withNewPersistentVolumeClaim()
            .withClaimName("nfs-pvc")
            .endPersistentVolumeClaim()
            .endVolume()
            .endSpec()
            .build())
        .create();
    return pod;
  }

  private List<String> createWorkerPods(JobRequest request, io.fabric8.kubernetes.client.KubernetesClient client,
      String uuid, String imageName, final String namespace) {
    List<String> podNames = new ArrayList<>();
    for (int i = 0; i < request.getNumberOfWorkers(); i++) {
      String workerPodName = String.format("worker-%s-%d", uuid.subSequence(0, 5), i);
      client.pods().inNamespace(namespace).resource(
          new PodBuilder()
              .withNewMetadata()
              .withName(workerPodName)
              .withNamespace(namespace)
              .addToLabels("mpi", uuid)
              .endMetadata()
              .withNewSpec()
              .addNewContainer()
              .withName("alpine-mpi")
              .withImage(imageName)
              .withCommand("sh", "-c", "/usr/sbin/sshd -D && tail -f /dev/null")
              .addNewPort()
              .withContainerPort(22)
              .endPort()
              .addNewVolumeMount()
              .withName("nfs-volume")
              .withMountPath("/shared-nfs")
              .endVolumeMount()
              .endContainer()
              .withRestartPolicy("Never")
              .addNewVolume()
              .withName("nfs-volume")
              .withNewPersistentVolumeClaim()
              .withClaimName("nfs-pvc")
              .endPersistentVolumeClaim()
              .endVolume()
              .endSpec()
              .build())
          .create();
      podNames.add(workerPodName);
    }
    return podNames;
  }

  private List<String> createWorkerPods(MakefileRequest request, io.fabric8.kubernetes.client.KubernetesClient client,
      String uuid, String imageName, final String namespace) {
    List<String> podNames = new ArrayList<>();
    for (int i = 0; i < request.getNumberOfWorkers(); i++) {
      String workerPodName = String.format("worker-%s-%d", uuid.subSequence(0, 5), i);
      client.pods().inNamespace(namespace).resource(
          new PodBuilder()
              .withNewMetadata()
              .withName(workerPodName)
              .withNamespace(namespace)
              .addToLabels("mpi", uuid)
              .endMetadata()
              .withNewSpec()
              .addNewContainer()
              .withName("alpine-mpi")
              .withImage(imageName)
              .withCommand("sh", "-c", "/usr/sbin/sshd -D && tail -f /dev/null")
              .addNewPort()
              .withContainerPort(22)
              .endPort()
              .addNewVolumeMount()
              .withName("nfs-volume")
              .withMountPath("/shared-nfs")
              .endVolumeMount()
              .endContainer()
              .withRestartPolicy("Never")
              .addNewVolume()
              .withName("nfs-volume")
              .withNewPersistentVolumeClaim()
              .withClaimName("nfs-pvc")
              .endPersistentVolumeClaim()
              .endVolume()
              .endSpec()
              .build())
          .create();
      podNames.add(workerPodName);
    }
    return podNames;
  }

  private void validate(JobRequest request) {
    if (request.getNumberOfWorkers() <= 0 || request.getNumberOfProcess() <= 0) {
      throw new IllegalArgumentException("Número de container e processos deve ser maior que 0");
    }
  }

  private void validate(MakefileRequest request) {
    if (request.getNumberOfWorkers() <= 0) {
      throw new IllegalArgumentException("Número de container deve ser maior que 0");
    }
  }

  private List<String> getHostsFrom(io.fabric8.kubernetes.client.KubernetesClient client, final String namespace,
      List<String> podNames) {
    return podNames.stream()
        .map(name -> client.pods().inNamespace(namespace).withName(name).get())
        .filter(worker -> worker.getStatus() != null &&
            "Running".equals(worker.getStatus().getPhase()))
        .map(worker -> worker.getStatus().getPodIP())
        .collect(Collectors.toList());
  }

  private String command(String code, int numProcesses, String path, String hosts) {
    String base64 = Base64.getEncoder().encodeToString(code.getBytes());
    return String.format(
        "mkdir %s &&" +
            " echo '%s' | base64 -d > %s/code.c &&" +
            " mpicc %s/code.c -o %s/code &&" +
            " mpirun --allow-run-as-root --oversubscribe -np %d  -host %s %s/code &&" +
            " rm -rf %s",
        path,
        base64,
        path,
        path,
        path,
        numProcesses,
        hosts,
        path,
        path);
  }

  private String command(String code, String makefile, String path, String hosts) {
    final String hostsMakefile = String.format("HOSTS = --allow-run-as-root --oversubscribe -host %s \n", hosts);
    String makefileContent = hostsMakefile + makefile;
    String codeEncoded = Base64.getEncoder().encodeToString(code.getBytes());
    String makefileEncoded = Base64.getEncoder().encodeToString(makefileContent.getBytes());

    return String.format(
        "mkdir -p %s &&" +
            " echo '%s' | base64 -d > %s/code.c &&" +
            " echo '%s' | base64 -d > %s/Makefile &&" +
            "cd %s && make &&" +
            "cd .. && rm -rf %s",
        path, codeEncoded, path, makefileEncoded, path, path, path);
  }

}
