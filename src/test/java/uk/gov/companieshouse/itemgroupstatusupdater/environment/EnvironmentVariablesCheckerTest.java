package uk.gov.companieshouse.itemgroupstatusupdater.environment;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.API_URL;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.BACKOFF_DELAY;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.BOOTSTRAP_SERVER_URL;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.CHS_API_KEY;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.CONCURRENT_LISTENER_INSTANCES;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.DOCUMENT_API_LOCAL_URL;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.GROUP_ID;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.INVALID_MESSAGE_TOPIC;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.MAX_ATTEMPTS;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.PAYMENTS_API_URL;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.SERVER_PORT;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.TOPIC;

import com.github.stefanbirkner.systemlambda.SystemLambda.WithEnvironmentVariables;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.itemgroupstatusupdater.TestConfig;

@SpringBootTest
@ActiveProfiles("test_main_positive")
@Import(TestConfig.class)
class EnvironmentVariablesCheckerTest {

    private static final String TOKEN_VALUE = "token value";

    @DisplayName("returns true if all required environment variables are present")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsTrue() throws Exception {
        final var allVariables = buildAllVariablesMap();
        final var environmentVariables = buildVariables(allVariables);
        environmentVariables.execute(() ->
            assertTrue(EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent()));
    }

    @DisplayName("returns false if API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfApiUrlMissing() throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(API_URL);
    }

    @DisplayName("returns false if BACKOFF_DELAY is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfBackoffDelayMissing() throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(BACKOFF_DELAY);
    }

    @DisplayName("returns false if BOOTSTRAP_SERVER_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfBootstrapServerUrlMissing() throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(BOOTSTRAP_SERVER_URL);
    }

    @DisplayName("returns false if CHS_API_KEY is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfChsApiKeyMissing() throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(CHS_API_KEY);
    }

    @DisplayName("returns false if CONCURRENT_LISTENER_INSTANCES is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfConcurrentListenerInstancesMissing() throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(CONCURRENT_LISTENER_INSTANCES);
    }

    @DisplayName("returns false if DOCUMENT_API_LOCAL_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfDocumentApiLocalUrlMissing()
        throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(DOCUMENT_API_LOCAL_URL);
    }

    @DisplayName("returns false if GROUP_ID is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfGroupIdMissing()
        throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(GROUP_ID);
    }

    @DisplayName("returns false if INVALID_MESSAGE_TOPIC is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfInvalidMessageTopicMissing()
        throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(INVALID_MESSAGE_TOPIC);
    }

    @DisplayName("returns false if MAX_ATTEMPTS is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfMaxAttemptsMissing()
        throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(MAX_ATTEMPTS);
    }

    @DisplayName("returns false if PAYMENTS_API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfPaymentsApiUrlMissing() throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(PAYMENTS_API_URL);
    }

    @DisplayName("returns false if SERVER_PORT is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfServerPortMissing() throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(SERVER_PORT);
    }

    @DisplayName("returns false if TOPIC is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfTopicMissing() throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(TOPIC);
    }

    private void populateAllVariablesExceptOneAndAssertSomethingMissing(
        final EnvironmentVariablesChecker.RequiredEnvironmentVariables excludedVariable)
        throws Exception {
        final var variables = buildVariablesMap(excludedVariable);
        final var environmentVariables = buildVariables(variables);
        environmentVariables.execute(() ->
            assertFalse(EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent()));

    }

    private Map<String, String> buildAllVariablesMap() {
        final Map<String, String> variables = new HashMap<>();
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(
            variable -> variables.put(variable.getName(), TOKEN_VALUE));
        return variables;
    }

    private Map<String, String> buildVariablesMap(
        final EnvironmentVariablesChecker.RequiredEnvironmentVariables excludedVariable) {
        final Map<String, String> variables = new HashMap<>();
        stream(EnvironmentVariablesChecker.RequiredEnvironmentVariables.values()).forEach(
            variable -> {
                if (variable != excludedVariable) {
                    variables.put(variable.getName(), TOKEN_VALUE);
                }
            });
        return variables;
    }

    private WithEnvironmentVariables buildVariables(final Map<String, String> variables) {
        int index = 0;
        WithEnvironmentVariables environmentVariables = null;
        for (String key : variables.keySet()) {
            final var value = variables.get(key);
            environmentVariables = index == 0 ? withEnvironmentVariable(key, value)
                : environmentVariables.and(key, value);
            index++;
        }
        return environmentVariables;
    }

}