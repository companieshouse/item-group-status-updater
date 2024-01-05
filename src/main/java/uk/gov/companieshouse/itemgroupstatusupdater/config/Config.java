package uk.gov.companieshouse.itemgroupstatusupdater.config;

import static uk.gov.companieshouse.itemgroupstatusupdater.ItemGroupStatusUpdaterApplication.NAMESPACE;

import consumer.deserialization.AvroDeserializer;
import org.springframework.context.annotation.Scope;
import uk.gov.companieshouse.itemgroupstatusupdater.kafka.InvalidMessageRouter;
import uk.gov.companieshouse.itemgroupstatusupdater.kafka.MessageFlags;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.logging.util.DataMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import java.util.Map;

@Configuration
@EnableKafka
public class Config {

    @Bean
    public ConsumerFactory<String, ItemGroupProcessed> consumerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                        ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class,
                        ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, StringDeserializer.class,
                        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"),
                new StringDeserializer(), new ErrorHandlingDeserializer<>(new AvroDeserializer<>(ItemGroupProcessed.class)));
    }

    @Bean
    public ProducerFactory<String, ItemGroupProcessed> producerFactory(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                                                           MessageFlags messageFlags,
                                                           @Value("${invalid_message_topic}") String invalidMessageTopic,
                                                           AvroSerializer<ItemGroupProcessed> serializer) {
        return new DefaultKafkaProducerFactory<>(
                Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ProducerConfig.ACKS_CONFIG, "all",
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                        ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, InvalidMessageRouter.class.getName(),
                        "message.flags", messageFlags,
                        "invalid.message.topic", invalidMessageTopic),
                new StringSerializer(),
                (topic, data) -> {
                    try {
                        return serializer.toBinary(data); //creates a leading space
                    } catch (SerializationException e) {
                        var dataMap = new DataMap.Builder()
                                .topic(topic)
                                .kafkaMessage(data.toString())
                                .build()
                                .getLogMap();
                        getLogger().error("Caught SerializationException serializing kafka message.", dataMap);
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @Bean
    @Scope("prototype")
    public AvroSerializer<ItemGroupProcessed> serializer() {
        return new SerializerFactory().getSpecificRecordSerializer(ItemGroupProcessed.class);
    }

    @Bean
    public KafkaTemplate<String, ItemGroupProcessed> kafkaTemplate(ProducerFactory<String, ItemGroupProcessed> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ItemGroupProcessed> kafkaListenerContainerFactory(ConsumerFactory<String, ItemGroupProcessed> consumerFactory,
                                                                                                 @Value("${consumer.concurrency}") Integer concurrency) {
        ConcurrentKafkaListenerContainerFactory<String, ItemGroupProcessed> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

        @Bean
        Logger getLogger(){
            return LoggerFactory.getLogger(NAMESPACE);
        }

}

