package com.walmart.fms.order.valueobject.events;

import com.walmart.fms.order.valueobject.FmsSubstitutedItemValueObject;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class FmsPickedItemValueObject {

  private String departmentID;

  private String orderedCin;

  private long quantity;

  private double weight;

  private String pickedItemDescription;

  private String pickerUserName;

  private BigDecimal unitPrice;

  private List<FmsPickedItemUpcVo> pickedItemUpcList;

  private List<FmsSubstitutedItemValueObject> substitutedItems;
}
