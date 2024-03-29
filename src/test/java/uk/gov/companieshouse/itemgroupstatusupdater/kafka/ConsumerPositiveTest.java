package uk.gov.companieshouse.itemgroupstatusupdater.kafka;

import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.patchRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.ITEM_GROUP_PROCESSED;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.PATCH_ORDERED_ITEM_URI;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.ERROR_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.INVALID_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.MAIN_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.RETRY_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.noOfRecordsForTopic;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.setUpSendAndWaitForMessage;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
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
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupstatusupdater.service.ItemStatusGroupUpdaterService;
import uk.gov.companieshouse.itemgroupstatusupdater.service.PatchOrderedItemService;
import uk.gov.companieshouse.itemgroupstatusupdater.service.Service;
import uk.gov.companieshouse.itemgroupstatusupdater.service.ServiceParameters;
import uk.gov.companieshouse.logging.Logger;

@SpringBootTest
@ActiveProfiles("test_main_positive")
@AutoConfigureWireMock(port = 0)
class ConsumerPositiveTest extends AbstractKafkaIntegrationTest {

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
    public void setup() {
        testConsumer.poll(Duration.ofSeconds(1));
    }

    @Test
    @DisplayName("Contents of Kafka message are consumed and relayed as HTTP request")
    void messageIsConsumedAndIsPropagated() throws Exception {

        // given
        givenThat(patch(urlEqualTo(PATCH_ORDERED_ITEM_URI))
            .willReturn(ok()));

        // when
        final ConsumerRecords<?, ?> consumerRecords = setUpSendAndWaitForMessage(environment,
            testConsumer, testProducer, latch);

        // then
        assertThat(noOfRecordsForTopic(consumerRecords, MAIN_TOPIC)).isEqualTo(1);
        assertThat(noOfRecordsForTopic(consumerRecords, RETRY_TOPIC)).isZero();
        assertThat(noOfRecordsForTopic(consumerRecords, ERROR_TOPIC)).isZero();
        assertThat(noOfRecordsForTopic(consumerRecords, INVALID_TOPIC)).isZero();
        verify(service).processMessage(new ServiceParameters(ITEM_GROUP_PROCESSED));
        WireMock.verify(patchRequestedFor(urlEqualTo(PATCH_ORDERED_ITEM_URI)));

    }

}
