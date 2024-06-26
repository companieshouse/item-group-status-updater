package uk.gov.companieshouse.itemgroupstatusupdater.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.ITEM_GROUP_PROCESSED;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.RETRY_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.noOfRecordsForTopic;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;

@SpringBootTest
@ActiveProfiles("test_main_nonretryable")
class ConsumerInvalidTopicTest extends AbstractKafkaIntegrationTest {

    @Autowired
    private KafkaProducer<String, ItemGroupProcessed> testProducer;
    @Autowired
    private KafkaConsumer<String, ItemGroupProcessed> testConsumer;
    @Autowired
    private CountDownLatch latch;

    @BeforeEach
    public void drainKafkaTopics() {
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @Test
    void testPublishToInvalidMessageTopicIfInvalidDataDeserialised() throws InterruptedException {

        //when
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(), "key",
                ITEM_GROUP_PROCESSED));
        if (!latch.await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }

        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, Duration.ofSeconds(10L), 2);

        //then
        assertThat(noOfRecordsForTopic(consumerRecords, MAIN_TOPIC)).isEqualTo(1);
        assertThat(noOfRecordsForTopic(consumerRecords, RETRY_TOPIC)).isZero();
        assertThat(noOfRecordsForTopic(consumerRecords, ERROR_TOPIC)).isZero();
        assertThat(noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isEqualTo(1);
    }

}
