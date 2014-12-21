package io.switchboard.streams;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.server.japi.PathMatcher;
import akka.http.server.japi.RouteResult;
import akka.japi.pf.ReceiveBuilder;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import scala.collection.Seq;
import scala.collection.JavaConverters;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Christoph Grotz on 09.12.14.
 */
public class StreamManagement extends AbstractActor {

  private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  private ZkClient zkClient = new ZkClient("127.0.0.1:2181");

  public StreamManagement() {

    receive(ReceiveBuilder
      .match(RetrieveStreams.class, message -> {
        log.info("retrieve streams {}", message);
        sender().tell(Lists.newArrayList(new Stream(UUID.randomUUID().toString(), "stream1"), new Stream(UUID.randomUUID().toString(), "stream2"),new Stream(UUID.randomUUID().toString(), "stream3")), self());
      })
      .match(RetrieveStream.class, message -> {
        log.info("retrieve stream {}", message);
        sender().tell(new Stream(message.getId(), "stream" + message.getId()), self());
      })
      .match(DeleteStream.class, message -> {
        log.info("delete stream {}", message);
        sender().tell(new Stream(message.getId(), "stream"+message.getId()), self());
      })
      .match(UpdateStream.class, message -> {
        log.info("update stream {}", message);
        Stream stream = message.getStream();
        stream.setId(message.getStreamId());
        sender().tell(stream, self());
      })
      .match(CreateStream.class, message -> {
        log.info("create stream {}", message);
        Stream stream = message.getStream();
        stream.setId(UUID.randomUUID().toString());
        sender().tell(stream, self());
      })
      .build());
  }

  public static class Stream implements RouteResult {
    private String name;
    private String id;

    private Stream() {
    }

    private Stream(String id, String name) {
      this.name = name;
      this.id = id;
    }


    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }

  public static RetrieveStreams retrieve() {
    return new RetrieveStreams();
  }
  public static RetrieveStream retrieve(String id) {
    return new RetrieveStream(id);
  }

  public static DeleteStream delete(String id) {
    return new DeleteStream(id);
  }

  public static CreateStream create(Stream stream) {
    return new CreateStream(stream);
  }

  public static UpdateStream update(String streamId, Stream stream) {
    return new UpdateStream(streamId, stream);
  }

  private static class RetrieveStreams {
  }

  private static class DeleteStream {
    private final String id;

    public DeleteStream(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  private static class RetrieveStream {
    private final String id;

    public RetrieveStream(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  private static class CreateStream {
    private final Stream stream;

    public CreateStream(Stream stream) {
      this.stream = stream;
    }

    public Stream getStream() {
      return stream;
    }
  }

  private static class UpdateStream {
    private final String streamId;
    private final Stream stream;

    public UpdateStream(String streamId, Stream stream) {
      this.streamId =  streamId;
      this.stream = stream;
    }

    public Stream getStream() {
      return stream;
    }

    public String getStreamId() {
      return streamId;
    }
  }
}
