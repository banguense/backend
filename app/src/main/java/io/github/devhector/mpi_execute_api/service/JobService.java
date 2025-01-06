package io.github.devhector.mpi_execute_api.service;

import java.util.UUID;

import io.github.devhector.mpi_execute_api.model.JobEntity;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.JobResponse;
import io.github.devhector.mpi_execute_api.repository.JobRepository;

public class JobService {
  private final KubernetesService kubernetesService;
  private final JobRepository repository;

  public JobService(KubernetesService kubernetesService, JobRepository repository) {
    this.kubernetesService = kubernetesService;
    this.repository = repository;
  }

  public JobResponse createJob(JobRequest request) {
    String uuid = UUID.randomUUID().toString();

    kubernetesService.createJob(request);

    JobEntity entity = new JobEntity.Builder()
        .uuid(uuid).status("PENDING").workers(request.getNumberOfWorkers())
        .processess(request.getNumberOfProcess()).code(request.getCode()).build();

    repository.save(entity);

    return new JobResponse(uuid);
  }
}
