package io.switchboard.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.consumer.TopicFilter;
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
public class KafkaPublisher implements  Publisher {

  public static Publisher get(String groupId, String topic) {
    return new KafkaPublisher(groupId, topic);
  }

  private final String topic;
  private final ConsumerConnector consumer;

  public KafkaPublisher(String groupId, String topic) {
    Properties props = new Properties();
    props.put("zookeeper.connect", "127.0.0.1:2181");
    props.put("group.id", groupId);
    props.put("zookeeper.session.timeout.ms", "400");
    props.put("zookeeper.sync.time.ms", "200");
    props.put("auto.commit.interval.ms", "1000");

    this.consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(props));

    this.topic = topic;
  }

  @Override
  public void subscribe(Subscriber subscriber) {
    System.out.println("subscribe "+subscriber);
    Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
    topicCountMap.put(topic, 1);
    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
    KafkaStream<byte[], byte[]> stream =  consumerMap.get(topic).get(0);

    KafkaSubscription subscription = new KafkaSubscription(subscriber, stream);
    subscriber.onSubscribe(subscription);
  }
}
