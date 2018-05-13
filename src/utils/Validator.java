package utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javafx.scene.control.Alert;

public class Validator {
    private static boolean isValid(final String URL){
        List<String> urls = Arrays.asList(
                "mp4"
                ,"m4a"
                ,"m4v"
                ,"m3u8"
                ,"fxm"
                ,"flv"
        );
        int dotIndex = URL.lastIndexOf(".") + 1;
        String extension = URL.substring(dotIndex);

        return urls.contains(extension);
    }

    private static boolean isFile(final String URL){
        File file = new File(URL);
        return file.isFile();
    }

    public static boolean validationPassed(final String URL){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (!isValid(URL)){
            alert.setHeaderText("Wrong file format!");
            alert.setContentText("Please choose the video file");
            alert.showAndWait();
            return false;
        }

        if (!isFile(URL)) {
            alert.setHeaderText("File not exists!");
            alert.setContentText("Please choose the video file");
            alert.showAndWait();
            return false;
        }
        return true;
    }
}
