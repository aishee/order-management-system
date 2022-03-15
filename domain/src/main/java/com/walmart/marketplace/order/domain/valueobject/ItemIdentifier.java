package com.walmart.marketplace.order.domain.valueobject;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ItemIdentifier implements Serializable {

  @Column(name = "ITEM_ID")
  private String itemId;

  @Column(name = "ITEM_TYPE")
  private String itemType;
}
