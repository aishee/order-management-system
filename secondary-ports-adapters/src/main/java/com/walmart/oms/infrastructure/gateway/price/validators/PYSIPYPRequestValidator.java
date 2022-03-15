package com.walmart.oms.infrastructure.gateway.price.validators;

import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.tax.calculator.dto.Tax;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class PYSIPYPRequestValidator {

  public boolean isValidReverseSaleRequest(
      OrderCancelledDomainEventMessage orderCancelledDomainEventMessage) {
    String errorMessage = null;
    if (ObjectUtils.isEmpty(orderCancelledDomainEventMessage)) {
      errorMessage = "Couldn't perform PYSIPYP reverseSale as empty request received.";
    }

    if (!ObjectUtils.isEmpty(errorMessage)) {
      log.error(errorMessage);
      return false;
    }
    return true;
  }

  private boolean isValidOrder(OmsOrder omsOrder) {
    return !StringUtils.isEmpty(omsOrder.getStoreOrderId())
        && !StringUtils.isEmpty(omsOrder.getStoreId());
  }

  public boolean isValidRecordSaleRequest(OmsOrder order, Map<String, Tax> taxInfoMap) {
    String errorMessage = null;
    if (ObjectUtils.isEmpty(order)) {
      errorMessage = "OmsOrder recieved empty for PYSISPYP record sale call";
    } else if (!isValidOrder(order)) {
      errorMessage =
          String.format(
              "OmsOrder does not contain storeOrderId or StoreId fields to place reverse sale request for vendorOrderId: %s",
              order.getVendorOrderId());
    } else if (!order.isMarketPlaceOrder()) {
      errorMessage =
          "OmsOrder should be from marketplace Vertical for vendorOrderId: "
              + order.getVendorOrderId();
    } else if (CollectionUtils.isEmpty(taxInfoMap)) {
      errorMessage =
          "TaxInformation should be non empty for vendorOrderId : " + order.getVendorOrderId();
    }
    if (!ObjectUtils.isEmpty(errorMessage)) {
      errorMessage = "RecordSale request Validation Error : " + errorMessage;
      log.error(errorMessage);
      return false;
    }
    return true;
  }
}
