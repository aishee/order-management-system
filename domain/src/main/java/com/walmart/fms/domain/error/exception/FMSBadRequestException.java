package com.walmart.fms.domain.error.exception;

import com.walmart.fms.domain.error.ErrorType;
import lombok.Getter;

@Getter
public class FMSBadRequestException extends FMSGenericException {

  public FMSBadRequestException(String message) {
    super(message, 400, ErrorType.INVALID_REQUEST_EXCEPTION);
  }
}
