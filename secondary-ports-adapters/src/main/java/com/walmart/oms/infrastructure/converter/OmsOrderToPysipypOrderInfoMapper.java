package com.walmart.oms.infrastructure.converter;

import com.walmart.common.utils.NumberUtils;
import com.walmart.oms.domain.event.messages.OrderCancelledDomainEventMessage;
import com.walmart.oms.infrastructure.constants.PYSIPYPConstants;
import com.walmart.oms.infrastructure.gateway.price.PYSIPYPServiceConfiguration;
import com.walmart.oms.infrastructure.gateway.price.dto.DetailLine;
import com.walmart.oms.infrastructure.gateway.price.dto.MarketPlace;
import com.walmart.oms.infrastructure.gateway.price.dto.OrderInformation;
import com.walmart.oms.infrastructure.gateway.price.dto.OrderedItem;
import com.walmart.oms.infrastructure.gateway.price.dto.PYSIPYPHeader;
import com.walmart.oms.infrastructure.gateway.price.dto.PriceInfo;
import com.walmart.oms.infrastructure.gateway.price.dto.UPCDetail;
import com.walmart.oms.order.aggregateroot.OmsOrder;
import com.walmart.oms.order.domain.entity.OmsOrderItem;
import com.walmart.oms.order.domain.entity.PickedItem;
import com.walmart.oms.order.domain.entity.SubstitutedItem;
import com.walmart.tax.calculator.dto.Tax;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OmsOrderToPysipypOrderInfoMapper {

  @ManagedConfiguration private PYSIPYPServiceConfiguration pysipypServiceConfiguration;

  public OrderInformation buildPysipypRequestforMarketPlaceOrder(
      OmsOrder omsOrder, Map<String, Tax> taxInfoMap) {

    return OrderInformation.builder()
        .header(getHeaderForRecordSale(omsOrder))
        .orderedItems(getOrderItems(omsOrder.getOrderItemList()))
        .marketPlace(getMarketPlace(omsOrder))
        .consumerId(PYSIPYPConstants.GIF)
        .cardTypeUsed(PYSIPYPConstants.CREDIT_CARD)
        .colleagueDiscount(PYSIPYPConstants.NOT_APPLICABLE)
        .minBasketChargeAppliedOnPricing(false)
        .detailLine(getDetailLines(omsOrder, taxInfoMap))
        .build();
  }

  private List<DetailLine> getDetailLines(OmsOrder omsOrder, Map<String, Tax> taxInfoMap) {
    return omsOrder.getOrderItemList().stream()
        .filter(omsOrderItem -> !omsOrderItem.isZeroPricedNilPick())
        .map(omsOrderItem -> getDetailLine(omsOrderItem, taxInfoMap))
        .collect(Collectors.toList());
  }

  private DetailLine getDetailLine(OmsOrderItem omsOrderItem, Map<String, Tax> taxInfoMap) {
    PickedItem pickedItem = omsOrderItem.getPickedItem();
    Tax taxInfo = taxInfoMap.get(pickedItem.getPickedItemUpc());
    DetailLine.DetailLineBuilder detailLineBuilder =
        DetailLine.builder()
            .productID(pickedItem.getOrderedCin())
            .colleagueDiscountedPrice(PYSIPYPConstants.NOT_APPLICABLE)
            .replenishUnitIndicator(PYSIPYPConstants.NOT_APPLICABLE)
            .storeTotalPrice(pickedItem.getTotalPrice().toString())
            .uom(omsOrderItem.getUom())
            .upcDetails(getPickedItemUpcDetails(pickedItem))
            .taxRate(getTaxRate(taxInfo))
            .taxType(getTaxType(taxInfo))
            .taxRecord(getTaxRecord(taxInfo))
            .pickedQuantity(pickedItem.getQuantity());

    if (pickedItem.isSubstituted()) {
      List<com.walmart.oms.infrastructure.gateway.price.dto.SubstitutedItem> substitutedItems =
          omsOrderItem.getSubstitutedItems().stream()
              .filter(
                  substitutedItem ->
                      // check whether substituted item upc is available and present in map.
                      substitutedItem
                          .getSubstitutedItemUpcDetail()
                          .filter(taxInfoMap::containsKey)
                          .isPresent())
              .map(
                  substitutedItem ->
                      buildPysipypRequestSubstitutedItem(
                          substitutedItem,
                          taxInfoMap.get(substitutedItem.getSubstitutedItemUpcDetail().get())))
              .collect(Collectors.toList());
      detailLineBuilder.substitutedItems(substitutedItems);
      detailLineBuilder.substitutionQty(substitutedItems.size());
    }
    return detailLineBuilder.build();
  }

  private com.walmart.oms.infrastructure.gateway.price.dto.SubstitutedItem
      buildPysipypRequestSubstitutedItem(SubstitutedItem substitutedItem, Tax taxInfo) {

    return com.walmart.oms.infrastructure.gateway.price.dto.SubstitutedItem.builder()
        .wmItemNum(substitutedItem.getWalmartItemNumber())
        .code("S")
        .department(substitutedItem.getDepartment())
        .description(substitutedItem.getDescription())
        .isOverridden(false)
        .pickedBy("CONSTANTS")
        .posDesc(substitutedItem.getDescription())
        .storeUnitPrice(substitutedItem.getSubstitutedItemUnitPrice().doubleValue())
        .storeTotalPrice(substitutedItem.getSubstitutedItemTotalPrice().doubleValue())
        .originalStoreTotalPrice(substitutedItem.getSubstitutedItemTotalPrice().doubleValue())
        .quantity(substitutedItem.getQuantity())
        .taxRate(getTaxRate(taxInfo))
        .taxType(getTaxType(taxInfo))
        .taxRecord(getTaxRecord(taxInfo))
        .uom(substitutedItem.getSubstitutedItemUom().orElse("E"))
        .upc(substitutedItem.getSubstitutedItemUpcDetail().orElse(null))
        .build();
  }

  private String getTaxRate(Tax taxInfo) {
    return taxInfo != null && taxInfo.getRate() != null && taxInfo.getRate().getValue() != null
        ? taxInfo.getRate().getValue().toString()
        : null;
  }

  private String getTaxRecord(Tax taxInfo) {
    return taxInfo != null && taxInfo.getRateId() != null ? taxInfo.getRateId().trim() : null;
  }

  private String getTaxType(Tax taxInfo) {
    return taxInfo != null && taxInfo.getTaxCode() != null ? taxInfo.getTaxCode().toString() : null;
  }

  private List<UPCDetail> getPickedItemUpcDetails(PickedItem pickedItem) {
    return pickedItem.getPickedItemUpcList().stream()
        .map(
            upc ->
                UPCDetail.builder()
                    .department(pickedItem.getDepartmentID())
                    .upc(upc.getUpc())
                    .qtySold(upc.getQuantity())
                    .weight(Double.toString(upc.getWeight()))
                    .wmItemNum(upc.getWin())
                    .build())
        .collect(Collectors.toList());
  }

  private List<OrderedItem> getOrderItems(List<OmsOrderItem> orderItems) {
    return orderItems.stream()
        .filter(omsOrderItem -> !omsOrderItem.isZeroPricedNilPick())
        .map(this::getOrderedItemForOrderedInfo)
        .collect(Collectors.toList());
  }

  private OrderedItem getOrderedItemForOrderedInfo(OmsOrderItem omsOrderItem) {
    return OrderedItem.builder()
        .skuId(omsOrderItem.getSkuId())
        .cin(omsOrderItem.getCin())
        .quantity(omsOrderItem.getQuantity())
        .priceInfo(getPriceInfo(omsOrderItem))
        .build();
  }

  private PriceInfo getPriceInfo(OmsOrderItem omsOrderItem) {
    double finalAmount = NumberUtils.getRoundedDouble(omsOrderItem.getFinalPriceOfOrderedItem());
    double unitPrice = NumberUtils.getRoundedDouble(omsOrderItem.getOrderedOrPickedItemUnitPrice());

    double upliftedListPrice = omsOrderItem.getVendorUnitPriceAmount().doubleValue();

    return PriceInfo.builder()
        .finalAmount(finalAmount)
        .listPrice(unitPrice)
        .upliftedListPrice(upliftedListPrice)
        .rawTotalPrice(finalAmount)
        .quantityAsQualifier(0L)
        .quantityDiscounted(0)
        .build();
  }

  private PYSIPYPHeader getHeaderForRecordSale(OmsOrder omsOrder) {
    return PYSIPYPHeader.builder()
        .storeNumber(omsOrder.getStoreId())
        .orderNumber(omsOrder.getStoreOrderId())
        .messageType(PYSIPYPConstants.ODS_CALC_TOTAL)
        .countryCode(PYSIPYPConstants.COUNTRY_CODE_GB)
        .accessCode(pysipypServiceConfiguration.getAccessCode())
        .build();
  }

  private MarketPlace getMarketPlace(OmsOrder omsOrder) {
    MarketPlace marketPlace = new MarketPlace();
    marketPlace.setVendorName(omsOrder.getVendorName());
    return marketPlace;
  }

  private PYSIPYPHeader getHeaderForReverseSale(
      OrderCancelledDomainEventMessage orderCancelledDomainEventMessage) {
    return PYSIPYPHeader.builder()
        .storeNumber(orderCancelledDomainEventMessage.getStoreId())
        .orderNumber(orderCancelledDomainEventMessage.getStoreOrderId())
        .messageType(PYSIPYPConstants.REVERSE_SALE)
        .countryCode(PYSIPYPConstants.COUNTRY_CODE_GB)
        .saleType(PYSIPYPConstants.SALE_TYPE_REVERSE)
        .accessCode(pysipypServiceConfiguration.getAccessCode())
        .build();
  }

  public OrderInformation buildReverseSaleRequest(
      OrderCancelledDomainEventMessage orderCancelledDomainEventMessage) {
    return OrderInformation.builder()
        .header(getHeaderForReverseSale(orderCancelledDomainEventMessage))
        .build();
  }
}
