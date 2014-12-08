package io.switchboard.api;

import akka.actor.ActorSystem;
import akka.http.server.japi.*;
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
            ServerSendEvent.create("group3", "switchboard")
          )
        ),
        post(
          KafkaEndpoint.apply(actorSystem).createRoute()
        )
      )
    );
  }
}
