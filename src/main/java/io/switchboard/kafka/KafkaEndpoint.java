package io.switchboard.kafka;

import akka.actor.ActorSystem;
import akka.http.model.japi.HttpEntity;
import akka.http.server.japi.*;
import akka.stream.FlowMaterializer;
import akka.stream.scaladsl.Sink;
import akka.stream.scaladsl.SubscriberSink;
import io.switchboard.kafka.KafkaProducer;

/**
 *
 * Kafka Api Endpoint
 *
 * Created by Christoph Grotz on 07.12.14.
 */
public class KafkaEndpoint extends HttpApp {

  private final ActorSystem actorSystem;

  private KafkaEndpoint(ActorSystem actorSystem) {
    this.actorSystem = actorSystem;
  }

  public static KafkaEndpoint apply(ActorSystem actorSystem) {
    return new KafkaEndpoint(actorSystem);
  }

  private KafkaProducer producer = new KafkaProducer();

  @Override
  public Route createRoute() {
    return post(
            handleWith(
                    ctx -> {
                      HttpEntity entity = ctx.request().entity();

                      entity.getDataBytes().to(new SubscriberSink(producer)).run(FlowMaterializer.create(actorSystem));

                      return ctx.completeWithStatus(200);
                    }
            )
    );
  }
}
