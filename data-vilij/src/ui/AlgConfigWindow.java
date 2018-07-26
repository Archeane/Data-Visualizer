package ui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import static vilij.settings.PropertyTypes.IS_WINDOW_RESIZABLE;

public class AlgConfigWindow extends UITemplate {

    ApplicationTemplate applicationTemplate;
    private Scene currentScene;
    private Stage stage;
    private GridPane pane;
    private Scene scene;

    private AlgorithmType algorithm;

    public AlgConfigWindow(Stage primaryStage, Scene currentScene, ApplicationTemplate applicationTemplate, AlgorithmType algo){
        super(primaryStage,applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        this.currentScene = currentScene;
        stage = primaryStage;
        algorithm = algo;
    }

    public void display(String title){
        setWindow(title);

        GridPane workspace = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(20);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(25);
        RowConstraints row2 = new RowConstraints();
        row2.setPercentHeight(50);
        RowConstraints row3 = new RowConstraints();
        row3.setPercentHeight(25);
        workspace.getRowConstraints().addAll(row1,row2,row3);
        workspace.getColumnConstraints().addAll(col1,col2);

        HBox titleContainer = new HBox();
        Text panelTitle = new Text("Algorithmn Run Configuration");
        panelTitle.setFont(Font.font("Verdana", 40));
        titleContainer.getChildren().add(panelTitle);
        titleContainer.setPadding(new Insets(20));

        Text maxIteration = new Text("Max Iteration");
        maxIteration.setFont(Font.font("Verdana", 20));
        Text update = new Text("Update Interval");
        update.setFont(Font.font("Verdana", 20));
        Text run = new Text("Continous Run?");
        run.setFont(Font.font("Verdana", 20));
        workspace.add(maxIteration,1,0);
        workspace.add(update,1,1);
        workspace.add(run, 1, 2);

        TextField maxIt = new TextField();
        maxIt.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    maxIt.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        if(algorithm.getMaxIteration() != 0){
            maxIt.setText(Integer.toString(algorithm.getMaxIteration()));
        }
        TextField updateInt = new TextField();
        updateInt.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    updateInt.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        if(algorithm.getUpdateInterval() != 0){
            updateInt.setText(Integer.toString(algorithm.getUpdateInterval()));
        }
        CheckBox isContinous = new CheckBox();
        isContinous.setSelected(algorithm.isContinous());

        workspace.add(maxIt, 2,0);
        workspace.add(updateInt, 2,1);
        workspace.add(isContinous, 2,2);

        TextField numOfClusters = new TextField();;
        if(algorithm.getAlgorithmnType().toUpperCase().contains("CLUSTER")){
            Text clusters = new Text("Number of Clusters");
            clusters.setFont(Font.font("Verdana", 20));
            numOfClusters.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue,
                                    String newValue) {
                    if (!newValue.matches("\\d*")) {
                        updateInt.setText(newValue.replaceAll("[^\\d]", ""));
                    }
                }
            });
            if(algorithm.getNumOfClusters() != 0){
                numOfClusters.setText(Integer.toString(algorithm.getNumOfClusters()));
            }
            workspace.add(clusters,1,3);
            workspace.add(numOfClusters,2,3);
        }


        Button confirm = new Button("Confirm Changes");
        confirm.setAlignment(Pos.CENTER);
        confirm.setOnAction(event -> {
            String x = maxIt.getText();
            String y = updateInt.getText();

            int max = 0;
            if(x != null) {
                try {
                    max = Integer.parseInt(maxIt.getText());
                    algorithm.setMaxIteration(max);
                }catch(Exception e){}
            }
            int up = 0;
            if(y != null) {
                try {
                    up = Integer.parseInt(updateInt.getText());
                    algorithm.setUpdateInterval(up);
                }catch(Exception e){}
            }
            if(algorithm.getAlgorithmnType().toUpperCase().equals("CLUSTERING")){
                if(numOfClusters.getText() != null){
                    try {
                        int temp3 = Integer.parseInt(numOfClusters.getText());
                        algorithm.setNumOfClusters(temp3);
                    }catch(Exception e){}
                }
            }
            algorithm.setContinous(isContinous.isSelected());

            //TODO: change this back if runTimeButton doesn't display after inputting config settings
            /*
            if(x != null && y != null && algorithm.getRadioButton().isSelected()){
                ((AppUI)(applicationTemplate.getUIComponent())).runTimeButton(algorithm.getAlgorithmnType(), max,up,isContinous.isSelected(), algorithm.getNumOfClusters());
            }
            */
            stage.setScene(currentScene);
        });

        pane.getRowConstraints().addAll(row1,row2);
        pane.add(titleContainer,1,0);
        pane.add(workspace,0,1);
        pane.add(confirm, 1,2);
    }

    protected void setWindow(String title) {
        stage.setTitle(title);
        stage.setResizable(applicationTemplate.manager.getPropertyValueAsBoolean(IS_WINDOW_RESIZABLE.name()));

        pane = new GridPane();
        pane.setMinWidth(windowWidth);
        pane.setMinHeight(windowHeight);

        scene = windowWidth < 1 || windowHeight < 1 ? new Scene(workspace)
                : new Scene(pane, windowWidth, windowHeight);
        stage.getIcons().add(logo);
        stage.setScene(scene);
        stage.show();
    }
}
