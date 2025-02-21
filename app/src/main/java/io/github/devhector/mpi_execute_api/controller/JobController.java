package io.github.devhector.mpi_execute_api.controller;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.devhector.mpi_execute_api.exception.InvalidAccessKeyException;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.JobResponse;
import io.github.devhector.mpi_execute_api.model.MakefileRequest;
import io.github.devhector.mpi_execute_api.model.UploadResponse;
import io.github.devhector.mpi_execute_api.service.JobService;
import io.github.devhector.mpi_execute_api.service.StatusService;

@RestController
@RequestMapping("/api")
public class JobController {
  private JobService jobService;
  private StatusService statusService;

  @Autowired
  public JobController(JobService jobService, StatusService statusService) {
    this.jobService = jobService;
    this.statusService = statusService;
  }

  @PostMapping("/runAsync")
  public ResponseEntity<JobResponse> runAsync(@RequestBody JobRequest request) {
    request.setUuid(UUID.randomUUID().toString());
    jobService.runAsync(request);
    return ResponseEntity.ok(new JobResponse(request.getUuid()));
  }

  @GetMapping("/status/{uuid}")
  public ResponseEntity<JobResponse> status(@PathVariable String uuid) {
    Optional<JobResponse> jobResponse = statusService.check(uuid);

    return jobResponse.map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/upload")
  public ResponseEntity<UploadResponse> upload(@RequestParam("files") MultipartFile[] files) {
    String uuid = jobService.upload(files);
    return ResponseEntity.ok(new UploadResponse(uuid));
  }

  @PostMapping("/makefile")
  public ResponseEntity<JobResponse> makefile(@RequestBody MakefileRequest request) {
    jobService.makefileRunner(request);
    return ResponseEntity.ok(new JobResponse(request.getUuid()));
  }

  @ExceptionHandler(InvalidAccessKeyException.class)
  public ResponseEntity<Void> handleInvalidAccessKey() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

}
