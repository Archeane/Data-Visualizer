package algorithm.implementation;

import algorithm.Clusterer;
import algorithm.DataSet;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomClusterer extends Clusterer {

    private DataSet dataset;

    private final int                   maxIterations;
    private final int                   updateInterval;
    private AtomicBoolean               tocontinue;
    private boolean                     isContinous;
    private ApplicationTemplate         applicationTemplate;
    private LineChart<Number,Number>    chart;
    private List<String> instances;


    public RandomClusterer(ApplicationTemplate applicationTemplate,DataSet dataset,
                           int maxIterations, int updateInterval,
                           boolean isContinous, int numberOfClusters) {
        super(numberOfClusters);

        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.isContinous = isContinous;
        this.applicationTemplate = applicationTemplate;
        this.instances = dataset.getInstances();
        this.chart = ((AppUI)(applicationTemplate.getUIComponent())).getChart();
        if(updateInterval > maxIterations || maxIterations == 0 || updateInterval == 0 || numberOfClusters <= 0 || maxIterations >= instances.size()){
            throw new IllegalArgumentException();
        }
    }

    public RandomClusterer(DataSet dataset,
                           int maxIterations, int updateInterval,
                           boolean isContinous, int numberOfClusters) {
        super(numberOfClusters);

        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.isContinous = isContinous;

        this.instances = dataset.getInstances();

        if(updateInterval > maxIterations || maxIterations == 0 || updateInterval == 0 || numberOfClusters <= 0 || maxIterations >= instances.size()){
            throw new IllegalArgumentException();
        }
    }


    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }

    public static String getName(){ return "RandomClusterer";}


    /**0. display param dataset data
     * 1. create new dataset instance
     * 2. add new instance to dataset every iteration
     * 3. remove that instance from parm dataset
     * 4. update chart with new dataset data
     */
    @Override
    public void run() {
        ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(true);
        ((AppUI)(applicationTemplate.getUIComponent())).setRunButtonDisabled(true);
        int instanceIndex = 0;

        for (int i = 1; i <= maxIterations; i++) {
            if(i % updateInterval == 0){
                ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(true);
                String label = Integer.toString((int)(Math.random()* numberOfClusters + 1));
                dataset.updateLabel(instances.get(instanceIndex), label);
                instanceIndex++;

                Platform.runLater(()->{
                    dataset.toChartData(chart,(applicationTemplate.getUIComponent()).getPrimaryScene());
                });

                if (!isContinous) {
                    try {
                        ((AppUI) (applicationTemplate.getUIComponent())).pauseAlgorithmn();
                        ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }


        }
        ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(false);
        ((AppUI)(applicationTemplate.getUIComponent())).setRunButtonDisabled(false);
    }

}
