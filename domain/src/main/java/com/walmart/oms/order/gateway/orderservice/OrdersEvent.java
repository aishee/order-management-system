package com.walmart.oms.order.gateway.orderservice;

import java.sql.Timestamp;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@EqualsAndHashCode
@ToString
public final class OrdersEvent<T> {

  private static final long serialVersionUID = -1765573867496688553L;

  @XmlElement(name = "event_id", required = true)
  @NotNull
  private String eventId; // orderId or purchaseOrderId or shipmentId

  @XmlElement(name = "event_name", required = true)
  @NotNull
  private String eventName; // event-type (Order/PurchaseOrder/Shipments)

  @XmlElement(name = "event_source", required = true)
  @NotNull
  private String eventSource; // data provider (PGOMS/COMS/FOMS)

  @XmlElement(name = "event_time", required = true)
  @NotNull
  private Timestamp eventTime; // timestamp at which message has been triggered

  @XmlElement(name = "tenant_id")
  private String tenantId;

  @XmlElement(name = "vertical_id", required = true)
  @NotNull
  private String verticalId;

  @XmlElement(name = "shard_id", required = true)
  private Integer shardId;

  @XmlElement(name = "src_created_dt", required = true)
  @NotNull
  private Timestamp srcCreatedDt;

  @XmlElement(name = "src_modified_dt", required = true)
  @NotNull
  private Timestamp srcModifiedDt;

  @XmlElement(name = "event_payload", required = true)
  @NotNull
  private T eventPayload;

  public Timestamp getEventTime() {
    if (eventTime != null) {
      return new Timestamp(eventTime.getTime());
    }
    return null;
  }

  public void setEventTime(Timestamp eventTime) {
    if (eventTime != null) {
      this.eventTime = new Timestamp(eventTime.getTime());
    }
  }

  public Timestamp getSrcCreatedDt() {
    if (srcCreatedDt != null) {
      return new Timestamp(srcCreatedDt.getTime());
    }
    return null;
  }

  public void setSrcCreatedDt(Timestamp srcCreatedDt) {
    if (srcCreatedDt != null) {
      this.srcCreatedDt = new Timestamp(srcCreatedDt.getTime());
    }
  }

  public Timestamp getSrcModifiedDt() {
    if (srcModifiedDt != null) {
      return new Timestamp(srcModifiedDt.getTime());
    }
    return null;
  }

  public void setSrcModifiedDt(Timestamp srcModifiedDt) {
    if (srcModifiedDt != null) {
      this.srcModifiedDt = new Timestamp(srcModifiedDt.getTime());
    }
  }
}
