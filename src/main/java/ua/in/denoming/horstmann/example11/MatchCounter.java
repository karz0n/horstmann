package ua.in.denoming.horstmann.example11;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang3.Validate;

public class MatchCounter implements Callable<Integer> {
    private String path;
    private String keyword;
    private ExecutorService executor;

    MatchCounter(String path, String keyword, ExecutorService executor) {
        Validate.notEmpty(path);
        Validate.notEmpty(keyword);
        Validate.notNull(executor);

        this.path = path;
        this.keyword = keyword;
        this.executor = executor;
    }

    @Override
    public Integer call() throws Exception {
        File pathFile = new File(path);

        Validate.isTrue(pathFile.exists());
        if (pathFile.isFile()) {
            return searchInFile(pathFile)
                    ? 1
                    : 0;
        }

        int count = 0;
        File[] files = pathFile.listFiles();
        Validate.notNull(files);
        if (files.length == 0) {
            return 0;
        }

        List<Future<Integer>> futures = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                MatchCounter c = new MatchCounter(file.getPath(), keyword, executor);
                futures.add(executor.submit(c));
            } else {
                if (searchInFile(file)) {
                    count++;
                }
            }
        }

        for (Future<Integer> future : futures) {
            count += future.get();
        }

        return count;
    }

    private boolean searchInFile(File file) {
        try (Scanner in = new Scanner(file)) {
            boolean found = false;
            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.contains(keyword)) {
                    found = true;
                    break;
                }
            }
            return found;
        } catch (FileNotFoundException e) {
            return false;
        }
    }
}
