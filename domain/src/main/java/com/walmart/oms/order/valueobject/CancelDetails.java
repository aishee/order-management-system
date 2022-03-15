package com.walmart.oms.order.valueobject;

import com.walmart.common.domain.type.CancellationSource;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelDetails implements Serializable {

  @Column(name = "CANCELLED_REASON_CODE")
  private String cancelledReasonCode;

  @Column(name = "CANCELLED_BY")
  @Enumerated(EnumType.STRING)
  private CancellationSource cancelledBy;

  @Column(name = "CANCELLED_REASON_DESCRIPTION")
  private String cancelledReasonDescription;

  public String getCancelledBySourceName() {
    return cancelledBy.getSourceName();
  }
}
