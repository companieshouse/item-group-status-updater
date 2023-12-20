package uk.gov.companieshouse.itemgroupstatusupdater;

import org.springframework.stereotype.Component;

@Component
public class NonRetryableExceptionService implements Service {

    @Override
    public void processMessage(ServiceParameters parameters) {
        throw new NonRetryableException("Unable to handle message");
    }
}
