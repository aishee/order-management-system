package com.walmart.oms.converter;

import com.walmart.common.domain.type.CancellationReason;
import com.walmart.common.domain.type.CancellationSource;
import com.walmart.common.domain.valueobject.CancellationDetails;
import com.walmart.oms.commands.OmsCancelOrderCommand;
import com.walmart.oms.domain.event.messages.OmsOrderEnrichmentFailureEventMessage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public class OmsCancelOrderCommandMapper {
  public static final OmsCancelOrderCommandMapper INSTANCE =
      Mappers.getMapper(OmsCancelOrderCommandMapper.class);

  public OmsCancelOrderCommand mapToOmsCancelOrderCommand(OmsOrderEnrichmentFailureEventMessage omsOrderEnrichmentFailureEventMessage) {
    return OmsCancelOrderCommand.builder()
        .sourceOrderId(omsOrderEnrichmentFailureEventMessage.getSourceOrderId())
        .tenant(omsOrderEnrichmentFailureEventMessage.getTenant())
        .vertical(omsOrderEnrichmentFailureEventMessage.getVertical())
        .cancellationDetails(buildCancelDetailsForIroFailure())
        .build();
  }

  private CancellationDetails buildCancelDetailsForIroFailure() {
    return CancellationDetails.builder()
        .cancelledBy(CancellationSource.OMS)
        .cancelledReasonCode(CancellationReason.IRO_FAILURE.getCode())
        .cancelledReasonDescription(CancellationReason.IRO_FAILURE.getDescription())
        .build();
  }
}
