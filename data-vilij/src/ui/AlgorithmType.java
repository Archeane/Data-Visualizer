package ui;

import javafx.event.EventHandler;
import javafx.scene.control.RadioButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;
import static vilij.templates.UITemplate.SEPARATOR;

public class AlgorithmType {

    private String                       algorithmnType;
    private String                       algorithmnId;
    private int                          maxIteration;
    private int                          updateInterval;
    private boolean                      isContinous;
    private int                          numOfClusters;

    private RadioButton                  radioButton;
    private ImageView                    configure;

    private ApplicationTemplate applicationTemplate;

    private AlgorithmType algo;

    public AlgorithmType(ApplicationTemplate applicationTemplate, String algorithmnType, int id){
        this.applicationTemplate = applicationTemplate;
        this.algorithmnType = algorithmnType;
        this.algorithmnId = algorithmnType+id;
        this.algo = this;

        radioButton = new RadioButton(algorithmnType+id);
        radioButton.setUserData(algorithmnType+id);

        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String configPath = String.join(SEPARATOR,
                iconsPath,
                manager.getPropertyValue(AppPropertyTypes.ALGORITHMN_CONFIG_ICON.name()));
        configure = new ImageView(new Image(getClass().getResourceAsStream(configPath)));
        configure.setFitHeight(10);
        configure.setFitWidth(10);
        configure.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                AlgConfigWindow configWindow = new AlgConfigWindow(applicationTemplate.getUIComponent().getPrimaryWindow(),
                                                                    applicationTemplate.getUIComponent().getPrimaryScene(),
                                                                    applicationTemplate, algo);
                configWindow.display("Algorithmn Configuration Window");

            }
        });
    }

    public boolean hasRunTimeConfig(){
        if(maxIteration != 0 && updateInterval != 0){
            if(algorithmnType.toUpperCase().equals("CLUSTERING") || algorithmnType.toUpperCase().equals("KMEANS")){
                if(numOfClusters != 0)
                    return true;
            }
            return true;
        }
        return false;
    }

    public void setMaxIteration(int maxIteration){this.maxIteration = maxIteration;}
    public void setUpdateInterval(int updateInterval){this.updateInterval = updateInterval;}
    public void setContinous(boolean isContinous){this.isContinous = isContinous;}
    public void setNumOfClusters(int num){this.numOfClusters = num;}
    public String getAlgorithmnType(){return algorithmnType;}
    public int getMaxIteration(){return maxIteration;}
    public int getUpdateInterval(){return updateInterval;}
    public String getAlgorithmnId(){return algorithmnId;}
    public int getNumOfClusters(){return numOfClusters;}
    public boolean isContinous(){return isContinous;}
    public RadioButton getRadioButton(){return radioButton;}
    public ImageView getConfigureImage(){return configure;}
}
