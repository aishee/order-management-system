package com.walmart.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerInitializer {

  /**
   * To initialize headers for each Rest end point accessible from swagger.
   *
   * @return api
   */
  @Bean
  public Docket api() {
    return new Docket(DocumentationType.OAS_30)
        .select()
        .apis(RequestHandlerSelectors.basePackage("org.springframework.boot").negate())
        .paths(PathSelectors.any())
        .build()
        .apiInfo(apiInfo())
        .useDefaultResponseMessages(false);
  }

  @Bean
  @ConditionalOnProperty(name = "runtime.context.environment", havingValue = "PROD")
  public UiConfiguration submitMethodsDisabledForProd() {
    // for Prod env, we will not be able to trigger the functionality from Swagger.
    return UiConfigurationBuilder.builder().supportedSubmitMethods(new String[] {}).build();
  }

  @SuppressWarnings("rawtypes")
  private List<VendorExtension> extensions() {
    final List<VendorExtension> vendorExtensions = new ArrayList<>();
    // these fields are recommended from Walmart.
    vendorExtensions.add(new StringVendorExtension("x-audience", "company-internal"));
    vendorExtensions.add(
        new StringVendorExtension("x-api-id", "e3c28632-038f-4f35-947d-8b909d11b81e"));
    return vendorExtensions;
  }

  private Contact contact() {
    return new Contact(
        "OMS Team", "https://groceries.asda.com/", "INTL-EC-ASDA-Groce71@email.wal-mart.com");
  }

  private ApiInfo apiInfo() {

    return new ApiInfo(
        "Integrated Marketplace",
        "Integrated Marketplace",
        "1.0",
        "https://confluence.walmart.com/pages/viewpage.action?pageId=366997288",
        contact(),
        "Apache 2.0",
        "http://www.apache.org/licenses/LICENSE-2.0",
        extensions());
  }
}
