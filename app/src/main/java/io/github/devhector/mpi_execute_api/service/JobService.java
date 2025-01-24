package io.github.devhector.mpi_execute_api.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.devhector.mpi_execute_api.exception.InvalidAccessKeyException;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.JobResponse;

@Service
public class JobService {
  private final String accessKey = "";
  private final KubernetesService kubernetesService;

  @Autowired
  public JobService(KubernetesService kubernetesService) {
    this.kubernetesService = kubernetesService;
  }

  public JobResponse createJob(JobRequest request) {
    if (!accessKey.equals(request.getAccessKey())) {
      throw new InvalidAccessKeyException("Invalid Access Key!");
    }

    request.setUuid(UUID.randomUUID().toString());

    kubernetesService.createJob(request);

    return new JobResponse(request.getUuid());
  }

  public JobResponse run(JobRequest request) {
    if (!accessKey.equals(request.getAccessKey())) {
      throw new InvalidAccessKeyException("Invalid Access Key!");
    }

    request.setUuid(UUID.randomUUID().toString());

    return kubernetesService.run(request);
  }
}
