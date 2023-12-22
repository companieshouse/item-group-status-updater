package uk.gov.companieshouse.itemgroupstatusupdater.logging;

import static java.util.Collections.singletonList;

import java.util.Map;
import uk.gov.companieshouse.logging.util.DataMap;

public class LoggingUtils {

    private LoggingUtils() {}

    public static Map<String, Object> getLogMap(
        final String orderNumber,
        final String itemId,
        final String status,
        final String digitalDocumentLocation) {
        return new DataMap.Builder()
            .orderId(orderNumber)
            .itemId(itemId)
            .status(status)
            .digitalDocumentLocation(digitalDocumentLocation)
            .build()
            .getLogMap();
    }

    public static Map<String, Object> getLogMap(
        final String orderNumber,
        final String itemId,
        final String status,
        final String digitalDocumentLocation,
        final String error) {
        return new DataMap.Builder()
            .orderId(orderNumber)
            .itemId(itemId)
            .status(status)
            .digitalDocumentLocation(digitalDocumentLocation)
            .errors(singletonList(error))
            .build()
            .getLogMap();
    }
}
