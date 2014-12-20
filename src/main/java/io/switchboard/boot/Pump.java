package io.switchboard.boot;

import akka.actor.ActorSystem;
import akka.stream.FlowMaterializer;
import io.switchboard.api.Api;
import io.switchboard.processing.Switchboard;
import joptsimple.internal.Strings;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Christoph Grotz on 20.12.14.
 */
public class Pump {
  private static final Logger LOG = LoggerFactory.getLogger(Boot.class);

  public static void main(String ... args) throws Exception {
    try {
      Options options = new Options();
      options.addOption("e", true, "expression");

      PosixParser parser = new PosixParser();
      CommandLine cmd = parser.parse(options, args);

      String expression;
      if (cmd.hasOption("e")) {
        expression = cmd.getOptionValue("e");
      } else if (!Strings.isNullOrEmpty(System.getenv("SWITCHBOARD_EXPRESSION"))) {
        expression = System.getenv("SWITCHBOARD_EXPRESSION");
      } else {
        throw new IllegalArgumentException("Missing expression");
      }

      String groupId;
      if (cmd.hasOption("g")) {
        groupId = cmd.getOptionValue("g");
      } else if (!Strings.isNullOrEmpty(System.getenv("SWITCHBOARD_GROUP_ID"))) {
        groupId = System.getenv("SWITCHBOARD_GROUP_ID");
      } else {
        throw new IllegalArgumentException("Missing groupId");
      }

      ActorSystem actorSystem = ActorSystem.create();

      Switchboard.expression(expression)
        .runWithKafka(FlowMaterializer.create(actorSystem),
          "adhoc-group-" + System.currentTimeMillis());
    }
    catch(Exception exp) {
      LOG.error("Error while starting", exp);
    }
  }

}
