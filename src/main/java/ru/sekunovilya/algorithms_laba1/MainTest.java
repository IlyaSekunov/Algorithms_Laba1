package ru.sekunovilya.algorithms_laba1;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.pow;

/**
 * Class that tests 3 algorithms to search target element within N * M matrix.
 * N <= M, N = 2 ^ 13, M = 2 ^ x. Class can use two ways of table generation:
 * 1) A[i][j] = (N / M * i + j) * 2, target = 2 * n + 1;
 * 2) A[i][j] = (N / M * i * j) * 2, target = 16 * n + 1;
 * To change table generation just pass the appropriate function that generates table (firstTableGeneration(n, m) or
 * secondTableGeneration(n, m)) to function enrichLineChart(ListChart, TableGenerator). To make it more useful I created
 * functional interface TableGenerator which has method generateTable(m, n) which returns table and according target.
 * **/
public class MainTest extends Application {
    /**
     * Count of tests that applies to each algorithm on each M = 2 ^ x
     * **/
    private final static int TESTS_COUNT = 50;

    /**
     * Launch start() method JavaFX application
     * **/
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the JavaFX application in order to draw graphics which reflects algorithms tests
     * **/
    @Override
    public void start(Stage stage) {
        LineChart<Number, Number> lineChart = createLineChart();
        enrichLineChart(lineChart, MainTest::secondGenerationTable);
        drawChart(lineChart, stage);
    }

    /**
     * Generates table where table[i][j] = (n / m * i + j) * 2, target = 2 * n + 1
     * @param m row count
     * @param n column count
     * **/
    private static TableGeneration firstTableGeneration(int m, int n) {
        int[][] table = new int[m][n];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                table[i][j] = (n / m * i + j) * 2;
            }
        }
        return new TableGeneration(table, 2 * n + 1);
    }

    /**
     * Generates table where table[i][j] = (n / m * i * j) * 2, target = 16 * n + 1
     * @param m row count
     * @param n column count
     * **/
    private static TableGeneration secondGenerationTable(int m, int n) {
        int[][] table = new int[m][n];
        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                table[i][j] = n / m * i * j * 2;
            }
        }
        return new TableGeneration(table, 16 * n + 1);
    }

    /**
     * Apply binary search to each row trying to find target.
     * @param table table M * N
     * @param target element is going to be searched within table
     * **/
    public static Pair<Integer, Integer> binarySearchAlgorithm(int[][] table, int target) {
        for (int row = 0; row < table.length; ++row) {
            int possibleIndex = Arrays.binarySearch(table[row], target);
            if (possibleIndex >= 0) {
                return new Pair<>(row, possibleIndex);
            }
        }
        return new Pair<>(-1, -1);
    }

    /**
     * Finds the position of target in the table. Starting from upper right corner.
     * If current element is less than target -> go to the next row,
     * if current element is greater than target -> go to the previous column
     * if current element equals to target -> return [row, column].
     * @param table table M * N
     * @param target element is going to be searched within table
     * **/
    public static Pair<Integer, Integer> ladderSearchAlgorithm(int[][] table, int target) {
        int currentRow = 0, currentColumn = table[0].length - 1;
        while (currentRow < table.length && currentColumn >= 0) {
            if (table[currentRow][currentColumn] < target) {
                ++currentRow;
            } else if (table[currentRow][currentColumn] > target) {
                --currentColumn;
            } else {
                return new Pair<>(currentRow, currentColumn);
            }
        }
        return new Pair<>(-1, -1);
    }

    /**
     * Main logic like at ladder algorithm, but now current column is found using exponent search.
     * @param table table M * N
     * @param target element is going to be searched within table
     * **/
    public static Pair<Integer, Integer> ladderExponentSearchAlgorithm(int[][] table, int target) {
        int currentRow = 0, currentColumn = table[0].length - 1;
        while (currentRow < table.length && currentColumn >= 0) {
            if (table[currentRow][currentColumn] < target) {
                ++currentRow;
            } else if (table[currentRow][currentColumn] > target) {
                currentColumn = exponentSearch(table[currentRow], currentColumn, target);
                if (currentColumn < 0) currentColumn = -(currentColumn + 2);
            } else {
                return new Pair<>(currentRow, currentColumn);
            }
        }
        return new Pair<>(-1, -1);
    }

    /**
     * Returns the index of target element in array if array contains that element.
     * Else returns (-(insertion point) - 1) where insertion point is a place where target element should take place
     * @param arr array within target should be searched
     * @param start start index left which target should be searched
     * @param target element is going to be searched
     * **/
    private static int exponentSearch(int[] arr, int start, int target) {
        if (arr.length == 0) return -1;
        if (arr[start] == target) return start;
        int right = start, left = right;
        for (int i = 1; left >= 0; i *= 2) {
            if (arr[left] > target) {
                right = left - 1;
                left -= i;
            } else {
                return Arrays.binarySearch(arr, left, right + 1, target);
            }
        }
        return Arrays.binarySearch(arr, 0, max(0, right) + 1, target);
    }

    /**
     * Fixes start time in nanoseconds, starts algorithm, fixes end time in nanoseconds and returns
     * difference between end and start
     * @param algorithm algorithm to be tested
     * **/
    private static long testAlgorithm(Algorithm algorithm) {
        long start = System.nanoTime();
        algorithm.start();
        long end = System.nanoTime();
        return end - start;
    }

    /**
     * Creates line chart
     * **/
    private static LineChart<Number, Number> createLineChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Time in microseconds");
        xAxis.setLabel("M");
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Algorithms");
        return lineChart;
    }

    /**
     * Draws line chart
     * */
    private static void drawChart(LineChart<Number, Number> lineChart, Stage stage) {
        stage.setScene(new Scene(lineChart, 800, 600));
        stage.show();
    }

    /**
     * Enriches line chart with 3 lines which called 'Binary search', 'Ladder search' and 'Ladder exponent search'.
     * Table generator is used to generate table and receive target
     * Test each algorithm and add information at chart. To make results more honest each algorithm is tested
     * TESTS_COUNT = 25 times and then takes average value from all test.
     * @param lineChart line chart to be enriched with points
     * @param tableGenerator table generator which provides table and target
     * **/
    private static void enrichLineChart(LineChart<Number, Number> lineChart, TableGenerator tableGenerator) {
        int n = (int)pow(2, 13);
        List<Pair<Number, Number>> binarySearchData = new ArrayList<>();
        List<Pair<Number, Number>> ladderSearchData = new ArrayList<>();
        List<Pair<Number, Number>> ladderExponentSearchData = new ArrayList<>();
        long[] binarySearchTimeResults = new long[TESTS_COUNT];
        long[] ladderSearchTimeResults = new long[TESTS_COUNT];
        long[] ladderExponentSearchTimeResults = new long[TESTS_COUNT];
        for (int i = 0; i <= 13; ++i) {
            int m = (int)pow(2, i);
            TableGeneration tableGeneration = tableGenerator.generateTable(m, n);
            for (int test = 0; test < TESTS_COUNT; ++test) {
                binarySearchTimeResults[test] = testAlgorithm(() -> binarySearchAlgorithm(tableGeneration.table, tableGeneration.target));
                ladderSearchTimeResults[test] = testAlgorithm(() -> ladderSearchAlgorithm(tableGeneration.table, tableGeneration.target));
                ladderExponentSearchTimeResults[test] = testAlgorithm(() -> ladderExponentSearchAlgorithm(tableGeneration.table, tableGeneration.target));
            }
            long binarySearchAverageResult = Arrays.stream(binarySearchTimeResults).sum() / TESTS_COUNT;
            long ladderSearchAverageResult = Arrays.stream(ladderSearchTimeResults).sum() / TESTS_COUNT;
            long ladderExponentSearchAverageResult = Arrays.stream(ladderExponentSearchTimeResults).sum() / TESTS_COUNT;
            binarySearchData.add(new Pair<>(m, binarySearchAverageResult));
            ladderSearchData.add(new Pair<>(m, ladderSearchAverageResult));
            ladderExponentSearchData.add(new Pair<>(m, ladderExponentSearchAverageResult));
        }
        lineChart.getData().add(getOneLineAtChart("Binary search", binarySearchData));
        lineChart.getData().add(getOneLineAtChart("Ladder search", ladderSearchData));
        lineChart.getData().add(getOneLineAtChart("Ladder exponent search", ladderExponentSearchData));
    }

    /**
     * Creates new line at chart from list of points where key = x value and value = y value with name passed as parameter
     * @param name name of the line on a chart
     * @param data list of points to be added to a chart. Pair.key = time in microseconds, Pair.value = M (2 ^ x)
     * **/
    private static XYChart.Series<Number, Number> getOneLineAtChart(String name, List<Pair<Number, Number>> data) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (Pair<Number, Number> dot : data) {
            series.getData().add(new XYChart.Data<>(dot.getKey(), dot.getValue()));
        }
        return series;
    }
}