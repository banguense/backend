package io.github.devhector.mpi_execute_api.service;

import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.model.JobRequest;

public class KubernetesService {
  private final KubernetesClient kubernetesClient;

  public KubernetesService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public void createJob(JobRequest request) {
    kubernetesClient.createJob(request);
  }

}
