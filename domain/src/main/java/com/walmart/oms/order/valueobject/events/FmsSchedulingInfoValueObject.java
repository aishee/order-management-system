package com.walmart.oms.order.valueobject.events;

import java.util.Date;
import lombok.Data;

@Data
public class FmsSchedulingInfoValueObject {

  private String tripId;

  private int doorStepTime;

  private String vanId;

  private String scheduleNumber;

  private Date plannedDueTime;

  private String loadNumber;
}
