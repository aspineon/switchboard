package io.switchboard.streams;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.server.japi.RouteResult;
import akka.japi.pf.ReceiveBuilder;
import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by Christoph Grotz on 09.12.14.
 */
public class StreamManagement extends AbstractActor {

  private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
  private MongoClient client;

  private StreamManagement(String clientUri) throws UnknownHostException {
    client = new MongoClient(new MongoClientURI(clientUri));
    DB switchboard = client.getDB("switchboard");
    DBCollection streams = switchboard.getCollection("streams");

    receive(ReceiveBuilder
      .match(RetrieveStreams.class, message -> {
        log.info("retrieve streams {}", message);
        sender().tell(streams.find().toArray().stream().map(dbObject -> new Stream().setId(dbObject.get("id").toString()).setName(dbObject.get("name").toString())).collect(Collectors.toList()), self());
      })
      .match(RetrieveStream.class, message -> {
        log.info("retrieve stream {}", message);
        DBObject dbObject = streams.findOne(new BasicDBObject("id", message.id));
        sender().tell(new Stream().setId(dbObject.get("id").toString()).setName(dbObject.get("name").toString()), self());
      })
      .match(DeleteStream.class, message -> {
        log.info("delete stream {}", message);
        DBObject dbObject = streams.findOne(new BasicDBObject("id", message.id));
        Stream stream = new Stream().setId(dbObject.get("id").toString()).setName(dbObject.get("name").toString());
        streams.remove(new BasicDBObject("id", message.id));
        sender().tell(stream, self());
      })
      .match(UpdateStream.class, message -> {
        log.info("update stream {}", message);
        Stream stream = message.getStream();
        stream.setId(message.getStreamId());
        streams.update(new BasicDBObject("id", message.getStreamId()), new BasicDBObject("id",message.getStreamId()).append("name",message.getStream().getName()));
        sender().tell(stream, self());
      })
      .match(CreateStream.class, message -> {
        log.info("create stream {}", message);
        String id = UUID.randomUUID().toString();

        Stream stream = message.getStream();
        stream.setId(id);
        streams.insert(new BasicDBObject("id", id).append("name", message.getStream().getName()));
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
