package io.switchboard.api;

import akka.actor.ActorSystem;
import akka.http.model.japi.HttpMethods;
import akka.http.model.japi.HttpResponse;
import akka.http.model.japi.headers.AccessControlAllowHeaders;
import akka.http.model.japi.headers.AccessControlAllowMethods;
import akka.http.model.japi.headers.AccessControlAllowOrigin;
import akka.http.model.japi.headers.HttpOriginRange;
import akka.http.server.japi.*;
import io.switchboard.streams.StreamsApi;

/**
 * Created by Christoph Grotz on 07.12.14.
 */
public class Api extends SwitchboardHttpApp {

  private final ActorSystem actorSystem;

  private Api(ActorSystem actorSystem) {
    this.actorSystem = actorSystem;
  }

  public static Api apply(ActorSystem actorSystem) {
    return new Api(actorSystem);
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
      StreamsApi.create(actorSystem).createRoute()
    );
  }

  public void bindRoute(String host, int port) {
    bindRoute(host, port, actorSystem);
  }
}
