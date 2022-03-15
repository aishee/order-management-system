package com.walmart.marketplace.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmart.common.domain.type.SubstitutionOption;
import com.walmart.marketplace.order.domain.entity.type.Vendor;
import com.walmart.oms.domain.error.ErrorType;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MarketPlaceOrderResponse implements Serializable {

  @JsonProperty("data")
  private MarketPlaceOrderResponse.MarketPlaceOrderResponseData data;

  @JsonProperty("errors")
  private List<MarketPlaceOrderResponse.MarketPlaceError> errors;

  @Data
  @Builder
  public static class MarketPlaceOrderResponseData implements Serializable {

    @JsonProperty(value = "market_place_order_id")
    private String id;

    @JsonProperty(value = "external_order_id")
    private String externalOrderId;

    @JsonProperty(value = "store_id")
    private String storeId;

    @JsonProperty(value = "vendor_store_id")
    private String vendorStoreId;

    @JsonProperty(value = "order_status")
    private String orderStatus;

    @JsonProperty(value = "first_name")
    private String firstName;

    @JsonProperty(value = "last_name")
    private String lastName;

    @JsonProperty(value = "payment_info")
    private MarketPlaceOrderResponse.PaymentInfo payment;

    @JsonProperty(value = "source_modified_date")
    private Date sourceOrderCreationTime;

    @JsonProperty(value = "estimated_due_time")
    private Date estimatedDueTime;

    @JsonProperty(value = "vendor_id")
    private Vendor vendor;

    @Valid
    @JsonProperty(value = "items")
    private List<MarketPlaceResponseItemData> marketPlaceItems;
  }

  @Data
  @Builder
  public static class MarketPlaceResponseItemData implements Serializable {

    @JsonProperty(value = "external_item_id")
    private String externalItemId;

    @JsonProperty(value = "item_description")
    private String itemDescription;

    @JsonProperty(value = "item_id")
    private String itemId;

    @JsonProperty(value = "item_type")
    private String itemType;

    @JsonProperty(value = "vendor_instance_id")
    private String vendorInstanceId;

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
    private List<MarketPlaceResponseBundledItemData> bundledItems;
  }

  @Data
  @Builder
  public static class MarketPlaceResponseBundledItemData implements Serializable {
    @JsonProperty(value = "bundle_quantity")
    private long bundleQuantity;

    @JsonProperty(value = "bundle_sku_id")
    private String bundleSkuId;

    @JsonProperty(value = "bundle_instance_id")
    private String bundleInstanceId;

    @JsonProperty(value = "bundle_description")
    private String bundleDescription;
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

  @Data
  @Builder
  public static class MarketPlaceError implements Serializable {

    @JsonProperty("error_code")
    private ErrorType errorCode;

    @JsonProperty("error_message")
    private String errorMessage;

    @JsonProperty("code")
    private ErrorType code;

    @JsonProperty("type")
    private String type;

    @JsonProperty("message")
    private String message;
  }
}
