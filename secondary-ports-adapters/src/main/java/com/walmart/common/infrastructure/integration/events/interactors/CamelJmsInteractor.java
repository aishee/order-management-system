package com.walmart.common.infrastructure.integration.events.interactors;

import com.walmart.common.domain.BaseEntity;
import com.walmart.common.domain.event.processing.EgressEvent;
import com.walmart.common.domain.event.processing.EventResponse;
import com.walmart.common.domain.event.processing.Interactor;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** An {@link Interactor} which uses apache camel for Jms interactions. */
@Slf4j
@Component("camelJmsProducer")
public class CamelJmsInteractor<T extends BaseEntity, R, W> implements Interactor<T, R, W> {

  /** An instance of {@link CamelContext} */
  @Autowired private CamelContext camelContext;

  /** An instance of {@link ProducerTemplate} */
  private ProducerTemplate producerTemplate;

  /** Initialize the producer template after the bean is constructed. */
  @PostConstruct
  private void initProducerTemplateIfNotPresent() {
    if (producerTemplate == null) {
      producerTemplate = camelContext.createProducerTemplate();
    }
  }

  @Override
  public EventResponse<W> call(EgressEvent<T, R> event) {
    initProducerTemplateIfNotPresent();
    producerTemplate.sendBody(event.getDestination(), event.getMessage());
    return new EventResponse<>();
  }
}
