package uk.gov.companieshouse.itemgroupstatusupdater.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import static org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy.SINGLE_TOPIC;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupstatusupdater.exception.RetryableException;
import uk.gov.companieshouse.itemgroupstatusupdater.service.Service;
import uk.gov.companieshouse.itemgroupstatusupdater.service.ServiceParameters;

/**
 * Consumes messages from the configured main Kafka topic.
 */
@Component
public class Consumer {

    private final Service service;
    private final MessageFlags messageFlags;

    public Consumer(Service service, MessageFlags messageFlags) {
        this.service = service;
        this.messageFlags = messageFlags;
    }

    /**
     * Consume a message from the main Kafka topic.
     *
     * @param message A message containing a payload.
     */
    @KafkaListener(
            id = "${consumer.group_id}",
            containerFactory = "kafkaListenerContainerFactory",
            topics = "${consumer.topic}",
            groupId = "${consumer.group_id}"
    )
    @RetryableTopic(
            attempts = "${consumer.max_attempts}",
            autoCreateTopics = "false",
            backoff = @Backoff(delayExpression = "${consumer.backoff_delay}"),
            dltTopicSuffix = "-error",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            sameIntervalTopicReuseStrategy = SINGLE_TOPIC,
            include = RetryableException.class
    )
    public void consume(Message<ItemGroupProcessed> message) {
        try {
            service.processMessage(new ServiceParameters(message.getPayload()));
        } catch (RetryableException e) {
            messageFlags.setRetryable(true);
            throw e;
        }
    }
}
