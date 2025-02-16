package io.github.devhector.mpi_execute_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.MakefileRequest;

@Service
public class KubernetesService {
  private final KubernetesClient kubernetesClient;

  @Autowired
  public KubernetesService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public String runAsync(JobRequest request) {
    return kubernetesClient.runAsync(request);
  }

  public String makefileRunner(MakefileRequest request) {
    return kubernetesClient.makefileRunner(request);
  }

}
