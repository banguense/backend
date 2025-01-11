package io.github.devhector.mpi_execute_api.model;

public class JobResponse {
  private final String id;
  private final String result;

  public JobResponse(String id) {
    this.id = id;
    this.result = null;
  }

  public JobResponse(String id, String result) {
    this.id = id;
    this.result = result;
  }

  public String getId() {
    return id;
  }

  public String getResult() {
    return result;
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
