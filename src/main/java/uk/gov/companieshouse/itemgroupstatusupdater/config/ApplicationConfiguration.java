package uk.gov.companieshouse.itemgroupstatusupdater.config;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.itemgroupstatusupdater.service.PatchOrderedItemService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
public class ApplicationConfiguration {

    public static final String APPLICATION_NAMESPACE = "item-group-status-updater";

    // TODO DCAC-216: Remove this with RestTemplate?
    @Bean
    RestTemplate restTemplate(RestTemplateBuilder builder) {
        final var httpClient = HttpClients.custom().disableRedirectHandling().build();
        final var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return builder.additionalMessageConverters(getJsonMessageConverters())
            .requestFactory(() -> requestFactory)
            .build();
    }

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

    private List<HttpMessageConverter<?>> getJsonMessageConverters() {
        final List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new MappingJackson2HttpMessageConverter());
        return converters;
    }




}
