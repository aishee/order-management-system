package com.walmart.marketplace.justeats.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.walmart.common.domain.type.Currency;
import com.walmart.marketplace.order.domain.valueobject.Money;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@ToString
public class JustEatsWebHookRequest {

  @JsonProperty("type")
  public String type;

  @JsonProperty("posLocationId")
  public String posLocationId;

  @JsonProperty("id")
  public String id;

  @JsonProperty("items")
  public List<Item> items = Collections.emptyList();

  @JsonProperty("created_at")
  public String createdAt;

  @JsonProperty("collect_at")
  public String collectAt;

  @JsonProperty("third_party_order_reference")
  public String thirdPartyOrderReference;

  @JsonProperty("payment")
  public Payment payment;

  @JsonProperty("delivery")
  public Delivery delivery;

  public Date getArrivalTime() {
    return new Date(Instant.ofEpochSecond(Long.parseLong(getCollectAt())).toEpochMilli());
  }

  public String getCustomerFirstName() {
    return Optional.ofNullable(getDelivery()).map(Delivery::getFirstName).orElse("");
  }

  public String getCustomerLastName() {
    return Optional.ofNullable(getDelivery()).map(Delivery::getLastName).orElse("");
  }

  public Date getOrderCreationTime() {
    return new Date(Long.parseLong(this.getCreatedAt()));
  }

  public Money getTotalPayment() {
    return new Money(this.getPayment().getFinalAmountIncludingTax(), Currency.GBP);
  }

  public Money getTaxAmount() {
    return new Money(this.getPayment().getTaxAmount(), Currency.GBP);
  }

  public Money getSubTotal() {
    return new Money(this.getPayment().getSubTotal(), Currency.GBP);
  }

  @JsonIgnore
  public Money getBagFee() {
    return new Money(this.getPayment().getBagFee(), Currency.GBP);
  }
}
