package com.walmart.oms.order.domain;

import com.walmart.common.config.POSConfig;
import com.walmart.common.constants.OmsConstants;
import com.walmart.common.domain.type.Currency;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.domain.entity.PickedItem;
import com.walmart.oms.order.domain.entity.PickedItemUpc;
import com.walmart.oms.order.factory.OmsOrderFactory;
import com.walmart.oms.order.gateway.IPricingGateway;
import com.walmart.oms.order.gateway.ITaxGateway;
import com.walmart.oms.order.repository.IOmsOrderRepository;
import com.walmart.oms.order.valueobject.Money;
import com.walmart.oms.order.valueobject.PricingResponse;
import com.walmart.tax.calculator.dto.Tax;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OmsOrderPickCompleteDomainService {

  @Autowired private ITaxGateway taxGateway;

  @Autowired private OmsOrderFactory omsOrderFactory;

  @Autowired private IPricingGateway pricingGateway;

  @Autowired private IOmsOrderRepository omsOrderRepository;

  @ManagedConfiguration private POSConfig posConfig;

  /**
   * Performing pricing on the order after pick complete. This includes adding carrier bad item,
   * calling tax service, PYSIPYP service and marking EPOS_COMPLETE.
   *
   * @param omsOrder Oms Order entity.
   */
  public void performPricingOnTheOrder(OmsOrder omsOrder) {

    addCarrierBagItem(omsOrder);
    Map<String, Tax> taxInfoMap =
        taxGateway.fetchTaxData(omsOrder.getOrderItemList(), omsOrder.getSourceOrderId());

    Optional<PricingResponse> pricingResponse = pricingGateway.priceOrder(omsOrder, taxInfoMap);

    pricingResponse.ifPresent(
        pricing -> {
          omsOrder.enrichPickedItemsAfterPricing(omsOrder, pricing);
          omsOrder.completePricing();
          omsOrderRepository.save(omsOrder);
        });
  }

  // For Carrier Bag Charge Inclusion
  private void addCarrierBagItem(OmsOrder omsOrder) {
    if (omsOrder.hasCarrierBag()) {
      log.info("Adding Carrier Bag to ordered item list");
      includeCarrierBagItem(omsOrder);
    }
  }

  private void includeCarrierBagItem(OmsOrder omsOrder) {
    Money carrierBagCharge = getCarrierBagCharge(omsOrder);
    OmsOrderItem omsOrderItem = createOrderedItem(omsOrder, carrierBagCharge);

    omsOrderItem.getItemPriceInfo().withUnitPriceFromCatalog(carrierBagCharge);
    PickedItem pickedItem = getCarrierBagPickedItem(omsOrder);

    omsOrderItem.enrichPickedInfoWithPickedItem(pickedItem);
    pickedItem.updateOrderItem(omsOrderItem);

    omsOrder.addItem(omsOrderItem);
  }

  private OmsOrderItem createOrderedItem(OmsOrder omsOrder, Money carrierBagCharge) {
    return omsOrderFactory.createOrderedItem(
        omsOrder,
        OmsConstants.CARRIER_BAG_CIN,
        OmsConstants.CARRIER_BAG_ITEM_DESCRIPTION,
        OmsConstants.DEFAULT_CARRIER_BAG_QUANTITY,
        carrierBagCharge,
        carrierBagCharge,
        SubstitutionOption.DO_NOT_SUBSTITUTE);
  }

  private Money getCarrierBagCharge(OmsOrder omsOrder) {
    return new Money(
        BigDecimal.valueOf(omsOrder.getPriceInfo().getCarrierBagCharge()), Currency.GBP);
  }

  private PickedItem getCarrierBagPickedItem(OmsOrder omsOrder) {
    PickedItemUpc pickedUpc = createPickedItemUpc(omsOrder);
    List<PickedItemUpc> pickedItemUpcList = Collections.singletonList(pickedUpc);
    return createPickedItem(pickedItemUpcList);
  }

  private PickedItem createPickedItem(List<PickedItemUpc> pickedItemUpcList) {
    return omsOrderFactory.createPickedItem(
        OmsConstants.CARRIER_BAG_ITEM_DESCRIPTION,
        posConfig.getCarrierBagDepartmentID(),
        OmsConstants.CARRIER_BAG_CIN,
        OmsConstants.PICKED_BY,
        pickedItemUpcList);
  }

  private PickedItemUpc createPickedItemUpc(OmsOrder omsOrder) {
    return omsOrderFactory.createPickedItemUpc(
        OmsConstants.DEFAULT_CARRIER_BAG_QUANTITY,
        BigDecimal.valueOf(omsOrder.getPriceInfo().getCarrierBagCharge()),
        OmsConstants.UOM_VALUE_E,
        posConfig.getCarrierBagWin(),
        posConfig.getCarrierBagUpc());
  }
}
