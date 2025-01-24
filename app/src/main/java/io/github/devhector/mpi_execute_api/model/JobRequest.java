package io.github.devhector.mpi_execute_api.model;

public class JobRequest {
  private Integer numberOfWorkers;
  private Integer numberOfProcess;
  private String accessKey;
  private String code;
  private String uuid;

  public JobRequest(Integer numberOfWorkers, Integer numberOfProcess, String code) {
    this.numberOfWorkers = numberOfWorkers;
    this.numberOfProcess = numberOfProcess;
    this.code = code;
  }

  public JobRequest(Integer numberOfWorkers, Integer numberOfProcess, String code, String accessKey, String uuid) {
    this.numberOfWorkers = numberOfWorkers;
    this.numberOfProcess = numberOfProcess;
    this.accessKey = accessKey;
    this.code = code;
    this.uuid = uuid;
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

}
