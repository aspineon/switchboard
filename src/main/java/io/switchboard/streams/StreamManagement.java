package io.switchboard.streams;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.server.japi.RouteResult;
import akka.japi.pf.ReceiveBuilder;
import com.google.common.collect.Lists;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import scala.collection.Seq;
import scala.collection.JavaConverters;

import java.util.List;
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
              log.info("retrieve streams {}",message);
              List<Stream> streams = JavaConverters.asJavaListConverter(ZkUtils.getAllTopics(zkClient))
                      .asJava().stream().map(str -> new Stream(str))
                      .collect(Collectors.toList());
              sender().tell(streams, self());
            }).build());
  }

  public static class Stream implements RouteResult {
    private String name;

    private Stream() {
    }

    private Stream(String name) {
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
