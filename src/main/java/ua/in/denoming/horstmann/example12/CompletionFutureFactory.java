package ua.in.denoming.horstmann.example12;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;

import java.util.function.Consumer;

public class CompletionFutureFactory {
    public static CompletableFuture<String> createAlreadyCompleted(String value) {
        return CompletableFuture.completedFuture(value);
    }

    public static CompletableFuture<Long> createDelayed(int delay) {
        CompletableFuture<Long> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() -> {
            long start = System.nanoTime();
            Thread.sleep(delay);
            completableFuture.complete(System.nanoTime() - start);
            return null;
        });

        return completableFuture;
    }

    public static CompletableFuture<String> applyConsistently(String... values) {
        if (values.length == 0) {
            return createAlreadyCompleted("<empty>");
        }

        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(
            () -> values[0]
        );

        for (int i = 1; i < values.length; i++) {
            String v = values[i];
            completableFuture = completableFuture.thenApply(s -> s + " " + v);
        }

        return completableFuture;
    }

    public static CompletableFuture<Integer> combineSimple(CompletableFuture<Integer> first, CompletableFuture<Integer> second) {
        return first.thenCombine(second, (s1, s2) -> s1 + s2);
    }

    public static CompletableFuture<Void> forkJoin(CompletableFuture<?>... futures) {
        return CompletableFuture.allOf(futures);
    }

    public static void withErrorHandler(String errorText, Consumer<? super Throwable> handler) {
        CompletableFuture
            .supplyAsync(() -> {
                throw new RuntimeException(errorText);
            })
            .handle((s, t) -> {
                handler.accept(t);
                return null;
            });
    }
}
