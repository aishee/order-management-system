package com.walmart.oms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(
    scanBasePackages = {"com.walmart.*", "io.strati.*"},
    exclude = {DataSourceAutoConfiguration.class})
@EnableAsync(proxyTargetClass = true)
public class OrderManagementSystemApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrderManagementSystemApplication.class, args);
  }
}
