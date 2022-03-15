package com.walmart.common.domain.type;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;

@Getter
public enum Vertical {
  MARKETPLACE("MARKETPLACE", VerticalType.MARKETPLACE),
  ASDAGR("ASDAGR", VerticalType.GR),
  DEFAULTGM("DefaultGM", VerticalType.GM),
  DEFAULTGR("DefaultGR", VerticalType.GR),
  DEFAULTMARKETPLACE("DefaultMarketPlace", VerticalType.MARKETPLACE);

  public enum VerticalType {
    GM,
    GR,
    MARKETPLACE
  }

  private final String verticalId;
  private final VerticalType verticalType;

  Vertical(String verticalId, VerticalType verticalType) {
    this.verticalId = verticalId;
    this.verticalType = verticalType;
  }

  protected static final Map<String, Vertical> verticalCache =
      Arrays.stream(Vertical.values()).collect(toMap(Vertical::getVerticalId, Function.identity()));

  public static Vertical get(String verticalId) {
    return verticalCache.get(verticalId);
  }

  public VerticalType getVerticalType() {
    return verticalType;
  }
}
