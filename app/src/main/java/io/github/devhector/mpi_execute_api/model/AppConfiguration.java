package io.github.devhector.mpi_execute_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class AppConfiguration {

  @Id
  private String id = "DEFAULT";
  private String accessKey;
  private int maxContainers;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public int getMaxContainers() {
    return maxContainers;
  }

  public void setMaxContainers(int maxContainers) {
    this.maxContainers = maxContainers;
  }

}
