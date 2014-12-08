package io.switchboard.api;

import akka.actor.ActorSystem;
import akka.http.model.HttpProtocols;
import akka.http.model.japi.HttpResponse;
import akka.http.model.japi.*;
import akka.http.model.japi.headers.AccessControlAllowOrigin;
import akka.http.model.japi.headers.ContentType;
import akka.http.model.japi.headers.HttpOriginRange;
import akka.http.server.japi.*;
import akka.japi.JavaPartialFunction;
import akka.stream.scaladsl.PublisherSource;
import akka.stream.scaladsl.SubscriberSink;
import akka.util.ByteString;
import akka.util.ByteString$;
import com.google.common.net.HttpHeaders;
import io.switchboard.kafka.KafkaConsumer;
import io.switchboard.kafka.KafkaEndpoint;

/**
 * Created by Christoph Grotz on 07.12.14.
 */
public class BasicApi extends HttpApp {

  private final ActorSystem actorSystem;

  private BasicApi(ActorSystem actorSystem) {
    this.actorSystem = actorSystem;
  }

  public static BasicApi apply(ActorSystem actorSystem) {
    return new BasicApi(actorSystem);
  }

  @Override
  public Route createRoute() {
    return route(
      path(
        "api",
        "v1",
        "messages"
      ).route(
        get(
          handleWith(
            new ServerSendEvent()
          )
        ),
        post(
          KafkaEndpoint.apply(actorSystem).createRoute()
        )
      )
    );
  }
}
