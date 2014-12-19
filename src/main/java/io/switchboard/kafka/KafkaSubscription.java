package io.switchboard.kafka;

import akka.japi.JavaPartialFunction;
import akka.util.ByteString;
import akka.util.ByteString$;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import scala.Function1;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Christoph Grotz on 07.12.14.
 */
public class KafkaSubscription implements Subscription, Runnable {
  private final KafkaStream<byte[], byte[]> stream;
  private final Subscriber<String> subscriber;
  private final KafkaPublisher publisher;

  private AtomicBoolean running =  new AtomicBoolean(true);
  private AtomicLong requested = new AtomicLong();

  public KafkaSubscription(KafkaPublisher publisher, Subscriber<String> subscriber, KafkaStream<byte[], byte[]> stream) {
    this.stream = stream;
    this.subscriber = subscriber;
    this.publisher = publisher;
  }

  @Override
  public void request(long n) {
    requested.getAndAdd(n);
  }

  @Override
  public void cancel() {
    running.set(false);
  }

  @Override
  public void run() {
    try {
      ConsumerIterator<byte[], byte[]> itr = stream.iterator();
      while (running.get()) {
        if (requested.get() > 0) {
          try {
            MessageAndMetadata<byte[], byte[]> x = itr.next();
            subscriber.onNext(new String(x.message()));
            requested.decrementAndGet();
          }
          catch (Exception exp) {
            subscriber.onError(exp);
          }
        }
      }
    }
    catch (Exception exp) {
      subscriber.onError(exp);
    }
  }
}
