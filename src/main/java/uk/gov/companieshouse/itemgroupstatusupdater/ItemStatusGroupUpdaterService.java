package uk.gov.companieshouse.itemgroupstatusupdater;

import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

/**
 * Service that converts the <code>item-group-processed</code> Kafka message it receives into a
 * REST request it dispatches to the Orders API (<code>orders-api-ch-gov-uk</code>).
 */
@Component
class ItemStatusGroupUpdaterService implements Service {

    private final Logger logger;

    ItemStatusGroupUpdaterService(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void processMessage(ServiceParameters parameters) {

        final var message = parameters.getData();
        final var orderNumber = message.getOrderNumber();
        final var itemId = message.getItem().getId();

        logger.info("Processing message " + message + " for order ID " + orderNumber +
            ", item ID " + itemId + ".", getLogMap(orderNumber, itemId));

    }

    private Map<String, Object> getLogMap(final String orderId, final String itemId) {
        return new DataMap.Builder()
            .orderId(orderId)
            .itemId(itemId)
            .build()
            .getLogMap();
    }

}