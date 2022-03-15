package com.walmart.marketplace.repository.infrastructure.mssql.config;

import com.walmart.common.config.BaseCCMConfig;
import io.strati.configuration.annotation.Configuration;
import io.strati.configuration.annotation.DefaultValue;
import io.strati.configuration.annotation.PostRefresh;
import io.strati.configuration.annotation.Property;
import io.strati.configuration.context.ConfigurationContext;
import io.strati.configuration.listener.ChangeLog;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/** CCM Managed Bean for Uber Gateway Configurations Maps to CCM config "UberConfig" */
@Configuration(configName = "SqlServerDataSourceConfig")
@Getter
@Setter
@ToString
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SqlServerDataSourceConfiguration extends BaseCCMConfig {

  @DefaultValue.String("jdbc:sqlserver://127.0.0.1;databaseName=OMSCORE")
  @Property(propertyName = "jdbcUrl")
  private String jdbcUrl;

  @DefaultValue.String("SA")
  @Property(propertyName = "userName")
  private String userName;

  @DefaultValue.String("")
  @Property(propertyName = "password")
  private String password;

  @DefaultValue.String("com.microsoft.sqlserver.jdbc.SQLServerDriver")
  @Property(propertyName = "driverClassName")
  private String driverClassName;

  @DefaultValue.Int(20)
  @Property(propertyName = "maxPoolSize")
  private Integer maxPoolSize;

  @DefaultValue.Int(2)
  @Property(propertyName = "minIdle")
  private Integer minIdle;

  @DefaultValue.String("SELECT 1")
  @Property(propertyName = "connectionTestQuery")
  private String connectionTestQuery;

  @DefaultValue.Long(600000)
  @Property(propertyName = "idleTimeout")
  private Long idleTimeout;

  @DefaultValue.Long(5000)
  @Property(propertyName = "validationTimeout")
  private Long validationTimeout;

  @DefaultValue.Long(5000)
  @Property(propertyName = "connectionTimeout")
  private Long connectionTimeout;

  @Override
  protected String getConfigName() {
    return "SqlServerDataSourceConfig";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
