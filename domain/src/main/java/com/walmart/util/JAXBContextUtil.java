package com.walmart.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class JAXBContextUtil {

  public static <T> JAXBContext getJAXBContext(Class<T> classType) {
    try {
      return JAXBContext.newInstance(classType);
    } catch (JAXBException jaxbException) {
      log.error(String.format("Not able to create %s : ", classType.getName()), jaxbException);
      throw new AssertionError(jaxbException);
    }
  }
}
