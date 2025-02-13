package io.github.devhector.mpi_execute_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.devhector.mpi_execute_api.model.AppConfiguration;
import io.github.devhector.mpi_execute_api.repository.AppConfigurationRepository;

@Service
public class AdminService {
  private final AppConfigurationRepository configRepository;

  @Value("${app.accessKey}")
  private String accessKey;

  @Value("${app.maxContainers}")
  private int maxContainers;

  public AdminService(AppConfigurationRepository configRepository) {
    this.configRepository = configRepository;
  }

  public String getAccessKey() {
    return configRepository.findById("DEFAULT")
        .orElseGet(this::createDefaultConfig)
        .getAccessKey();
  }

  public int getMaxContainers() {
    return configRepository.findById("DEFAULT")
        .orElseGet(this::createDefaultConfig)
        .getMaxContainers();
  }

  public void updateSettings(String newAccessKey, int newMaxContainers) {
    AppConfiguration config = configRepository.findById("DEFAULT")
        .orElseGet(this::createDefaultConfig);

    config.setAccessKey(newAccessKey);
    config.setMaxContainers(newMaxContainers);

    configRepository.save(config);
  }

  private AppConfiguration createDefaultConfig() {
    AppConfiguration config = new AppConfiguration();
    config.setAccessKey(accessKey);
    config.setMaxContainers(maxContainers);
    return configRepository.save(config);
  }
}
