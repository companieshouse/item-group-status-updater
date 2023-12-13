package uk.gov.companieshouse.itemgroupstatusupdater.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// TODO DCAC-216: Remove this temporary test code.
public class ItemStatusUpdateDto {

    private final String status;

    @JsonProperty("digital_document_location")
    private final String digitalDocumentLocation;

    public ItemStatusUpdateDto(String status, String digitalDocumentLocation) {
        this.status = status;
        this.digitalDocumentLocation = digitalDocumentLocation;
    }

    public String getStatus() {
        return status;
    }

    public String getDigitalDocumentLocation() {
        return digitalDocumentLocation;
    }
}
