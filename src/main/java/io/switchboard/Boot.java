package io.switchboard;

import akka.actor.ActorSystem;
import io.switchboard.api.BasicApi;

/**
 * Main class for switchboard
 *
 * Created by Christoph Grotz on 18.11.14.
 */
public class Boot{

  public static void main(String ... args) throws Exception {
    ActorSystem actorSystem = ActorSystem.create();
    BasicApi.apply(actorSystem).bindRoute("0.0.0.0", 8080);
  }

}
