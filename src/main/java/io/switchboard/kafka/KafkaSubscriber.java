package io.switchboard.kafka;

import akka.stream.FlowMaterializer;
import akka.stream.scaladsl.Sink;
import akka.stream.scaladsl.Source;
import akka.util.ByteString;
import io.switchboard.boot.Config;
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
public class KafkaSubscriber implements Subscriber<String> {

  private final Producer<String, String> producer;
  private String topic;
  private Subscription subscription;

  public KafkaSubscriber(String topic) {
    Properties props = new Properties();

    props.put("metadata.broker.list", Config.get().get(Config.SWITCHBOARD_METADATA_BROKER_LIST));
    props.put("serializer.class", "kafka.serializer.StringEncoder");

    this.producer = new Producer<>(new ProducerConfig(props));
    this.topic = topic;

    Runtime.getRuntime().addShutdownHook(new Thread(() -> producer.close()));
  }

  public void send(String topic, String message) {
    this.producer.send(new KeyedMessage<String,String>(topic, message));
  }

  @Override
  public void onSubscribe(Subscription s) {
    this.subscription = s;
    s.request(4);
  }

  @Override
  public void onNext(String string) {
    send(topic, string);
    subscription.request(4);
  }

  @Override
  public void onError(Throwable t) {
    t.printStackTrace();
  }

  @Override
  public void onComplete() {

  }
}
