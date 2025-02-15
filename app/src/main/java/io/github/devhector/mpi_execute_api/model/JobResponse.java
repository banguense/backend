package io.github.devhector.mpi_execute_api.model;

public class JobResponse {
  private final String id;
  private final String output;
  private final Long elapsedTime;
  private final Integer numberOfWorkers;
  private JobStatus status;

  public JobResponse(String id, String output, Long elapsedTime, Integer numberOfWorkers, JobStatus status) {
    this.id = id;
    this.output = output;
    this.elapsedTime = elapsedTime;
    this.numberOfWorkers = numberOfWorkers;
    this.status = status;
  }

  public JobResponse(Integer numberOfWorkers, String id, String output, long elapsedTime) {
    this.numberOfWorkers = numberOfWorkers;
    this.id = id;
    this.output = output;
    this.elapsedTime = elapsedTime;
    this.status = JobStatus.RUNNING;
  }

  public JobResponse(String id) {
    this.id = id;
    this.output = null;
    this.numberOfWorkers = null;
    this.elapsedTime = null;
    this.status = JobStatus.RUNNING;
  }

  public JobResponse(String id, String output) {
    this.id = id;
    this.output = output;
    this.numberOfWorkers = null;
    this.elapsedTime = null;
    this.status = JobStatus.RUNNING;
  }

  public String getId() {
    return id;
  }

  public String getOutput() {
    return output;
  }

  public Long getElapsedTime() {
    return elapsedTime;
  }

  public Integer getNumberOfWorkers() {
    return numberOfWorkers;
  }

  public JobStatus getStatus() {
    return status;
  }

  public void setStatus(JobStatus status) {
    this.status = status;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    JobResponse other = (JobResponse) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
}
