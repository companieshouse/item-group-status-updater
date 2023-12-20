package uk.gov.companieshouse.itemgroupstatusupdater.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class ApplicationConfiguration {

    public static final String APPLICATION_NAMESPACE = "item-group-status-updater";

    @Bean
    Logger getLogger(){
        return LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    }

}
