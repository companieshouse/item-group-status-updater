package uk.gov.companieshouse.itemgroupstatusupdater;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.companieshouse.itemgroupstatusupdater.config.TestConfig;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test_main_positive")
class ItemGroupStatusUpdaterApplicationTests {

	@SuppressWarnings("squid:S2699") // at least one assertion
	@Test
	void contextLoads() {
	}

}
