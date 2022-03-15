package com.walmart.oms.domain.error.exception;

import com.walmart.oms.domain.error.ErrorType;
import lombok.Getter;

@Getter
public class OMSBadRequestException extends OMSGenericException {

  public OMSBadRequestException(String message) {
    super(message, 400, ErrorType.INVALID_REQUEST_EXCEPTION);
  }

  public OMSBadRequestException(String message, Throwable cause) {
    super(message, 400, ErrorType.INVALID_REQUEST_EXCEPTION, cause);
  }
}
