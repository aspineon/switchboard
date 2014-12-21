package io.switchboard.api;

import akka.actor.ActorRef;
import akka.dispatch.ExecutionContexts;
import akka.http.marshallers.xml.ScalaXmlSupport;
import akka.http.marshallers.xml.ScalaXmlSupport$;
import akka.http.model.headers.Accept$;
import akka.http.model.japi.*;
import akka.http.model.japi.headers.Accept;
import akka.http.model.japi.headers.RawHeader;
import akka.http.server.japi.*;
import akka.http.server.japi.impl.RouteResultImpl;
import akka.japi.*;
import akka.pattern.Patterns;
import akka.stream.javadsl.Source;
import akka.stream.scaladsl.SourcePipe;
import akka.util.ByteString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
import io.switchboard.streams.StreamManagement;
import scala.concurrent.Future;

import javax.xml.bind.JAXB;
import java.io.ByteArrayOutputStream;

/**
 * Created by Christoph Grotz on 09.12.14.
 */
public abstract class SwitchboardHttpApp extends HttpApp {

  public Route completeWithActorCall(ActorRef actorRef, Object message, int timeout) {
    return handleWith(
            ctx -> {
              ContentType contentType = extractResponseType(ctx);
              return ctx.complete(HttpResponse.create().withEntity(HttpEntities.createChunked(contentType, Source.from(Patterns.ask(actorRef, message, timeout).map(new Marshalling(ctx), ExecutionContexts.global())).asScala())));
            }
    );
  }

  protected ContentType extractResponseType(RequestContext ctx) {
    Accept accept = ctx.request().getHeader(Accept.class).getOrElse(Accept.create(MediaRanges.create(MediaTypes.APPLICATION_JSON)));
    if(Iterables.contains(accept.getMediaRanges(), MediaRanges.create(MediaTypes.APPLICATION_JSON))) {
      return ContentType.create(MediaTypes.APPLICATION_JSON);
    }
    else if(Iterables.contains(accept.getMediaRanges(), MediaRanges.create(MediaTypes.APPLICATION_XML))) {
      return ContentType.create(MediaTypes.APPLICATION_XML);
    }
    else {
      return ContentType.create(MediaTypes.APPLICATION_JSON);
    }
  }

  protected <T> RouteResult completeWithActorCall(RequestContext ctx, final Class<T> valueType, JavaPartialFunction<T, Future<Object>> actorCall) {
    final ObjectMapper mapper = new ObjectMapper();
    final ContentType contentType = extractResponseType(ctx);
    SourcePipe<ByteString> source = (SourcePipe<ByteString>)
      ((SourcePipe<T>)ctx.request().entity().getDataBytes().collect(new JavaPartialFunction<ByteString, Object>() {
        @Override
        public Object apply(ByteString x, boolean isCheck) throws Exception {
          return mapper.readValue(x.utf8String(), valueType);
        }
      })).mapAsync(actorCall).map(new JavaPartialFunction<Object, ByteString>() {
        @Override
        public ByteString apply(Object param, boolean isCheck) throws Exception {
          ObjectWriter writer = new ObjectMapper().enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).writer();
          return ByteString.fromString(writer.writeValueAsString(param));
        }
      });

    return ctx.complete(HttpResponse.create().withEntity(HttpEntities.createChunked(contentType, source)));
  }

  public static class Marshalling extends JavaPartialFunction<Object, ByteString> {
    private final RequestContext ctx;

    public Marshalling(RequestContext ctx) {
      this.ctx = ctx;
    }

    @Override
    public ByteString apply(Object x, boolean isCheck) throws Exception {
      Accept accept = ctx.request().getHeader(Accept.class).getOrElse(Accept.create(MediaRanges.create(MediaTypes.APPLICATION_JSON)));

      if(Iterables.contains(accept.getMediaRanges(), MediaRanges.create(MediaTypes.APPLICATION_JSON))) {
        return toJson(x);
      }
      else if(Iterables.contains(accept.getMediaRanges(), MediaRanges.create(MediaTypes.APPLICATION_XML))) {
        return toXml(x);
      }
      else {
        return toJson(x);
      }
    }

    private ByteString toJson(Object x) throws JsonProcessingException {
      ObjectWriter writer = new ObjectMapper().enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).writer();
      return ByteString.fromString(writer.writeValueAsString(x));
    }

    private ByteString toXml(Object x) throws JsonProcessingException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      JAXB.marshal(x, baos);
      return ByteString.fromString(baos.toString());
    }
  }
}
