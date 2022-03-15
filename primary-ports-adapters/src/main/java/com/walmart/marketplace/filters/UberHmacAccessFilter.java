package com.walmart.marketplace.filters;

import com.walmart.marketplace.infrastructure.gateway.uber.UberServiceConfiguration;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class UberHmacAccessFilter extends OncePerRequestFilter {

  private static final String UBER_HEADER_KEY = "X-Uber-Signature";
  @ManagedConfiguration UberServiceConfiguration uberServiceConfiguration;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return !path.startsWith("/marketplace/uber/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      FilterChain filterChain)
      throws ServletException, IOException {
    String headerAuthKey = httpServletRequest.getHeader(UBER_HEADER_KEY);
    if (headerAuthKey != null) {
      MultiReadHttpServletRequestWrapper wrappedRequest =
          new MultiReadHttpServletRequestWrapper(httpServletRequest);
      String requestData = getRequestData(wrappedRequest);
      String secretKey =
          MultiReadHttpServletRequestWrapper.encode(
              uberServiceConfiguration.getClientSecret(), requestData);
      if (secretKey != null && validateHeader(headerAuthKey, secretKey)) {
        filterChain.doFilter(wrappedRequest, httpServletResponse);
      } else {
        log.warn("Authorization encode is not matching or Secret Key is Missing");
        httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      }
    } else {
      log.warn("Authorization header is missing");
      httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
  }

  private boolean validateHeader(String headerAuthKey, String secretKey) {
    return MessageDigest.isEqual(
        headerAuthKey.getBytes(StandardCharsets.UTF_8), secretKey.getBytes(StandardCharsets.UTF_8));
  }

  private String getRequestData(MultiReadHttpServletRequestWrapper requestWrapper) {
    String requestData = requestWrapper.getBody();
    if (StringUtils.isBlank(requestData)) {
      requestData = requestWrapper.getRequestURI();
      if (StringUtils.isBlank(requestData)) {
        requestData = requestWrapper.getQueryString();
      }
    }
    return requestData;
  }
}
