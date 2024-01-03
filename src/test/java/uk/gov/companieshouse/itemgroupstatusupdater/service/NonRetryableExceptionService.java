package uk.gov.companieshouse.itemgroupstatusupdater.service;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.itemgroupstatusupdater.exception.NonRetryableException;

@Component
public class NonRetryableExceptionService implements Service {

    @Override
    public void processMessage(ServiceParameters parameters) {
        throw new NonRetryableException("Unable to handle message",
            new Exception("Unable to handle message"));
    }
}
