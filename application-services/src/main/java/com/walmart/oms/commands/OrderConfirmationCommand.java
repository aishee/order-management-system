package com.walmart.oms.commands;

import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderConfirmationCommand {

  private String sourceOrderId;

  private Tenant tenant;

  private Vertical vertical;
}
