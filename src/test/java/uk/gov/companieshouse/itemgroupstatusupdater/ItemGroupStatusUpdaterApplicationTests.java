package uk.gov.companieshouse.itemgroupstatusupdater;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test_main_positive")
class ItemGroupStatusUpdaterApplicationTests {

	@SuppressWarnings("squid:S2699") // at least one assertion
	@Test
	void contextLoads() {
	}

}
