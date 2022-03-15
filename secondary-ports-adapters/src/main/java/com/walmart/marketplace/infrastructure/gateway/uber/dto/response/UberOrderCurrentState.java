package com.walmart.marketplace.infrastructure.gateway.uber.dto.response;

/** Enum has values for currentState values of an UberOrder */
public enum UberOrderCurrentState {
  CREATED,
  ACCEPTED,
  DENIED,
  FINISHED,
  CANCELLED
}
