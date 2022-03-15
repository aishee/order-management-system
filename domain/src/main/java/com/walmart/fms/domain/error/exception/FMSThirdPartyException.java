package com.walmart.fms.domain.error.exception;

import com.walmart.fms.domain.error.ErrorType;
import lombok.Getter;

@Getter
public class FMSThirdPartyException extends FMSGenericException {

  public FMSThirdPartyException(String message) {
    super(message, 500, ErrorType.INTERNAL_SERVICE_EXCEPTION);
  }
}
