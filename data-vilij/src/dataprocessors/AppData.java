package dataprocessors;

import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor processor;
    private ApplicationTemplate applicationTemplate;

    private ArrayList<Class> classes;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath){
        File file = dataFilePath.toFile();
        try {
            Scanner input = new Scanner(file);
            String str = input.useDelimiter("\\A").next();
            String[] what = str.split("\n");
            if(what.length >= 10){
                ErrorDialog temp = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                temp.show( "Data overflow", "The loaded file has "+what.length+" lines. The first 10 is loaded");
            }
            processor.processString(str);
            ((AppUI)(applicationTemplate.getUIComponent())).setTextAreaText(str);
            String dataProperties = "The source path "+dataFilePath.toString();
            dataProperties += dataProperties(str);
            ((AppUI)(applicationTemplate.getUIComponent())).setDataProperties(dataProperties);

            ((AppUI)(applicationTemplate.getUIComponent())).setLoadingState(true);
            ((AppUI)(applicationTemplate.getUIComponent())).setLeftPanelVisibility(true);
            ((AppUI)(applicationTemplate.getUIComponent())).getTextArea().setDisable(true);

            input.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(Exception f){
            ErrorDialog dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            PropertyManager manager  = applicationTemplate.manager;
            String          errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
            String          errMsg   = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
            String          errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
            String          eMsg     = f.getMessage();
            dialog.show(errTitle, errMsg + errInput+eMsg);
        }
    }

    public void loadData(String dataString)throws Exception{
        processor.processString(dataString);

    }

    @Override
    public void saveData(Path dataFilePath) {
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            writer.write(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public boolean isValidData(String data) throws dataprocessors.TSDProcessor.DuplicateInstanceException{
        processor.processString(data);
        return true;
    }

    public String dataProperties(String data){
        String[] lines = data.split("\n");
        String dataprop = "";
        dataprop += "\n"+ "Number of instances: "+lines.length + "\n";
        dataprop += "Label Names: "+ "\n";
        List<String> labels = new ArrayList<String>();
        for(int i = 0; i < lines.length; i++){
            String[] point = lines[i].split("\t");
            String label = point[1];
            if(labels.size() == 0){
                labels.add(label);
                dataprop += "     -"+label + "\n ";
            }else {
                boolean found = false;
                for(int j = 0; j < labels.size(); j++){
                    if(labels.get(j).equals(label)){
                        found = true;
                    }
                }
                if(!found) {
                    labels.add(label);
                    dataprop += "    -" +label + "\n ";
                }
            }
        }
        dataprop += "Number of labels: " + labels.size() + "\n";
        return dataprop;

    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart(),
                ((applicationTemplate.getUIComponent())).getPrimaryScene());
    }

    public List<Double> getAxisMax(){
        List<Double> values = new ArrayList<Double>();
        values.add(processor.getMinX());
        values.add(processor.getMinY());
        values.add(processor.getMaxX());
        values.add(processor.getMaxY());
        return values;
    }

    public void setAlgorithmnClasses(String packageName){
        ArrayList classes=new ArrayList();
        try{
            // Get a File object for the package
            File directory=null;
            try {
                directory=new File(Thread.currentThread().getContextClassLoader().getResource(packageName.replace('.', '/')).getFile());
            } catch(NullPointerException x) {
                System.out.println("Nullpointer");
                throw new ClassNotFoundException("algorithm.implementation does not appear to be a valid package");
            }
            if(directory.exists()) {
                // Get the list of the files contained in the package
                String[] files=directory.list();
                for(int i=0; i<files.length; i++) {
// we are only interested in .class files
                    if(files[i].endsWith(".class")) {
                        // removes the .class extension
                        classes.add(Class.forName(packageName+'.'+files[i].substring(0, files[i].length()-6)));
                    }
                }
            } else {
                System.out.println("Directory does not exist");
                throw new ClassNotFoundException(packageName+" does not appear to be a valid package");
            }
            Class[] classesA=new Class[classes.size()];
            classes.toArray(classesA);
            this.classes = classes;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getAlgorithmnClasses(String getNameMethodName){
        ArrayList<String> c = new ArrayList<String>();
        try {
            for (int i = 0; i < classes.size(); i++) {
                Method m = this.classes.get(i).getMethod(getNameMethodName);
                String str = (String)m.invoke(null);
                c.add(str);
            }
        }catch(Exception e){e.printStackTrace();}
        return c;
    }

    public ArrayList<Class> getClasses(){return classes;}
    public Class dynamicLoadingAlgo(String packageName, String algorithmn){
        ArrayList classes=new ArrayList();
        try{
            // Get a File object for the package
            File directory=null;
            try {
                directory=new File(Thread.currentThread().getContextClassLoader().getResource(packageName.replace('.', '/')).getFile());
            } catch(NullPointerException x) {
                System.out.println("Nullpointer");
                throw new ClassNotFoundException("algorithm.implementation does not appear to be a valid package");
            }
            if(directory.exists()) {
                // Get the list of the files contained in the package
                String[] files=directory.list();
                for(int i=0; i<files.length; i++) {
// we are only interested in .class files
                    if(files[i].endsWith(".class")) {
                        // removes the .class extension
                        classes.add(Class.forName(packageName+'.'+files[i].substring(0, files[i].length()-6)));
                    }
                }
            } else {
                System.out.println("Directory does not exist");
                throw new ClassNotFoundException(packageName+" does not appear to be a valid package");
            }
            Class[] classesA=new Class[classes.size()];
            classes.toArray(classesA);
            this.classes = classes;

        } catch (Exception e) {
            e.printStackTrace();
        }

        for(int i = 0; i < classes.size(); i++){
            Class ctnClass = (Class)classes.get(i);
            try{
                Method m = ctnClass.getMethod("getName");
                String str = (String)m.invoke(null);
                if(str.equals(algorithmn)){
                    return (Class)classes.get(i);
                }
            }catch(NoSuchMethodException f){
                System.out.println("getName method was not found in class "+ctnClass.toString());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
