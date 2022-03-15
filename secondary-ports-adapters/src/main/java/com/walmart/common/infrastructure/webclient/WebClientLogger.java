package com.walmart.common.infrastructure.webclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.StringWriter;
import javax.xml.bind.JAXB;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

@Slf4j
public abstract class WebClientLogger {

  @Autowired protected ObjectMapper jsonObjectMapper;

  protected abstract boolean isLogEnabled();

  protected void logXMLRequest(String uri, Object object) {
    if (isLogEnabled()) {
      StringWriter sw = new StringWriter();
      JAXB.marshal(object, sw);
      log.info("URI:{} , Request:{}", uri, sw);
    }
  }

  protected void logXMLResponse(String uri, Object object) {
    if (isLogEnabled()) {
      StringWriter sw = new StringWriter();
      JAXB.marshal(object, sw);
      log.info("URI:{} , Response:{}", uri, sw);
    }
  }

  @SneakyThrows
  protected void logRequest(String message, Object object) {
    if (isLogEnabled()) {
      log.info("Message:{} , Request:{}", message, jsonObjectMapper.writeValueAsString(object));
    }
  }

  @SneakyThrows
  protected void logRequest(String message) {
    if (isLogEnabled()) {
      log.info("Message:{}", message);
    }
  }

  @SneakyThrows
  protected <T> void logResponse(String message, ResponseEntity<T> responseEntity) {
    if (isLogEnabled()) {
      log.info(
          "Message:{} Response:{}", message, jsonObjectMapper.writeValueAsString(responseEntity));
    }
  }
}
