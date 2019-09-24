package application.scenebuilder;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import application.*;

public class MainMenuController {

    @FXML
    private VBox creationBox;

    @FXML
    private Button createButton;

    @FXML
    void handleCreate() {
    	Main.changeScene("CreateMenu.fxml", this);
    }
}