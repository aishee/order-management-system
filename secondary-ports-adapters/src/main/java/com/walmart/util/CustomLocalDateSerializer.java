package com.walmart.util;

import static com.walmart.util.DateTimeUtil.formatLocalDate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/** This class is used to serialize localdate value to string in the format YYYY-MM-dd */
@Slf4j
public class CustomLocalDateSerializer extends StdSerializer<LocalDate> {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public CustomLocalDateSerializer() {
    super(LocalDate.class);
  }

  @Override
  public void serialize(
      LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {
    Optional<String> opDate = formatLocalDate(localDate, FORMATTER);
    opDate.ifPresent(
        date -> {
          try {
            jsonGenerator.writeString(date);
          } catch (IOException exception) {
            String errorMsg =
                String.format(
                    "Error occurred when writing formatted to report payload, date:%s", date);
            log.error(errorMsg, exception);
          }
        });
  }
}
