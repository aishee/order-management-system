package com.walmart.oms.order.valueobject;

import com.walmart.common.domain.AssertionConcern;
import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MobilePhone extends AssertionConcern implements Serializable {

  private String number;

  public MobilePhone(String number) {
    this.number = number;
  }
}
