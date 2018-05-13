package model;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Model{
    private static String URL = "";
    private static ObservableList<Integer> values = FXCollections.observableArrayList();
    private static int framesCount = 0;
    private static boolean isHandsWindowOpen = false;

    public static void setURL(String URL) {
        Model.URL = URL;
    }

    public static String getURL() {
        return URL;
    }

    public static void changeValues(List<Integer> list){
        values.clear();
        values.addAll(list);
    }

    public static ObservableList<Integer> getValues() {
        return values;
    }

    public static int getFramesCount() {
        return Model.framesCount;
    }

    public static void setFramesCount(int framesCount) {
        Model.framesCount = framesCount;
    }

    public static boolean isHandsWindowOpen() {
        return isHandsWindowOpen;
    }

    public static void setHandsWindowOpen(boolean isHandsWindowOpen) {
        Model.isHandsWindowOpen = isHandsWindowOpen;
    }
}
