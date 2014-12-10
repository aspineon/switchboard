package io.switchboard.streams;

import akka.actor.UntypedActor;
import akka.http.server.japi.RouteResult;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christoph Grotz on 09.12.14.
 */
public class StreamManagement extends UntypedActor {

  private static final Logger LOG = LoggerFactory.getLogger(StreamManagement.class);

  @Override
  public void onReceive(Object message) throws Exception {
    if(message instanceof RetrieveStreams) {
      LOG.info("retrieve streams {}",message);
      getSender().tell(Lists.newArrayList(new Streams("Test"),new Streams("Test2"),new Streams("Test3")), getSelf());
    }
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
