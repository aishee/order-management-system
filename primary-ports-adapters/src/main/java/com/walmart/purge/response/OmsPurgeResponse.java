package com.walmart.purge.response;

import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OmsPurgeResponse implements Serializable {
  private String message;

  public static OmsPurgeResponse from(String message) {
    return new OmsPurgeResponse(message);
  }
}
