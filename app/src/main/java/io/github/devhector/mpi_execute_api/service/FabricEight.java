package io.github.devhector.mpi_execute_api.service;

import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.model.JobRequest;

public class FabricEight implements KubernetesClient {
  private final KubernetesClientBuilder builder;

  public FabricEight(KubernetesClientBuilder builder) {
    this.builder = builder;
  }

  public void createJob(JobRequest request) {
    try (io.fabric8.kubernetes.client.KubernetesClient client = builder.build()) {
    }
  }

}
