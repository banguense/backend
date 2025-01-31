package io.github.devhector.mpi_execute_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.JobResponse;
import io.github.devhector.mpi_execute_api.model.MakefileRequest;

@Service
public class KubernetesService {
  private final KubernetesClient kubernetesClient;

  @Autowired
  public KubernetesService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public void createJob(JobRequest request) {
    kubernetesClient.createJob(request);
  }

  public JobResponse run(JobRequest request) {
    String result = kubernetesClient.run(request);
    return new JobResponse(request.getUuid(), result);
  }

  public JobResponse makefileRunner(MakefileRequest request) {
    String result = kubernetesClient.makefileRunner(request);
    return new JobResponse(request.getUuid(), result);
  }

}
