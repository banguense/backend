package io.github.devhector.mpi_execute_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.repository.JobRepository;
import io.github.devhector.mpi_execute_api.service.FabricEight;
import io.github.devhector.mpi_execute_api.service.JobService;
import io.github.devhector.mpi_execute_api.service.KubernetesService;
import io.github.devhector.mpi_execute_api.service.StatusService;

@Configuration
public class AppConfig {

  @Bean
  public KubernetesClient kubernetesClient() {
    return new FabricEight(
        new KubernetesClientBuilder().withConfig(Config.autoConfigure(null)),
        new JobBuilder());
  }

  @Bean
  public KubernetesService kubernetesService(KubernetesClient kubernetesClient) {
    return new KubernetesService(kubernetesClient);
  }

  @Bean
  public JobService jobService(KubernetesService service, JobRepository jobRepository) {
    return new JobService(service, jobRepository);
  }

  @Bean
  public StatusService statusService(JobRepository jobRepository) {
    return new StatusService(jobRepository);
  }
}
