package com.walmart.marketplace.filters;

import com.walmart.marketplace.infrastructure.gateway.justeats.config.JustEatsServiceConfiguration;
import io.strati.configuration.annotation.ManagedConfiguration;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JustEatsHmacAccessFilter extends OncePerRequestFilter {

  private static final String JUST_EATS_HEADER_KEY = "Authorization";
  @ManagedConfiguration private JustEatsServiceConfiguration justEatsServiceConfiguration;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return !path.startsWith("/marketplace/justeats/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      FilterChain filterChain)
      throws ServletException, IOException {
    String headerAuthKey = httpServletRequest.getHeader(JUST_EATS_HEADER_KEY);
    // if JustEats request doesn't contain Mandatory header for Authorization.
    if (headerAuthKey == null) {
      String errorMessage =
          String.format("Authorization header %s is missing", JUST_EATS_HEADER_KEY);
      log.warn(errorMessage);
      httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
      return;
    }

    MultiReadHttpServletRequestWrapper wrappedRequest =
        new MultiReadHttpServletRequestWrapper(httpServletRequest);
    // Generate Secret key header based on Private API Key from Secrets.
    String secretKey = justEatsServiceConfiguration.getClientSecret();
    if (secretKey == null) {
      String errorMessage = "Secret Key is missing for JustEats Web hook authorization processing.";
      log.warn(errorMessage);
      httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
      return;
    }

    if (!validateHeader(headerAuthKey, secretKey)) {
      String errorMessage =
          String.format(
              "Authorization encode is not matching with JustEats request header key=%s header value=%s",
              JUST_EATS_HEADER_KEY, headerAuthKey);
      log.warn(errorMessage);
      httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
      return;
    }

    filterChain.doFilter(wrappedRequest, httpServletResponse);
  }

  private boolean validateHeader(String headerAuthKey, String secretKey) {
    return MessageDigest.isEqual(
        headerAuthKey.getBytes(StandardCharsets.UTF_8), secretKey.getBytes(StandardCharsets.UTF_8));
  }
}
