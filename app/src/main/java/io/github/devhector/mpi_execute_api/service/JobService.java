package io.github.devhector.mpi_execute_api.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.devhector.mpi_execute_api.model.JobEntity;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.JobResponse;
import io.github.devhector.mpi_execute_api.model.StatusResponse;
import io.github.devhector.mpi_execute_api.repository.JobRepository;

@Service
public class JobService {
  private final KubernetesService kubernetesService;
  private final JobRepository repository;

  @Autowired
  public JobService(KubernetesService kubernetesService, JobRepository repository) {
    this.kubernetesService = kubernetesService;
    this.repository = repository;
  }

  public JobResponse createJob(JobRequest request) {
    request.setUuid(UUID.randomUUID().toString());

    kubernetesService.createJob(request);

    JobEntity entity = new JobEntity.Builder()
        .uuid(request.getUuid()).status("PENDING").workers(request.getNumberOfWorkers())
        .processess(request.getNumberOfProcess()).code(request.getCode()).build();

    repository.save(entity);

    return new JobResponse(request.getUuid());
  }

  public StatusResponse jobStatus(String uuid) {
    Optional<JobEntity> entityOptional = repository.findByUuid(uuid);

    if (entityOptional.isPresent()) {
      JobEntity entity = entityOptional.get();
      return new StatusResponse(entity.getUuid(), entity.getStatus(), entity.getErrorMessage());
    }

    return new StatusResponse(uuid, "NOT_FOUND", "Job not found for the given UUID!");
  }

  public JobResponse run(JobRequest request) {
    request.setUuid(UUID.randomUUID().toString());

    return kubernetesService.run(request);
  }
}
