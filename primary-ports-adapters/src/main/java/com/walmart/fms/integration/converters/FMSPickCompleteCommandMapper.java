package com.walmart.fms.integration.converters;

import com.walmart.fms.commands.FmsPickCompleteCommand;
import com.walmart.fms.domain.error.exception.FMSBadRequestException;
import com.walmart.fms.integration.xml.beans.orderpickcomplete.Amount;
import com.walmart.fms.integration.xml.beans.orderpickcomplete.Department;
import com.walmart.fms.integration.xml.beans.orderpickcomplete.Item;
import com.walmart.fms.integration.xml.beans.orderpickcomplete.Offering;
import com.walmart.fms.integration.xml.beans.orderpickcomplete.Quantity;
import com.walmart.fms.integration.xml.beans.orderpickcomplete.UpdateOrderPickedStatusRequest;
import com.walmart.fms.integration.xml.beans.orderpickcomplete.UpdateOrderPickedStatusRequest.MessageBody.CustomerOrder.FulfillmentOrders.OrderLineInfo;
import com.walmart.fms.integration.xml.beans.orderpickcomplete.UpdateOrderPickedStatusRequest.MessageBody.CustomerOrder.FulfillmentOrders.OrderLineInfo.OrderLine.Product;
import com.walmart.fms.integration.xml.beans.orderpickcomplete.Weight;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;

@Mapper
@Slf4j
public abstract class FMSPickCompleteCommandMapper {
  public static final FMSPickCompleteCommandMapper INSTANCE =
      Mappers.getMapper(FMSPickCompleteCommandMapper.class);
  private static final String REGEX = "^[0-9]+$";

  @Mapping(
      target = "data.orderInfo.storeOrderId",
      expression = "java(getStoreOrderId(updateOrderPickedStatusRequest))")
  @Mapping(
      target = "data.orderInfo.storeId",
      expression = "java(getStoreId(updateOrderPickedStatusRequest))")
  @Mapping(target = "data.orderInfo.tenant", constant = "ASDA")
  @Mapping(target = "data.orderInfo.vertical", constant = "MARKETPLACE")
  @Mapping(
      target = "data.orderInfo.cancelledReasonCode",
      expression =
          "java(getCancelReasonCode(updateOrderPickedStatusRequest.getMessageBody().getCustomerOrder().get(0).getFulfillmentOrders().get(0)))")
  @Mapping(
      target = "data.orderInfo.cancelledReasonDescription",
      expression =
          "java(getCancelReasonDescription(updateOrderPickedStatusRequest.getMessageBody().getCustomerOrder().get(0).getFulfillmentOrders().get(0)))")
  @Mapping(
      target = "data.orderInfo.orderStatus",
      expression =
          "java(getOrderStatus(updateOrderPickedStatusRequest.getMessageBody().getCustomerOrder().get(0).getFulfillmentOrders().get(0)))")
  @Mapping(
      target = "data.pickedItems",
      expression =
          "java(convertToPickItemInfoList(updateOrderPickedStatusRequest.getMessageBody().getCustomerOrder().get(0).getFulfillmentOrders().get(0).getOrderLineInfo()))")
  public abstract FmsPickCompleteCommand convertToPickcompleteCommand(
      UpdateOrderPickedStatusRequest updateOrderPickedStatusRequest);

  @Mapping(
      target = "cin",
      expression = "java(String.valueOf(orderLineInfo.getOrderLine().getProduct().get(0).getId()))",
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(
      target = "pickedItemUpcs",
      source = "orderLine.product",
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(
      target = "pickedBy",
      source = "pickedBy.userID",
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(
      target = "departmentId",
      expression = "java(getDepartmentNumber(orderLineInfo.getOrderLine().getProduct().get(0)))")
  @Mapping(
      target = "orderedQuantity",
      expression = "java(orderLineInfo.getOrderLine().getOrderLineQuantity().getAmount().longValue())")
  @Mapping(
      target = "pickedItemDescription",
      expression =
          "java(getPickedItemDescription(orderLineInfo.getOrderLine().getProduct().get(0)))")
  @Mapping(
      target = "substitutedItemInfoList",
      source = "substitutionInfo")
  public abstract FmsPickCompleteCommand.PickedItemInfo convertToPickItemInfo(
      OrderLineInfo orderLineInfo);

  @Mapping(
      target = "upc",
      expression = "java(product.getGlobalTradeItem().get(0).getGtin())",
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(
      target = "weight",
      expression = "java(getWeight(product))",
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "pickedQuantity", expression = "java(getPickQuantity(product))")
  @Mapping(target = "win", expression = "java(getWin(product))")
  @Mapping(
      target = "unitPrice",
      source = "product.price.amount.value",
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(
      target = "uom",
      expression = "java(getUom(product))",
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  public abstract FmsPickCompleteCommand.PickedItemUpc convertToPickItemUpc(Product product);

  public List<FmsPickCompleteCommand.PickedItemInfo> convertToPickItemInfoList(
      List<OrderLineInfo> orderLineInfoList) {
    List<FmsPickCompleteCommand.PickedItemInfo> pickedItemInfoList = null;
    if (!CollectionUtils.isEmpty(orderLineInfoList)) {
      pickedItemInfoList = new ArrayList<>();
      for (OrderLineInfo orderLineInfo : orderLineInfoList) {
        pickedItemInfoList.add(convertToPickItemInfo(orderLineInfo));
      }
      return pickedItemInfoList;
    } else {
      throw new FMSBadRequestException("Order Info is Empty");
    }
  }

  @Mapping(target = "walmartItemNumber",
      expression = "java(getSubstitutedItemWin(substitutionInfo))")
  @Mapping(target = "consumerItemNumber",
      expression = "java(getSubstitutedItemCin(substitutionInfo))",
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  @Mapping(target = "department",
      expression = "java(getSubstitutedItemDepartment(substitutionInfo))")
  @Mapping(target = "description",
      expression = "java(getSubstitutedItemDescription(substitutionInfo))")
  @Mapping(target = "quantity",
      expression = "java(getSubstitutedItemQuantity(substitutionInfo))")
  @Mapping(target = "weight",
      expression = "java(getSubstitutedItemWeight(substitutionInfo))")
  @Mapping(target = "unitPrice",
      expression = "java(getSubstitutedItemPrice(substitutionInfo))")
  @Mapping(target = "upcs",
      source = "substitutionInfo.product")
  public abstract FmsPickCompleteCommand.SubstitutedItemInfo convertToSubstitutedItem(
      OrderLineInfo.SubstitutionInfo substitutionInfo);

  @Mapping(
      target = "uom",
      expression = "java(getSubstitutedItemUom(substitutionProductInfo))")
  @Mapping(
      target = "upc",
      expression = "java(getSubstitutedItemUpc(substitutionProductInfo))",
      nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
  public abstract FmsPickCompleteCommand.SubstitutedItemUpc convertToSubstitutedItemUpc(
      OrderLineInfo.SubstitutionInfo.Product substitutionProductInfo);

  public String getStoreOrderId(UpdateOrderPickedStatusRequest updateOrderPickedStatusRequest) {
    try {
      if (updateOrderPickedStatusRequest
              .getMessageBody()
              .getCustomerOrder()
              .get(0)
              .getOrderHeader()
              .getOrderNumber()
          != null) {
        return String.valueOf(
            updateOrderPickedStatusRequest
                .getMessageBody()
                .getCustomerOrder()
                .get(0)
                .getOrderHeader()
                .getOrderNumber());
      } else {
        throw new FMSBadRequestException("Order Number is Null");
      }
    } catch (Exception e) {
      throw new FMSBadRequestException("Order Number is Null");
    }
  }

  public String getStoreId(UpdateOrderPickedStatusRequest updateOrderPickedStatusRequest) {
    try {
      if (updateOrderPickedStatusRequest
              .getMessageBody()
              .getCustomerOrder()
              .get(0)
              .getFulfillmentOrders()
              .get(0)
              .getNode()
              .getNodeID()
          > 0) {
        return String.valueOf(
            updateOrderPickedStatusRequest
                .getMessageBody()
                .getCustomerOrder()
                .get(0)
                .getFulfillmentOrders()
                .get(0)
                .getNode()
                .getNodeID());
      }
    } catch (Exception e) {
      log.debug("Store id null in UpdateOrderPickedStatusRequest");
    }
    return null;
  }

  public String getSubstitutedItemUpc(OrderLineInfo.SubstitutionInfo.Product
                                          substitutionProductInfo) {
    return Optional.ofNullable(substitutionProductInfo.getGlobalTradeItem())
        .flatMap(globalTradeItem -> globalTradeItem
            .stream()
            .findFirst()
            .map(OrderLineInfo.SubstitutionInfo.Product.GlobalTradeItem::getGtin))
        .orElse(null);
  }

  public String getSubstitutedItemUom(OrderLineInfo.SubstitutionInfo.Product
                                          substitutionProductInfo) {
    return Optional.ofNullable(substitutionProductInfo.getPickedQuantity())
        .map(Quantity::getUom)
        .orElse(null);
  }

  public String getSubstitutedItemCin(OrderLineInfo.SubstitutionInfo substitutionInfo) {
    return Optional.ofNullable(substitutionInfo.getProduct())
        .flatMap(products -> products.stream().findFirst()
            .map(OrderLineInfo.SubstitutionInfo.Product::getId)
            .map(String::valueOf))
        .orElse(null);
  }

  public BigDecimal getSubstitutedItemPrice(OrderLineInfo.SubstitutionInfo substitutionInfo) {
    return Optional.ofNullable(substitutionInfo.getProduct())
        .flatMap(products -> products.stream().findFirst()
            .map(OrderLineInfo.SubstitutionInfo.Product::getPrice)
            .map(OrderLineInfo.SubstitutionInfo.Product.Price::getAmount)
            .map(Amount::getValue)
        ).orElse(BigDecimal.ZERO);
  }

  public Long getSubstitutedItemQuantity(OrderLineInfo.SubstitutionInfo substitutionInfo) {
    return Optional.ofNullable(substitutionInfo.getProduct())
        .flatMap(products -> products.stream().findFirst()
            .map(OrderLineInfo.SubstitutionInfo.Product::getPickedQuantity)
            .map(Quantity::getAmount)
            .map(BigDecimal::longValue)
        ).orElse(0L);
  }

  public String getSubstitutedItemDescription(OrderLineInfo.SubstitutionInfo substitutionInfo) {
    return Optional.ofNullable(substitutionInfo.getProduct())
        .flatMap(products -> products.stream().findFirst()
            .map(OrderLineInfo.SubstitutionInfo.Product::getDescription))
        .orElse(null);
  }

  public String getSubstitutedItemDepartment(OrderLineInfo.SubstitutionInfo substitutionInfo) {
    return Optional.ofNullable(substitutionInfo.getProduct())
        .flatMap(products -> products.stream().findFirst()
            .map(Offering::getDepartment)
            .map(Department::getNumber)
            .map(String::valueOf))
        .orElse(null);
  }

  public String getSubstitutedItemWin(OrderLineInfo.SubstitutionInfo substitutionInfo) {
    return Optional.ofNullable(substitutionInfo.getProduct())
        .flatMap(products -> products.stream().findFirst()
            .map(OrderLineInfo.SubstitutionInfo.Product::getGlobalTradeItem)
            .flatMap(globalTradeItems -> globalTradeItems.stream().findFirst()
                .map(OrderLineInfo.SubstitutionInfo.Product.GlobalTradeItem::getItem))
            .flatMap(items -> items.stream().findFirst()
                .map(Item::getNumber))
            .map(String::valueOf))
        .orElse(null);
  }

  public double getWeight(Product product) {
    double weight = 0;
    if (product != null && product.getWeight() != null && product.getWeight().getAmount() != null) {
      weight = product.getWeight().getAmount().doubleValue();
    }
    return weight;
  }

  public String getDepartmentNumber(Product product) {
    String department = null;
    if (product != null
        && product.getDepartment() != null
        && product.getDepartment().getNumber() != null) {
      department = String.valueOf(product.getDepartment().getNumber());
    }
    return department;
  }

  public String getPickedItemDescription(Product product) {
    return Optional.ofNullable(product).map(Product::getDescription).orElse(null);
  }

  public Long getPickQuantity(Product product) {
    return (product != null
        && product.getPickedQuantity() != null
        && product.getPickedQuantity().getAmount() != null)
        ? product.getPickedQuantity().getAmount().longValue()
        : 0L;
  }

  public String getWin(Product product) {
    return (product != null
        && product.getGlobalTradeItem() != null
        && product.getGlobalTradeItem().get(0) != null
        && product.getGlobalTradeItem().get(0).getItem() != null
        && product.getGlobalTradeItem().get(0).getItem().get(0) != null)
        ? String.valueOf(product.getGlobalTradeItem().get(0).getItem().get(0).getNumber())
        : null;
  }

  public String getOrderStatus(
      UpdateOrderPickedStatusRequest.MessageBody.CustomerOrder.FulfillmentOrders
          fulfillmentOrders) {
    if (fulfillmentOrders != null
        && fulfillmentOrders.getStatus() != null
        && fulfillmentOrders.getStatus().getCode() != null) {
      String statusCode = fulfillmentOrders.getStatus().getCode();
      if (statusCode != null && statusCode.trim().matches(REGEX)) {
        int state = Integer.parseInt(statusCode.trim());
        return StoreOrderStatus.getOrderStatusByCode(state);
      }
    }
    return null;
  }

  public String getCancelReasonCode(
      UpdateOrderPickedStatusRequest.MessageBody.CustomerOrder.FulfillmentOrders
          fulfillmentOrders) {
    if (fulfillmentOrders != null
        && fulfillmentOrders.getStatus() != null
        && fulfillmentOrders.getStatus().getCode() != null) {
      return fulfillmentOrders.getStatus().getCode();
    }
    return null;
  }

  public String getCancelReasonDescription(
      UpdateOrderPickedStatusRequest.MessageBody.CustomerOrder.FulfillmentOrders
          fulfillmentOrders) {
    if (fulfillmentOrders != null
        && fulfillmentOrders.getStatus() != null
        && fulfillmentOrders.getStatus().getCode() != null) {
      String statusCode = fulfillmentOrders.getStatus().getCode();
      if (statusCode != null && statusCode.trim().matches(REGEX)) {
        int state = Integer.parseInt(statusCode.trim());
        if (state == StoreOrderStatus.CANCELLED.getCode()) {
          return fulfillmentOrders.getStatus().getDescription();
        }
      }
    }
    return null;
  }

  public String getUom(Product product) {
    String uom = "EACH";
    if (product != null) {
      if (product.getWeight() != null
          && product.getWeight().getAmount().compareTo(BigDecimal.ZERO) > 0) {
        uom = product.getWeight().getUom();
      } else {
        if (product.getPickedQuantity() != null) {
          uom = product.getPickedQuantity().getUom();
        }
      }
    }
    return uom;
  }

  public enum StoreOrderStatus {
    PICK_COMPLETE("PICK_COMPLETE", 7),
    CANCELLED("CANCELLED", 9300);
    private final String name;
    private final int code;

    StoreOrderStatus(String name, int code) {
      this.name = name;
      this.code = code;
    }

    public static String getOrderStatusByCode(int code) {
      for (FMSPickCompleteCommandMapper.StoreOrderStatus storeOrderStatus :
          FMSPickCompleteCommandMapper.StoreOrderStatus.values()) {
        if (code == storeOrderStatus.getCode()) {
          return storeOrderStatus.getName();
        }
      }
      log.warn("Unrecognizable order Status code found !!! code:{}", code);
      return null;
    }

    public String getName() {
      return name;
    }

    public Integer getCode() {
      return code;
    }
  }

  public Double getSubstitutedItemWeight(OrderLineInfo.SubstitutionInfo substitutionInfo) {
    return Optional.ofNullable(substitutionInfo.getProduct())
        .flatMap(products -> products.stream().findFirst()
            .map(OrderLineInfo.SubstitutionInfo.Product::getWeight)
            .map(Weight::getAmount)
            .map(BigDecimal::doubleValue)
        ).orElse(null);
  }
}
