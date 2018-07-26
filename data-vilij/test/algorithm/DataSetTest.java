package algorithm;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

public class DataSetTest {
    @Test
    public void addSingleInstance() {
        DataSet d = new DataSet();
        try {
            File f = new File(DataSetTest.class.getResource("DatasetSingleValidLineTester").toURI());
            d.fromTSDFile(f.toPath());
        }catch(Exception e){e.printStackTrace();}
    }

    /**
     *  param overflows the bounds of the the tsd textfile
     */
    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void addSingleInvalidInstance(){
        DataSet d = new DataSet();
        try {
            File f = new File(DataSetTest.class.getResource("DatasetSingleInvalidLineTester").toURI());
            d.fromTSDFile(f.toPath());
        }catch(URISyntaxException e) {
            e.printStackTrace();
        }catch(IOException f){
            f.printStackTrace();
        }
    }

    /**
     * Name of an instance is invalid
     * @throws DataSet.InvalidDataNameException
     */
    @Test(expected = DataSet.InvalidDataNameException.class)
    public void addSingleInvalidNameInstance() throws DataSet.InvalidDataNameException{
        DataSet d = new DataSet();
        try {
            File f = new File(DataSetTest.class.getResource("DatasetSingleInvalidNameTester").toURI());
            d.fromTSDFile(f.toPath());
        }catch(URISyntaxException e) {
            e.printStackTrace();
        }catch(IOException f){
            f.printStackTrace();
        }
    }


}