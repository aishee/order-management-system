package com.walmart.fms.integration.kafka.processors.orderupdateprocessors;

import javax.xml.bind.JAXBException;

public interface GIFOrderUpdateEventProcessor {
  void process(String message) throws JAXBException;
}
