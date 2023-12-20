package uk.gov.companieshouse.itemgroupstatusupdater;

import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;

public class Constants {

    private Constants() {
    }

    public static final ItemGroupProcessed ITEM_GROUP_PROCESSED = ItemGroupProcessed.newBuilder()
            .setGroupItem("/item-groups/IG-123456-123456/items/111-222-333")
            .setItem(createItem())
            .setOrderNumber("ORD-123456-123456")
            .build();

    private static uk.gov.companieshouse.itemgroupprocessed.Item createItem() {
        return new uk.gov.companieshouse.itemgroupprocessed.Item(
                "10371283",
                "processing",
                "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf"
        );
    }

}