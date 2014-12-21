package io.switchboard.streams.domain;

public class Stream {
  private String name;
  private String id;

  public Stream() {
  }

  public Stream(String id, String name) {
    this.name = name;
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public Stream setName(String name) {
    this.name = name;
    return this;
  }

  public String getId() {
    return id;
  }

  public Stream setId(String id) {
    this.id = id;
    return this;
  }
}
