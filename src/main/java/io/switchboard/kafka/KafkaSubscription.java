package io.switchboard.kafka;

import akka.util.ByteString;
import akka.util.ByteString$;
import kafka.consumer.KafkaStream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Iterator;

/**
 * Created by Christoph Grotz on 07.12.14.
 */
public class KafkaSubscription implements Subscription {
  private final KafkaStream<byte[], byte[]> stream;
  private final Subscriber subscriber;

  public KafkaSubscription(Subscriber subscriber, KafkaStream<byte[], byte[]> stream) {
    this.stream = stream;
    this.subscriber = subscriber;
  }

  @Override
  public void request(long n) {
    long i=0;
    Iterator itr = stream.iterator();
    while(itr.hasNext() && i<n) {
      ByteString data = ByteString$.MODULE$.apply("Data: " +itr.next());
      subscriber.onNext(data);
    }
  }

  @Override
  public void cancel() {

  }
}
