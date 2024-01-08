package uk.gov.companieshouse.itemgroupstatusupdater.util;

import uk.gov.companieshouse.itemgroupprocessed.ItemGroupProcessed;

public class TestConstants {

    private TestConstants() {}

    public static final String ORDER_NUMBER = "ORD-844016-962315";
    public static final String ITEM_ID = "CCD-289716-962308";
    public static final String STATUS = "satisfied";
    public static final String DIGITAL_DOCUMENT_LOCATION =
        "s3://document-api-images-cidev/docs/--EdB7fbldt5oujK6Nz7jZ3hGj_x6vW8Q_2gQTyjWBM/application-pdf";
    public static final String PATCH_ORDERED_ITEM_URI =
        "/orders/ORD-844016-962315/items/CCD-289716-962308";

    public static final ItemGroupProcessed ITEM_GROUP_PROCESSED = ItemGroupProcessed.newBuilder()
        .setGroupItem("/item-groups/IG-123456-123456/items/CCD-289716-962308")
        .setItem(createItem())
        .setOrderNumber(ORDER_NUMBER)
        .build();

    private static uk.gov.companieshouse.itemgroupprocessed.Item createItem() {
        return new uk.gov.companieshouse.itemgroupprocessed.Item(
            ITEM_ID,
            STATUS,
            DIGITAL_DOCUMENT_LOCATION
        );
    }

}
