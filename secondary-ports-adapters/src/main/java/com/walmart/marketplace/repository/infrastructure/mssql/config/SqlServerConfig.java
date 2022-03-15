package com.walmart.marketplace.repository.infrastructure.mssql.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.strati.configuration.annotation.ManagedConfiguration;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    entityManagerFactoryRef = "sqlServerEntityManagerFactory",
    transactionManagerRef = "sqlServerTransactionManager",
    basePackages = "com.walmart")
public class SqlServerConfig {

  @ManagedConfiguration SqlServerDataSourceConfiguration sqlServerDataSourceConfig;

  @Bean
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSourceProperties sqlServerDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  public DataSource sqlServerDataSource() {

    Assert.notNull(sqlServerDataSourceConfig, "No Datasource configuration supplied");

    HikariConfig config = new HikariConfig();

    config.setDriverClassName(sqlServerDataSourceConfig.getDriverClassName());
    config.setJdbcUrl(sqlServerDataSourceConfig.getJdbcUrl());
    config.setUsername(sqlServerDataSourceConfig.getUserName());
    config.setPassword(sqlServerDataSourceConfig.getPassword());
    config.setMaximumPoolSize(sqlServerDataSourceConfig.getMaxPoolSize());
    config.setMinimumIdle(sqlServerDataSourceConfig.getMinIdle());
    config.setConnectionTestQuery(sqlServerDataSourceConfig.getConnectionTestQuery());
    config.setIdleTimeout(sqlServerDataSourceConfig.getIdleTimeout());
    config.setConnectionTimeout(sqlServerDataSourceConfig.getConnectionTimeout());
    config.setValidationTimeout(sqlServerDataSourceConfig.getValidationTimeout());

    return new HikariDataSource(config);
  }

  @Bean(name = "sqlServerEntityManagerFactory")
  public LocalContainerEntityManagerFactoryBean sqlServerEntityManagerFactory(
      @Qualifier("sqlServerDataSource") DataSource sqlServerDataSource,
      EntityManagerFactoryBuilder builder) {

    return builder
        .dataSource(sqlServerDataSource)
        .packages("com.walmart")
        .persistenceUnit("sqlserver")
        .build();
  }

  @Bean
  public PlatformTransactionManager sqlServerTransactionManager(
      @Qualifier("sqlServerEntityManagerFactory") EntityManagerFactory factory) {
    return new JpaTransactionManager(factory);
  }
}
