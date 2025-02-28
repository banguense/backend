package io.github.devhector.mpi_execute_api.model;

import io.github.devhector.mpi_execute_api.interfaces.Request;

public class JobRequest implements Request {
  private String compilationDirective;
  private Integer numberOfWorkers;
  private Integer numberOfProcess;
  private String accessKey;
  private String arguments;
  private String code;
  private String uuid;

  public JobRequest(String compilationDirective, Integer numberOfWorkers, Integer numberOfProcess, String accessKey,
      String arguments, String code) {
    this.compilationDirective = compilationDirective;
    this.numberOfWorkers = numberOfWorkers;
    this.numberOfProcess = numberOfProcess;
    this.accessKey = accessKey;
    this.arguments = arguments;
    this.code = code;
  }

  public String getCompilationDirective() {
    return compilationDirective;
  }

  public void setCompilationDirective(String compilationDirective) {
    this.compilationDirective = compilationDirective;
  }

  public Integer getNumberOfWorkers() {
    return numberOfWorkers;
  }

  public void setNumberOfWorkers(Integer numberOfWorkers) {
    this.numberOfWorkers = numberOfWorkers;
  }

  public Integer getNumberOfProcess() {
    return numberOfProcess;
  }

  public void setNumberOfProcess(Integer numberOfProcess) {
    this.numberOfProcess = numberOfProcess;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getArguments() {
    return arguments;
  }

  public void setArguments(String arguments) {
    this.arguments = arguments;
  }

}
