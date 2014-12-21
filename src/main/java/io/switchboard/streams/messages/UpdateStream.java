package io.switchboard.streams.messages;

import io.switchboard.streams.domain.Stream;

public class UpdateStream {
  private final String streamId;
  private final Stream stream;

  public UpdateStream(String streamId, Stream stream) {
    this.streamId = streamId;
    this.stream = stream;
  }

  public Stream getStream() {
    return stream;
  }

  public String getStreamId() {
    return streamId;
  }
}