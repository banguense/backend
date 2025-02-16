package io.github.devhector.mpi_execute_api.model;

public class UploadResponse {
  private final String id;

  public UploadResponse(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return "UploadResponse [id=" + id + "]";
  }
}
