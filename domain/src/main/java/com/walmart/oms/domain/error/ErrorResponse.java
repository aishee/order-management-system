package com.walmart.oms.domain.error;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class ErrorResponse implements Serializable {
  private final List<Error> errors;

  public ErrorType getErrorType() {
    return Optional.ofNullable(getErrors())
        .flatMap(errorList -> errorList.stream().findFirst())
        .map(Error::getType)
        .orElse(null);
  }
}
