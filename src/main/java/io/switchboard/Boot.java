package io.switchboard;

import akka.actor.ActorSystem;
import akka.stream.FlowMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.japi.Procedure;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import io.switchboard.processing.Switchboard;
import scala.concurrent.duration.FiniteDuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Main class for switchboard
 *
 * Created by Christoph Grotz on 18.11.14.
 */
public class Boot{

  public static void main(String ... args) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ActorSystem actorSystem = ActorSystem.create();
    //BasicApi.apply(actorSystem).bindRoute("0.0.0.0", 8080);

    Source<ObjectNode> source = Source.from( Lists.newArrayList(
      mapper.createObjectNode().put("type", "request").put("country", "India"),
      mapper.createObjectNode().put("type", "request").put("country", "Cananda"),
      mapper.createObjectNode().put("type", "request").put("country", "Pakistan"),
      mapper.createObjectNode().put("type", "request").put("country", "US").put("city", "NY"),
      mapper.createObjectNode().put("type", "request").put("country", "US").put("city", "LA")
    ));
    //Source<ObjectNode> source = Source.from(new FiniteDuration(1, TimeUnit.SECONDS),new FiniteDuration(1, TimeUnit.SECONDS), () -> node);
    Sink<ObjectNode> sink = Sink.foreach(param -> System.out.println(param));

    /*Switchboard
      .expression("type=request AND (country=India OR city=NY)")
      .runWith(FlowMaterializer.create(actorSystem), source, sink);*/

    Switchboard
      .expression("type=request AND country=India OR city=NY")
      .runWith(FlowMaterializer.create(actorSystem), source, sink);
  }

}
