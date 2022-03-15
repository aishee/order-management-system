package com.walmart.common.metrics;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MetricConstants {
  public static final String TYPE = "type";
  public static final String STATUS = "status";
  public static final String IS_SUCCESS = "isSuccess";
  public static final String EXCEPTION_TYPE = "exceptionType";
  public static final String METHOD_NAME = "methodName";
  public static final String IS_REPOSITORY = "isRepository";
  public static final String API = "api";

  @Getter
  public enum MetricCounters {
    UBER_INVOCATION("uber_invocation"),
    JUST_EATS_INVOCATION("just_eats_invocation"),
    SECONDARY_PORT_INVOCATION("secondary_port_invocation"),
    PRIMARY_PORT_INVOCATION("primary_port_invocation"),
    BUSINESS_REPORT_INVOCATION("business_report_invocation"),
    BUSINESS_REPORT_EXCEPTION("business_report_exception"),
    UBER_EXCEPTION("uber_exception"),
    JUST_EATS_EXCEPTION("just_eats_exception"),
    PYSIPYP_EXCEPTION("pysipyp_exception"),
    TAX_EXCEPTION("tax_exception"),
    IRO_EXCEPTION("iro_exception"),
    PRIMARY_PORT_EXCEPTION_COUNTER("primary_port_exception_counter"),
    OMS_PURGE_EXCEPTION_COUNTER("oms_purge_exception_counter"),
    OMS_ORDER_REPLAY_EXCEPTION_COUNTER("oms_order_replay_exception_counter"),
    OMS_ORDER_REPLAY_INVOCATION_COUNTER("oms_order_replay_invocation_counter"),
    UBER_WEB_HOOK_EXCEPTION("uber_web_hook_exception"),
    JUST_EAT_WEB_HOOK_EXCEPTION("just_eat_web_hook_exception"),
    WEB_HOOK_COUNTER("web_hook_counter");

    private final String counter;

    MetricCounters(String counter) {
      this.counter = counter;
    }
  }

  @Getter
  public enum MetricTypes {
    UBER_ACCEPT_ORDER("uber_accept_order"),
    UBER_CANCEL_ORDER("uber_cancel_order"),
    UBER_GET_REPORT("uber_get_report"),
    UBER_GET_STORES("uber_get_stores"),
    UBER_UPDATE_ITEM("uber_update_item"),
    UBER_PATCH_CART("uber_patch_cart"),
    UBER_DENY_ORDER("uber_deny_order"),
    UBER_GET_ORDER("uber_get_order"),
    JUST_EATS_ACCEPT_ORDER("just_eats_accept_order"),
    JUST_EATS_DENY_ORDER("just_eats_deny_order"),
    JUST_EATS_UPDATE_ITEM("just_eats_update_item"),
    IRO_GET_ITEM("iro_get_item"),
    PYSIPYP_RECORD_SALE("pysipyp_record_sale"),
    PYSIPYP_REVERSE_SALE("pysipyp_reverse_sale"),
    TAX_CALCULATION("tax_calculation"),
    BUSINESS_REPORT("business_report"),
    OMS_PURGE("oms_purge"),
    OMS_ORDER_REPLAY("oms_order_replay"),
    UBER_ORDER_CREATE("uber_order_create"),
    UBER_ORDER_CANCEL("uber_order_cancel"),
    UBER_ORDER_REPORT("uber_order_report"),
    JUST_EAT_ORDER_CREATE("just_eat_order_create");

    private final String type;

    MetricTypes(String type) {
      this.type = type;
    }
  }
}
