package com.walmart.fms.order.valueobject;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class Picker implements Serializable {

  @Builder
  public Picker(String pickerUserName) {
    this.pickerUserName = Objects.requireNonNull(pickerUserName, "picked user name cannot be null");
  }

  @Column(name = "PICKED_BY")
  private String pickerUserName;
}
