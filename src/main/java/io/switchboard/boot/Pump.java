package io.switchboard.boot;

import akka.actor.ActorSystem;
import akka.stream.FlowMaterializer;
import io.switchboard.processing.Switchboard;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by Christoph Grotz on 20.12.14.
 */
public class Pump {
  private static final Logger LOG = LoggerFactory.getLogger(Boot.class);

  public static void main(String ... args) throws Exception {
    try {
      Options options = new Options();
      options.addOption("e", true, "expression");
      options.addOption("g", true, "group id");
      options.addOption("b", true, "broker list");
      options.addOption("z", true, "zookeeper connect");

      PosixParser parser = new PosixParser();
      CommandLine cmd = parser.parse(options, args);

      Optional<String> expression = Config.get().extractPropertyValue(cmd, "e", Config.SWITCHBOARD_EXPRESSION);
      if (!expression.isPresent()) {
        throw new IllegalArgumentException("Missing expression");
      }

      Optional<String> groupId = Config.get().extractPropertyValue(cmd, "g", Config.SWITCHBOARD_GROUP_ID);
      if (!groupId.isPresent()) {
        throw new IllegalArgumentException("Missing groupId");
      }

      Config.get().extractPropertyValue(cmd, "b", Config.SWITCHBOARD_METADATA_BROKER_LIST, "localhost:9092");
      Config.get().extractPropertyValue(cmd, "z", Config.SWITCHBOARD_ZOOKEEPER_CONNECT, "127.0.0.1:2181");

      ActorSystem actorSystem = ActorSystem.create();

      Switchboard.expression(expression.get())
        .runWithKafka(FlowMaterializer.create(actorSystem), groupId.get());
    }
    catch(Exception exp) {
      LOG.error("Error while starting", exp);
    }
  }

}
