package algorithm.implementation;

import algorithm.DataSet;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class RandomClassifierTest {


    @Test
    public void run() {
        DataSet dataSet = new DataSet();
        try{
            File f = new File(KMeansClustererTest.class.getResource("sample.tsd").toURI());
            dataSet = DataSet.fromTSDFile(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RandomClassifier cluster = new RandomClassifier(dataSet, 50, 50, true, 0);
        Thread t = new Thread(cluster);
        t.start();
    }

    /**
     * updateInterval is higher than maxIterations, which is an illegal argument as updateInterval cannot be higher than max number of iteration times
     */
    @Test(expected = IllegalArgumentException.class)
    public void runTest() {
        DataSet dataSet = new DataSet();
        try{
            File f = new File(RandomClassifierTest.class.getResource("sample.tsd").toURI());
            dataSet = DataSet.fromTSDFile(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RandomClassifier cluster = new RandomClassifier(dataSet, 30, 60, true, 0);
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
        RandomClassifier cluster = new RandomClassifier(dataSet, 30, 0, true, 0);
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
        RandomClassifier cluster = new RandomClassifier(dataSet, 0, 30, true, 0);
        Thread t = new Thread(cluster);
        t.start();
    }

    /**
     * number of cluseters for randomclassifier should always be 0
     */
    @Test(expected = IllegalArgumentException.class)
    public void runOverflowCluster() {
        DataSet dataSet = new DataSet();
        try{
            File f = new File(RandomClassifier.class.getResource("sample.tsd").toURI());
            dataSet = DataSet.fromTSDFile(f.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        RandomClassifier cluster = new RandomClassifier(dataSet, 2, 1, true, 2);
        Thread t = new Thread(cluster);
        t.start();
    }


}