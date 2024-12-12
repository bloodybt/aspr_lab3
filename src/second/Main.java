package second;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the directory path:");
        String directoryPath;

        try {
            directoryPath = reader.readLine();
        } catch (IOException e) {
            System.err.println("Error reading input. Exiting.");
            return;
        }

        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Invalid directory path. Exiting.");
            return;
        }

        System.out.println("Scanning directory: " + directoryPath);

        List<File> textFiles = findTextFiles(directory);
        if (textFiles.isEmpty()) {
            System.out.println("No text files found in the directory.");
            return;
        }

        System.out.println("\nUsing Work Stealing:");
        long startStealing = System.currentTimeMillis();
        int totalCharactersStealing = countCharactersWorkStealing(textFiles);
        long endStealing = System.currentTimeMillis();
        System.out.println("Total characters: " + totalCharactersStealing);
        System.out.println("Time: " + (endStealing - startStealing) + " ms");

        System.out.println("\nUsing Work Dealing:");
        long startDealing = System.currentTimeMillis();
        int totalCharactersDealing = countCharactersWorkDealing(textFiles);
        long endDealing = System.currentTimeMillis();
        System.out.println("Total characters: " + totalCharactersDealing);
        System.out.println("Time: " + (endDealing - startDealing) + " ms");
    }

    private static List<File> findTextFiles(File directory) {
        List<File> textFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    textFiles.add(file);
                }
            }
        }
        return textFiles;
    }

    private static int countCharactersWorkStealing(List<File> textFiles) throws InterruptedException, ExecutionException {
        ForkJoinPool pool = new ForkJoinPool();
        CharacterCountTask task = new CharacterCountTask(textFiles);
        return pool.invoke(task);
    }

    private static class CharacterCountTask extends RecursiveTask<Integer> {
        private static final int THRESHOLD = 5;
        private final List<File> files;

        CharacterCountTask(List<File> files) {
            this.files = files;
        }

        @Override
        protected Integer compute() {
            if (files.size() <= THRESHOLD) {
                return computeDirectly();
            } else {
                int mid = files.size() / 2;
                CharacterCountTask leftTask = new CharacterCountTask(files.subList(0, mid));
                CharacterCountTask rightTask = new CharacterCountTask(files.subList(mid, files.size()));
                invokeAll(leftTask, rightTask);
                return leftTask.join() + rightTask.join();
            }
        }

        private Integer computeDirectly() {
            int total = 0;
            for (File file : files) {
                try {
                    int count = countCharactersInFile(file);
                    System.out.println("File: " + file.getName() + ", Characters: " + count);
                    total += count;
                } catch (IOException e) {
                    System.err.println("Error reading file: " + file.getName());
                }
            }
            return total;
        }
    }

    private static int countCharactersWorkDealing(List<File> textFiles) throws InterruptedException, ExecutionException {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Integer>> futures = new ArrayList<>();

        for (File file : textFiles) {
            futures.add(executor.submit(() -> {
                try {
                    int count = countCharactersInFile(file);
                    System.out.println("File: " + file.getName() + ", Characters: " + count);
                    return count;
                } catch (IOException e) {
                    System.err.println("Error reading file: " + file.getName());
                    return 0;
                }
            }));
        }

        int total = 0;
        for (Future<Integer> future : futures) {
            total += future.get();
        }

        executor.shutdown();
        return total;
    }

    private static int countCharactersInFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            int count = 0;
            int c;
            while ((c = reader.read()) != -1) {
                count++;
            }
            return count;
        }
    }
}
