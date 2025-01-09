package io.github.devhector.mpi_execute_api.service;

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

}
