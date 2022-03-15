package com.walmart.fms.domain.error.exception;

import static java.util.Collections.singletonList;

import com.walmart.fms.domain.error.Error;
import com.walmart.fms.domain.error.ErrorResponse;
import com.walmart.fms.domain.error.ErrorType;
import lombok.Getter;

@Getter
public abstract class FMSGenericException extends RuntimeException {
  private final ErrorResponse errorResponse;

  protected FMSGenericException(String message, int errorCode, ErrorType errorType) {
    super(message);
    this.errorResponse = new ErrorResponse(singletonList(new Error(errorCode, errorType, message)));
  }

  public ErrorType getErrorType() {
    return errorResponse.getErrorType();
  }
}
