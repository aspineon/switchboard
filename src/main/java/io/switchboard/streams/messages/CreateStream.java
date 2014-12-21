package io.switchboard.streams.messages;

import io.switchboard.streams.domain.Stream;

public class CreateStream {
  private final Stream stream;

  public CreateStream(Stream stream) {
    this.stream = stream;
  }

  public Stream getStream() {
    return stream;
  }
}