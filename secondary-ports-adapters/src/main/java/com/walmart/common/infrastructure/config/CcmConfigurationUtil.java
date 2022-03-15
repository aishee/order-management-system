package com.walmart.common.infrastructure.config;

import io.strati.StratiServiceProvider;
import io.strati.configuration.ConfigType;
import io.strati.libs.logging.kafka.common.config.ConfigException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcmConfigurationUtil {

  public static <T> T getCcmConfigurationModel(Class<T> classObject) {

    return StratiServiceProvider.getInstance()
        .getConfigurationService()
        .orElseThrow(
            () ->
                new ConfigException(
                    "Application can't be booted as required CCM service not found."))
        .getConfiguration(classObject, ConfigType.SIMPLE, false);
  }
}
