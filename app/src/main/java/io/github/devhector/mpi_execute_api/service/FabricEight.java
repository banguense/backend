package io.github.devhector.mpi_execute_api.service;

import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
      final String podName = "alpine-mpi";
      final String imageName = "localhost:32000/alpine-mpi:local";
      final String namespace = Optional.ofNullable(client.getNamespace()).orElse("default");
      final Pod pod = client.pods().inNamespace(namespace).resource(
          new PodBuilder()
              .withNewMetadata()
              .withName(podName)
              .withNamespace(namespace)
              .endMetadata()
              .withNewSpec()
              .addNewContainer()
              .withName(podName)
              .withImage(imageName)
              .withCommand("sh", "-c", command(request.getCode()))
              .endContainer()
              .withRestartPolicy("Never")
              .endSpec()
              .build())
          .create();
      logger.info("Pod is ready now");
      final LogWatch lw = client.pods().inNamespace(namespace).withName(pod.getMetadata().getName())
          .watchLog(System.out);
      TimeUnit.SECONDS.sleep(2L);
      String log = client.pods().inNamespace(namespace).withName(podName).getLog();
      logger.info("Watching Pod logs for 10 seconds...");
      logger.info("Deleting Pod...");
      client.resource(pod).inNamespace(namespace).delete();
      lw.close();
      logger.info("Closing Pod log watch");
      return log;
    } catch (Exception e) {
      e.printStackTrace();
      return e.getMessage().toString();
    }
  }

  private String command(String code) {
    return String.format(
        "echo '%s' > /tmp/code.c && gcc /tmp/code.c -o /tmp/code && ./tmp/code",
        code.replace("'", "'\"'\"'"));
  }

}
