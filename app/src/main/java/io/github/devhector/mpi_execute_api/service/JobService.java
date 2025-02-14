package io.github.devhector.mpi_execute_api.service;

import io.github.devhector.mpi_execute_api.model.Job;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.JobResponse;
import io.github.devhector.mpi_execute_api.model.JobStatus;
import io.github.devhector.mpi_execute_api.model.MakefileRequest;
import io.github.devhector.mpi_execute_api.repository.JobRepository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.devhector.mpi_execute_api.exception.InvalidAccessKeyException;

@Service
public class JobService {
  private static final Logger logger = LoggerFactory.getLogger(JobService.class);

  @Value("${app.accessKey}")
  private String accessKey;

  @Value("${app.maxContainers}")
  private Integer maxContainers;

  private final KubernetesService kubernetesService;

  private final JobRepository jobRepository;

  @Autowired
  public JobService(KubernetesService kubernetesService, JobRepository jobRepository) {
    this.kubernetesService = kubernetesService;
    this.jobRepository = jobRepository;
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

    TimeWatch watch = TimeWatch.start();
    String output = kubernetesService.run(request);
    Long elapsedTimeInSecond = watch.time(TimeUnit.SECONDS);

    return new JobResponse(request.getNumberOfWorkers(), request.getUuid(), output, elapsedTimeInSecond);
  }

  public JobResponse makefileRunner(MakefileRequest request) {
    validate(request);

    request.setUuid(UUID.randomUUID().toString());

    return kubernetesService.makefileRunner(request);
  }

  public CompletableFuture<JobResponse> runAsync(JobRequest request) {
    validate(request);

    request.setUuid(UUID.randomUUID().toString());

    String output = null;
    Long elapsedTimeInSecond = 0L;

    Job job = new Job();
    job.setUuid(request.getUuid());
    job.setStatus(JobStatus.RUNNING);
    job.setElapsedTime(elapsedTimeInSecond);
    jobRepository.save(job);

    TimeWatch watch = TimeWatch.start();

    try {
      output = kubernetesService.run(request);
      elapsedTimeInSecond = watch.time(TimeUnit.SECONDS);

      job.setStatus(JobStatus.COMPLETED);
      job.setOutput(output);

    } catch (Exception e) {
      job.setStatus(JobStatus.FAILED);
      job.setOutput("Erro: " + e.getMessage());

      logger.error("Erro ao executar o job: " + request.getUuid(), e);
    } finally {
      job.setElapsedTime(elapsedTimeInSecond);
      jobRepository.save(job);
    }

    JobResponse response = new JobResponse(request.getNumberOfWorkers(), request.getUuid(), output,
        elapsedTimeInSecond);
    response.setStatus(job.getStatus());

    return CompletableFuture.completedFuture(response);
  }

  private void validate(JobRequest request) throws InvalidAccessKeyException {
    if (!accessKey.equals(request.getAccessKey())) {
      throw new InvalidAccessKeyException("Invalid Access Key!");
    }
    if (request.getNumberOfWorkers() > maxContainers) {
      request.setNumberOfWorkers(maxContainers);
    }
  }

  private void validate(MakefileRequest request) throws InvalidAccessKeyException {
    if (!accessKey.equals(request.getAccessKey())) {
      throw new InvalidAccessKeyException("Invalid Access Key!");
    }
    if (request.getNumberOfWorkers() > maxContainers) {
      request.setNumberOfWorkers(maxContainers);
    }
  }
}
