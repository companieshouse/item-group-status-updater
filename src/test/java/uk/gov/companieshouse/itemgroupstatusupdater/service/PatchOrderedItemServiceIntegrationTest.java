package uk.gov.companieshouse.itemgroupstatusupdater.service;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.DIGITAL_DOCUMENT_LOCATION;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.ITEM_ID;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.ORDER_NUMBER;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.PATCH_ORDERED_ITEM_URI;
import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.STATUS;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.companieshouse.itemgroupstatusupdater.config.TestConfig;
import uk.gov.companieshouse.itemgroupstatusupdater.exception.RetryableException;

/**
 * Integration tests the {@link PatchOrderedItemService}.
 */
@SpringBootTest
@Tag("integration-test")
@ActiveProfiles("test_main_positive")
@Import(TestConfig.class)
class PatchOrderedItemServiceIntegrationTest {

    @RegisterExtension
    static final WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .configureStaticDsl(true)
            .build();

    @DynamicPropertySource
    static void wireMockProperties(DynamicPropertyRegistry registry) {
        registry.add("wiremock.server.port", wireMock::getPort);
    }

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

        assertOrdersApiRequestIssuePropagatedAsRetryableException(INTERNAL_SERVER_ERROR);
    }

    private void assertOrdersApiRequestIssuePropagatedAsRetryableException(
        final HttpStatus underlyingStatus) {
        final RetryableException exception =
            assertThrows(RetryableException.class,
                this::patchOrderedItem);
        assertThat(exception.getMessage(),
            allOf(
                containsString("Received unexpected response status code "
                    + underlyingStatus.value()),
                containsString("patch ordered item at " + PATCH_ORDERED_ITEM_URI)));
    }

    private void patchOrderedItem() throws Exception {
        final String wireMockPort = environment.getProperty("wiremock.server.port");
        withEnvironmentVariable("API_URL", "http://localhost:" + wireMockPort)
            .and("CHS_API_KEY", "Token value")
            .and("PAYMENTS_API_URL", "NOT-USED")
            .and("DOCUMENT_API_LOCAL_URL", "NOT-USED")
            .and("ORACLE_QUERY_API_URL", "NOT-USED")
            .execute(() -> serviceUnderTest.patchOrderedItem(ORDER_NUMBER, ITEM_ID, STATUS,
                DIGITAL_DOCUMENT_LOCATION));
    }

}