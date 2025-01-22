package io.github.devhector.mpi_execute_api.service;

import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.model.JobRequest;
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
  public String run(JobRequest request) {
    try (io.fabric8.kubernetes.client.KubernetesClient client = clientBuilder.build()) {
      String uuid = request.getUuid().substring(0, 5);
      String podName = "master-" + uuid;
      String imageName = "localhost:32000/alpine-mpi:v0.2";
      final String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");

      List<String> podNames = new ArrayList<>();
      for (int i = 0; i < request.getNumberOfWorkers(); i++) {
        String workerPodName = String.format("worker-%s-%d", uuid.subSequence(0, 5), i);
        client.pods().inNamespace(namespace).resource(
            new PodBuilder()
                .withNewMetadata()
                .withName(workerPodName)
                .withNamespace(namespace)
                .addToLabels("app", "mpi-worker")
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

      // client.pods().inNamespace(namespace).withName(podNames.getLast()).waitUntilReady(10L,
      // TimeUnit.SECONDS);
      logger.info("workers pod created and wait 40s " + podNames.getLast());
      client.pods()
          .inNamespace(namespace)
          .withName(podNames.getLast())
          .waitUntilCondition(
              pod -> pod != null && "Running".equalsIgnoreCase(pod.getStatus().getPhase()),
              40L,
              TimeUnit.SECONDS);

      List<String> hostAddresses = getHostsFrom(client, namespace, podNames);

      logger.info("list of hosts: " + hostAddresses.toString());

      Pod pod = client.pods().inNamespace(namespace).resource(
          new PodBuilder()
              .withNewMetadata()
              .withName(podName)
              .withNamespace(namespace)
              .addToLabels("app", "mpi-worker")
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

      logger.info("Pod is ready now");
      final LogWatch lw = client.pods().inNamespace(namespace).withName(pod.getMetadata().getName())
          .watchLog(System.out);

      TimeUnit.SECONDS.sleep(5L);
      String log = client.pods().inNamespace(namespace).withName(podName).getLog();
      lw.close();
      logger.info("Closing Pod log watch");

      podNames.forEach(name -> client.pods().inNamespace(namespace).withName(name).delete());
      client.pods().inNamespace(namespace).withName(podName).delete();
      client.resource(pod).inNamespace(namespace).delete();
      logger.info("Deleting all pods");

      return log;
    } catch (Exception e) {
      e.printStackTrace();
      return e.getMessage().toString();
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
        "mkdir %s && echo '%s' | base64 -d > %s/code.c && mpicc %s/code.c -o %s/code && mpirun --allow-run-as-root --oversubscribe -np %d  -host %s %s/code && rm -rf %s",
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

}
