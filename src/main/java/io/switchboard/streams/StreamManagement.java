package io.switchboard.streams;

import akka.actor.AbstractActor;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.server.japi.RouteResult;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christoph Grotz on 09.12.14.
 */
public class StreamManagement extends AbstractActor {

  private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

  public StreamManagement() {
    receive(ReceiveBuilder
            .match(RetrieveStreams.class, message -> {
              log.info("retrieve streams {}",message);
              sender().tell(Lists.newArrayList(new Streams("Test"), new Streams("Test2"), new Streams("Test3")), self());
            }).build());
  }

  public static class Streams implements RouteResult {
    private String name;

    private Streams() {
    }

    private Streams(String name) {
      this.name = name;
    }


    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  public static final RetrieveStreams retrieve() {
    return new RetrieveStreams();
  }

  private static class RetrieveStreams {
  }
}
