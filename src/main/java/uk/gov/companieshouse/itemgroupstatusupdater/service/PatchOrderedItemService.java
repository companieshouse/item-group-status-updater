package uk.gov.companieshouse.itemgroupstatusupdater.service;

import static java.util.Collections.singletonList;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriTemplate;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.order.item.ItemStatusUpdateApi;
import uk.gov.companieshouse.itemgroupstatusupdater.exception.NonRetryableException;
import uk.gov.companieshouse.itemgroupstatusupdater.exception.RetryableException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

/**
 * Service that propagates the updated status of an item in an order by sending a PATCH request to
 * the Orders API (<code>orders-api-ch-gov-uk</code>).
 */
@Service
public class PatchOrderedItemService {

    private static final UriTemplate PATCH_ORDERED_ITEM_URL = new UriTemplate(
        "/orders/{orderNumber}/items/{itemId}");

    private final ApiClientService apiClientService;

    private final Logger logger;

    public PatchOrderedItemService(
        ApiClientService apiClientService,
        Logger logger) {
        this.apiClientService = apiClientService;
        this.logger = logger;
    }

    public void patchOrderedItem(
        final String orderNumber,
        final String itemId,
        final String status,
        final String digitalDocumentLocation) {
        final var uri = PATCH_ORDERED_ITEM_URL.expand(orderNumber, itemId).toString();
        logger.info("Patching ordered item at " + uri + ".",
            getLogMap(orderNumber, itemId, status, digitalDocumentLocation));
        patchOrderedItem(uri, orderNumber, itemId, status, digitalDocumentLocation);
    }

    private void patchOrderedItem(
        final String uri,
        final String orderNumber,
        final String itemId,
        final String status,
        final String digitalDocumentLocation) {

        final var apiClient = apiClientService.getInternalApiClient();
        final var update = new ItemStatusUpdateApi(status, digitalDocumentLocation);

        try {
            apiClient.privateOrderResourceHandler().patchOrderedItem(uri, update).execute();
        } catch (ApiErrorResponseException ex) {
            final String error = "Received unexpected response status code " + ex.getStatusCode()
                + ", and status message '" + ex.getStatusMessage()
                + "' sending request to patch ordered item at " + uri + ".";
            logger.error(error, ex,
                getLogMap(orderNumber, itemId, status, digitalDocumentLocation, error));
            throw new RetryableException(error, ex);
        } catch (URIValidationException ex) {
            // Should this happen (unlikely), it is a programmatic error, hence not recoverable.
            final String error = "Invalid URI " + uri + " for patch ordered item.";
            logger.error(error, ex,
                getLogMap(orderNumber, itemId, status, digitalDocumentLocation, error));
            throw new NonRetryableException(error, ex);
        }
    }

    private Map<String, Object> getLogMap(
        final String orderNumber,
        final String itemId,
        final String status,
        final String digitalDocumentLocation) {
        return new DataMap.Builder()
            .orderId(orderNumber)
            .itemId(itemId)
            .status(status)
            .digitalDocumentLocation(digitalDocumentLocation)
            .build()
            .getLogMap();
    }

    private Map<String, Object> getLogMap(
        final String orderNumber,
        final String itemId,
        final String status,
        final String digitalDocumentLocation,
        final String error) {
        return new DataMap.Builder()
            .orderId(orderNumber)
            .itemId(itemId)
            .status(status)
            .digitalDocumentLocation(digitalDocumentLocation)
            .errors(singletonList(error))
            .build()
            .getLogMap();
    }

}
