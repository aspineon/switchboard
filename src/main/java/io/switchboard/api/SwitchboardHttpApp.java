package io.switchboard.api;

import akka.actor.ActorRef;
import akka.dispatch.ExecutionContexts;
import akka.http.marshallers.xml.ScalaXmlSupport;
import akka.http.marshallers.xml.ScalaXmlSupport$;
import akka.http.model.headers.Accept$;
import akka.http.model.japi.*;
import akka.http.model.japi.headers.Accept;
import akka.http.model.japi.headers.RawHeader;
import akka.http.server.japi.HttpApp;
import akka.http.server.japi.RequestContext;
import akka.http.server.japi.Route;
import akka.http.server.japi.RouteResult;
import akka.http.server.japi.impl.RouteResultImpl;
import akka.japi.*;
import akka.pattern.Patterns;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Iterables;
import com.google.common.net.HttpHeaders;
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

  private ContentType extractResponseType(RequestContext ctx) {
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

  private static class Marshalling extends JavaPartialFunction<Object, ByteString> {
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
