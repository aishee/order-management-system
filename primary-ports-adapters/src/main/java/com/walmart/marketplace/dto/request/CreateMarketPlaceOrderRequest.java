package com.walmart.marketplace.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmart.common.domain.type.Currency;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.marketplace.order.domain.valueobject.Money;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CreateMarketPlaceOrderRequest implements Serializable {

  @Valid private CreateMarketPlaceOrderRequest.CreateMarketPlaceOrderRequestData data;

  @JsonIgnore
  public PaymentInfo getPayment() {
    return this.data.payment;
  }

  @JsonIgnore
  public Money getTotal(Currency currency) {
    return new Money(this.getPayment().getTotal(), currency);
  }

  @JsonIgnore
  public Money getBagFee(Currency currency) {
    return new Money(this.getPayment().getBagFee(), currency);
  }

  @JsonIgnore
  public Money getTotalFee(Currency currency) {
    return new Money(this.getPayment().getTotalFee(), currency);
  }

  @JsonIgnore
  public Money getTax(Currency currency) {
    return new Money(this.getPayment().getTax(), currency);
  }

  @JsonIgnore
  public Money getTotalFeeTax(Currency currency) {
    return new Money(this.getPayment().getTotalFeeTax(), currency);
  }

  @JsonIgnore
  public Money getSubTotal(Currency currency) {
    return new Money(this.getPayment().getSubTotal(), currency);
  }

  @Data
  @AllArgsConstructor
  @Builder
  public static class CreateMarketPlaceOrderRequestData implements Serializable {

    @JsonProperty(value = "external_order_id")
    @Valid
    private String externalOrderId;

    @JsonProperty(value = "external_native_order_id")
    private String externalNativeOrderId;

    @JsonProperty(value = "store_id")
    @NotNull
    @NotEmpty
    private String storeId;

    @NotNull
    @Valid
    @JsonProperty(value = "first_name")
    private String firstName;

    @JsonProperty(value = "last_name")
    @Valid
    private String lastName;

    @JsonProperty(value = "payment_info")
    @NotNull
    @NotEmpty
    private PaymentInfo payment;

    @JsonProperty(value = "source_modified_date")
    private Date sourceOrderCreationTime;

    @JsonProperty(value = "estimated_due_time")
    @NotNull
    private Date estimatedArrivalTime;

    @JsonProperty(value = "vendor_id")
    @NotNull
    private Vendor vendor;

    @Valid
    @JsonProperty(value = "items")
    private List<MarketPlaceRequestItemData> marketPlaceItems;
  }

  @Data
  @Builder
  public static class MarketPlaceRequestItemData implements Serializable {

    @NotNull
    @JsonProperty(value = "external_item_id")
    private String externalItemId;

    @JsonProperty(value = "item_description")
    private String itemDescription;

    @NotNull
    @JsonProperty(value = "item_id")
    private String itemId;

    @NotNull
    @JsonProperty(value = "item_type")
    private String itemType;

    @JsonProperty(value = "vendor_instance_id")
    private String vendorInstanceId;

    @PositiveOrZero
    @JsonProperty(value = "quantity")
    private long quantity;

    @JsonProperty(value = "unit_price")
    private double unitPrice;

    @JsonProperty(value = "base_unit_price")
    private double baseUnitPrice;

    @JsonProperty(value = "total_price")
    private double totalPrice;

    @JsonProperty(value = "base_total_price")
    private double baseTotalPrice;

    @JsonProperty(value = "substitution_option")
    private SubstitutionOption substitutionOption;

    @JsonProperty(value = "bundled_items")
    private List<MarketPlaceRequestBundledItemData> marketPlaceBundledItems;

    public SubstitutionOption getSubstitutionOption() {
      return Optional.ofNullable(substitutionOption).orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
    }
  }

  @Data
  @Builder
  public static class MarketPlaceRequestBundledItemData implements Serializable {

    @JsonProperty(value = "bundle_quantity")
    private long bundleQuantity;

    @JsonProperty(value = "bundle_sku_id")
    private String bundleSkuId;

    @JsonProperty(value = "bundle_instance_id")
    private String bundleInstanceId;

    @JsonProperty(value = "bundle_description")
    private String bundleDescription;

    @JsonProperty(value = "item_quantity")
    private long itemQuantity;
  }

  @Data
  @Builder
  public static class PaymentInfo implements Serializable {

    @JsonProperty(value = "total")
    private BigDecimal total;

    @JsonProperty(value = "sub_total")
    private BigDecimal subTotal;

    @JsonProperty(value = "tax")
    private BigDecimal tax;

    @JsonProperty(value = "total_fee")
    private BigDecimal totalFee;

    @JsonProperty(value = "total_fee_tax")
    private BigDecimal totalFeeTax;

    @JsonProperty(value = "bag_fee")
    private BigDecimal bagFee;
  }
}
