package io.github.devhector.mpi_execute_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MpiExecuteApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(MpiExecuteApiApplication.class, args);
  }

}
