package com.walmart.oms;

import com.walmart.marketplace.filters.JustEatsHmacAccessFilter;
import com.walmart.marketplace.filters.UberHmacAccessFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@Configuration
@EnableWebSecurity
public class RESTSecurityConfig extends WebSecurityConfigurerAdapter {

  @Bean
  public UberHmacAccessFilter hmacAccessFilter() {
    return new UberHmacAccessFilter();
  }

  @Bean
  public JustEatsHmacAccessFilter justEatsHmacAccessFilter() {
    return new JustEatsHmacAccessFilter();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf()
        .disable()
        .authorizeRequests()
        .antMatchers("/**")
        .permitAll()
        .and()
        .addFilterBefore(hmacAccessFilter(), FilterSecurityInterceptor.class)
        .addFilterBefore(justEatsHmacAccessFilter(), FilterSecurityInterceptor.class);
  }
}
