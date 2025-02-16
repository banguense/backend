package io.github.devhector.mpi_execute_api.interfaces;

import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.MakefileRequest;

public interface KubernetesClient {
  String runAsync(JobRequest request);

  String makefileRunner(MakefileRequest request);
}
