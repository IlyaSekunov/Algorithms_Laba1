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

import static java.lang.Math.pow;

public class MainTest extends Application {
    @Override
    public void start(Stage stage) {
        LineChart<Number, Number> lineChart = createLineChart();
        enrichLineChart(lineChart, MainTest::secondGenerationTable);
        drawChart(lineChart, stage);
    }

    public static void main(String[] args) {
        launch(args);
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
     * Test each algorithm and add information at chart
     * **/
    private static void enrichLineChart(LineChart<Number, Number> lineChart, TableGenerator tableGenerator) {
        int n = (int)pow(2, 13);
        List<Pair<Number, Number>> binarySearchData = new ArrayList<>();
        List<Pair<Number, Number>> ladderSearchData = new ArrayList<>();
        List<Pair<Number, Number>> ladderExponentSearchData = new ArrayList<>();
        for (int i = 0; i < 13; ++i) {
            int m = (int)pow(2, i);
            TableGeneration tableGeneration = tableGenerator.generateTable(m, n);
            binarySearchData.add(new Pair<>(m, testAlgorithm(() -> binarySearchAlgorithm(tableGeneration.table, tableGeneration.target))));
            ladderSearchData.add(new Pair<>(m, testAlgorithm(() -> ladderSearchAlgorithm(tableGeneration.table, tableGeneration.target))));
            ladderExponentSearchData.add(new Pair<>(m, testAlgorithm(() -> ladderExponentSearchAlgorithm(tableGeneration.table, tableGeneration.target))));
        }
        lineChart.getData().add(getOneLineAtChart("Binary search", binarySearchData));
        lineChart.getData().add(getOneLineAtChart("Ladder search", ladderSearchData));
        lineChart.getData().add(getOneLineAtChart("Ladder exponent search", ladderExponentSearchData));
    }

    /**
     * Creates line chart
     * **/
    private static LineChart<Number, Number> createLineChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Time in microseconds");
        xAxis.setLabel("m");
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Algorithms");
        return lineChart;
    }

    /**
     * Creates new line at chart from list of points where key = x value and value = y value with name passed as parameter
     * **/
    private static XYChart.Series<Number, Number> getOneLineAtChart(String name, List<Pair<Number, Number>> data) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (Pair<Number, Number> dot : data) {
            series.getData().add(new XYChart.Data<>(dot.getKey(), dot.getValue()));
        }
        return series;
    }

    /**
     * Fixes start time in nanoseconds, starts algorithm, fixes end time in nanoseconds and returns
     * difference between end and start
     * **/
    private static long testAlgorithm(Algorithm algorithm) {
        long start = System.nanoTime();
        algorithm.start();
        long end = System.nanoTime();
        return end - start;
    }

    /**
     * Generates table where table[i][j] = (n / m * i + j) * 2, target = 2 * n + 1
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
     * **/
    public static Pair<Integer, Integer> ladderExponentSearchAlgorithm(int[][] table, int target) {
        int currentRow = 0, currentColumn = table[0].length - 1;
        while (currentRow < table.length && currentColumn >= 0) {
            if (table[currentRow][currentColumn] < target) {
                ++currentRow;
            } else if (table[currentRow][currentColumn] > target) {
                currentColumn = exponentSearch(table[currentRow], currentColumn, target);
                if (table[currentRow][currentColumn] > target) --currentColumn;
            } else {
                return new Pair<>(currentRow, currentColumn);
            }
        }
        return new Pair<>(-1, -1);
    }

    private static int exponentSearch(int[] arr, int start, int target) {
        int right = start, left = right;
        for (int i = 1; left >= 0; i *= 2) {
            if (arr[left] > target) {
                right = left - 1;
                left -= i;
            } else {
                int index = Arrays.binarySearch(arr, left, right, target);
                return index >= 0 ? index : -(index + 1);
            }
        }
        return 0;
    }
}