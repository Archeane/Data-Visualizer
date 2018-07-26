package dataprocessors;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Tooltip;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    public static class DuplicateInstanceException extends Exception{
        private static final String NAME_ERROR_MSG = "All data instance names must not have duplicates.";

        public DuplicateInstanceException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }


    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;

    private Map<Point2D, String> dataNames;
    private LineChart.Series<Number,Number> lineSeries;

    private List<String> instanceNames;

    private double maxX, maxY, minX, minY;

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
        dataNames = new HashMap<>();
        instanceNames = new ArrayList<>();
    }

    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws DuplicateInstanceException{
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();

        String[] lines = tsdString.split("\n");
        double averageYValue = 0.0;
        minX = 99999999;
        maxX = -9999999;
        minY = 99999999;
        maxY = -9999999;
        for(int i = 0; i < lines.length; i++){
            try {
                String[] list = lines[i].split("\t");
                String name = checkedname(list[0]);
                checkedDuplicates(name);
                String label = list[1];
                String[] pair = list[2].split(",");
                Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                dataLabels.put(name, label);
                dataPoints.put(name, point);
                dataNames.put(point, name);

                if(point.getX() > maxX){
                    maxX = point.getX();
                }
                if(point.getX() < minX){
                    minX = point.getX();
                }
                if(point.getY() > maxY){
                    maxY = point.getY();
                }
                if(point.getY() < minY){
                    minY = point.getY();
                }

            }catch(Exception e){
                instanceNames.clear();
                errorMessage.setLength(0);
                errorMessage.append(e.getClass().getSimpleName()).append(":").append(e.getMessage()).append("error is text area line ").append(i+1);
                hadAnError.set(true);
                break;
            }
        }
        lineSeries = new LineChart.Series<>();

        if (errorMessage.length() > 0)
            throw new dataprocessors.TSDProcessor.DuplicateInstanceException(errorMessage.toString());

        instanceNames.clear();
    }

    void toChartData(LineChart<Number, Number> chart, Scene scene) {
        chart.getData().add(lineSeries);
        Set<String> labels = new HashSet<>(dataLabels.values());

        for (String label : labels) {
            LineChart.Series<Number, Number> series = new LineChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new LineChart.Data<>(point.getX(), point.getY()));
            });
            chart.getData().add(series);
            for (LineChart.Data<Number, Number> d : series.getData()) {
                double yVal = (double)d.getYValue();
                double xVal = (double)d.getXValue();
                Point2D point = new Point2D(xVal,yVal);
                String name = dataNames.get(point);
                Tooltip.install(d.getNode(), new Tooltip(name));
                //Adding class on hover
                d.getNode().setOnMouseEntered(event -> scene.setCursor(Cursor.HAND));

                //Removing class on exit
                d.getNode().setOnMouseExited(event -> scene.setCursor(Cursor.DEFAULT));
            }
        }
    }

    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    public double getMinX(){return minX;}
    public double getMaxX(){return maxX;}
    public double getMinY(){return minY;}
    public double getMaxY(){return maxY;}

    public void checkedDuplicates(String name) throws DuplicateInstanceException{
        for(String str : instanceNames){
            if(str.equals(name)){
                throw new DuplicateInstanceException(name);
            }
        }
        instanceNames.add(name);
    }

    public String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }
}
