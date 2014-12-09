package io.switchboard.api;

import akka.actor.ActorRef;
import akka.dispatch.ExecutionContexts;
import akka.http.model.japi.*;
import akka.http.server.japi.HttpApp;
import akka.http.server.japi.Route;
import akka.japi.JavaPartialFunction;
import akka.pattern.Patterns;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import scala.concurrent.Future;

/**
 * Created by Christoph Grotz on 09.12.14.
 */
public abstract class SwitchboardHttpApp extends HttpApp {
  public Route completeWithFuture(ActorRef actorRef, Object message, int timeout) {
    return handleWith(
            ctx -> {
              Future<Object> future = Patterns.ask(actorRef, message, timeout);
              Future<ByteString> future2 = future.map(new JavaPartialFunction<Object, ByteString>() {
                @Override
                public ByteString apply(Object x, boolean isCheck) throws Exception {
                  ObjectWriter writer = new ObjectMapper().enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).writer();
                  return ByteString.fromString(writer.writeValueAsString(x));
                }
              }, ExecutionContexts.global());


              ContentType json = ContentType.create(MediaTypes.APPLICATION_JSON);
              Source<ByteString> source = Source.from(future2);
              akka.stream.scaladsl.Source<ByteString> source2 = source.asScala();
              HttpEntityChunked entity = HttpEntities.createChunked(json, source2);
              HttpResponse resp = HttpResponse.create().withEntity(entity);
              return ctx.complete(resp);
            }
    );
  }
}
