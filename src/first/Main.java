package first;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введіть кількість рядків: ");
        int rows = scanner.nextInt();
        System.out.print("Введіть кількість стовпців: ");
        int cols = scanner.nextInt();

        int[][] matrix = generateMatrix(rows, cols);
        printMatrix(matrix, 100, 100);

        int firstElement = matrix[0][0];
        System.out.println("Перший згенерований елемент: " + firstElement);

        System.out.println("\nWork Stealing Approach:");
        long startStealing = System.currentTimeMillis();
        int resultStealing = findMinWorkStealing(matrix, firstElement);
        long endStealing = System.currentTimeMillis();
        System.out.println("Result: " + resultStealing);
        System.out.println("Time: " + (endStealing - startStealing) + " ms");

        System.out.println("\nWork Dealing Approach:");
        long startDealing = System.currentTimeMillis();
        int resultDealing = findMinWorkDealing(matrix, firstElement);
        long endDealing = System.currentTimeMillis();
        System.out.println("Result: " + resultDealing);
        System.out.println("Time: " + (endDealing - startDealing) + " ms");
    }

    private static int[][] generateMatrix(int rows, int cols) {
        Random random = new Random();
        int[][] matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextInt(1000);
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix, int maxRows, int maxCols) {
        for (int i = 0; i < Math.min(matrix.length, maxRows); i++) {
            for (int j = 0; j < Math.min(matrix[i].length, maxCols); j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static int findMinWorkStealing(int[][] matrix, int firstElement) throws InterruptedException, ExecutionException {
        ForkJoinPool pool = new ForkJoinPool();
        MinTask task = new MinTask(matrix, 0, matrix.length, firstElement);
        return pool.invoke(task);
    }

    private static class MinTask extends RecursiveTask<Integer> {
        private static final int THRESHOLD = 100;
        private final int[][] matrix;
        private final int startRow;
        private final int endRow;
        private final int thresholdValue;

        MinTask(int[][] matrix, int startRow, int endRow, int firstElement) {
            this.matrix = matrix;
            this.startRow = startRow;
            this.endRow = endRow;
            this.thresholdValue = firstElement * 2;
        }

        @Override
        protected Integer compute() {
            if (endRow - startRow <= THRESHOLD) {
                return computeDirectly();
            } else {
                int mid = (startRow + endRow) / 2;
                MinTask leftTask = new MinTask(matrix, startRow, mid, thresholdValue);
                MinTask rightTask = new MinTask(matrix, mid, endRow, thresholdValue);
                invokeAll(leftTask, rightTask);
                return Math.min(leftTask.join(), rightTask.join());
            }
        }

        private Integer computeDirectly() {
            int min = Integer.MAX_VALUE;
            for (int i = startRow; i < endRow; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    if (matrix[i][j] >= thresholdValue) {
                        min = Math.min(min, matrix[i][j]);
                    }
                }
            }
            return min == Integer.MAX_VALUE ? -1 : min;
        }
    }

    private static int findMinWorkDealing(int[][] matrix, int firstElement) throws InterruptedException, ExecutionException {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int rowsPerTask = (int) Math.ceil((double) matrix.length / numThreads);
        Future<Integer>[] futures = new Future[numThreads];
        int thresholdValue = firstElement * 2;

        for (int i = 0; i < numThreads; i++) {
            int startRow = i * rowsPerTask;
            int endRow = Math.min(startRow + rowsPerTask, matrix.length);
            futures[i] = executor.submit(() -> {
                int min = Integer.MAX_VALUE;
                for (int row = startRow; row < endRow; row++) {
                    for (int col = 0; col < matrix[row].length; col++) {
                        if (matrix[row][col] >= thresholdValue) {
                            min = Math.min(min, matrix[row][col]);
                        }
                    }
                }
                return min;
            });
        }

        int globalMin = Integer.MAX_VALUE;
        for (Future<Integer> future : futures) {
            globalMin = Math.min(globalMin, future.get());
        }

        executor.shutdown();
        return globalMin == Integer.MAX_VALUE ? -1 : globalMin;
    }
}
