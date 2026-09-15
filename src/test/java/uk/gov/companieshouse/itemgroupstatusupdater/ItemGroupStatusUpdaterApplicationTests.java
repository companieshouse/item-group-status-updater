package uk.gov.companieshouse.itemgroupstatusupdater;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.itemgroupstatusupdater.config.TestConfig;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test_main_positive")
class ItemGroupStatusUpdaterApplicationTests {

	@Autowired
	private Environment environment;

	@Test
	void contextLoads() {
		assertThat(environment.getProperty("management.opentelemetry.enabled"))
				.isEqualTo("false");
		assertThat(environment.getProperty(
				"management.opentelemetry.logging.export.otlp.endpoint"))
				.isEqualTo("http://localhost:4318/v1/logs");
		assertThat(environment.getProperty(
				"management.opentelemetry.tracing.export.otlp.endpoint"))
				.isEqualTo("http://localhost:4318/v1/traces");
		assertThat(environment.getProperty(
				"management.opentelemetry.metrics.export.otlp.endpoint"))
				.isEqualTo("http://localhost:4318/v1/metrics");
	}

}
