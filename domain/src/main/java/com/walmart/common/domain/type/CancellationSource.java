package com.walmart.common.domain.type;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;

@Getter
public enum CancellationSource {
  STORE("STORE"),
  VENDOR("VENDOR"),
  OMS("OMS");

  private final String sourceName;

  CancellationSource(String name) {
    this.sourceName = name;
  }

  private static final Map<String, CancellationSource> cancellationSourceCache =
      Arrays.stream(CancellationSource.values())
          .collect(toMap(CancellationSource::getSourceName, Function.identity()));

  public static CancellationSource get(String sourceName) {
    return cancellationSourceCache.getOrDefault(sourceName, STORE);
  }
}
