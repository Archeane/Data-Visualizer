package ui;

import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

public class AppUITester extends UITemplate {

    ApplicationTemplate applicationTemplate;

    private TextArea        textArea;


    public AppUITester(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;

        initialize();
    }

    @Override
    public void initialize(){
        layout();
    }
    public void layout(){
        textArea = new TextArea();

    }

    public void setTextAreaText(String text){textArea.setText(text);}
    public TextArea getTextArea(){return textArea;}
    public ApplicationTemplate getApplicationTemplate(){return applicationTemplate;}



}
