package com.walmart.common.domain;

public class AssertionConcern {

  protected AssertionConcern() {
    super();
  }

  protected void assertArgumentLength(String value, int minLength, int maxLength, String message) {
    int length = value.trim().length();
    if (length < minLength || length > maxLength) {
      throw new IllegalArgumentException(message);
    }
  }

  protected void assertArgumentNotEmpty(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
  }

  protected void assertArgumentNotNull(Object anObject, String message) {
    if (anObject == null) {
      throw new IllegalArgumentException(message);
    }
  }

  protected void assertArgumentTrue(boolean flag, String message) {
    if (!flag) {
      throw new IllegalArgumentException(message);
    }
  }
}
