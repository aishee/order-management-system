package com.walmart.oms.domain.error.exception;

import static java.util.Collections.singletonList;

import com.walmart.oms.domain.error.Error;
import com.walmart.oms.domain.error.ErrorResponse;
import com.walmart.oms.domain.error.ErrorType;
import lombok.Getter;

@Getter
public abstract class OMSGenericException extends RuntimeException {

  private final ErrorResponse errorResponse;

  protected OMSGenericException(String message, int errorCode, ErrorType errorType) {
    super(message);
    this.errorResponse = new ErrorResponse(singletonList(new Error(errorCode, errorType, message)));
  }

  protected OMSGenericException(
      String message, int errorCode, ErrorType errorType, Throwable cause) {
    super(message, cause);
    this.errorResponse = new ErrorResponse(singletonList(new Error(errorCode, errorType, message)));
  }

  public ErrorType getErrorType() {
    return errorResponse.getErrorType();
  }
}
