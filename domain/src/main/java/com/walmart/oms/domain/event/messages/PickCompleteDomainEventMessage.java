package com.walmart.oms.domain.event.messages;

import com.walmart.common.domain.event.processing.Message;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickCompleteDomainEventMessage implements Message {

  private String sourceOrderId;
  private Tenant tenant;
  private Vertical vertical;
}
