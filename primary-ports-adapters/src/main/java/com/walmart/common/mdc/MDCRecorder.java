package com.walmart.common.mdc;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/** This is a Common class component that used to record MDC. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class MDCRecorder {

  /**
   * @param api {@code name of the API}
   * @param domain {name of the domain}
   */
  public static void initMDC(String api, String domain) {
    MDC.put("api", api);
    MDC.put("domain", domain);
  }

  public static void clear() {
    MDC.clear();
  }
}
