package io.github.devhector.mpi_execute_api.model;

public class JobResponse {
  private final String id;
  private final String output;

  public JobResponse(String id) {
    this.id = id;
    this.output = null;
  }

  public JobResponse(String id, String output) {
    this.id = id;
    this.output = output;
  }

  public String getId() {
    return id;
  }

  public String getOutput() {
    return output;
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
