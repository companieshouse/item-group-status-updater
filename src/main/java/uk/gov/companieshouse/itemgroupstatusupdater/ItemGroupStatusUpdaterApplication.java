package uk.gov.companieshouse.itemgroupstatusupdater;

import static uk.gov.companieshouse.itemgroupstatusupdater.environment.EnvironmentVariablesChecker.allRequiredEnvironmentVariablesPresent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ItemGroupStatusUpdaterApplication {

	public static void main(String[] args) {
		if (allRequiredEnvironmentVariablesPresent()) {
			SpringApplication.run(ItemGroupStatusUpdaterApplication.class, args);
		}
	}

}
