package com.walmart.fms.order.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemUpcInfo implements Serializable {

  private List<String> upcNumbers;
}
