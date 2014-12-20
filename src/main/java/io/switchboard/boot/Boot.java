package io.switchboard.boot;

import akka.actor.ActorSystem;
import io.switchboard.api.Api;
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
      options.addOption("b", true, "broker list");
      options.addOption("z", true, "zookeeper connect");

      PosixParser parser = new PosixParser();
      CommandLine cmd = parser.parse(options, args);

      String host =             Config.get().extractPropertyValue(cmd, "z", Config.SWITCHBOARD_HOST_INTERFACE, "0.0.0.0");
      String portStr =          Config.get().extractPropertyValue(cmd, "p", Config.SWITCHBOARD_HOST_PORT, "8080");
      String brokerList =       Config.get().extractPropertyValue(cmd, "b", Config.SWITCHBOARD_METADATA_BROKER_LIST, "localhost:9092");
      String zookeeperConnect = Config.get().extractPropertyValue(cmd, "z", Config.SWITCHBOARD_ZOOKEEPER_CONNECT, "127.0.0.1:2181");

      api(ActorSystem.create()).bindRoute(host, Integer.parseInt(portStr));

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

  private static Api api(ActorSystem actorSystem) {
    return Api.apply(actorSystem);
  }

}
