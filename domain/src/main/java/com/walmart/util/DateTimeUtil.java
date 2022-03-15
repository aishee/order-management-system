package com.walmart.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class DateTimeUtil {

  public static XMLGregorianCalendar getTime(Date date) {
    XMLGregorianCalendar xmlTime = null;
    if (null != date) {
      try {
        GregorianCalendar calendar = new GregorianCalendar();
        TimeZone utc = TimeZone.getTimeZone("UTC");
        calendar.setTimeZone(utc);
        calendar.setTime(date);
        xmlTime = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
      } catch (DatatypeConfigurationException e) {
        log.error("Error in time conversion", e);
      }
    }
    return xmlTime;
  }

  public static Optional<String> formatLocalDate(LocalDate localDate, DateTimeFormatter format) {
    if (null == format || null == localDate) {
      return Optional.empty();
    }
    return Optional.of(localDate.format(format));
  }

  public static Date fromLocalDateTime(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}
