package io.github.devhector.mpi_execute_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.devhector.mpi_execute_api.model.JobEntity;

public interface JobRepository extends JpaRepository<JobEntity, Long> {
  Optional<JobEntity> findByUuid(String Uuid);
}
