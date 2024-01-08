package uk.gov.companieshouse.itemgroupstatusupdater.service;

import static uk.gov.companieshouse.itemgroupstatusupdater.logging.LoggingUtils.getLogMap;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.logging.Logger;

/**
 * Service that converts the <code>item-group-processed</code> Kafka message it receives into a
 * REST request it dispatches to the Orders API (<code>orders-api-ch-gov-uk</code>).
 */
@Component
public class ItemStatusGroupUpdaterService implements Service {

    private final Logger logger;
    private final PatchOrderedItemService patchOrderedItemService;

    public ItemStatusGroupUpdaterService(Logger logger, PatchOrderedItemService patchOrderedItemService) {
        this.logger = logger;
        this.patchOrderedItemService = patchOrderedItemService;
    }

    @Override
    public void processMessage(ServiceParameters parameters) {

        final var message = parameters.getData();
        final var orderNumber = message.getOrderNumber();
        final var itemId = message.getItem().getId();
        final var status = message.getItem().getStatus();
        final var digitalDocumentLocation = message.getItem().getDigitalDocumentLocation();

        logger.info("Processing message " + message + " for order ID " + orderNumber +
            ", item ID " + itemId + ".", getLogMap(orderNumber, itemId, status, digitalDocumentLocation));

        patchOrderedItemService.patchOrderedItem(orderNumber, itemId, status, digitalDocumentLocation);
    }

}