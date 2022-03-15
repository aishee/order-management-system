package com.walmart.oms.integration.jms.config;

import io.strati.configuration.annotation.ManagedConfiguration;
import javax.jms.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
@Slf4j
public class JMSConfiguration {

  private static final String TYPE = "_type";

  @ManagedConfiguration private ActiveMQRedeliveryPolicyConfig activemqRedeliveryPolicyConfig;

  @Bean
  public JmsListenerContainerFactory<DefaultMessageListenerContainer> defaultConnectionFactory(
      ConnectionFactory connectionFactory,
      DefaultJmsListenerContainerFactoryConfigurer configurer,
      DefaultJmsListenerContainerFactory jmsListenerContainerFactory) {
    jmsListenerContainerFactory.setErrorHandler(
        throwable -> log.error("JMS Listener encountered an error : ", throwable));
    configurer.configure(jmsListenerContainerFactory, connectionFactory);
    return jmsListenerContainerFactory;
  }

  @Bean
  public MessageConverter jacksonJmsMessageConverter() {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);
    converter.setTypeIdPropertyName(TYPE);
    return converter;
  }

  private RedeliveryPolicy queueRedeliveryPolicy() {
    RedeliveryPolicy policy = new RedeliveryPolicy();
    int maxRedelivery = activemqRedeliveryPolicyConfig.getMaximumRedeliveries();
    log.info("ActiveMQ maximum redelivery value is set as : {}", maxRedelivery);
    policy.setMaximumRedeliveries(maxRedelivery);
    return policy;
  }

  @Bean
  public ActiveMQConnectionFactoryCustomizer configureRedeliveryPolicy() {
    return connectionFactory -> {
      RedeliveryPolicy redeliveryPolicy = queueRedeliveryPolicy();
      // configure redelivery policy
      connectionFactory.setRedeliveryPolicy(redeliveryPolicy);
    };
  }
}
