package io.switchboard;

import akka.actor.ActorSystem;
import akka.stream.FlowMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.common.collect.Lists;
import io.switchboard.api.BasicApi;
import io.switchboard.kafka.KafkaSubscriber;
import io.switchboard.processing.Switchboard;
import joptsimple.internal.Strings;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for switchboard
 *
 * Created by Christoph Grotz on 18.11.14.
 */
public class Boot {
  private static final Logger LOG = LoggerFactory.getLogger(Boot.class);

  public static void main(String ... args) throws Exception {
    try {
      Options options = new Options();
      options.addOption("i", true, "interface");
      options.addOption("p", true, "port");

      PosixParser parser = new PosixParser();
      CommandLine cmd = parser.parse(options, args);

      String host;
      if (cmd.hasOption("i")) {
        host = cmd.getOptionValue("i");
      } else if (!Strings.isNullOrEmpty(System.getenv("SWITCHBOARD_HOST_INTERFACE"))) {
        host = System.getenv("SWITCHBOARD_HOST_INTERFACE");
      } else {
        host = "0.0.0.0";
      }

      String portStr;
      if (cmd.hasOption("p")) {
        portStr = cmd.getOptionValue("p");
      } else if (!Strings.isNullOrEmpty(System.getenv("SWITCHBOARD_HOST_PORT"))) {
        portStr = System.getenv("SWITCHBOARD_HOST_PORT");
      } else {
        portStr = "8080";
      }

      ActorSystem actorSystem = ActorSystem.create();
      BasicApi.apply(actorSystem).bindRoute(host, Integer.parseInt(portStr));

      /*
      Switchboard
        .expression("FROM switchboard | type=request AND country=India OR city=NY AND value.numeric = 1 | TO topic2")
        .runWithKafka(FlowMaterializer.create(actorSystem),
          "adhoc-group-" + System.currentTimeMillis());

      Source.from(Lists.newArrayList(
        "{\"type\":\"request\",\"country\":\"India\",\"value\": { \"numeric\": 1}}",
        "{\"type\":\"request\",\"country\":\"Cananda\",\"value\": { \"numeric\": 1}}",
        "{\"type\":\"request\",\"country\":\"Pakistan\",\"value\": { \"numeric\": 1}}",
        "{\"type\":\"request\",\"country\":\"US\",\"city\":\"NY\",\"value\": { \"numeric\": 1}}",
        "{\"type\":\"request\",\"country\":\"US\",\"city\":\"LA\",\"value\": { \"numeric\": 1}}"
      )).runWith(Sink.create(new KafkaSubscriber("switchboard")), FlowMaterializer.create(actorSystem));
      */
    }
    catch(ParseException exp) {
      LOG.error("Error while parsing arguments", exp);
    }
  }

}
