package uk.gov.companieshouse.itemgroupstatusupdater;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ItemGroupStatusUpdaterApplication {

	public static final String NAMESPACE = "item-group-status-updater";

	public static void main(String[] args) {
		SpringApplication.run(ItemGroupStatusUpdaterApplication.class, args);
	}

}
