package uk.gov.companieshouse.itemgroupstatusupdater.environment;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.API_URL;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.CHS_API_KEY;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.DOCUMENT_API_LOCAL_URL;
import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.RequiredEnvironmentVariables.PAYMENTS_API_URL;

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

    @DisplayName("returns false if CHS_API_KEY is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfChsApiKeyMissing() throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(CHS_API_KEY);
    }

    @DisplayName("returns false if DOCUMENT_API_LOCAL_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfDocumentApiLocalUrlMissing()
        throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(DOCUMENT_API_LOCAL_URL);
    }

    @DisplayName("returns false if PAYMENTS_API_URL is missing")
    @Test
    void checkEnvironmentVariablesAllPresentReturnsFalseIfPaymentsApiUrlMissing() throws Exception {
        populateAllVariablesExceptOneAndAssertSomethingMissing(PAYMENTS_API_URL);
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