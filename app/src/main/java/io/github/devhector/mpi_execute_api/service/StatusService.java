package io.github.devhector.mpi_execute_api.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.github.devhector.mpi_execute_api.model.JobResponse;
import io.github.devhector.mpi_execute_api.repository.JobRepository;

@Service
public class StatusService {
  private static final Logger logger = LoggerFactory.getLogger(StatusService.class);

  private final JobRepository jobRepository;

  public StatusService(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  public Optional<JobResponse> check(String uuid) {
    logger.info("Checagem do uuid: " + uuid);
    return jobRepository.findByUuid(uuid)
        .map(job -> new JobResponse(
            job.getUuid(),
            job.getOutput(),
            job.getElapsedTime(),
            job.getNumberOfWorkers(),
            job.getStatus()));
  }

}
