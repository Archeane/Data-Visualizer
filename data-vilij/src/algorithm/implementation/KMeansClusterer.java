package algorithm.implementation;

import algorithm.Clusterer;
import algorithm.DataSet;
import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private DataSet dataset;
    private List<Point2D> centroids;

    private final int                   maxIterations;
    private final int                   updateInterval;
    private final boolean               isContinous;
    private final AtomicBoolean         tocontinue;

    private ApplicationTemplate         applicationTemplate;
    private LineChart<Number,Number>    chart;

    public KMeansClusterer(ApplicationTemplate applicationTemplate,
                           DataSet dataset,
                           int maxIterations,
                           int updateInterval,
                           boolean isContinous, int numberOfClusters) {
        super(numberOfClusters);
        this.applicationTemplate = applicationTemplate;
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.isContinous = isContinous;
        this.tocontinue = new AtomicBoolean(false);
        chart = ((AppUI)(applicationTemplate.getUIComponent())).getChart();
    }

    public KMeansClusterer(DataSet dataset, int maxIterations,
                           int updateInterval, boolean isContinous,
                           int numberOfClusters) throws IllegalArgumentException{
        super(numberOfClusters);
        if(updateInterval > maxIterations || maxIterations == 0 || updateInterval == 0){
            throw new IllegalArgumentException();
        }
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.isContinous = isContinous;
        this.tocontinue = new AtomicBoolean(false);
    }

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }

    public static String getName(){return "KMeansClusterer";}

    @Override
    public void run() {
        ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(true);
        ((AppUI)(applicationTemplate.getUIComponent())).setRunButtonDisabled(true);
        int iteration = 0;
        while (iteration++ < maxIterations) {
            if(iteration % updateInterval == 0) {
                ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(true);
                initializeCentroids();
                assignLabels();
                recomputeCentroids();

                Platform.runLater(() -> {
                    dataset.toChartData(chart, (applicationTemplate.getUIComponent()).getPrimaryScene());
                });

                if (!isContinous) {
                    try {
                        ((AppUI) (applicationTemplate.getUIComponent())).pauseAlgorithmn();
                        ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        ((AppUI)(applicationTemplate.getUIComponent())).setRunButtonDisabled(false);
        ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(false);
    }

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            //TODO: fix " java.lang.IllegalArgumentException: bound must be positive" on next line
            int i = r.nextInt(instanceNames.size());
            if(!chosen.isEmpty()) {
                while (chosen.contains(instanceNames.get(i)))
                    ++i;
            }
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance      = Double.MAX_VALUE;
            int    minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

}