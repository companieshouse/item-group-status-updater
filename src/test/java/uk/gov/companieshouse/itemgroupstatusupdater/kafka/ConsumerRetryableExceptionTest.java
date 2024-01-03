package uk.gov.companieshouse.itemgroupstatusupdater.kafka;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.itemgroupstatusupdater.TestUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.TestUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.TestUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.TestUtils.RETRY_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.ITEM_GROUP_PROCESSED;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.PATCH_ORDERED_ITEM_URI;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupstatusupdater.TestUtils;
import uk.gov.companieshouse.itemgroupstatusupdater.service.ItemStatusGroupUpdaterService;
import uk.gov.companieshouse.itemgroupstatusupdater.service.PatchOrderedItemService;
import uk.gov.companieshouse.itemgroupstatusupdater.service.Service;
import uk.gov.companieshouse.itemgroupstatusupdater.service.ServiceParameters;
import uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants;
import uk.gov.companieshouse.logging.Logger;

@SpringBootTest
@ActiveProfiles("test_main_retryable")
@AutoConfigureWireMock(port = 0)
class ConsumerRetryableExceptionTest extends AbstractKafkaIntegrationTest {

    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        public Service getService(final Logger logger, final PatchOrderedItemService patcher) {
            return new ItemStatusGroupUpdaterService(logger, patcher);
        }

    }

    @Autowired
    private Environment environment;

    @Autowired
    private KafkaProducer<String, ItemGroupProcessed> testProducer;
    @Autowired
    private KafkaConsumer<String, ItemGroupProcessed> testConsumer;

    @Autowired
    private CountDownLatch latch;

    @SpyBean
    private Service service;

    @BeforeEach
    public void drainKafkaTopics() {
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("Contents of Kafka message are consumed multiple times before being sent to DLT")
    void testRepublishToErrorTopicThroughRetryTopics() throws Exception {

        givenThat(patch(urlEqualTo(PATCH_ORDERED_ITEM_URI))
            .willReturn(notFound()));

        // when
        final ConsumerRecords<?, ?> consumerRecords = setUpSendAndWaitForMessage();

        //then
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, MAIN_TOPIC)).isEqualTo(1);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, RETRY_TOPIC)).isEqualTo(3);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, ERROR_TOPIC)).isEqualTo(1);
        assertThat(TestUtils.noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isZero();
        verify(service, times(4)).processMessage(new ServiceParameters(ITEM_GROUP_PROCESSED));
        WireMock.verify(exactly(4), patchRequestedFor(urlEqualTo(PATCH_ORDERED_ITEM_URI)));
    }

    private ConsumerRecords<?, ?> setUpSendAndWaitForMessage() throws Exception {
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        withEnvironmentVariable("API_URL", "http://localhost:" + wireMockPort)
            .and("CHS_API_KEY", "Token value")
            .and("PAYMENTS_API_URL", "NOT-USED")
            .and("DOCUMENT_API_LOCAL_URL", "NOT-USED")
            .execute(this::sendAndWaitForMessage);
        return KafkaTestUtils.getRecords(testConsumer, 10000L, 6);
    }

    private void sendAndWaitForMessage() throws InterruptedException {
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(), "key",
            TestConstants.ITEM_GROUP_PROCESSED));
        if (!latch.await(5L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }
    }
}
