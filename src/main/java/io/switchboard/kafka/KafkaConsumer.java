package io.switchboard.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Example Kafka Consumer
 *
 * Created by Christoph Grotz on 06.12.14.
 */
public class KafkaConsumer implements  Publisher {

  public static Publisher get(String topic) {
    return new KafkaConsumer(topic);
  }

  private final String topic;
  private final ConsumerConnector consumer;

  public KafkaConsumer(String topic) {
    Properties props = new Properties();
    props.put("zookeeper.connect", "127.0.0.1:2181");
    props.put("group.id", "group1");
    props.put("zookeeper.session.timeout.ms", "400");
    props.put("zookeeper.sync.time.ms", "200");
    props.put("auto.commit.interval.ms", "1000");

    this.consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));
    this.topic = topic;
  }

  @Override
  public void subscribe(Subscriber subscriber) {
    Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
    topicCountMap.put(topic, 1);
    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
    KafkaStream<byte[], byte[]> stream =  consumerMap.get(topic).get(0);

    KafkaSubscription subscription = new KafkaSubscription(subscriber, stream);
    subscriber.onSubscribe(subscription);
  }
}
