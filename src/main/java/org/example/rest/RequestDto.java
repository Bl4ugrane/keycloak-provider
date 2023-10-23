package org.example.rest;

public record RequestDto(String mobileNumber, String organizationId, String organizationType, String inn, String number) {
}