package io.github.devhector.mpi_execute_api.interfaces;

import io.github.devhector.mpi_execute_api.model.JobRequest;

public interface KubernetesClient {
  void createJob(JobRequest request);

  String run(JobRequest request);
}
