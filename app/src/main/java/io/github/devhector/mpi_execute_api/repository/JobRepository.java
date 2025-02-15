package io.github.devhector.mpi_execute_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.devhector.mpi_execute_api.model.Job;

public interface JobRepository extends JpaRepository<Job, Long> {
  Optional<Job> findByUuid(String uuid);
}
