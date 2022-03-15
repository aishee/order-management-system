package com.walmart.oms.domain.event.messages;

import com.walmart.common.domain.event.processing.Message;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderCreatedDomainEventMessage implements Message {

  private String sourceOrderId;
  private Tenant tenant;
  private Vertical vertical;
}
