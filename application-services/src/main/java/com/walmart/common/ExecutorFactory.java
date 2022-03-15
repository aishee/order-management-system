package com.walmart.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** This utility class provides Executor instance. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutorFactory {

  /**
   * @param factoryNamePattern {@code ThreadFactory naming pattern}
   * @return {@link Executor}
   */
  public static Executor newSingleThreadExecutor(String factoryNamePattern) {
    return newSingleThreadExecutor(threadFactory(factoryNamePattern));
  }

  /**
   * @param threadFactory {@link ThreadFactory}
   * @return {@link Executor}
   */
  private static Executor newSingleThreadExecutor(ThreadFactory threadFactory) {
    return Executors.newSingleThreadExecutor(threadFactory);
  }

  private static ThreadFactory threadFactory(String factoryNamePattern) {
    return new ThreadFactoryBuilder().setNameFormat(factoryNamePattern).build();
  }
}
