package com.walmart.fms.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.fms.order.aggregateroot.FmsOrder;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "OMSCORE.FULFILLMENT_ORDER_TIMESTAMPS")
public class FmsOrderTimestamps extends BaseEntity implements Serializable {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "REF_RECORD_ID")
  private FmsOrder order;

  @Column(name = "PICKING_START_TIME")
  private Date pickingStartTime;

  @Column(name = "CANCELLED_TIME")
  private Date cancelledTime;

  @Column(name = "PICK_COMPLETE_TIME")
  private Date pickCompleteTime;

  @Column(name = "SHIP_CONFIRM_TIME")
  private Date shipConfirmTime;

  @Column(name = "ORDER_DELIVERED_TIME")
  private Date orderDeliveredTime;

  @Column(name = "PICK_UP_READY_TIME")
  private Date pickupReadyTime;

  @Column(name = "PICK_UP_TIME")
  private Date pickupTime;

  @Builder
  public FmsOrderTimestamps(
      String id,
      FmsOrder order,
      Date pickingStartTime,
      Date cancelledTime,
      Date pickCompleteTime,
      Date shipConfirmTime,
      Date orderDeliveredTime,
      Date pickupReadyTime,
      Date pickupTime) {
    super(id);

    this.order = Objects.requireNonNull(order);
    this.pickingStartTime = pickingStartTime;
    this.cancelledTime = cancelledTime;
    this.pickCompleteTime = pickCompleteTime;
    this.shipConfirmTime = shipConfirmTime;
    this.orderDeliveredTime = orderDeliveredTime;
    this.pickupReadyTime = pickupReadyTime;
    this.pickupTime = pickupTime;
  }

  @Override
  public String toString() {
    return "FmsOrderTimestamps{"
        + "order="
        + order
        + ", pickingStartTime="
        + pickingStartTime
        + ", cancelledTime="
        + cancelledTime
        + ", pickCompleteTime="
        + pickCompleteTime
        + ", shipConfirmTime="
        + shipConfirmTime
        + ", orderDeliveredTime="
        + orderDeliveredTime
        + ", pickupReadyTime="
        + pickupReadyTime
        + ", pickupTime="
        + pickupTime
        + '}';
  }

  public void updateDeliveredTime(Date orderDeliveredTime) {
    this.orderDeliveredTime = orderDeliveredTime;
  }

  public void updatePickCompleteTime(Date pickCompleteTime) {
    this.pickCompleteTime = pickCompleteTime;
  }

  public void updateCancelledTime(Date cancelledTime) {
    this.cancelledTime = cancelledTime;
  }

  public void updatePickStartedTime(Date pickingStartTime) {
    this.pickingStartTime = pickingStartTime;
  }
}
