package com.walmart.oms.domain.error;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
@JsonInclude(NON_NULL)
public class ErrorDetail implements Serializable {
  private final String field;
  private final String issue;
}
