package com.walmart.common.domain.type;

public enum SubstitutionOption {
  DO_NOT_SUBSTITUTE,
  CANCEL_ENTIRE_ORDER,
  SUBSTITUTE;

  public boolean isSubstitutionOptionCancelOrder() {
    return this == CANCEL_ENTIRE_ORDER;
  }
}
