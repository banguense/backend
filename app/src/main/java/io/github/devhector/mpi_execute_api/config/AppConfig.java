package io.github.devhector.mpi_execute_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.fabric8.kubernetes.api.model.batch.v1.JobBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.github.devhector.mpi_execute_api.interfaces.KubernetesClient;
import io.github.devhector.mpi_execute_api.repository.JobRepository;
import io.github.devhector.mpi_execute_api.service.FabricEight;
import io.github.devhector.mpi_execute_api.service.JobService;
import io.github.devhector.mpi_execute_api.service.KubernetesService;

@Configuration
public class AppConfig {

  @Bean
  public KubernetesClient kubernetesClient() {
    return new FabricEight(new KubernetesClientBuilder(), new JobBuilder());
  }

  @Bean
  public KubernetesService kubernetesService(KubernetesClient kubernetesClient) {
    return new KubernetesService(kubernetesClient);
  }

  @Bean
  public JobService jobService(KubernetesService service, JobRepository repository) {
    return new JobService(service, repository);
  }
}
