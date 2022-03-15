package com.walmart.common.domain.type;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;

@Getter
public enum Tenant {
  ASDA("ASDA"),
  DEFAULT("DEFAULT");

  private static final Map<String, Tenant> tenantCache =
      Arrays.stream(Tenant.values()).collect(toMap(Tenant::getTenantId, Function.identity()));

  private final String tenantId;

  Tenant(String tenantId) {
    this.tenantId = tenantId;
  }

  public static Tenant get(String tenantId) {
    return tenantCache.getOrDefault(tenantId, DEFAULT);
  }
}
