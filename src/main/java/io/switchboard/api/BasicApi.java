package io.switchboard.api;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.model.japi.HttpEntity;
import akka.http.model.japi.HttpMethods;
import akka.http.model.japi.HttpResponse;
import akka.http.model.japi.headers.AccessControlAllowHeaders;
import akka.http.model.japi.headers.AccessControlAllowMethods;
import akka.http.model.japi.headers.AccessControlAllowOrigin;
import akka.http.model.japi.headers.HttpOriginRange;
import akka.http.server.japi.*;
import akka.stream.FlowMaterializer;
import akka.stream.scaladsl.SubscriberSink;
import io.switchboard.kafka.KafkaSubscriber;
import io.switchboard.streams.StreamManagement;

/**
 * Created by Christoph Grotz on 07.12.14.
 */
public class BasicApi extends SwitchboardHttpApp {

  private final ActorSystem actorSystem;
  private final ActorRef streamManagement;

  private final PathMatcher<String> id = PathMatchers.segment();
  private KafkaSubscriber producer = new KafkaSubscriber("switchboard");

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
        "api","v1","streams"
      ).route(
        get(
          completeWithActorCall(streamManagement, StreamManagement.retrieve(), 1000)
        )
      ),
      path(
              "api","v1","streams", id
      ).route(
              post(
                      handleWith(
                              id,
                              (ctx, stream) -> {
                                HttpEntity entity = ctx.request().entity();

                                entity.getDataBytes()
                                        .to(new SubscriberSink(producer))
                                        .run(FlowMaterializer.create(actorSystem));

                                return ctx.completeWithStatus(200);
                              }
                      )
              )
      )
    );
  }

  public void bindRoute(String host, int port) {
    bindRoute(host, port, actorSystem);
  }
}
