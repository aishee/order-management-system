package com.walmart.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.walmart.common.domain.type.Tenant;
import com.walmart.common.domain.type.Vertical;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderVO {

  private final String sourceOrderId;
  private final Tenant tenant;
  private final Vertical vertical;
}
