package com.walmart.fms.order.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.fms.order.valueobject.ItemUpcInfo;
import java.io.IOException;
import javax.persistence.AttributeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemUpcInfoConverter implements AttributeConverter<ItemUpcInfo, String> {

  private static final Logger logger = LoggerFactory.getLogger(ItemUpcInfoConverter.class);

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(ItemUpcInfo attribute) {

    String itemUpcInfoJson = null;

    if (attribute != null) {
      try {
        itemUpcInfoJson = objectMapper.writeValueAsString(attribute);
      } catch (final JsonProcessingException e) {
        logger.error("JSON writing error", e);
      }
    }

    return itemUpcInfoJson;
  }

  @Override
  public ItemUpcInfo convertToEntityAttribute(String dbData) {

    ItemUpcInfo itemUpcInfo = null;
    if (dbData != null) {
      try {
        itemUpcInfo = objectMapper.readValue(dbData, ItemUpcInfo.class);

      } catch (final IOException e) {
        logger.error("JSON reading error", e);
      }
    }
    return itemUpcInfo;
  }
}
