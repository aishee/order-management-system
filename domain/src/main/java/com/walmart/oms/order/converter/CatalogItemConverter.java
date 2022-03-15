package com.walmart.oms.order.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.oms.order.valueobject.CatalogItem;
import java.io.IOException;
import javax.persistence.AttributeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CatalogItemConverter implements AttributeConverter<CatalogItem, String> {

  private static final Logger logger = LoggerFactory.getLogger(CatalogItemConverter.class);

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(CatalogItem attribute) {
    String catalogInfoJson = null;
    try {
      catalogInfoJson = objectMapper.writeValueAsString(attribute);
    } catch (final JsonProcessingException e) {
      logger.error("JSON writing error", e);
    }

    return catalogInfoJson;
  }

  @Override
  public CatalogItem convertToEntityAttribute(String dbData) {

    CatalogItem catalogItem = null;
    try {
      catalogItem = objectMapper.readValue(dbData, CatalogItem.class);
    } catch (final IOException e) {
      logger.error("JSON reading error", e);
    }

    return catalogItem;
  }
}
