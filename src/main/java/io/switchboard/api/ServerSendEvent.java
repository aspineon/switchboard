package io.switchboard.api;

import akka.http.model.japi.*;
import akka.http.model.japi.ContentType;
import akka.http.model.japi.headers.*;
import akka.http.server.japi.Handler;
import akka.http.server.japi.RequestContext;
import akka.http.server.japi.RouteResult;
import akka.stream.scaladsl.PublisherSource;
import com.google.common.collect.Lists;
import io.switchboard.kafka.KafkaConsumer;

import java.util.HashMap;

/**
 * Created by Christoph Grotz on 07.12.14.
 */
public class ServerSendEvent implements Handler {
  @Override
  public RouteResult handle(RequestContext ctx) {
    MediaType mediaType = MediaTypes.custom("text", "event-stream", false, false, Lists.newArrayList("sse"), new HashMap<>());
    akka.http.model.japi.ContentType contentType = akka.http.model.japi.ContentType.create(mediaType, HttpCharsets.UTF_8);

    akka.http.model.japi.HttpResponse response = HttpResponse.create()
      .addHeader(AccessControlAllowOrigin.create(HttpOriginRange.ALL))
      .addHeader(AccessControlAllowHeaders.create("Access-Control-Allow-Origin", "Access-Control-Allow-Method"))
      .addHeader(AccessControlAllowMethods.create(HttpMethods.GET, HttpMethods.POST, HttpMethods.PUT, HttpMethods.OPTIONS, HttpMethods.DELETE))
      .withEntity(HttpEntities.createCloseDelimited(contentType, new PublisherSource(KafkaConsumer.get("switchboard"))));

    return ctx.complete(response);
  }
}
