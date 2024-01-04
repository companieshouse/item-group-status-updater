package uk.gov.companieshouse.itemgroupstatusupdater.kafka;

import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.ITEM_GROUP_PROCESSED;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;
import uk.gov.companieshouse.itemgroupstatusupdater.config.TestConfig;
import uk.gov.companieshouse.itemgroupstatusupdater.ItemGroupStatusUpdaterApplication;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * "Test" class re-purposed to produce Item Group Processed messages to the <code>item-group-processed</code>
 * topic in Tilt. This is NOT to be run as part of an automated test suite. It is for manual testing only.
 */
@SpringBootTest(classes = ItemGroupStatusUpdaterApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(locations="classpath:item-group-processed-in-tilt.properties")
@Import(TestConfig.class)
@SuppressWarnings("squid:S3577") // This is NOT to be run as part of an automated test suite.
class ItemGroupProcessedInTiltProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger("ItemGroupProcessedInTiltProducer");

    private static final int MESSAGE_WAIT_TIMEOUT_SECONDS = 10;

    @Value("${consumer.topic}")
    private String itemGroupProcessedTopic;

    @Autowired
    private KafkaProducer<String, ItemGroupProcessed> testProducer;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    void produceMessageToTilt() throws InterruptedException, ExecutionException, TimeoutException {
        final var future = testProducer.send(new ProducerRecord<>(
                itemGroupProcessedTopic, 0, System.currentTimeMillis(), "key", ITEM_GROUP_PROCESSED));
        final var result = future.get(MESSAGE_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        final var partition = result.partition();
        final var offset = result.offset();
        LOGGER.info("Message " + ITEM_GROUP_PROCESSED + " delivered to topic " + itemGroupProcessedTopic
                + " on partition " + partition + " with offset " + offset + ".");
    }
}
