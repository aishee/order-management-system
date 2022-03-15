package com.walmart.marketplace.order.domain.valueobject.mappers;

import com.walmart.common.domain.valueobject.CancellationDetails;
import com.walmart.oms.order.valueobject.CancelDetails;
import com.walmart.oms.order.valueobject.events.CancellationDetailsValueObject;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class CancellationDetailsValueObjectMapper {

  public static final CancellationDetailsValueObjectMapper INSTANCE =
      Mappers.getMapper(CancellationDetailsValueObjectMapper.class);

  public abstract CancellationDetailsValueObject modelToValueObject(CancelDetails cancellationDetails);

  public abstract CancellationDetailsValueObject modelToValueObject(CancellationDetails cancellationDetails);

  public abstract CancellationDetails modelToDomainObject(CancellationDetailsValueObject cancellationDetails);

}
