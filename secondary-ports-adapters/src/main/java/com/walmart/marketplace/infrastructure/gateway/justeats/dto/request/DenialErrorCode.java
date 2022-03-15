package com.walmart.marketplace.infrastructure.gateway.justeats.dto.request;

import lombok.Getter;

@Getter
public enum DenialErrorCode {
  INACTIVE("the POS is offline and cannot take orders."),
  INCORRECT_SETUP("the configuration details sent with the order are wrong (e.g. store ID)"),
  IN_USE("the POS is currently in use and cannot take requests."),
  TIMEOUT("the request to the POS timed out."),
  NOT_SUPPORTED("integrated ordering is not supported at this restaurant."),
  MENU_ERROR("the order had incorrect items (not in stock, PLU not in POS)."),
  MALFORMED_REQUEST("the order request was malformed (e.g. malformed JSON)."),
  AUTH_FAILED("the order authorization was incorrect."),
  STORE_CLOSED("the store is closed and cannot take orders."),
  TENDER_ERROR("the tender_type sent to the POS is wrong.");

  private final String errorMessage;

  DenialErrorCode(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
