package com.walmart.marketplace.dwh.config;

import com.walmart.common.config.BaseCCMConfig;
import io.strati.configuration.annotation.Configuration;
import io.strati.configuration.annotation.DefaultValue;
import io.strati.configuration.annotation.PostRefresh;
import io.strati.configuration.context.ConfigurationContext;
import io.strati.configuration.listener.ChangeLog;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Configuration(configName = "DWHConfig")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DWHConfig extends BaseCCMConfig {

  @DefaultValue.String("informatica.prod.ukgr-informatica.ukgrsps.prod.walmart.com")
  private String ipAddress;

  @DefaultValue.Int(22)
  private int port;

  @DefaultValue.String("/app/etl_app/Informatica/tenants/ukgr/nfsshared/etl/ubereats/orderhistory/")
  private String orderHistoryUploadPath;

  @DefaultValue.String("/app/etl_app/Informatica/tenants/ukgr/nfsshared/etl/ubereats/downtime/")
  private String downTimeUploadPath;

  @DefaultValue.String("etlappsvc")
  private String userName;

  @DefaultValue.String("/root/.ssh/id_rsa")
  private String rsaPath;

  @DefaultValue.String("/tmp/UberReports/")
  private String localDownloadDirectory;

  @Override
  protected String getConfigName() {
    return "DWHConfig";
  }

  @PostRefresh
  @Override
  protected void configChanged(
      String configName, List<ChangeLog> changeLogs, ConfigurationContext context) {
    super.configChanged(configName, changeLogs, context);
  }
}
