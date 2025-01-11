package io.github.devhector.mpi_execute_api.service;

import io.fabric8.kubernetes.api.model.EmptyDirVolumeSource;
import io.fabric8.kubernetes.api.model.batch.v1.Job;
import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.model.JobRequest;

public class FabricEight implements KubernetesClient {
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
      Job job = jobBuilder.withNewMetadata().withName(request.getUuid()).endMetadata()
          .withNewSpec().withTtlSecondsAfterFinished(5)
          .withNewTemplate()
          .withNewMetadata().withName("mpi-job-template").endMetadata()
          .withNewSpec().addNewContainer()
          .withName("runner").withImage("openmpi-alpine:latest")
          .withCommand("sh", "-c",
              "echo '" + request.getCode() + "' > /code/main.c && gcc /code/main.c -o /code/main && /code/main")
          .addNewVolumeMount().withName("code-volume").withMountPath("/code").endVolumeMount().endContainer()
          .withRestartPolicy("Never")
          .addNewVolume().withName("code-volume").withEmptyDir(new EmptyDirVolumeSource()).endVolume()
          .endSpec().endTemplate().endSpec().build();

      client.batch().v1().jobs().resource(job).create();

      boolean completed = false;

      while (!completed) {
        Job currentJob = client.batch().v1().jobs().inNamespace("default").withName(request.getUuid()).get();

        if (currentJob.getStatus() != null && currentJob.getStatus().getSucceeded() != null
            && currentJob.getStatus().getSucceeded() == 1) {
          completed = true;
        }
        Thread.sleep(500);
      }

      String podName = client.pods().inNamespace("default").withLabel("job-name", request.getUuid()).list().getItems()
          .get(0).getMetadata().getName();

      String logs = client.pods().inNamespace("default").withName(podName).getLog();

      return logs;
    } catch (Exception e) {
      e.printStackTrace();
      return "erro";
    }
  }
}
