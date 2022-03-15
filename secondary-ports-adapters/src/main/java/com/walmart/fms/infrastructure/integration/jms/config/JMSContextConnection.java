package com.walmart.fms.infrastructure.integration.jms.config;

import io.strati.StratiServiceProvider;
import io.strati.configuration.annotation.ManagedConfiguration;
import io.strati.messaging.jms.MessagingJMSService;
import io.strati.messaging.jms.spi.AxonMessagingJMSServiceImpl;
import javax.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Component;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter;

@Slf4j
@Configuration
@EnableJms
public class JMSContextConnection {

  @ManagedConfiguration private JmsProducerClientConfig jmsProducerClientConfig;

  private static CloudMessagingCustomFactory cloudMessagingCustomFactory(
      JmsClientConfig jmsClientConfig) throws JMSException {
    CloudMessagingCustomFactory cloudMessagingCustomFactory = new CloudMessagingCustomFactory();
    MessagingJMSService messagingService =
        StratiServiceProvider.getInstance().getMessagingJMSService().orElse(null);
    if (messagingService != null && StringUtils.isNotBlank(jmsClientConfig.getCloudConfigName())) {
      // Get a new instance of ConnectionFactory using messagingService and providing the client
      // config which is resolved using CCM
      cloudMessagingCustomFactory.setConnectionFactory(
          messagingService.getConnectionFactory(jmsClientConfig.getCloudConfigName()));
    } else {
      AxonMessagingJMSServiceImpl axonMessagingJMSService = new AxonMessagingJMSServiceImpl();
      if (StringUtils.isNotBlank(jmsClientConfig.getCloudConfigName())) {
        cloudMessagingCustomFactory.setConnectionFactory(
            axonMessagingJMSService.getConnectionFactory(jmsClientConfig.getCloudConfigName()));
      } else {
        cloudMessagingCustomFactory.setConnectionFactory(
            axonMessagingJMSService.getConnectionFactory());
      }
    }
    return cloudMessagingCustomFactory;
  }

  private static UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter(
      CloudMessagingCustomFactory cloudMessagingCustomFactory, String userName, String password) {
    UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter =
        new UserCredentialsConnectionFactoryAdapter();
    userCredentialsConnectionFactoryAdapter.setTargetConnectionFactory(
        cloudMessagingCustomFactory.getConnectionFactory());
    userCredentialsConnectionFactoryAdapter.setUsername(userName);
    userCredentialsConnectionFactoryAdapter.setPassword(password);
    return userCredentialsConnectionFactoryAdapter;
  }

  private static CachingConnectionFactory cachingConnectionFactory(
      UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter,
      int cacheSize,
      boolean reconnectOnException) {
    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
    cachingConnectionFactory.setTargetConnectionFactory(userCredentialsConnectionFactoryAdapter);
    cachingConnectionFactory.setSessionCacheSize(cacheSize);
    cachingConnectionFactory.setReconnectOnException(reconnectOnException);
    return cachingConnectionFactory;
  }

  private static JmsComponent getJmsComponent(JmsClientConfig jmsClientConfig) throws JMSException {
    JmsComponent jmsComponent = new JmsComponent();
    CloudMessagingCustomFactory cloudMessagingCustomFactory =
        cloudMessagingCustomFactory(jmsClientConfig);
    UserCredentialsConnectionFactoryAdapter userCredentialsConnectionFactoryAdapter = null;
    if (StringUtils.isNotBlank(jmsClientConfig.getUserName())
        && StringUtils.isNotBlank(jmsClientConfig.getPassword())) {
      userCredentialsConnectionFactoryAdapter =
          userCredentialsConnectionFactoryAdapter(
              cloudMessagingCustomFactory,
              jmsClientConfig.getUserName(),
              jmsClientConfig.getPassword());
      if (jmsClientConfig.isCachedClient()) {
        CachingConnectionFactory cachingConnectionFactory =
            cachingConnectionFactory(
                userCredentialsConnectionFactoryAdapter,
                jmsClientConfig.getCacheSize(),
                jmsClientConfig.isReconnectOnException());
        jmsComponent.setConnectionFactory(cachingConnectionFactory);
      } else {
        jmsComponent.setConnectionFactory(userCredentialsConnectionFactoryAdapter);
      }
    } else {
      jmsComponent.setConnectionFactory(cloudMessagingCustomFactory.getConnectionFactory());
    }
    return jmsComponent;
  }

  @Bean
  AxonMessagingJMSServiceImpl axonMessagingJMSServiceImpl() {
    return new AxonMessagingJMSServiceImpl();
  }

  @Bean(name = "MaasMqProducerComponent")
  public Component createMaasMqProducerConFactory() throws JMSException {
    return getJmsComponent(jmsProducerClientConfig.getGifClientConfig());
  }
}
