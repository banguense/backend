package io.github.devhector.mpi_execute_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "jobs")
public class JobEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String uuid;

  @Column(nullable = false)
  private String status;

  @Lob
  private String result;

  @Lob
  private String errorMessage;

  private int workers;
  private int processess;

  @Lob
  private String code;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public int getWorkers() {
    return workers;
  }

  public void setWorkers(int workers) {
    this.workers = workers;
  }

  public int getProcessess() {
    return processess;
  }

  public void setProcessess(int processess) {
    this.processess = processess;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public static class Builder {
    private Long id;
    private String uuid;
    private String status;
    private String result;
    private String errorMessage;
    private int workers;
    private int processess;
    private String code;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder uuid(String uuid) {
      this.uuid = uuid;
      return this;
    }

    public Builder status(String status) {
      this.status = status;
      return this;
    }

    public Builder result(String result) {
      this.result = result;
      return this;
    }

    public Builder errorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
      return this;
    }

    public Builder workers(int workers) {
      this.workers = workers;
      return this;
    }

    public Builder processess(int processess) {
      this.processess = processess;
      return this;
    }

    public Builder code(String code) {
      this.code = code;
      return this;
    }

    public JobEntity build() {
      JobEntity jobEntity = new JobEntity();
      jobEntity.setId(id);
      jobEntity.setUuid(uuid);
      jobEntity.setStatus(status);
      jobEntity.setResult(result);
      jobEntity.setErrorMessage(errorMessage);
      jobEntity.setWorkers(workers);
      jobEntity.setProcessess(processess);
      jobEntity.setCode(code);
      return jobEntity;
    }
  }

}
