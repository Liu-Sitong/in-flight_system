package main.view;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import main.MainApp;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Locale;

public class WelcomeController {

    private MainApp mainApp;
    private Locale locale;
    private final int asd = 1;

    @FXML
    private ToggleGroup Language;
    @FXML
    private RadioButton zhButton;
    @FXML
    private RadioButton enButton;
    @FXML
    private RadioButton frButton;
    @FXML
    private Button startButton;

    /**
     *
     * add listener to button, change the content of start button by language
     *
     */
    @FXML
    private void initialize() {
        locale = Locale.ENGLISH;


        zhButton.selectedProperty().addListener(new javafx.beans.value.ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                startButton.setText("开始");
                locale = Locale.CHINESE;
            }
        });

        enButton.selectedProperty().addListener(new javafx.beans.value.ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                startButton.setText("Start");
                locale = Locale.ENGLISH;
            }
        });

        frButton.selectedProperty().addListener(new javafx.beans.value.ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                startButton.setText("Commencer");
                locale = Locale.FRENCH;
            }
        });

    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleStart() {
        mainApp.showMovieOverview(locale);
    }

    /**
     * when user type enter, click start
     *
     * @param keyEvent KeyEvent
     */
    @FXML
    private void keyboard_Start(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER){
            handleStart();
        }
    }
}
