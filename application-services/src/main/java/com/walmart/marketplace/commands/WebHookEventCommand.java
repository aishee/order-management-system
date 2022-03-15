package com.walmart.marketplace.commands;

import com.walmart.marketplace.order.domain.entity.type.Vendor;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WebHookEventCommand {

  private String sourceEventId;

  private Instant requestTime;

  private String userId;

  private String externalOrderId;

  private String resourceURL;

  private String eventType;

  private Vendor vendor;
}
