package io.github.devhector.mpi_execute_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.devhector.mpi_execute_api.model.JobEntity;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long> {
  Optional<JobEntity> findByUuid(String Uuid);
}
