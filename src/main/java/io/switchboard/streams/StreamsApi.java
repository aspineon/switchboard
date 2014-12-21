package io.switchboard.streams;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.ExecutionContexts;
import akka.http.model.japi.ContentType;
import akka.http.model.japi.HttpEntities;
import akka.http.model.japi.HttpEntity;
import akka.http.model.japi.HttpResponse;
import akka.http.server.japi.*;
import akka.japi.JavaPartialFunction;
import akka.pattern.Patterns;
import akka.stream.FlowMaterializer;
import akka.stream.javadsl.Source;
import akka.stream.scaladsl.SubscriberSink;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.switchboard.api.SwitchboardHttpApp;
import io.switchboard.boot.Config;
import io.switchboard.kafka.KafkaSubscriber;
import io.switchboard.streams.domain.Stream;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

/**
 * Created by Christoph Grotz on 20.12.14.
 */
public class StreamsApi extends SwitchboardHttpApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsApi.class);

  private final ActorSystem actorSystem;
  private final ActorRef streamManagement;

  private final PathMatcher<String> id = PathMatchers.segment();
  private Subscriber<String> subscriber;

  private StreamsApi(ActorSystem actorSystem) {
    this.actorSystem = actorSystem;
    this.subscriber = new KafkaSubscriber("switchboard");
    this.streamManagement = actorSystem.actorOf(Props.create(StreamManagement.class, Config.get().get(Config.SWITCHBOARD_MONGO_URI)));
  }

  public static StreamsApi create(ActorSystem actorSystem) {
    return new StreamsApi(actorSystem);
  }

  @Override
  public Route createRoute() {
    ObjectMapper mapper = new ObjectMapper();
    return route(
      path(
        "api", "v1", "streams"
      ).route(
        get(
          completeWithActorCall(streamManagement, StreamManagement.retrieve(), 1000)
        ),
        post(
          handleWith(ctx -> completeWithActorCall(ctx, Stream.class, new JavaPartialFunction<Stream, Future<Object>>() {
            @Override
            public Future<Object> apply(Stream stream, boolean isCheck) throws Exception {
              return Patterns.ask(streamManagement, StreamManagement.create(stream), 1000);
            }
          }))
        )
      ),
      path(
        "api", "v1", "streams", id
      ).route(
        get(
          handleWith(id, (ctx, elementId) -> {
              ContentType contentType = extractResponseType(ctx);
              Object message = StreamManagement.retrieve(elementId);
              return ctx.complete(HttpResponse.create().withEntity(HttpEntities.createChunked(contentType, Source.from(Patterns.ask(streamManagement, message, 1000).map(new Marshalling(ctx), ExecutionContexts.global())).asScala())));
            }
          )
        ),
        put(
          handleWith(id, (ctx, streamId) -> completeWithActorCall(ctx, Stream.class, new JavaPartialFunction<Stream, Future<Object>>() {
            @Override
            public Future<Object> apply(Stream stream, boolean isCheck) throws Exception {
              return Patterns.ask(streamManagement, StreamManagement.update(streamId, stream), 1000);
            }
          }))
        ),
        delete(
          handleWith(id, (ctx, streamId) -> {
              ContentType contentType = extractResponseType(ctx);
              Object message = StreamManagement.delete(streamId);
              return ctx.complete(HttpResponse.create().withEntity(HttpEntities.createChunked(contentType, Source.from(Patterns.ask(streamManagement, message, 1000).map(new Marshalling(ctx), ExecutionContexts.global())).asScala())));
            }
          )
        ),
        post(
          handleWith(id, this::publishEvent)
        )
      )
    );
  }

  private RouteResult publishEvent(RequestContext ctx, String stream) {
    HttpEntity entity = ctx.request().entity();

    entity.getDataBytes()
      .to(new SubscriberSink(subscriber))
      .run(FlowMaterializer.create(actorSystem));

    return ctx.completeWithStatus(200);
  }
}
