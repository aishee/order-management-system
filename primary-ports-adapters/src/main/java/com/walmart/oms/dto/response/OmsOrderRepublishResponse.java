package com.walmart.oms.dto.response;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OmsOrderRepublishResponse implements Serializable {
  private static final String REPLAY_STATUS = "Republishing Of Orders is started successfully.";
  private final String message;

  public static OmsOrderRepublishResponse accepted() {
    return new OmsOrderRepublishResponse(REPLAY_STATUS);
  }
}
