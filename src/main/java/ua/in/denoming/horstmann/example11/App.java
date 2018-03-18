package ua.in.denoming.horstmann.example11;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class App {
    public static void main(String[] args) {
        App app = new App();
        app.execute();
    }

    private void execute() {
        try (Scanner in = new Scanner(System.in)) {
            String path = getPath(in);
            String keyword = getKeyword(in);

            ExecutorService executor = Executors.newCachedThreadPool();
            MatchCounter counter = new MatchCounter(path, keyword, executor);
            Future<Integer> future = executor.submit(counter);
            try {
                int count = future.get();
                System.out.println("Number of found files: " + count);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                executor.shutdown();
            }
        }
    }

    private String getPath(Scanner in) {
        while (true) {
            System.out.print("Enter path: ");
            String path = in.nextLine();
            if (Files.exists(Paths.get(path))) {
                return path;
            } else {
                System.out.println("Path not exists");
            }
        }
    }

    private String getKeyword(Scanner in) {
        System.out.print("Enter keyword: ");
        return in.nextLine();
    }
}
