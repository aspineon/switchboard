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
    /*
    new PublisherSource<>(new KafkaConsumer("group2", "switchboard")).foreach(new JavaPartialFunction() {

      @Override
      public Object apply(Object x, boolean isCheck) throws Exception {
        System.out.println(x);
        return x;
      }
    }, FlowMaterializer.create(actorSystem));*/
  }

}
