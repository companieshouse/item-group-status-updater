package uk.gov.companieshouse.itemgroupstatusupdater.kafka;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.itemgroupstatusupdater.Constants.ITEM_GROUP_PROCESSED;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;


@ExtendWith(MockitoExtension.class)
class InvalidMessageRouterTest {

    private InvalidMessageRouter invalidMessageRouter;

    @Mock
    private MessageFlags flags;

    @BeforeEach
    void setup() {
        invalidMessageRouter = new InvalidMessageRouter();
        invalidMessageRouter.configure(Map.of("message.flags", flags, "invalid.message.topic", "invalid"));
    }

    @Test
    void testOnSendRoutesMessageToInvalidMessageTopicIfNonRetryableExceptionThrown() {
        // given
        ProducerRecord<String, ItemGroupProcessed> message = new ProducerRecord<>("main", "key", ITEM_GROUP_PROCESSED);

        // when
        ProducerRecord<String, ItemGroupProcessed> actual = invalidMessageRouter.onSend(message);

        // then
        assertThat(actual, is(equalTo(new ProducerRecord<>("invalid", "key", ITEM_GROUP_PROCESSED))));
    }

    @Test
    void testOnSendRoutesMessageToTargetTopicIfRetryableExceptionThrown() {
        // given
        ProducerRecord<String, ItemGroupProcessed> message = new ProducerRecord<>("main", "key", ITEM_GROUP_PROCESSED);
        when(flags.isRetryable()).thenReturn(true);

        // when
        ProducerRecord<String, ItemGroupProcessed> actual = invalidMessageRouter.onSend(message);

        // then
        assertThat(actual, is(sameInstance(message)));
    }

}
