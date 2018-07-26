package algorithm.implementation;

import algorithm.Classifier;
import algorithm.DataSet;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ProgressBar;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;
    private ApplicationTemplate applicationTemplate;

    private int maxIterations;
    private int updateInterval;
    private LineChart.Series<Number, Number> lineSeries;
    private LineChart<Number, Number> chart;
   // private List<Point2D> linePoints;

    // currently, this value does not change after instantiation
    private AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public static String getName(){
        return "RandomClassifier";
    }

    public RandomClassifier(ApplicationTemplate applicationTemplate , DataSet dataset ,
                            int maxIterations ,
                            int updateInterval,
                            boolean tocontinue, int numOfClustersInvalid) throws IllegalArgumentException{

        if(updateInterval > maxIterations || maxIterations == 0 || updateInterval == 0 || numOfClustersInvalid > 0){
            throw new IllegalArgumentException();
        }

        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.applicationTemplate = applicationTemplate;
        this.lineSeries = new LineChart.Series<>();
        chart = ((AppUI)(applicationTemplate.getUIComponent())).getChart();
    }

    public RandomClassifier(DataSet dataset ,
                            int maxIterations ,
                            int updateInterval,
                            boolean tocontinue, int numOfClustersInvalid) throws IllegalArgumentException{

        if(updateInterval > maxIterations || maxIterations == 0 || updateInterval == 0 || numOfClustersInvalid >0){
            throw new IllegalArgumentException();
        }

        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.lineSeries = new LineChart.Series<>();
    }

    @Override
    public void run() {
        ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(true);
        ((AppUI)(applicationTemplate.getUIComponent())).setRunButtonDisabled(true);
        for (int currIteration = 1; currIteration <= maxIterations; currIteration++) {
            if (currIteration % updateInterval == 0) {
                ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(true);
                double xMax = ((AppUI) applicationTemplate.getUIComponent()).getxAxis().getUpperBound();
                double xMin = ((AppUI) applicationTemplate.getUIComponent()).getxAxis().getLowerBound();
                double yMax = ((AppUI) applicationTemplate.getUIComponent()).getyAxis().getUpperBound();
                double yMin = ((AppUI) applicationTemplate.getUIComponent()).getyAxis().getLowerBound();

                double a = new Double(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
                double b = 5.0;
                double c = new Double(RAND.nextDouble() * 100).intValue();

                if (a == 0 && b == 0) {
                    // Remove previous line and throw error
                    Platform.runLater(()->{
                            System.out.println("INSIDE generated num is 0 run(), changing label");
                            ((AppUI) applicationTemplate.getUIComponent()).getWarningLabel().setVisible(true);
                    });
                    try {
                        Thread.sleep(1300);
                    } catch (Exception ex) {
                        System.out.println("OH MY GOD");
                    }

                } else {
                    ((AppUI) applicationTemplate.getUIComponent()).getWarningLabel().setVisible(false);
                }

                ArrayList<Point2D> linePoints = new ArrayList<>();

                double slopeX = -b / a;
                double constantX = c / a;
                double slopeY = a / b;
                double constantY = c / b;

                double xforyMax = getXY(slopeX, constantX, yMax);   // X-coordinate for yMax
                double xforyMin = getXY(slopeX, constantX, yMin);   // Y-coordinate for yMin
                double yforxMax = getXY(slopeY, constantY, xMax);
                double yforxMin = getXY(slopeY, constantY, xMin);

                System.out.println(xMin + "<" + xforyMax + "<" + xMax + "||" + yMin + ", " + yforxMax + "<" + yMax);

                if (xMin <= xforyMax && xforyMax <= xMax)
                    linePoints.add(new Point2D(xforyMax, yMax));
                if (xMin <= xforyMin && xforyMin <= xMax)
                    linePoints.add(new Point2D(xforyMin, yMin));
                if (yMin <= yforxMax && yforxMax <= yMax)
                    linePoints.add(new Point2D(xMax, yforxMax));
                if (yMin <= yforxMin && yforxMin <= yMax)
                    linePoints.add(new Point2D(xMin, yforxMin));

                System.out.println(linePoints.toString());

                if (linePoints.isEmpty() || linePoints.size() == 1) {
                    // Remove previous line and throw error
                    Platform.runLater(()->{
                        System.out.println("INSIDE linepoints is empty run(), changing label");
                        ((AppUI) applicationTemplate.getUIComponent()).getWarningLabel().setVisible(true);
                        chart.getData().remove(lineSeries);
                    });
                    try {
                        Thread.sleep(1300);
                    } catch (Exception ex) {
                        System.out.println("OH MY GOD");
                    }
                } else {
                    Platform.runLater(() ->{
                        if (!chart.getData().contains(lineSeries))
                            chart.getData().add(lineSeries);

                        lineSeries.getData().clear();

                        lineSeries.getData().add(new LineChart.Data<>(linePoints.get(0).getX(),
                                linePoints.get(0).getY()));
                        lineSeries.getData().add(new LineChart.Data<>(linePoints.get(1).getX(),
                                linePoints.get(1).getY()));

                        lineSeries.getNode().setId("classifierLine");
                        for (int i = 0; i < lineSeries.getData().size(); i++) {
                            lineSeries.getData().get(i).getNode().setId("classifierSymbol");
                        }
                    });
                    try {
                        Thread.sleep(1300);
                    } catch (Exception ex) {
                        System.out.println("OH MY GOD");
                    }

                }
            if (!tocontinue.get()) {
                //TODO: replae after testing
                ((AppUI) (applicationTemplate.getUIComponent())).pauseAlgorithmn();
                ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(false);
            }
            }
            System.out.printf("Iteration number %d: ", currIteration);
            System.out.println();
        }
        ((AppUI)(applicationTemplate.getUIComponent())).setScrnshotButtonDisabled(false);
        ((AppUI)(applicationTemplate.getUIComponent())).setRunButtonDisabled(false);
    }

    public double getXY(double slope, double constant, double coord) {
        return slope * coord + constant;
    }

}
