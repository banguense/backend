package io.github.devhector.mpi_execute_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.devhector.mpi_execute_api.exception.InvalidAccessKeyException;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.JobResponse;
import io.github.devhector.mpi_execute_api.service.JobService;

@RestController
@RequestMapping("/api")
public class JobController {
  private JobService jobService;

  @Autowired
  public JobController(JobService jobService) {
    this.jobService = jobService;
  }

  @PostMapping("/submit")
  public ResponseEntity<JobResponse> createJob(@RequestBody JobRequest request) {
    JobResponse response = jobService.createJob(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/run")
  public ResponseEntity<JobResponse> run(@RequestBody JobRequest request) {
    JobResponse response = jobService.run(request);
    return ResponseEntity.ok(response);
  }

  @ExceptionHandler(InvalidAccessKeyException.class)
  public ResponseEntity<Void> handleInvalidAccessKey() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

}
