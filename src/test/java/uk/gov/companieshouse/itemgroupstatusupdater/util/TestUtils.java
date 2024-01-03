package uk.gov.companieshouse.itemgroupstatusupdater.util;

import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.PATCH_ORDERED_ITEM_URI;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

public class TestUtils {

    public static final String MAIN_TOPIC = "echo";
    public static final String RETRY_TOPIC = "echo-retry";
    public static final String ERROR_TOPIC = "echo-error";
    public static final String INVALID_TOPIC = "echo-invalid";

    private TestUtils() {}

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
}
