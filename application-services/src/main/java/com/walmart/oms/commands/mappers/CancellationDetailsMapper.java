package com.walmart.oms.commands.mappers;

import com.walmart.common.domain.valueobject.CancellationDetails;
import com.walmart.oms.order.valueobject.CancelDetails;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public abstract class CancellationDetailsMapper {
  public static final CancellationDetailsMapper INSTANCE =
      Mappers.getMapper(CancellationDetailsMapper.class);

  public abstract CancelDetails convertToValueObject(CancellationDetails cancellationDetails);

  public abstract CancellationDetails convertToDomainObject(CancelDetails cancellationDetails);

}
