package com.walmart.common.infrastructure.webclient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

public abstract class WebClientExecutor extends WebClientLogger {
  protected ScheduledExecutorService retryScheduledExecutorService;
  protected ThreadFactory threadFactory;
  protected int threadPoolSize;
  protected ExecutorService executorService;

  protected abstract ThreadFactory getThreadFactory();

  protected abstract int getThreadPoolSize();

  protected void initExecutor() {
    this.threadFactory = getThreadFactory();
    this.threadPoolSize = getThreadPoolSize();
    this.executorService = getExecutorService();
  }

  private ExecutorService getExecutorService() {
    return Executors.newFixedThreadPool(threadPoolSize, threadFactory);
  }
}
