package ui;

import actions.AppActions;
import dataprocessors.AppData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

//TODO: find out the number of serei
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
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display

    private boolean                      loadedData;

    public LineChart<Number, Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
        loadedData = false;
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
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
        scrnshotButton.setDisable(true);
    }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis      xAxis   = new NumberAxis();
        NumberAxis      yAxis   = new NumberAxis();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));

        VBox leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.3);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.3);

        Text   leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname       = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize       = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();

        HBox processButtonsBox = new HBox();
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.getChildren().add(displayButton);

        leftPanel.getChildren().addAll(leftPanelTitle, textArea, processButtonsBox);

        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);

        //TODO: change chart.css reference
        String chartCSSPath = "../../resources/chart.css";
        URL temp = getClass().getResource("ui/chart.css");
        primaryScene.getStylesheets().add(getClass().getResource("chart.css").toExternalForm());

        //read only
        CheckBox checkBox = new CheckBox("Read Only");
        checkBox.setOnAction(e ->{
            if(checkBox.isSelected()){
                textArea.setDisable(true);
            }else{
                textArea.setDisable(false);
            }
        });
        appPane.getChildren().add(checkBox);

    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        if (newValue.charAt(newValue.length() - 1) == '\n')
                            hasNewText = true;
                        newButton.setDisable(false);
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
        /*
        textArea.setPrefRowCount(10);
        textArea.scrollTopProperty().addListener(e -> {
            //if(loadedData == true){
                textArea.setScrollTop(0);
            //}
        });
        */
    }

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

    private void setDisplayButtonActions() {
        displayButton.setOnAction(event -> {
            if (hasNewText) {
                try {
                    chart.getData().clear();
                    scrnshotButton.setDisable(true);
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    dataComponent.loadData(textArea.getText());
                    dataComponent.displayData();
                    scrnshotButton.setDisable(false);
                } catch (Exception e) {
                    ErrorDialog dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    PropertyManager manager  = applicationTemplate.manager;
                    String          errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
                    String          errMsg   = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
                    String          errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
                    String          eMsg     = e.getMessage();
                    dialog.show(errTitle, errMsg + errInput+eMsg);
                }

            }
        });
    }

}
