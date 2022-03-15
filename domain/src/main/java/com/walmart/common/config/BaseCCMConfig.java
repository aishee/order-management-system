package com.walmart.common.config;

import io.strati.configuration.context.ConfigurationContext;
import io.strati.configuration.listener.ChangeLog;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseCCMConfig {

  protected abstract String getConfigName();

  /**
   * Config changes logging.
   *
   * @param configName name of config file
   * @param changeLogs property change information
   * @param context configuration context name based on level
   */
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    if (getConfigName().equalsIgnoreCase(configName)) {
      changeLogs.forEach(
          changeLog ->
              log.info(
                  "configName={},changeType={},key={},oldValue={},newValue={}",
                  configName,
                  changeLog.getChangeType(),
                  changeLog.getKey(),
                  changeLog.getOldValue(),
                  changeLog.getNewValue()));
    }
  }
}
