package io.github.devhector.mpi_execute_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.devhector.mpi_execute_api.model.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
  Job findByUuid(String uuid);
}
