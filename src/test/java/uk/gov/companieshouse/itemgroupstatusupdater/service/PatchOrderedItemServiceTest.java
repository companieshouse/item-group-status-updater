package uk.gov.companieshouse.itemgroupstatusupdater.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.DIGITAL_DOCUMENT_LOCATION;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.ITEM_ID;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.ORDER_NUMBER;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.PATCH_ORDERED_ITEM_URI;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.STATUS;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.getExpectedReason;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.order.PrivateOrderResourceHandler;
import uk.gov.companieshouse.api.handler.order.request.OrderedItemPatch;
import uk.gov.companieshouse.api.model.order.item.ItemStatusUpdateApi;
import uk.gov.companieshouse.itemgroupstatusupdater.exception.NonRetryableException;
import uk.gov.companieshouse.itemgroupstatusupdater.exception.RetryableException;
import uk.gov.companieshouse.logging.Logger;

/**
 * Unit tests the {@link PatchOrderedItemService}.
 */
@ExtendWith(MockitoExtension.class)
class PatchOrderedItemServiceTest {

    @InjectMocks
    private PatchOrderedItemService serviceUnderTest;

    @Mock
    private Logger logger;

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivateOrderResourceHandler privateOrderResourceHandler;

    @Mock
    private OrderedItemPatch orderedItemPatch;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    @DisplayName("patchOrderedItem() patches the item successfully")
    void patchOrderedItemPatchesItemSuccessfully() {

        // Given
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateOrderResourceHandler()).thenReturn(
            privateOrderResourceHandler);
        when(privateOrderResourceHandler.patchOrderedItem(anyString(),
            any(ItemStatusUpdateApi.class))).thenReturn(orderedItemPatch);

        // When
        serviceUnderTest.patchOrderedItem(ORDER_NUMBER, ITEM_ID, STATUS,
            DIGITAL_DOCUMENT_LOCATION);
    }

    @Test
    @DisplayName("patchOrderedItem() propagates an ApiErrorResponseException wrapped in a RetryableException")
    void patchOrderedItemErrorsRetryablyForApiErrorResponseException()
        throws ApiErrorResponseException, URIValidationException {

        // Given
        final var underlyingErrorMessage = "Unknown IO error.";
        givenRequestExecutionException(
            ApiErrorResponseException.fromIOException(new IOException(underlyingErrorMessage)));

        // When and then
        final RetryableException exception =
            assertThrows(RetryableException.class,
                () -> serviceUnderTest.patchOrderedItem(ORDER_NUMBER, ITEM_ID, STATUS,
                    DIGITAL_DOCUMENT_LOCATION));
        assertThat(exception.getMessage(),
            is(getExpectedReason(INTERNAL_SERVER_ERROR.value(), underlyingErrorMessage)));
    }

    @Test
    @DisplayName("patchOrderedItem() propagates a URIValidationException wrapped in a NonRetryableException")
    void patchOrderedItemErrorsNonRetryablyForURIValidationException()
        throws ApiErrorResponseException, URIValidationException {

        // Given
        final var underlyingErrorMessage = "URI pattern does not match expected URI pattern for this resource.";
        givenRequestExecutionException(new URIValidationException(underlyingErrorMessage));

        // When and then
        final NonRetryableException exception =
            assertThrows(NonRetryableException.class,
                () -> serviceUnderTest.patchOrderedItem(ORDER_NUMBER, ITEM_ID, STATUS,
                    DIGITAL_DOCUMENT_LOCATION));
        final var expectedError = "Invalid URI " + PATCH_ORDERED_ITEM_URI +
            " for patch ordered item.";
        assertThat(exception.getMessage(), is(expectedError));
    }

    private void givenRequestExecutionException(final Exception exception)
        throws ApiErrorResponseException, URIValidationException {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateOrderResourceHandler()).thenReturn(
            privateOrderResourceHandler);
        when(privateOrderResourceHandler.patchOrderedItem(anyString(),
            any(ItemStatusUpdateApi.class))).thenReturn(orderedItemPatch);
        when(orderedItemPatch.execute()).thenThrow(exception);
    }

}