package io.switchboard.api;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.model.japi.HttpMethods;
import akka.http.model.japi.HttpResponse;
import akka.http.model.japi.headers.AccessControlAllowHeaders;
import akka.http.model.japi.headers.AccessControlAllowMethods;
import akka.http.model.japi.headers.AccessControlAllowOrigin;
import akka.http.model.japi.headers.HttpOriginRange;
import akka.http.server.japi.*;
import io.switchboard.kafka.KafkaEndpoint;
import io.switchboard.streams.StreamManagement;

/**
 * Created by Christoph Grotz on 07.12.14.
 */
public class BasicApi extends SwitchboardHttpApp {

  private final ActorSystem actorSystem;
  private final ActorRef streamManagement;

  private BasicApi(ActorSystem actorSystem) {
    this.actorSystem = actorSystem;
    this.streamManagement = actorSystem.actorOf(Props.create(StreamManagement.class));
  }

  public static BasicApi apply(ActorSystem actorSystem) {
    return new BasicApi(actorSystem);
  }

  @Override
  public Route createRoute() {
    return route(
      options(
              handleWith(ctx -> {
                akka.http.model.japi.HttpResponse response = HttpResponse.create()
                        .addHeader(AccessControlAllowOrigin.create(HttpOriginRange.ALL))
                        .addHeader(AccessControlAllowHeaders.create("Access-Control-Allow-Origin", "Access-Control-Allow-Method"))
                        .addHeader(AccessControlAllowMethods.create(HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT, HttpMethods.OPTIONS, HttpMethods.DELETE));

                return ctx.complete(response);
              })
      ),
      path(
        "api",
        "v1",
        "streams"
      ).route(
        get(
          /*handleWith(
                  ServerSendEvent.create("group3", "switchboard")
          )*/
          completeWithFuture(streamManagement, StreamManagement.retrieve(), 1000)
        ),
        post(
          KafkaEndpoint.apply(actorSystem).createRoute()
        )
      )
    );
  }

  public void bindRoute(String host, int port) {
    bindRoute(host, port, actorSystem);
  }
}
