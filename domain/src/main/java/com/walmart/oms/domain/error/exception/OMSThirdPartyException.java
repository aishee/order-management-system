package com.walmart.oms.domain.error.exception;

import com.walmart.oms.domain.error.ErrorType;
import lombok.Getter;

@Getter
public class OMSThirdPartyException extends OMSGenericException {

  public OMSThirdPartyException(String message) {
    super(message, 500, ErrorType.INTERNAL_SERVICE_EXCEPTION);
  }

  public OMSThirdPartyException(String message, Throwable cause) {
    super(message, 500, ErrorType.INTERNAL_SERVICE_EXCEPTION, cause);
  }
}
