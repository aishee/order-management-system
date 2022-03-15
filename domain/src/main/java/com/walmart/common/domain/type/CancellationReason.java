package com.walmart.common.domain.type;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;

@Getter
public enum CancellationReason {
  IRO_FAILURE("IRO Failure", "Item enrichment failure"),
  DEFAULT("NOT MENTIONED", "Not Mentioned"),
  CHANGED_MIND("1", "Changed Mind"),
  LOST_IN_STORE("2", "Lost in store"),
  EXPIRED("3", "Expired"),
  INCORRECT_ITEM("4", "Incorrect Item"),
  MISSING_ITEM("5", "Missing Item");

  private final String code;
  private final String description;


  CancellationReason(String code, String description) {
    this.code = code;
    this.description = description;
  }

  private static final Map<String, CancellationReason> cancellationReasonMap =
      Arrays.stream(CancellationReason.values())
          .collect(toMap(CancellationReason::getCode, Function.identity()));

  public static CancellationReason get(String code) {
    return cancellationReasonMap.getOrDefault(code, DEFAULT);
  }
}

