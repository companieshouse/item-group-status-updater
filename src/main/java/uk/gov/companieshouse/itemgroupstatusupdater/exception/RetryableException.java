package uk.gov.companieshouse.itemgroupstatusupdater.exception;

/**
 * A recoverable exception has occurred e.g. due to a service that is temporarily unavailable.
 */
public class RetryableException extends RuntimeException {

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}
