package io.github.devhector.mpi_execute_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import io.github.devhector.mpi_execute_api.model.User;
import io.github.devhector.mpi_execute_api.model.Role;
import io.github.devhector.mpi_execute_api.repository.UserRepository;

@Component
public class SecurityInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.admin.password}")
  private String adminPassword;

  public SecurityInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) throws Exception {
    if (userRepository.findByUsername("admin") == null) {
      User admin = new User();
      admin.setUsername("admin");
      admin.setPassword(passwordEncoder.encode(adminPassword));
      admin.setRole(Role.ADMIN);
      userRepository.save(admin);
      System.out.println("Usuário admin criado com sucesso!");
    }
  }
}
