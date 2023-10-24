package org.example.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequestDto(String mobileNumber, String code, String organizationId, String organizationType, String inn,
                         String managerMobileNumber) {

    public RequestDto(String organizationId, String organizationType, String inn, String managerMobileNumber) {
        this(null, null, organizationId, organizationType, inn, managerMobileNumber);
    }

    public RequestDto(String mobileNumber, String code) {
        this(mobileNumber, code, null, null, null, null);
    }
}