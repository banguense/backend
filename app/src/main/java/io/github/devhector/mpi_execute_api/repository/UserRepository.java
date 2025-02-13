package io.github.devhector.mpi_execute_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.devhector.mpi_execute_api.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);
}
