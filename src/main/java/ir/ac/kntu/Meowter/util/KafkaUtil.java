package ir.ac.kntu.Meowter.util;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.function.Consumer;

public class KafkaUtil {

    private final KafkaProducer<String, String> producer;
    private final KafkaConsumer<String, String> consumer;

    public KafkaUtil(String producerBootstrapServers, String consumerBootstrapServers, String consumerGroupId, String topic) {
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerBootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        this.producer = new KafkaProducer<>(producerProps);

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerBootstrapServers);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        this.consumer = new KafkaConsumer<>(consumerProps);

        this.consumer.subscribe(Collections.singletonList(topic));
    }

    public void sendNotification(String topic, String message) {
        producer.send(new ProducerRecord<>(topic, message), (metadata, exception) -> {
            if (exception == null) {
                System.out.println(CliFormatter.boldBlue("Sent notification to Kafka topic: ") + CliFormatter.boldRed(topic) + CliFormatter.boldYellow(", message: ") + CliFormatter.boldRed(message));
            } else {
                exception.printStackTrace();
            }
        });
    }

    public void listenForNotifications(Consumer<String> messageProcessor) {
        try {
            consumer.subscribe(Collections.singletonList("notifications"));
            System.out.println(CliFormatter.boldRed("Subscribed to topics: ") + consumer.subscription());

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    messageProcessor.accept(record.value());
                }
            }
        } catch (Exception ignored) {

        }
    }

    public void close() {
        producer.close();
        consumer.close();
    }
}
