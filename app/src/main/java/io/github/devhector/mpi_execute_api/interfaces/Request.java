package io.github.devhector.mpi_execute_api.interfaces;

public interface Request {
  Integer getNumberOfWorkers();

  void setNumberOfWorkers(Integer workers);

  String getAccessKey();
}
