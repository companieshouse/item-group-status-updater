package uk.gov.companieshouse.itemgroupstatusupdater.service;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.sdk.manager.ApiSdkManager;

@Service
public class ApiClientService {

    public InternalApiClient getInternalApiClient() {
        return ApiSdkManager.getPrivateSDK();
    }

}
