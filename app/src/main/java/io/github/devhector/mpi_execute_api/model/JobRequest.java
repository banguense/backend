package io.github.devhector.mpi_execute_api.model;

public class JobRequest {
  private Integer numberOfWorkers;
  private Integer numberOfProcess;
  private String code;

  public JobRequest(Integer numberOfWorkers, Integer numberOfProcess, String code) {
    this.numberOfWorkers = numberOfWorkers;
    this.numberOfProcess = numberOfProcess;
    this.code = code;
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

}
