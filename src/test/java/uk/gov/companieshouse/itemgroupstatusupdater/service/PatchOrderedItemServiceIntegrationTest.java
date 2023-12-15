package uk.gov.companieshouse.itemgroupstatusupdater.service;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import com.github.tomakehurst.wiremock.http.Fault;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.itemgroupstatusupdater.exception.RetryableException;

@SpringBootTest
@AutoConfigureWireMock(port = 0)
class PatchOrderedItemServiceIntegrationTest {

    private static final String ORDER_NUMBER = "ORD-844016-962315";
    private static final String ITEM_ID = "CCD-289716-962308";
    private static final String STATUS = "satisfied";
    private static final String DIGITAL_DOCUMENT_LOCATION =
        "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf";

    @Autowired
    private PatchOrderedItemService serviceUnderTest;

    @Autowired
    private Environment environment;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    @DisplayName("patchOrderedItem() patches the ordered item successfully")
    void patchOrderedItemPatchesItemSuccessfully() throws Exception {

        givenThat(patch(urlEqualTo("/orders/ORD-844016-962315/items/CCD-289716-962308"))
            .willReturn(ok()));

        patchOrderedItem();
    }

    @Test
    @DisplayName("patchOrderedItem() throws RetryableException for unknown item")
    void patchOrderedItemThrowsRetryableExceptionForUnknownItem() {

        givenThat(patch(urlEqualTo("/orders/ORD-844016-962315/items/CCD-289716-962308"))
            .willReturn(notFound()));

        assertOrdersApiRequestIssuePropagatedAsRetryableException(NOT_FOUND);
    }

    @Test
    @DisplayName("patchOrderedItem() throws RetryableException for service unavailable")
    void patchOrderedItemThrowsRetryableExceptionForServiceUnavailable() {

        givenThat(patch(urlEqualTo("/orders/ORD-844016-962315/items/CCD-289716-962308"))
            .willReturn(serviceUnavailable()));

        assertOrdersApiRequestIssuePropagatedAsRetryableException(SERVICE_UNAVAILABLE);
    }

    @Test
    @DisplayName("patchOrderedItem() throws RetryableException for connection reset")
    void patchOrderedItemThrowsRetryableExceptionForConnectionReset() {

        givenThat(patch(urlEqualTo("/orders/ORD-844016-962315/items/CCD-289716-962308"))
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertOrdersApiRequestIssuePropagatedAsRetryableException(INTERNAL_SERVER_ERROR,
            "Connection reset");
    }

    private void assertOrdersApiRequestIssuePropagatedAsRetryableException(
        final HttpStatus underlyingStatus) {
        assertOrdersApiRequestIssuePropagatedAsRetryableException(underlyingStatus,
            underlyingStatus.getReasonPhrase());
    }

    private void assertOrdersApiRequestIssuePropagatedAsRetryableException(
        final HttpStatus underlyingStatus, final String reasonPhrase) {
        final RetryableException exception =
            assertThrows(RetryableException.class,
                () -> patchOrderedItem());
        final String expectedReason =
            "Received unexpected response status code " + underlyingStatus.value()
                + ", and status message '" + reasonPhrase + "' sending request to "
                + "patch ordered item at /orders/ORD-844016-962315/items/CCD-289716-962308.";
        assertThat(exception.getMessage(), Is.is(expectedReason));
    }

    private void patchOrderedItem() throws Exception {
        final int statusCode;
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        withEnvironmentVariable("API_URL", "http://localhost:" + wireMockPort)
            .and("CHS_API_KEY", "Token value")
            .and("PAYMENTS_API_URL", "NOT-USED")
            .and("DOCUMENT_API_LOCAL_URL", "NOT-USED")
            .execute(() -> serviceUnderTest.patchOrderedItem(ORDER_NUMBER, ITEM_ID, STATUS,
                DIGITAL_DOCUMENT_LOCATION));
    }

}