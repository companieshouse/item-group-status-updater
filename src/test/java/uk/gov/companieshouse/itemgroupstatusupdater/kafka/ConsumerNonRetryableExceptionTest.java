package uk.gov.companieshouse.itemgroupstatusupdater.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.itemgroupstatusupdater.Constants.ITEM_GROUP_PROCESSED;
import static uk.gov.companieshouse.itemgroupstatusupdater.TestUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.TestUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.TestUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.TestUtils.RETRY_TOPIC;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupstatusupdater.TestUtils;
import uk.gov.companieshouse.itemgroupstatusupdater.exception.NonRetryableException;
import uk.gov.companieshouse.itemgroupstatusupdater.service.Service;
import uk.gov.companieshouse.itemgroupstatusupdater.service.ServiceParameters;

@SpringBootTest
@ActiveProfiles("test_main_nonretryable")
class ConsumerNonRetryableExceptionTest extends AbstractKafkaIntegrationTest {


    @Autowired
    private KafkaProducer<String, ItemGroupProcessed> testProducer;
    @Autowired
    private KafkaConsumer<String, ItemGroupProcessed> testConsumer;

    @Autowired
    private CountDownLatch latch;

    @MockBean
    private Service service;

    @BeforeEach
    public void drainKafkaTopics() {
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @Test
    void testRepublishToInvalidMessageTopicIfNonRetryableExceptionThrown() throws InterruptedException {
        //given
        doThrow(NonRetryableException.class).when(service).processMessage(any());

        //when
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(), "key",
                ITEM_GROUP_PROCESSED));
        if (!latch.await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }
        ConsumerRecords<?, ?> consumerRecords = KafkaTestUtils.getRecords(testConsumer, 10000L, 2);

        //then
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, MAIN_TOPIC)).isEqualTo(1);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, RETRY_TOPIC)).isZero();
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, ERROR_TOPIC)).isZero();
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isEqualTo(1);
        verify(service).processMessage(new ServiceParameters(ITEM_GROUP_PROCESSED));
    }
}
