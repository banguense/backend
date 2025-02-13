package io.github.devhector.mpi_execute_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.devhector.mpi_execute_api.model.AppConfiguration;

public interface AppConfigurationRepository extends JpaRepository<AppConfiguration, String> {
}
