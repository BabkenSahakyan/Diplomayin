package controllers;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import analyzers.VideoAnalyzer;
import controllers.util.playpause.PlayPauseByHand;
import factories.SceneFactory;
import model.Model;
import utils.Validator;

public class MainController implements Initializable {
    public TextField fileTextField;
    public Button browseButton;
    public Button openButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            openButton.setDisable(newValue.isEmpty());
        });

        fileTextField.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                if (db.getFiles().size() == 1) {
                    File file = db.getFiles().iterator().next();
                    if (file.isFile()) {
                        fileTextField.setText(file.getAbsolutePath());
                    }
                }
            }
        });
    }

    public void fileChooserPane(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("choose the utils");
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            fileTextField.setText(file.getAbsolutePath());
        }
    }

    public void openVideo(MouseEvent event) {
        String URL = fileTextField.getText();
        if (Validator.validationPassed(URL)) {
            Model.setURL(URL);

            VideoAnalyzer videoAnalyzer = VideoAnalyzer.getVideoAnalyzer(Model.getURL());
            videoAnalyzer.analyze();
            Stage stage = (Stage) ((Button) event.getSource()).getParent().getScene().getWindow();
            stage.setScene(SceneFactory.getScene("video"));
            stage.setOnHiding(event1 -> {
                if (Model.isHandsWindowOpen()){
                    PlayPauseByHand.close();
                }
            });
            stage.show();
        }
    }
}
