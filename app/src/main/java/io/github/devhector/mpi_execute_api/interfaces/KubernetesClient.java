package io.github.devhector.mpi_execute_api.interfaces;

import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.MakefileRequest;

public interface KubernetesClient {
  void createJob(JobRequest request);

  String run(JobRequest request);

  String makefileRunner(MakefileRequest request);
}
