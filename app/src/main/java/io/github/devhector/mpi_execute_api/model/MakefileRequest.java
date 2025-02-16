package io.github.devhector.mpi_execute_api.model;

public class MakefileRequest {
  private String uuid;
  private String makefile;
  private String accessKey;
  private Integer numberOfWorkers;

  public MakefileRequest(String uuid, String makefile, String accessKey, Integer numberOfWorkers) {
    this.uuid = uuid;
    this.makefile = makefile;
    this.accessKey = accessKey;
    this.numberOfWorkers = numberOfWorkers;
  }

  public Integer getNumberOfWorkers() {
    return numberOfWorkers;
  }

  public void setNumberOfWorkers(Integer numberOfWorkers) {
    this.numberOfWorkers = numberOfWorkers;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
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
