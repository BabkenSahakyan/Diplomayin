package factories;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class SceneFactory {
    public static Scene getScene(String sceneName){
        Scene scene = null;
        try {
            Parent root = FXMLLoader.load(SceneFactory.class.getResource(String.format("../views/%s.fxml", sceneName)));
            scene = new Scene(root);
            scene.getStylesheets().add(String.format("style/%s.css", sceneName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return scene;
    }
}
