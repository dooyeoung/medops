package com.medops.adapter.in.web.request;

public record UpdateReservationNoteRequest(String userId, String hospitalId, String note) {
}
