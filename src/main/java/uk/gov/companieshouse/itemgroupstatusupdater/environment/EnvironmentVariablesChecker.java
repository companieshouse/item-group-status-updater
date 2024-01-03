package uk.gov.companieshouse.itemgroupstatusupdater.environment;

import static uk.gov.companieshouse.itemgroupstatusupdater.ItemGroupStatusUpdaterApplication.NAMESPACE;

import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.exception.EnvironmentVariableException;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class EnvironmentVariablesChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public enum RequiredEnvironmentVariables {
        API_URL("API_URL"),
        BACKOFF_DELAY("BACKOFF_DELAY"),
        BOOTSTRAP_SERVER_URL("BOOTSTRAP_SERVER_URL"),
        CHS_API_KEY("CHS_API_KEY"),
        CONCURRENT_LISTENER_INSTANCES("CONCURRENT_LISTENER_INSTANCES"),
        DOCUMENT_API_LOCAL_URL("DOCUMENT_API_LOCAL_URL"),
        GROUP_ID("GROUP_ID"),
        INVALID_MESSAGE_TOPIC("INVALID_MESSAGE_TOPIC"),
        MAX_ATTEMPTS("MAX_ATTEMPTS"),
        PAYMENTS_API_URL("PAYMENTS_API_URL"),
        SERVER_PORT("SERVER_PORT"),
        TOPIC("TOPIC");

        private final String name;

        RequiredEnvironmentVariables(String name) { this.name = name; }

        public String getName() { return this.name; }
    }

    /**
     * Method to check if all of the required configuration variables
     * defined in the RequiredEnvironmentVariables enum have been set to a value
     * @return <code>true</code> if all required environment variables have been set, <code>false</code> otherwise
     */
    public static boolean allRequiredEnvironmentVariablesPresent() {
        EnvironmentReader environmentReader = new EnvironmentReaderImpl();
        var allVariablesPresent = true;
        LOGGER.info("Checking all environment variables present");
        for(RequiredEnvironmentVariables param : RequiredEnvironmentVariables.values()) {
            try{
                environmentReader.getMandatoryString(param.getName());
            } catch (EnvironmentVariableException eve) {
                allVariablesPresent = false;
                LOGGER.error(String.format("Required config item %s missing", param.getName()));
            }
        }

        return allVariablesPresent;
    }
}
