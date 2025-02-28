package io.github.devhector.mpi_execute_api.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.github.devhector.mpi_execute_api.exception.InvalidAccessKeyException;
import io.github.devhector.mpi_execute_api.interfaces.Request;
import io.github.devhector.mpi_execute_api.model.Job;
import io.github.devhector.mpi_execute_api.model.JobRequest;
import io.github.devhector.mpi_execute_api.model.JobStatus;
import io.github.devhector.mpi_execute_api.model.MakefileRequest;
import io.github.devhector.mpi_execute_api.repository.JobRepository;

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

  @Async("taskExecutor")
  public void makefileRunner(MakefileRequest request) {
    validate(request);

    String output = null;
    Long elapsedTimeInSecond = 0L;

    Job job = new Job();
    job.setUuid(request.getUuid());
    job.setStatus(JobStatus.RUNNING);
    job.setElapsedTime(elapsedTimeInSecond);
    jobRepository.save(job);

    try {
      TimeWatch watch = TimeWatch.start();
      output = kubernetesService.makefileRunner(request);
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
  }

  @Async("taskExecutor")
  public void runAsync(JobRequest request) {
    validate(request);

    String output = null;
    Long elapsedTimeInSecond = 0L;

    Job job = new Job();
    job.setUuid(request.getUuid());
    job.setStatus(JobStatus.RUNNING);
    job.setElapsedTime(elapsedTimeInSecond);
    jobRepository.save(job);

    try {
      TimeWatch watch = TimeWatch.start();
      output = kubernetesService.runAsync(request);
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
  }

  public String upload(MultipartFile[] files) {
    String uuid = UUID.randomUUID().toString();
    String UPLOAD_DIR = "/mnt/nfs_mount/master-" + uuid.substring(0, 5) + "/";

    try {
      Files.createDirectory(Path.of(UPLOAD_DIR));

      Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
      Files.setPosixFilePermissions(Path.of(UPLOAD_DIR), permissions);

      for (MultipartFile file : files) {
        Path filePath = Path.of(UPLOAD_DIR, file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (Exception e) {
      logger.error("Erro ao fazer o upload da requisicao: " + uuid, e);
    }
    return uuid;
  }

  private void validate(Request request)
      throws InvalidAccessKeyException {
    if (!accessKey.equals(request.getAccessKey())) {
      throw new InvalidAccessKeyException("Invalid Access Key!");
    }
    if (request.getNumberOfWorkers() > maxContainers) {
      request.setNumberOfWorkers(maxContainers);
    }
  }
}
