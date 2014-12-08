package io.switchboard;

import akka.actor.ActorSystem;
import io.switchboard.api.RestApi;

/**
 * Main class for switchboard
 *
 * Created by Christoph Grotz on 18.11.14.
 */
public class Boot{

  public static void main(String ... args) throws Exception {
    ActorSystem actorSystem = ActorSystem.create();
    RestApi.apply(actorSystem).bindRoute("0.0.0.0", 8080);
  }

}
