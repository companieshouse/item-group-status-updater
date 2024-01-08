package uk.gov.companieshouse.itemgroupstatusupdater.util;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.PATCH_ORDERED_ITEM_URI;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.core.env.Environment;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;

public class TestUtils {

    public static final String MAIN_TOPIC = "echo";
    public static final String RETRY_TOPIC = "echo-retry";
    public static final String ERROR_TOPIC = "echo-error";
    public static final String INVALID_TOPIC = "echo-invalid";

    private TestUtils() {
    }

    public static int noOfRecordsForTopic(ConsumerRecords<?, ?> records, String topic) {
        int count = 0;
        for (ConsumerRecord<?, ?> ignored : records.records(topic)) {
            count++;
        }
        return count;
    }

    public static String getExpectedReason(int statusCode, String reasonPhrase) {
        return "Received unexpected response status code " + statusCode
            + ", and status message '" + reasonPhrase + "' sending request to "
            + "patch ordered item at " + PATCH_ORDERED_ITEM_URI + ".";
    }

    public static ConsumerRecords<?, ?> setUpSendAndWaitForMessage(final Environment environment,
        final KafkaConsumer<String, ItemGroupProcessed> testConsumer,
        final KafkaProducer<String, ItemGroupProcessed> testProducer,
        final CountDownLatch latch) throws Exception {
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        withEnvironmentVariable("API_URL", "http://localhost:" + wireMockPort)
            .and("CHS_API_KEY", "Token value")
            .and("PAYMENTS_API_URL", "NOT-USED")
            .and("DOCUMENT_API_LOCAL_URL", "NOT-USED")
            .execute(() -> sendAndWaitForMessage(testProducer, latch));
        return KafkaTestUtils.getRecords(testConsumer, 10000L, 6);
    }

    private static void sendAndWaitForMessage(
        final KafkaProducer<String, ItemGroupProcessed> testProducer, final CountDownLatch latch)
        throws InterruptedException {
        testProducer.send(new ProducerRecord<>(MAIN_TOPIC, 0, System.currentTimeMillis(), "key",
            TestConstants.ITEM_GROUP_PROCESSED));
        if (!latch.await(30L, TimeUnit.SECONDS)) {
            fail("Timed out waiting for latch");
        }
    }
}
