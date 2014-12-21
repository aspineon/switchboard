package io.switchboard.streams.messages;

public class RetrieveStream {
  private final String id;

  public RetrieveStream(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}