package uk.gov.companieshouse.itemgroupstatusupdater.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.itemgroupstatusupdater.service.PatchOrderedItemService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class ApplicationConfiguration {

    public static final String APPLICATION_NAMESPACE = "item-group-status-updater";

    @Bean
    Logger getLogger(){
        return LoggerFactory.getLogger(APPLICATION_NAMESPACE);
    }

    // TODO DCAC-216: Remove this temporary test code.
    @Bean
    CommandLineRunner testServiceOnStartUp(final PatchOrderedItemService service) {
        return args -> service.patchOrderedItem(
            "ORD-844016-962315",
            "CCD-289716-962308",
            "satisfied",
            "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf");
    }

}
