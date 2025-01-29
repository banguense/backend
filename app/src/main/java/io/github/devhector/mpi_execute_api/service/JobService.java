package io.github.devhector.mpi_execute_api.service;

import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.JobResponse;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.devhector.mpi_execute_api.exception.InvalidAccessKeyException;

@Service
public class JobService {
  @Value("${app.accessKey}")
  private String accessKey;
  @Value("${app.maxContainers}")
  private Integer maxContainers;
  private final KubernetesService kubernetesService;

  @Autowired
  public JobService(KubernetesService kubernetesService) {
    this.kubernetesService = kubernetesService;
  }

  public JobResponse createJob(JobRequest request) {
    validate(request);

    request.setUuid(UUID.randomUUID().toString());

    kubernetesService.createJob(request);

    return new JobResponse(request.getUuid());
  }

  public JobResponse run(JobRequest request) {
    validate(request);

    request.setUuid(UUID.randomUUID().toString());

    return kubernetesService.run(request);
  }

  private void validate(JobRequest request) throws InvalidAccessKeyException {
    if (!accessKey.equals(request.getAccessKey())) {
      throw new InvalidAccessKeyException("Invalid Access Key!");
    }
    if (request.getNumberOfWorkers() > maxContainers) {
      request.setNumberOfWorkers(maxContainers);
    }
  }
}
