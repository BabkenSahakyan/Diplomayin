import javafx.application.Application;
import javafx.stage.Stage;

import org.opencv.core.Core;

import controllers.util.facerecognation.FaceRecognition;

public class Main extends Application{

    @Override
    public void start(Stage primaryStage) {
        FaceRecognition faceRecognition = new FaceRecognition();
        faceRecognition.show();
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }
}
