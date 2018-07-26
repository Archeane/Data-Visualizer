package algorithm;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Tooltip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This class specifies how an algorithm will expect the dataset to be. It is
 * provided as a rudimentary structure only, and does not include many of the
 * sanity checks and other requirements of the use cases. As such, you can
 * completely write your own class to represent a set of data instances as long
 * as the algorithm can read from and write into two {@link Map}
 * objects representing the name-to-label map and the name-to-location (i.e.,
 * the x,y values) map. These two are the {@link DataSet#labels} and
 * {@link DataSet#locations} maps in this class.
 *
 * @author Ritwik Banerjee
 */
public class DataSet {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    private static double minX, minY, maxX, maxY;

    private static String nameFormatCheck(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }

    private static Point2D locationOf(String locationString) {
        String[] coordinateStrings = locationString.trim().split(",");
        double xCoord = Double.parseDouble(coordinateStrings[0]);
        double yCoord = Double.parseDouble(coordinateStrings[1]);
        if(xCoord > maxX){
            maxX = xCoord;
        }
        if(xCoord < minX){
            minX = xCoord;
        }
        if(yCoord > maxY){
            maxY = yCoord;
        }
        if(yCoord < minY){
            minY = yCoord;
        }

        return new Point2D(xCoord, yCoord);
    }

    private Map<String, String>  labels;
    private Map<String, Point2D> locations;
    private List<String>         instances;

    /** Creates an empty dataset. */
    public DataSet() {
        labels = new HashMap<>();
        locations = new HashMap<>();
        instances = new ArrayList<>();
        minX = 99999999;
        maxX = -9999999;
        minY = 99999999;
        maxY = -9999999;
    }

    public Map<String, String> getLabels()     { return labels; }

    public Map<String, Point2D> getLocations() { return locations; }

    public void updateLabel(String instanceName, String newlabel) {
        if (labels.get(instanceName) == null)
            throw new NoSuchElementException();
        labels.put(instanceName, newlabel);
    }

    public void addInstance(String tsdLine) throws InvalidDataNameException {
        String[] arr = tsdLine.split("\t");
        labels.put(nameFormatCheck(arr[0]), arr[1]);
        locations.put(arr[0], locationOf(arr[2]));
        Point2D point = locationOf(arr[2]);
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
        instances.add(arr[0]);
    }

    public static DataSet fromTSDFile(Path tsdFilePath) throws IOException {
        DataSet dataset = new DataSet();
        Files.lines(tsdFilePath).forEach(line -> {
            try {
                dataset.addInstance(line);
            } catch (InvalidDataNameException e) {
                e.printStackTrace();
            }
        });
        return dataset;
    }

    public void toChartData(LineChart<Number, Number> chart, Scene scene) {
        Set<String> dataLabels = new HashSet<>(labels.values());
        chart.getData().clear();
        for (String label : dataLabels) {
            LineChart.Series<Number, Number> series = new LineChart.Series<>();
            series.setName(label);
            labels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = locations.get(entry.getKey());
                series.getData().add(new LineChart.Data<>(point.getX(), point.getY()));
            });
//            series.getNode().setId("dataLine");
            chart.getData().add(series);
        }
    }

    public double getMinX(){return minX;}
    public double getMaxX(){return maxX;}
    public double getMinY(){return minY;}
    public double getMaxY(){return maxY;}
    public List<String> getInstances(){return instances;}
    public void clear(){
        locations.clear();
        labels.clear();
        instances.clear();
    }

    public String toString(){
        String str = "";
        Iterator it = labels.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            str += (String)pair.getKey() + ", " + (String)pair.getValue() +"\n";
            // avoids a ConcurrentModificationException
        }
        return str;
    }

}
