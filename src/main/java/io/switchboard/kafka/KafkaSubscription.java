package io.switchboard.kafka;

import akka.japi.JavaPartialFunction;
import akka.util.ByteString;
import akka.util.ByteString$;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import scala.Function1;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Christoph Grotz on 07.12.14.
 */
public class KafkaSubscription implements Subscription {
  private final KafkaStream<byte[], byte[]> stream;
  private final Subscriber subscriber;

  private AtomicLong requested = new AtomicLong();

  public KafkaSubscription(Subscriber subscriber, KafkaStream<byte[], byte[]> stream) {
    this.stream = stream;
    this.subscriber = subscriber;
    new Thread(new Runnable() {
      @Override
      public void run() {
        stream.iterator().foreach(new JavaPartialFunction<MessageAndMetadata<byte[], byte[]>, Object>() {
          @Override
          public Object apply(MessageAndMetadata<byte[], byte[]> x, boolean isCheck) throws Exception {
            if(requested.get() > 0) {
              String message = "event: "+x.topic()+"\r\ndata: "+new String(x.message());
              System.out.println(message);
              subscriber.onNext(ByteString$.MODULE$.apply(message));
              requested.decrementAndGet();
            }
            return null;
          }
        });
      }
    }).start();
  }

  @Override
  public void request(long n) {
    requested.getAndAdd(n);
  }

  @Override
  public void cancel() {

  }
}
