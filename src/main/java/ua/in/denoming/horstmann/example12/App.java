package ua.in.denoming.horstmann.example12;

import org.apache.commons.lang3.Validate;

import java.util.concurrent.CompletableFuture;

public class App {
    public static void main(String[] args) {
        try {
            //
            // Create already completed future
            //
            Validate.isTrue(
                CompletionFutureFactory
                    .createAlreadyCompleted("Hello world")
                    .get()
                    .equals("Hello world")
            );


            //
            // Create delayed completion future
            //
            Validate.isTrue(
                CompletionFutureFactory
                    .createDelayed(1000)
                    .get() >= 1000
            );

            //
            // Apply consistently of completion futures
            //
            Validate.isTrue(
                CompletionFutureFactory
                    .applyConsistently("Hello", "World")
                    .get()
                    .equals("Hello World")
            );

            //
            // Compose completion futures
            //
            CompletionFutureFactory
                .createAlreadyCompleted("1000")
                .thenCompose(s -> {
                    Integer delay = Integer.valueOf(s);
                    return CompletionFutureFactory.createDelayed(delay);
                });

            //
            // Combine completion futures
            //
            Validate.isTrue(
                CompletionFutureFactory.combineSimple(
                    CompletableFuture.supplyAsync(() -> 2),
                    CompletableFuture.supplyAsync(() -> 2)
                )
                .get() == 4
            );

            CompletableFuture<Void> future = CompletionFutureFactory.forkJoin(
                CompletionFutureFactory.createDelayed(500),
                CompletionFutureFactory.createDelayed(1000),
                CompletionFutureFactory.createDelayed(1500)
            );
            long now = System.nanoTime();
            future.get();
            long diff = System.nanoTime() - now;
            Validate.isTrue(diff > 0);

            //
            // Experiment with error handling
            //
            CompletionFutureFactory.withErrorHandler("Error Text", e -> {
                Validate.isTrue(e.getMessage().contains("Error Text"));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
