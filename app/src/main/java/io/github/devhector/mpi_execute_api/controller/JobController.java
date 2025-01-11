package io.github.devhector.mpi_execute_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.JobResponse;
import io.github.devhector.mpi_execute_api.model.StatusResponse;
import io.github.devhector.mpi_execute_api.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

  @GetMapping("/status")
  public ResponseEntity<StatusResponse> jobStatus(@RequestParam String uuid) {
    StatusResponse status = jobService.jobStatus(uuid);
    return ResponseEntity.ok(status);
  }

  @PostMapping("/run")
  public ResponseEntity<JobResponse> run(@RequestBody JobRequest request) {
    JobResponse response = jobService.run(request);
    return ResponseEntity.ok(response);
  }

}
