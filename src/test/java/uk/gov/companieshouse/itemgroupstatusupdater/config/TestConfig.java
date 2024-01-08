package uk.gov.companieshouse.itemgroupstatusupdater.config;


import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.RETRY_TOPIC;

import consumer.deserialization.AvroDeserializer;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupstatusupdater.service.NonRetryableExceptionService;
import uk.gov.companieshouse.itemgroupstatusupdater.service.Service;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;


@TestConfiguration
public class TestConfig {

    @Bean
    CountDownLatch latch(@Value("${steps}") int steps) {
        return new CountDownLatch(steps);
    }

    @Bean
    KafkaConsumer<String, ItemGroupProcessed> testConsumer(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        KafkaConsumer<String, ItemGroupProcessed> kafkaConsumer = new KafkaConsumer<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false",
                        ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString()),
                new StringDeserializer(), new AvroDeserializer<>(ItemGroupProcessed.class));
        kafkaConsumer.subscribe(List.of(MAIN_TOPIC, ERROR_TOPIC, RETRY_TOPIC,
                INVALID_TOPIC));
        return kafkaConsumer;
    }

    @Bean
    KafkaProducer<String, ItemGroupProcessed> testProducer(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new KafkaProducer<>(
                Map.of(
                        ProducerConfig.ACKS_CONFIG, "all",
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers),
                new StringSerializer(),
                (topic, data) -> {
                    try {
                        return new SerializerFactory()
                                .getSpecificRecordSerializer(ItemGroupProcessed.class).toBinary(data);
                    } catch (SerializationException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    @Bean
    @Primary
    public Service getService() {
        return new NonRetryableExceptionService();
    }
}
