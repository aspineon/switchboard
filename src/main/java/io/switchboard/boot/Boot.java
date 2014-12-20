package io.switchboard.boot;

import akka.actor.ActorSystem;
import io.switchboard.api.Api;
import joptsimple.internal.Strings;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

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

      String host =             getPropertyValue(cmd, "z", "SWITCHBOARD_HOST_INTERFACE",       "0.0.0.0");
      Config.get().put(Config.SWITCHBOARD_HOST_INTERFACE, host);
      String portStr =          getPropertyValue(cmd, "p", "SWITCHBOARD_HOST_PORT",            "8080");
      Config.get().put(Config.SWITCHBOARD_HOST_PORT, portStr);
      String brokerList =       getPropertyValue(cmd, "b", "SWITCHBOARD_METADATA_BROKER_LIST", "localhost:9092");
      Config.get().put(Config.SWITCHBOARD_METADATA_BROKER_LIST, brokerList);
      String zookeeperConnect = getPropertyValue(cmd, "z", "SWITCHBOARD_ZOOKEEPER_CONNECT",    "127.0.0.1:2181");
      Config.get().put(Config.SWITCHBOARD_ZOOKEEPER_CONNECT, zookeeperConnect);

      ActorSystem actorSystem = ActorSystem.create();
      api(actorSystem).bindRoute(host, Integer.parseInt(portStr));

      /*
      Switchboard
        .expression("FROM switchboard | type=request AND (country=India OR city=NY) AND value.numeric = 1 | TO topic2")
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

  private static Optional<String> getPropertyValue(CommandLine cmd, String opt, String env) {
    Optional<String> property;
    if (cmd.hasOption(opt)) {
      property = Optional.of(cmd.getOptionValue(opt));
    } else if (!Strings.isNullOrEmpty(System.getenv(env))) {
      property = Optional.of(System.getenv(env));
    } else {
      property = Optional.empty();
    }
    return property;
  }

  private static String getPropertyValue(CommandLine cmd, String opt, String env, String def) {
    String property;
    if (cmd.hasOption(opt)) {
      property = cmd.getOptionValue(opt);
    } else if (!Strings.isNullOrEmpty(System.getenv(env))) {
      property = System.getenv(env);
    } else {
      property = def;
    }
    return property;
  }

  private static Api api(ActorSystem actorSystem) {
    return Api.apply(actorSystem);
  }

}
