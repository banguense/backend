package io.github.devhector.mpi_execute_api.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.MakefileRequest;

public class FabricEight implements KubernetesClient {
  private static final Logger logger = LoggerFactory.getLogger(FabricEight.class);

  private final KubernetesClientBuilder clientBuilder;

  public FabricEight(KubernetesClientBuilder clientBuilder) {
    this.clientBuilder = clientBuilder;
  }

  @Override
  public String makefileRunner(MakefileRequest request) {
    try (io.fabric8.kubernetes.client.KubernetesClient client = clientBuilder.build()) {
      final LogWatch lw;
      final String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");
      String log;
      String uuid = request.getUuid().substring(0, 5);
      String podName = "master-" + uuid;
      String imageName = "localhost:32000/alpine-mpi:v0.2";
      ArrayList<String> allPods = new ArrayList<>();

      try {
        allPods.add(podName);
        List<String> podNames = createWorkerPods(request, client, uuid, imageName, namespace);

        logger.info("waiting worker pods to be running");
        podNames.forEach(name -> client.pods()
            .inNamespace(namespace)
            .withName(name)
            .waitUntilCondition(
                pod -> pod != null && "Running".equalsIgnoreCase(pod.getStatus().getPhase()),
                120960000L,
                TimeUnit.SECONDS));

        allPods.addAll(podNames);
        List<String> hostAddresses = getHostsFrom(client, namespace, podNames);
        logger.info("list of hosts: " + hostAddresses.toString());

        Pod pod = createMasterPod(request, client, uuid, podName, imageName, namespace, hostAddresses);

        logger.info("Master Pod is ready now");
        lw = client.pods().inNamespace(namespace).withName(pod.getMetadata().getName())
            .watchLog(System.out);

        client.pods().inNamespace(namespace).withName(podName).waitUntilCondition(
            masterPod -> masterPod != null &&
                "Succeeded".equalsIgnoreCase(masterPod.getStatus().getPhase()) ||
                "Failed".equalsIgnoreCase(masterPod.getStatus().getPhase()),
            120960000L,
            TimeUnit.SECONDS);

        log = client.pods().inNamespace(namespace).withName(podName).getLog();
        logger.info("Closing Master Pod log watch");
        lw.close();
      } catch (Exception e) {
        e.printStackTrace();
        throw new KubernetesClientException("Operacao nao realizada", e);
      } finally {
        logger.info("Deleting all pods");
        allPods.forEach(name -> client.pods().inNamespace(namespace).withName(name).delete());
        client.pods().inNamespace(namespace).withName(podName).delete();
      }
      return filter(log);
    } catch (Exception e) {
      e.printStackTrace();
      throw new KubernetesClientException("Não foi possível completar a operação", e);
    }
  }

  @Override
  public String runAsync(JobRequest request) {
    try (io.fabric8.kubernetes.client.KubernetesClient client = clientBuilder.build()) {
      final LogWatch lw;
      final String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");
      String log;
      String uuid = request.getUuid().substring(0, 5);
      String podName = "master-" + uuid;
      String imageName = "localhost:32000/alpine-mpi:v0.2";
      ArrayList<String> allPods = new ArrayList<>();

      try {
        allPods.add(podName);
        List<String> podNames = createWorkerPods(request, client, uuid, imageName, namespace);

        logger.info("waiting worker pods to be running");
        podNames.forEach(name -> client.pods()
            .inNamespace(namespace)
            .withName(name)
            .waitUntilCondition(
                pod -> pod != null && "Running".equalsIgnoreCase(pod.getStatus().getPhase()),
                120960000L,
                TimeUnit.SECONDS));

        allPods.addAll(podNames);
        List<String> hostAddresses = getHostsFrom(client, namespace, podNames);
        logger.info("list of hosts: " + hostAddresses.toString());

        Pod pod = createMasterPod(request, client, uuid, podName, imageName, namespace, hostAddresses);

        logger.info("Master Pod is ready now");
        lw = client.pods().inNamespace(namespace).withName(pod.getMetadata().getName())
            .watchLog(System.out);

        client.pods().inNamespace(namespace).withName(podName).waitUntilCondition(
            masterPod -> masterPod != null &&
                "Succeeded".equalsIgnoreCase(masterPod.getStatus().getPhase()) ||
                "Failed".equalsIgnoreCase(masterPod.getStatus().getPhase()),
            120960000L,
            TimeUnit.SECONDS);

        log = client.pods().inNamespace(namespace).withName(podName).getLog();
        logger.info("Closing Master Pod log watch");
        lw.close();
      } catch (Exception e) {
        e.printStackTrace();
        throw new KubernetesClientException("Operacao nao realizada", e);
      } finally {
        logger.info("Deleting all pods");
        allPods.forEach(name -> client.pods().inNamespace(namespace).withName(name).delete());
        client.pods().inNamespace(namespace).withName(podName).delete();
      }
      return filter(log);
    } catch (Exception e) {
      e.printStackTrace();
      throw new KubernetesClientException("Não foi possível completar a operação", e);
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
                command(request, "/shared-nfs/" + podName, String.join(",", hostAddresses)))
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
            .withClaimName("nfs-root-pvc")
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
                command(request.getMakefile(), "/shared-nfs/" +
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
            .withClaimName("nfs-root-pvc")
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
              .withClaimName("nfs-root-pvc")
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
              .withClaimName("nfs-root-pvc")
              .endPersistentVolumeClaim()
              .endVolume()
              .endSpec()
              .build())
          .create();
      podNames.add(workerPodName);
    }
    return podNames;
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

  private String command(JobRequest request, String path, String hosts) {
    String base64 = Base64.getEncoder().encodeToString(request.getCode().getBytes());
    return String.format(
        "mkdir %s &&" +
            " echo '%s' | base64 -d > %s/code.c &&" +
            " mpicc %s/code.c -o %s/code %s &&" +
            " mpirun --allow-run-as-root --oversubscribe -np %d -host %s %s/code %s | tee %s/output",
        path,
        base64,
        path,
        path,
        path,
        request.getCompilationDirective(),
        request.getNumberOfProcess(),
        hosts,
        path,
        request.getArguments(),
        path);
  }

  private String command(String makefile, String path, String hosts) {
    final String hostsMakefile = String.format("HOSTS = --allow-run-as-root --oversubscribe -host %s \n", hosts);
    String makefileContent = hostsMakefile + makefile;
    String makefileEncoded = Base64.getEncoder().encodeToString(makefileContent.getBytes());

    return String.format(
        " echo '%s' | base64 -d > %s/Makefile &&" +
            "cd %s && make | tee output",
        makefileEncoded, path, path);
  }

}
