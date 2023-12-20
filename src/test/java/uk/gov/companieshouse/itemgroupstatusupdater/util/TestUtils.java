package uk.gov.companieshouse.itemgroupstatusupdater.util;

import static uk.gov.companieshouse.itemgroupstatusupdater.util.TestConstants.PATCH_ORDERED_ITEM_URI;

public class TestUtils {

    private TestUtils() {}

    public static String getExpectedReason(int statusCode, String reasonPhrase) {
        return "Received unexpected response status code " + statusCode
            + ", and status message '" + reasonPhrase + "' sending request to "
            + "patch ordered item at " + PATCH_ORDERED_ITEM_URI + ".";
    }
}
