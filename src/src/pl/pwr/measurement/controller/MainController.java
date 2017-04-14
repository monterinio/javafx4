package src.pl.pwr.measurement.controller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import src.pl.pwr.measurement.data.ConnectionData;
import src.pl.pwr.measurement.data.Data;
import src.pl.pwr.measurement.data.Strings;
import src.pl.pwr.measurement.util.ConnectionUtil;
import src.pl.pwr.measurement.util.GenerateDataUtil;
import src.pl.pwr.measurement.util.SaveLoadUtil;
import src.pl.pwr.measurement.util.WindowUtil;

public class MainController implements Initializable {

    private Data data;
    private ConnectionData connectionData;

    @FXML
    private MenuItem connectionSettingsItem;
    @FXML
    private MenuItem closeItem;
    @FXML
    private MenuItem aboutItem;
    @FXML
    private Button startMeasurement;
    @FXML
    private Button stopMeasurement;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Slider voltage1Slider;
    @FXML
    private Slider voltage2Slider;

    public MainController() {
        data = new Data();
        connectionData = SaveLoadUtil.loadApplicationState(Strings.FILE_NAME);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSliders();
        configureMenuItems();
        initializeButtons();
        startMeasurementButton();
        stopMeasurementButton();
    }

    private void initializeSliders() {
        initializeSlider(voltage1Slider,  data.getCurrent());
        initializeSlider(voltage2Slider, data.getPressure());
    }

    private void initializeSlider(Slider slider, SimpleDoubleProperty data) {
        slider.setDisable(true);
        slider.valueProperty().bind(data);
    }

    private void configureMenuItems() {
        connectionSettingsItem.setOnAction(x -> WindowUtil.loadWindowAndSendData(Strings.CONNECTION_LAYOUT_NAME,
                Strings.CONNECTION_SETTINGS_ITEM_NAME, connectionData));
        aboutItem.setOnAction(x -> WindowUtil.loadWindow(Strings.ABOUT_LAYOUT_NAME, Strings.ABOUT_ITEM_NAME));
        closeItem.setOnAction(x -> WindowUtil.loadWindow(Strings.EXIT_LAYOUT_NAME, Strings.EXIT_ITEM_NAME));
    }

    private void initializeButtons() {
        startMeasurementButton();
        stopMeasurementButton();
    }

    private void startMeasurementButton() {
        startMeasurement.setOnAction(x -> {
            progressBar.progressProperty().bind(startMeasurementDevicesService.progressProperty());
            progressIndicator.progressProperty().bind(startMeasurementDevicesService.progressProperty());
            startMeasurementDevicesService.start();

            startMeasurementDevicesService.setOnSucceeded(e -> {
                voltage1Slider.setDisable(startMeasurementDevicesService.getValue());
                voltage2Slider.setDisable(startMeasurementDevicesService.getValue());
                startMeasurementDataService.start();
            });

        });
    }

    private void stopMeasurementButton() {
        stopMeasurement.setOnAction(x-> {
            startMeasurementDevicesService.cancel();
            startMeasurementDevicesService.reset();

            startMeasurementDataService.cancel();
            startMeasurementDataService.reset();

            voltage1Slider.setDisable(true);
            voltage2Slider.setDisable(true);
        });
    }

    //klasa anonimowa
    Service<Boolean> startMeasurementDevicesService = new Service<Boolean>() {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    double max = 100;
                    for (int i = 0; i <= max; i++) {
                        if(isCancelled()) {
                            break;
                        }
                        updateProgress(i, max);
                        Thread.sleep(1);
                    }
                    return false;
                }
            };
        }
    };

    Service<Boolean> startMeasurementDataService = new Service<Boolean>() {
        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() throws Exception {
                    while(true) {
                        if(isCancelled()) {
                            break;
                        }
                        generateMeasurementData();
                        ConnectionUtil.run(connectionData, data);
                        System.out.println(data.getCurrent());
                        Thread.sleep(2000);
                    }
                    return true;
                }
            };
        }
    };

    private void generateMeasurementData() {
        data.setCurrent(GenerateDataUtil.generateVoltage1());
        data.setPressure(GenerateDataUtil.generateVoltage2());
    }

    public ConnectionData getConnectionData() {
        return connectionData;
    }
}
