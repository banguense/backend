package io.github.devhector.mpi_execute_api.model;

public class MakefileRequest {
  private Integer numberOfWorkers;
  private Integer numberOfProcess;
  private String accessKey;
  private String code;
  private String makefile;
  private String uuid;

  public MakefileRequest(Integer numberOfWorkers, String accessKey, String code, String makefile) {
    this.numberOfWorkers = numberOfWorkers;
    this.accessKey = accessKey;
    this.code = code;
    this.makefile = makefile;
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

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMakefile() {
    return makefile;
  }

  public void setMakefile(String makefile) {
    this.makefile = makefile;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
