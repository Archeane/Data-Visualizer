package ui;

import actions.AppActions;
import algorithm.DataSet;
import dataprocessors.AppData;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;


/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private TextArea                     textArea;       // text area for new data input

    private VBox                         leftPanel;
    private StackPane                    rightPanel;

    private Button                       displayButton;
    private Button                       testNonContinous;

    private Label                        dataProperties;
    private ToggleGroup                  radioGroup;
    GridPane                             algChooser;        //selects classification, clustering, kmeans
    private VBox                         algChooserBox;
    private VBox                         algTypeChooser;
    private ImageView                    runButton;

    private VBox                         runAlgControls;

    private Thread                       algorithmnThread;

    private boolean                      loadedData;
    private NumberAxis                   xAxis;
    private NumberAxis                   yAxis;

    private Label                        warningLabel;
    private DataSet                      dataSet;
    private Class                        algClass;

    public LineChart<Number, Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        textArea = new TextArea();
        this.applicationTemplate = applicationTemplate;
        loadedData = false;


        //System.out.println(classifer.getOutput());
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(SEPARATOR,
                iconsPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                true);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());

        scrnshotButton.setOnAction(e -> {
            try {
                ((AppActions) (applicationTemplate.getActionComponent())).handleScreenshotRequest();
            } catch (IOException e1) {
                ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                PropertyManager manager = applicationTemplate.manager;
                String errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
                String errMsg = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
                String errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
                dialog.show(errTitle, errMsg + errInput);
            }
        });
    }

    @Override
    public void initialize() {
        layout();
        setTextAreaActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
        scrnshotButton.setDisable(true);
        dataProperties.setText("");
    }

    private void layout() {

        setLeftPanel();
        workspace = new HBox(leftPanel, setRightPanel());
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);

        primaryScene.getStylesheets().add("css/chart.css");
        primaryScene.getStylesheets().add("css/AppUI.css");

        leftPanel.setVisible(false);
    }


    private GridPane algorithmType(int numOfAlg, String algType){

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(10);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);

        GridPane pane = new GridPane();
        pane.setMaxSize(windowWidth * 0.29, windowHeight * 0.06);
        pane.setMinSize(windowWidth * 0.29, windowHeight * 0.06);
        pane.getColumnConstraints().addAll(col1,col2);

        radioGroup = new ToggleGroup();
        List<AlgorithmType> algorithmTypeList = new ArrayList<AlgorithmType>();
        for(int i = 0; i < numOfAlg; i++){
            AlgorithmType algo = new AlgorithmType(applicationTemplate, algType, i);
            algorithmTypeList.add(algo);
            algo.getRadioButton().setToggleGroup(radioGroup);
            pane.add(algo.getRadioButton(),1,i);
            pane.add(algo.getConfigureImage(),2,i);
        }

        radioGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
                if (radioGroup.getSelectedToggle() != null) {
                    leftPanel.getChildren().removeAll(runAlgControls);
                    runTimeButton();
                    String alg = radioGroup.getSelectedToggle().getUserData().toString();
                    for(int i = 0; i < algorithmTypeList.size(); i++){
                        AlgorithmType ctnAlgo = algorithmTypeList.get(i);
                        if(ctnAlgo.getAlgorithmnId().equals(alg)){
                            if(ctnAlgo.hasRunTimeConfig()){
                                runTimeButton(ctnAlgo.getAlgorithmnType(), ctnAlgo.getMaxIteration(),
                                        ctnAlgo.getUpdateInterval(), ctnAlgo.isContinous(), ctnAlgo.getNumOfClusters());
                            }
                        }
                    }
                }

            }
        });

        return pane;
    }

    /**
     * Sets the UI for a choosing algo type
     * @param isDisabled
     */
    private void setAlgChooser(boolean isDisabled){
        leftPanel.getChildren().removeAll(runAlgControls);
        if(isDisabled) {
            leftPanel.getChildren().remove(algTypeChooser);
            leftPanel.getChildren().remove(algChooserBox);
        }else {
            algTypeChooser = new VBox();
            VBox.setVgrow(algTypeChooser, Priority.ALWAYS);
            algTypeChooser.setMaxSize(windowWidth * 0.29, windowHeight * 0.05);
            algTypeChooser.setMinSize(windowWidth * 0.29, windowHeight * 0.05);

            algChooserBox = new VBox();
            VBox.setVgrow(algChooserBox, Priority.ALWAYS);
            algChooserBox.setMaxSize(windowWidth * 0.29, windowHeight * 0.15);
            algChooserBox.setMinSize(windowWidth * 0.29, windowHeight * 0.15);

            ToggleGroup algorithmnType = new ToggleGroup();

            String[] data = textArea.getText().split("\n");
            int nonLabelCount = 0;
            for(int i = 0; i < data.length; i++){
                String[] point = data[i].split("\t");
                if(!(point[1].equals("null"))){
                    nonLabelCount++;
                }
            }

            AppData dataComponent = new AppData(applicationTemplate);
            dataComponent.setAlgorithmnClasses("algorithm.implementation");
            ArrayList<String> algorithmnClasses = dataComponent.getAlgorithmnClasses("getName");

            for(int i = 0; i < algorithmnClasses.size(); i++){
                if(algorithmnClasses.get(i).equals("RandomClassifier")){
                    if(nonLabelCount >= 2){
                        RadioButton algChooserBtn = new RadioButton(algorithmnClasses.get(i));
                        algChooserBtn.setUserData(algorithmnClasses.get(i));
                        algChooserBtn.setToggleGroup(algorithmnType);
                        algTypeChooser.getChildren().add(algChooserBtn);
                    }
                }else{
                    RadioButton algChooserBtn = new RadioButton(algorithmnClasses.get(i));
                    algChooserBtn.setUserData(algorithmnClasses.get(i));
                    algChooserBtn.setToggleGroup(algorithmnType);
                    algTypeChooser.getChildren().add(algChooserBtn);
                }
            }

            algorithmnType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    if (algorithmnType.getSelectedToggle() != null) {
                        leftPanel.getChildren().removeAll(runAlgControls);

                        String alg = algorithmnType.getSelectedToggle().getUserData().toString();
                        algClass = dataComponent.dynamicLoadingAlgo("algorithm.implementation",alg);

                        PropertyManager manager = applicationTemplate.manager;
                        int numOfAlg = 0;
                        if (alg.equals("RandomClassifier")) {
                            numOfAlg = Integer.parseInt(manager.getPropertyValue(AppPropertyTypes.CLASSIFICATION.name()));
                        } else if (alg.equals("RandomClusterer")){
                            numOfAlg = Integer.parseInt(manager.getPropertyValue(AppPropertyTypes.CLUSTERING.name()));
                        } else if (alg.equals("KMeansClusterer")){
                            numOfAlg = Integer.parseInt(manager.getPropertyValue(AppPropertyTypes.KMEANS.name()));
                        }else{
                            numOfAlg = 3;
                        }
                        algChooserBox.getChildren().remove(algChooser);
                        leftPanel.getChildren().remove(algChooserBox);

                        algChooser = algorithmType(numOfAlg, alg);
                        algChooser.setVisible(true);
                        algChooserBox.getChildren().add(algChooser);
                        leftPanel.getChildren().add(algChooserBox);
                    } else {
                        algChooserBox.getChildren().remove(algChooser);
                        leftPanel.getChildren().remove(algChooserBox);
                    }
                }
            });

            leftPanel.getChildren().add(algTypeChooser);
        }
    }

    private void setLeftPanel(){
        PropertyManager manager = applicationTemplate.manager;

        leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));
        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 1);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.3);

        VBox dataSettings = new VBox(5);
        VBox.setVgrow(dataSettings, Priority.ALWAYS);
        dataSettings.setMaxSize(windowWidth * 0.29, windowHeight * 0.5);
        dataSettings.setMinSize(windowWidth * 0.29, windowHeight * 0.5);

        Text   leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname       = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize       = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        dataProperties = new Label();
        dataProperties.setFont(Font.font("Verdana", 12));

        CheckBox checkBox = new CheckBox("Done?");
        checkBox.setSelected(true);
        checkBox.setOnAction(e ->{
            if(checkBox.isSelected()){
                textArea.setDisable(true);
                AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                try {
                    dataComponent.isValidData(textArea.getText());
                    dataProperties.setText(dataComponent.dataProperties(textArea.getText()));

                } catch (Exception exception) {
                    ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
                    String errMsg = manager.getPropertyValue(AppPropertyTypes.DATA_INVALID.name());
                    String errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
                    String eMsg = exception.getMessage();
                    dialog.show(errTitle, errMsg + errInput + eMsg);
                    return;
                }
                setAlgChooser(false);
            }else{
                textArea.setDisable(false);
                setAlgChooser(true);
            }
        });

        dataSettings.getChildren().addAll(leftPanelTitle,textArea,checkBox,dataProperties);
        leftPanel.getChildren().add(dataSettings);


        displayButton = new Button("Display");
        setDisplayButtonActions();
        testNonContinous = new Button("resume");


        leftPanel.getChildren().addAll(displayButton, testNonContinous);

    }
    private StackPane setRightPanel(){
        PropertyManager manager = applicationTemplate.manager;

        Text chartTitle = new Text(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
        chartTitle.setId("chartTitle");

        //chart
        xAxis   = new NumberAxis(0, 10, 1);
        yAxis   = new NumberAxis(0, 50, 5);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));

        rightPanel = new StackPane();
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.8);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.8);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        rightPanel.getChildren().addAll(chartTitle,chart);
        warningLabel = new Label("Classification line is out of range of the chart");
        rightPanel.getChildren().add(warningLabel);
        warningLabel.setVisible(false);
        return rightPanel;
    }


    public void runTimeButton(){
        leftPanel.getChildren().removeAll(runAlgControls);

        runAlgControls = new VBox();
        runAlgControls.setPadding(new Insets(10));
        VBox.setVgrow(runAlgControls, Priority.ALWAYS);

        String configPath = getRunButtonDisabledPath(true);
        runButton = new ImageView(new Image(getClass().getResourceAsStream(configPath)));
        runButton.setFitHeight(30);
        runButton.setFitWidth(30);

        runAlgControls.getChildren().add(runButton);
        leftPanel.getChildren().add(runAlgControls);

    }

    public void runTimeButton(String algorithmn, int maxIt, int updateInt, boolean isContinous, int numOfClusters){
        leftPanel.getChildren().removeAll(runAlgControls);
        runAlgControls = new VBox();
        runAlgControls.setPadding(new Insets(10));
        VBox.setVgrow(runAlgControls, Priority.ALWAYS);

        String configPath = getRunButtonDisabledPath(false);
        runButton = new ImageView(new Image(getClass().getResourceAsStream(configPath)));
        runButton.setFitHeight(30);
        runButton.setFitWidth(30);
        runButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event){
                try {
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    try {
                        dataComponent.loadData(((AppUI) (applicationTemplate.getUIComponent())).getTextArea().getText());
                    }catch(Exception e){e.printStackTrace();}
                    dataComponent.displayData();

                    List<Double> axisVal = dataComponent.getAxisMax();

                    xAxis.setAutoRanging(false);
                    yAxis.setAutoRanging(false);

                    xAxis.setLowerBound(axisVal.get(0)-1);
                    yAxis.setLowerBound(axisVal.get(1)-1);
                    xAxis.setUpperBound(axisVal.get(2)+1);
                    yAxis.setUpperBound(axisVal.get(3)+1);

                    Class algClass = dataComponent.dynamicLoadingAlgo("algorithm.implementation", algorithmn);
                        if(algClass != null){
                            try {
                                dataSet = DataSet.fromTSDFile(((AppActions)(applicationTemplate.getActionComponent())).getDataFilePath());

                                Constructor<?> dogConstructor = algClass.getConstructor(ApplicationTemplate.class, DataSet.class, Integer.TYPE,Integer.TYPE
                                        ,Boolean.TYPE,Integer.TYPE);
                                Object currentAlgorithmn = dogConstructor.newInstance(applicationTemplate , dataSet, maxIt,updateInt,isContinous, numOfClusters);

                                algorithmnThread = new Thread((Runnable)currentAlgorithmn);
                                algorithmnThread.setDaemon(true);
                                algorithmnThread.start();
                            }catch(Exception e){e.printStackTrace();}
                        }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        if(!isContinous) {
            Button resume = new Button("resume");
            runAlgControls.getChildren().add(resume);

            resume.setOnAction(event -> {
                try{
                    synchronized (algorithmnThread){
                        algorithmnThread.notifyAll();
                    }
                    scrnshotButton.setDisable(true);
                }catch(Exception e){e.printStackTrace();}
            });
        }

        runAlgControls.getChildren().add(runButton);
        leftPanel.getChildren().add(runAlgControls);

    }
    public void pauseAlgorithmn(){
        try {
            synchronized (algorithmnThread) {
                scrnshotButton.setDisable(false);
                algorithmnThread.wait();
            }
        }catch (Exception e){e.printStackTrace();}
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        if (newValue.charAt(newValue.length() - 1) == '\n')
                            newButton.setDisable(false);
                            saveButton.setDisable(false);
                    } else {
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });

        textArea.setPrefRowCount(10);
        textArea.scrollTopProperty().addListener((observable, oldValue, newValue) -> {
            if(loadedData){
                textArea.setScrollTop(0);
            }
        });

    }


    public void setDataProperties(String str){dataProperties.setText(str);}
    public void setTextAreaText(String str){
        textArea.setText(str);
    }
    public String getCurrentText() { return textArea.getText(); }
    public void setLoadingState(boolean state){
        loadedData = state;
    }
    public void setNewButtonState(boolean disabled){
        newButton.setDisable(disabled);
    }
    public void setSaveButtonState(boolean disabled){
        saveButton.setDisable(disabled);
    }
    public void setLeftPanelVisibility(boolean isDisabled){leftPanel.setVisible(isDisabled);}
    public TextArea getTextArea(){return textArea;}
    public void setScrnshotButtonDisabled(boolean isDisabled){scrnshotButton.setDisable(isDisabled);}
    public String getRunButtonDisabledPath(boolean isDisabled){
        if(isDisabled){
            PropertyManager manager = applicationTemplate.manager;
            String iconsPath = SEPARATOR + String.join(SEPARATOR,
                    manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                    manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
            String configPath = String.join(SEPARATOR,
                    iconsPath,
                    manager.getPropertyValue(AppPropertyTypes.ALGORITHMN_INACTIVE_RUN_ICON.name()));
            return configPath;
        }
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String configPath = String.join(SEPARATOR,
                iconsPath,
                manager.getPropertyValue(AppPropertyTypes.ALGORITHMN_RUN_ICON.name()));
        return configPath;
    }
    public void setRunButtonDisabled(boolean isDisabled){
        String configPath = getRunButtonDisabledPath(isDisabled);
        runButton.setImage(new Image(getClass().getResourceAsStream(configPath)));
        runButton.setDisable(isDisabled);
    }
    public NumberAxis getxAxis(){return xAxis;}
    public NumberAxis getyAxis(){return yAxis;}

    public Thread getAlgorithmnThread(){return algorithmnThread;}

    public void setDisplayButtonActions(){
        displayButton.setOnAction(event ->{

            AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
            dataComponent.clear();
            try {
                dataComponent.loadData(((AppUI) (applicationTemplate.getUIComponent())).getTextArea().getText());
            }catch(Exception e){e.printStackTrace();}
            dataComponent.displayData();

            List<Double> axisVal = dataComponent.getAxisMax();

            xAxis.setAutoRanging(false);
            yAxis.setAutoRanging(false);

            xAxis.setLowerBound(axisVal.get(0)-1);
            yAxis.setLowerBound(axisVal.get(1)-1);
            xAxis.setUpperBound(axisVal.get(2)+1);
            yAxis.setUpperBound(axisVal.get(3)+1);

            if(algClass != null){
                try {
                    dataSet = DataSet.fromTSDFile(((AppActions)(applicationTemplate.getActionComponent())).getDataFilePath());

                    Constructor<?> dogConstructor = algClass.getConstructor(ApplicationTemplate.class, DataSet.class, Integer.TYPE,Integer.TYPE
                                                                            ,Boolean.TYPE,Integer.TYPE);
                    Object currentAlgorithmn = dogConstructor.newInstance(applicationTemplate , dataSet, 50,20,true, 3);

                    Thread cluster = new Thread((Runnable)currentAlgorithmn);
                    cluster.setDaemon(true);
                    cluster.start();

                    //TODO: delete after testing
                    testNonContinous.setOnAction(e ->{
                        try{
                            synchronized (algorithmnThread){
                                algorithmnThread.notifyAll();
                                System.out.println("Notified clusterer");
                            }
                            scrnshotButton.setDisable(true);
                        }catch(Exception f){f.printStackTrace();}
                    });
                }catch(Exception e){e.printStackTrace();}
            }


        });
    }

    public Label getWarningLabel(){return warningLabel;}

}
