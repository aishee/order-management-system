package com.walmart.fms.order.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "OMSCORE.FULFILLMENT_ORDER_SCHEDULING_INFO")
public class FmsSchedulingInfo extends BaseEntity implements Serializable {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "REF_RECORD_ID")
  private FmsOrder order;

  @Column(name = "TRIP_ID")
  private String tripId;

  @Column(name = "DOOR_STEP_TIME")
  private int doorStepTime;

  @Column(name = "SLOT_START_TIME")
  private Date slotStartTime;

  @Column(name = "SLOT_END_TIME")
  private Date slotEndTime;

  @Column(name = "ORDER_DUE_TIME")
  private Date orderDueTime;

  @Column(name = "VAN_ID")
  private String vanId;

  @Column(name = "SCHEDULE_NUMBER")
  private String scheduleNumber;

  @Column(name = "LOAD_NUMBER")
  private String loadNumber;

  @Builder
  public FmsSchedulingInfo(
      String id,
      FmsOrder order,
      String tripId,
      int doorStepTime,
      Date slotStartTime,
      Date slotEndTime,
      Date orderDueTime,
      String vanId,
      String scheduleNumber,
      String loadNumber) {
    super(id);

    this.order = Objects.requireNonNull(order);
    this.tripId = tripId;
    this.doorStepTime = doorStepTime;
    this.slotStartTime = slotStartTime;
    this.slotEndTime = slotEndTime;
    this.orderDueTime = orderDueTime;
    this.vanId = vanId;
    this.scheduleNumber = scheduleNumber;
    this.loadNumber = loadNumber;
  }

  @Override
  public String toString() {
    return "SchedulingInfo{"
        + "order="
        + order
        + ", tripId='"
        + tripId
        + '\''
        + ", doorStepTime="
        + doorStepTime
        + ", slotStartTime="
        + slotStartTime
        + ", slotEndTime="
        + slotEndTime
        + ", orderDueTime="
        + orderDueTime
        + ", vanId='"
        + vanId
        + '\''
        + ", scheduleNumber='"
        + scheduleNumber
        + '\''
        + ", loadNumber='"
        + loadNumber
        + '\''
        + '}';
  }

  @JsonIgnore
  public Date getDeliverySlotEndTimeRange() {
    return this.getOrder().isMarketPlaceOrder() ? this.getOrderDueTime() : this.getSlotEndTime();
  }
}
