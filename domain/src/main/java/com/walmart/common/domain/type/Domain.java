package com.walmart.common.domain.type;

import lombok.Getter;

@Getter
public enum Domain {
  OMS("OMS"),
  MARKETPLACE("MARKETPLACE"),
  COMMON("COMMON"),
  FMS("FMS");

  private final String domainName;

  Domain(String domainName) {
    this.domainName = domainName;
  }

}
