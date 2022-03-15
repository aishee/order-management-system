package com.walmart.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompletableFutureUtil {

  /** Util for completable future. */
  public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
    CompletableFuture<Void> allFuturesDone =
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
    return allFuturesDone.thenApply(
        v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
  }
}
