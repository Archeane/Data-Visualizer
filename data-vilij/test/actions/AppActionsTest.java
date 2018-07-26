package actions;

import dataprocessors.TSDProcessor;
import org.junit.Test;
import org.junit.BeforeClass;
import ui.AppUITester;
import ui.DataVisualizer;
import vilij.templates.ApplicationTemplate;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class AppActionsTest {

    public static DataVisualizer dataVisualizer;
    public static ApplicationTemplate applicationTemplate;
    public static AppUITester appUI;
    public static AppActions actions;

    @BeforeClass
    public static void setUp(){
        Thread thread = new Thread(() -> {
            JFXPanel panel = new JFXPanel();
            Platform.runLater(new Runnable(){
                @Override
                public void run() {
                    Stage testStage = new Stage();
                    dataVisualizer = new DataVisualizer();
                    applicationTemplate = dataVisualizer;
                    applicationTemplate.init();
                    dataVisualizer.start(testStage);
                    appUI = new AppUITester(testStage, applicationTemplate);
                    actions = new AppActions(applicationTemplate);
                }
            });
        });
        thread.start();
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * save request completed
     */
    @Test//(expected = TSDProcessor.InvalidDataNameException.class)
    public void handleValidSaveRequest() {
        //DataVisualizerTester.launch(DataVisualizerTester.class);
        System.out.println("at the end");
        String testerString = "@Instance1\tlabel1\t1.5,2.2\n" +
                "@Instance2\tlabel1\t1.8,3\n" +
                "@Instance3\tlabel1\t2.1,2.9\n" +
                "@Instance4\tlabel2\t10,9.4";
        appUI.setTextAreaText(testerString);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                actions.handleSaveRequest();
            }
        });
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}