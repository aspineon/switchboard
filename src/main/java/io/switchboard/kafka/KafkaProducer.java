package io.switchboard.kafka;

import akka.stream.FlowMaterializer;
import akka.stream.scaladsl.Sink;
import akka.stream.scaladsl.Source;
import akka.util.ByteString;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.Properties;

/**
 *
 * Kafka Producer for the API
 *
 * Created by Christoph Grotz on 06.12.14.
 */
public class KafkaProducer implements Subscriber<ByteString> {

  private final Producer<String, String> producer;

  public KafkaProducer() {
    Properties props = new Properties();

    props.put("metadata.broker.list", "localhost:9092");
    props.put("serializer.class", "kafka.serializer.StringEncoder");

    this.producer = new Producer<>(new ProducerConfig(props));
  }

  public void send(String topic, String message) {
    this.producer.send(new KeyedMessage<String,String>(topic, message));
  }

  @Override
  public void onSubscribe(Subscription s) {

  }

  @Override
  public void onNext(ByteString byteString) {
    send("switchboard", byteString.utf8String());
  }

  @Override
  public void onError(Throwable t) {

  }

  @Override
  public void onComplete() {

  }
}
