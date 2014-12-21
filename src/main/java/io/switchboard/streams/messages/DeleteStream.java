package io.switchboard.streams.messages;

public class DeleteStream {
  private final String id;

  public DeleteStream(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }
}