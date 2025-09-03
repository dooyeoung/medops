package com.medops.adapter.in.web.request;

public record CancelReservationRequest(String userId, String hospitalId, String adminId) {
}
