package com.walmart.fms.order.valueobject.events;

import com.walmart.common.domain.type.SubstitutionOption;
import java.util.List;
import java.util.Optional;
import lombok.Data;

@Data
public class FmsOrderItemvalueObject {

  private String cin;

  private double unitPrice;

  private long quantity;

  private double weight;

  private String itemDescription;

  private String skuId;

  private String salesUnit;

  private String uom;

  private List<FmsOrderItemUpcValueObject> upcs;

  private FmsPickedItemValueObject pickedItem;

  private SubstitutionOption substitutionOption;

  private String imageUrl;

  public SubstitutionOption getSubstitutionOption() {
    return Optional.ofNullable(substitutionOption)
        .orElse(SubstitutionOption.DO_NOT_SUBSTITUTE);
  }
}
