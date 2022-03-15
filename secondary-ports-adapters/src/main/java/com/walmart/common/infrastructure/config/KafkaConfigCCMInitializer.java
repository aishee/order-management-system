package com.walmart.common.infrastructure.config;

import com.walmart.fms.infrastructure.integration.kafka.config.FmsKafkaProducerConfig;
import com.walmart.oms.infrastructure.configuration.OmsKafkaProducerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class KafkaConfigCCMInitializer {

  @Bean
  @Primary
  public OmsKafkaProducerConfig omsKafkaProducerConfig() {
    return CcmConfigurationUtil.getCcmConfigurationModel(OmsKafkaProducerConfig.class);
  }

  @Bean
  @Primary
  public FmsKafkaProducerConfig fmsKafkaProducerConfig() {
    return CcmConfigurationUtil.getCcmConfigurationModel(FmsKafkaProducerConfig.class);
  }
}
