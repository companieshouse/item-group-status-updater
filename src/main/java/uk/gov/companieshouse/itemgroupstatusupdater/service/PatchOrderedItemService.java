package uk.gov.companieshouse.itemgroupstatusupdater.service;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.itemgroupstatusupdater.dto.ItemStatusUpdateDto;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.util.DataMap;

/**
 * Service that propagates the updated status of an item in an order by sending a PATCH request to
 * the Orders API (<code>orders-api-ch-gov-uk</code>).
 */
@Service
public class PatchOrderedItemService {

    // TODO DCAC-216: Replace temporary test code with a private SDK based implementation.

    private final RestTemplate restTemplate;

    private final Logger logger;

    private final String chsApiUrl;

    private final String chsApiKey;

    public PatchOrderedItemService(
        RestTemplate restTemplate,
        Logger logger,
        @Value("${chs.api.url}") String chsApiUrl,
        @Value("${chs.api.key}") String chsApiKey) {
        this.restTemplate = restTemplate;
        this.logger = logger;
        this.chsApiUrl = chsApiUrl;
        this.chsApiKey = chsApiKey;
    }

    public void patchOrderedItem(
        final String orderNumber,
        final String itemId,
        final String status,
        final String digitalDocumentLocation) {

        final var headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setAccept(singletonList(APPLICATION_JSON));
        headers.setBasicAuth(chsApiKey);
        final var update = new ItemStatusUpdateDto(status, digitalDocumentLocation);
        final HttpEntity<ItemStatusUpdateDto> httpEntity = new HttpEntity<>(update, headers);

        try {
            restTemplate.exchange(
                chsApiUrl + orderedItemUrl(orderNumber, itemId),
                HttpMethod.PATCH,
                httpEntity,
                Void.class);
            logger.info("Item status update propagation successful for order number "
                + orderNumber + ", item " + itemId + ".",
                getLogMap(orderNumber, itemId, status, digitalDocumentLocation));
        } catch (RestClientException rce) {
            final String error = "Item status update propagation FAILED for order number "
                + orderNumber + ", item " + itemId + ", caught RestClientException with message "
                + rce.getMessage() + ".";
            logger.error(error,
                getLogMap(orderNumber, itemId, status, digitalDocumentLocation, rce.getMessage()));
            // TODO DCAC-216: throw new WhatException(error);?
        }
    }

    private String orderedItemUrl(final String orderNumber, final String itemId) {
        return "/orders/" + orderNumber + "/items/" + itemId;
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
