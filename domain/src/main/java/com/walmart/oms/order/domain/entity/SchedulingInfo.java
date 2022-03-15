package com.walmart.oms.order.domain.entity;

import com.walmart.common.domain.BaseEntity;
import com.walmart.oms.order.aggregateroot.OmsOrder;
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
@Table(name = "OMSCORE.OMS_ORDER_SCHEDULING_INFO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SchedulingInfo extends BaseEntity implements Serializable {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ORDER_RECORD_ID")
  private OmsOrder order;

  @Column(name = "TRIP_ID")
  private String tripId;

  @Column(name = "DOOR_STEP_TIME")
  private int doorStepTime;

  @Column(name = "VAN_ID")
  private String vanId;

  @Column(name = "SCHEDULE_NUMBER")
  private String scheduleNumber;

  @Column(name = "ORDER_DUE_TIME")
  private Date plannedDueTime;

  @Column(name = "LOAD_NUMBER")
  private String loadNumber;

  @Builder
  public SchedulingInfo(
      String id,
      OmsOrder order,
      String tripId,
      int doorStepTime,
      Date plannedDueTime,
      String vanId,
      String scheduleNumber,
      String loadNumber) {
    super(id);

    this.order = Objects.requireNonNull(order);

    this.tripId = tripId;
    this.doorStepTime = doorStepTime;
    this.plannedDueTime = plannedDueTime;
    this.vanId = vanId;
    this.scheduleNumber = scheduleNumber;
    this.loadNumber = loadNumber;
  }
}
