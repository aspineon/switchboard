package io.switchboard.api;

import akka.actor.ActorSystem;
import akka.http.model.ContentTypes;
import akka.http.model.japi.*;
import akka.http.model.japi.ContentType;
import akka.http.model.japi.headers.*;
import akka.http.server.japi.*;
import io.switchboard.kafka.KafkaEndpoint;

/**
 *
 * Rest API Entrypoint for Switchboard
 *
 * Created by Christoph Grotz on 06.12.14.
 */
public class RestApi extends HttpApp {

  private final ActorSystem actorSystem;

  private RestApi(ActorSystem actorSystem) {
    this.actorSystem = actorSystem;
  }

  public static RestApi apply(ActorSystem actorSystem) {
    return new RestApi(actorSystem);
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
      BasicApi.apply(actorSystem).createRoute()
    );
  }

  public void bindRoute(String host, int port) {
    bindRoute(host, port, actorSystem);
  }
}
