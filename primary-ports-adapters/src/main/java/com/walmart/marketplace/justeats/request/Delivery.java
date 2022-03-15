package com.walmart.marketplace.justeats.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@ToString
public class Delivery {

  @JsonProperty("first_name")
  public String firstName;

  @JsonProperty("last_name")
  public String lastName;
}
