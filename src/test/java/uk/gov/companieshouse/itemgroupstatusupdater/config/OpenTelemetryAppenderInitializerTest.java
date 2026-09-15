package uk.gov.companieshouse.itemgroupstatusupdater.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class OpenTelemetryAppenderInitializerTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(OpenTelemetry.class, OpenTelemetry::noop)
            .withUserConfiguration(OpenTelemetryAppenderInitializer.class);

    @Test
    void initializerIsDisabledByDefault() {
        contextRunner.run(context -> assertThat(context)
                .doesNotHaveBean(OpenTelemetryAppenderInitializer.class));
    }

    @Test
    void initializerIsEnabledByProperty() {
        contextRunner
                .withPropertyValues("management.opentelemetry.enabled=true")
                .run(context -> assertThat(context)
                        .hasSingleBean(OpenTelemetryAppenderInitializer.class));
    }
}