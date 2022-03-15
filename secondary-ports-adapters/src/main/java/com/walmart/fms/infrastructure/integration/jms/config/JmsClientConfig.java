package com.walmart.fms.infrastructure.integration.jms.config;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class JmsClientConfig {
  private String serverUrl;
  private String cloudConfigName;
  private int cacheSize = 10;
  private String userName;
  private String password;
  private boolean encryptedPassword;
  private boolean reconnectOnException;
  private boolean cachedClient;
  private int connAttemptTimeout;
  private int reconnAttemptTimeout;

  public String getCloudConfigName() {
    return cloudConfigName;
  }

  public void setCloudConfigName(String cloudConfigName) {
    this.cloudConfigName = cloudConfigName;
  }

  public int getCacheSize() {
    return cacheSize;
  }

  public void setCacheSize(int cacheSize) {
    this.cacheSize = cacheSize;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public boolean isEncryptedPassword() {
    return encryptedPassword;
  }

  public boolean isReconnectOnException() {
    return reconnectOnException;
  }

  public void setReconnectOnException(boolean reconnectOnException) {
    this.reconnectOnException = reconnectOnException;
  }

  public boolean isCachedClient() {
    return cachedClient;
  }

  public void setCachedClient(boolean cachedClient) {
    this.cachedClient = cachedClient;
  }

  public String getServerUrl() {
    return serverUrl;
  }

  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  public void setEncryptedPassword(boolean encryptedPassword) {
    this.encryptedPassword = encryptedPassword;
  }

  public int getConnAttemptTimeout() {
    return connAttemptTimeout;
  }

  public void setConnAttemptTimeout(int connAttemptTimeout) {
    this.connAttemptTimeout = connAttemptTimeout;
  }

  public int getReconnAttemptTimeout() {
    return reconnAttemptTimeout;
  }

  public void setReconnAttemptTimeout(int reconnAttemptTimeout) {
    this.reconnAttemptTimeout = reconnAttemptTimeout;
  }
}
