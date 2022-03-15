package com.walmart.oms.infrastructure.gateway.tax;

import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.gateway.ITaxGateway;
import com.walmart.tax.calculator.dto.CalculateTaxResponse;
import com.walmart.tax.calculator.dto.Item;
import com.walmart.tax.calculator.dto.Tax;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Slf4j
public class CalculatorTaxGateway implements ITaxGateway {

  @Autowired private CalculateTaxHttpWebClient calculateTaxHttpWebClient;

  @Override
  public Map<String, Tax> fetchTaxData(List<OmsOrderItem> orderedItemList, String sourceOrderId) {

    if (CollectionUtils.isEmpty(orderedItemList)) {
      return Collections.emptyMap();
    }

    try {
      CalculateTaxResponse calculateTaxResponse =
          calculateTaxHttpWebClient.executeTaxCall(orderedItemList, sourceOrderId);
      return populateTaxInfoMap(calculateTaxResponse);
    } catch (Exception ex) {
      log.error("Exception at CalculatorTaxGateway: ", ex);
      throw new OMSThirdPartyException("Error in Tax Gateway", ex);
    }
  }

  private Map<String, Tax> populateTaxInfoMap(CalculateTaxResponse calculateTaxResponse) {

    return Optional.ofNullable(calculateTaxResponse)
        .filter(this::isValidResponse)
        .map(
            taxResponse -> {
              // As part of Tax service there will be only one orderLine, so getting all the items
              // from that orderLine
              return taxResponse.getOrder().getOrderLines().stream()
                  .findFirst()
                  .map(orderLine -> orderLine.getItems().stream())
                  .orElseGet(Stream::empty)
                  .filter(item -> !CollectionUtils.isEmpty(item.getTaxes()))
                  .collect(Collectors.toMap(Item::getGtin, item -> item.getTaxes().get(0)));
            })
        .orElse(Collections.emptyMap());
  }

  private boolean isValidResponse(CalculateTaxResponse calculateTaxResponse) {
    return null != calculateTaxResponse
        && null != calculateTaxResponse.getOrder()
        && !CollectionUtils.isEmpty(calculateTaxResponse.getOrder().getOrderLines())
        && !CollectionUtils.isEmpty(
            calculateTaxResponse.getOrder().getOrderLines().get(0).getItems());
  }
}
