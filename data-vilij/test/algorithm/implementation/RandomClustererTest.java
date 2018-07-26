package algorithm.implementation;

import algorithm.DataSet;
import algorithm.DataSetTest;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
public class RandomClustererTest {


    /**
     * updateInterval is higher than maxIterations, which is an illegal argument as updateInterval cannot be higher than max number of iteration times
     */
    @Test(expected = IllegalArgumentException.class)
    public void runTest() {
        DataSet dataSet = new DataSet();
        try{
            File f = new File(KMeansClustererTest.class.getResource("sample.tsd").toURI());
            dataSet = DataSet.fromTSDFile(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RandomClusterer cluster = new RandomClusterer(dataSet, 30, 60, true, 2);
        Thread t = new Thread(cluster);
        t.start();
    }

    /**
     * updateInterval is 0, should not be a valid argument
     */
    @Test(expected = IllegalArgumentException.class)
    public void runZeroParam() {
        DataSet dataSet = new DataSet();
        try{
            File f = new File(KMeansClustererTest.class.getResource("sample.tsd").toURI());
            dataSet = DataSet.fromTSDFile(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RandomClusterer cluster = new RandomClusterer(dataSet, 30, 0, true, 2);
        Thread t = new Thread(cluster);
        t.start();
    }

    /**
     * maxIterations being 0 is not a valid argument
     */
    @Test(expected = IllegalArgumentException.class)
    public void runZeroMax() {
        DataSet dataSet = new DataSet();
        try{
            File f = new File(KMeansClustererTest.class.getResource("sample.tsd").toURI());
            dataSet = DataSet.fromTSDFile(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RandomClusterer cluster = new RandomClusterer(dataSet, 0, 30, true, 2);
        Thread t = new Thread(cluster);
        t.start();
    }

    /**
     * maxiterations should not exceed the length of tsd input files
     */
    @Test(expected = IllegalArgumentException.class)
    public void runOverflowParam() {
        DataSet dataSet = new DataSet();
        try{
            File f = new File(KMeansClustererTest.class.getResource("sample.tsd").toURI());
            dataSet = DataSet.fromTSDFile(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RandomClusterer cluster = new RandomClusterer(dataSet, 60, 0, true, 2);
        Thread t = new Thread(cluster);
        t.start();
    }


}