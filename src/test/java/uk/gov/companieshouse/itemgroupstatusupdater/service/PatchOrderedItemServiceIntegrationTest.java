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
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.DIGITAL_DOCUMENT_LOCATION;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.ITEM_ID;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.ORDER_NUMBER;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.PATCH_ORDERED_ITEM_URI;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.STATUS;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestUtils.getExpectedReason;

import com.github.tomakehurst.wiremock.http.Fault;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.itemgroupstatusupdater.config.TestConfig;
import uk.gov.companieshouse.itemgroupstatusupdater.exception.RetryableException;

/**
 * Integration tests the {@link PatchOrderedItemService}.
 */
@SpringBootTest
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("test_main_positive")
@Import(TestConfig.class)
class PatchOrderedItemServiceIntegrationTest {

    @Autowired
    private PatchOrderedItemService serviceUnderTest;

    @Autowired
    private Environment environment;

    @SuppressWarnings("squid:S2699") // at least one assertion
    @Test
    @DisplayName("patchOrderedItem() patches the ordered item successfully")
    void patchOrderedItemPatchesItemSuccessfully() throws Exception {

        givenThat(patch(urlEqualTo(PATCH_ORDERED_ITEM_URI))
            .willReturn(ok()));

        patchOrderedItem();
    }

    @Test
    @DisplayName("patchOrderedItem() throws RetryableException for unknown item")
    void patchOrderedItemThrowsRetryableExceptionForUnknownItem() {

        givenThat(patch(urlEqualTo(PATCH_ORDERED_ITEM_URI))
            .willReturn(notFound()));

        assertOrdersApiRequestIssuePropagatedAsRetryableException(NOT_FOUND);
    }

    @Test
    @DisplayName("patchOrderedItem() throws RetryableException for service unavailable")
    void patchOrderedItemThrowsRetryableExceptionForServiceUnavailable() {

        givenThat(patch(urlEqualTo(PATCH_ORDERED_ITEM_URI))
            .willReturn(serviceUnavailable()));

        assertOrdersApiRequestIssuePropagatedAsRetryableException(SERVICE_UNAVAILABLE);
    }

    @Test
    @DisplayName("patchOrderedItem() throws RetryableException for connection reset")
    void patchOrderedItemThrowsRetryableExceptionForConnectionReset() {

        givenThat(patch(urlEqualTo(PATCH_ORDERED_ITEM_URI))
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
                this::patchOrderedItem);
        assertThat(exception.getMessage(),
            Is.is(getExpectedReason(underlyingStatus.value(), reasonPhrase)));
    }

    private void patchOrderedItem() throws Exception {
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        withEnvironmentVariable("API_URL", "http://localhost:" + wireMockPort)
            .and("CHS_API_KEY", "Token value")
            .and("PAYMENTS_API_URL", "NOT-USED")
            .and("DOCUMENT_API_LOCAL_URL", "NOT-USED")
            .execute(() -> serviceUnderTest.patchOrderedItem(ORDER_NUMBER, ITEM_ID, STATUS,
                DIGITAL_DOCUMENT_LOCATION));
    }

}