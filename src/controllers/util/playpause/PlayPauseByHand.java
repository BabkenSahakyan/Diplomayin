package controllers.util.playpause;

import java.io.ByteArrayInputStream;
import java.io.File;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import model.Model;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import static org.opencv.imgcodecs.Imgcodecs.imencode;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.equalizeHist;

public class PlayPauseByHand {
    private static boolean destroyed = false;
    private static VideoCapture videoCapture = new VideoCapture(0);
    private static Mat matHand = new Mat();
    private static Stage stage = new Stage();

    private static CascadeClassifier openHandClassifier
            = new CascadeClassifier(new File("resources/hand/handOpen.xml").getPath());
    private static CascadeClassifier closeHandClassifier
            = new CascadeClassifier(new File("resources/hand/handClose.xml").getPath());

    private static short totalOpenHands;
    private static short totalCloseHands;

    private static IntegerProperty playPause = new SimpleIntegerProperty(0);

    private static int cores = Runtime.getRuntime().availableProcessors();

    public static void close(){
        destroyed = true;
        Model.setHandsWindowOpen(false);
        stage.close();
    }

    public static void show(){
        destroyed = false;

        stage.setOnHiding(event -> {
            destroyed = true;
            Model.setHandsWindowOpen(false);
        });

        stage.setTitle("hands");
        Pane pane = new Pane();
        ImageView imageView = new ImageView();
        imageView.setFitHeight(300);
        imageView.setFitWidth(300);
        pane.getChildren().add(imageView);
        Scene scene = new Scene(pane, 300, 300);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        Mat openGrayHand = new Mat();
        Mat closeGrayHand = new Mat();

        Runnable r = () -> {
            while (!destroyed) {
                videoCapture.read(matHand);
                MatOfByte buffer = new MatOfByte();
                imencode(".png", matHand, buffer);
                Image image = new Image(new ByteArrayInputStream(buffer.toArray()));
                Platform.runLater(() -> imageView.setImage(image));

                int runnable = runnableThreads();
                System.out.println(runnable);
                if (runnable < cores) {
                    Runnable rr = () -> {
                        System.out.println("calculating");
                        cvtColor(matHand, openGrayHand, COLOR_BGR2GRAY);
                        equalizeHist(openGrayHand, openGrayHand);
                        MatOfRect openHandDetections = new MatOfRect();
                        openHandClassifier.detectMultiScale(openGrayHand,
                                openHandDetections, 1.2, 2,
                                Objdetect.CASCADE_SCALE_IMAGE,
                                new Size(30, 30), new Size());

                        cvtColor(matHand, closeGrayHand, COLOR_BGR2GRAY);
                        equalizeHist(closeGrayHand, closeGrayHand);
                        MatOfRect closeHandDetections = new MatOfRect();
                        closeHandClassifier.detectMultiScale(closeGrayHand,
                                closeHandDetections, 1.2, 2,
                                Objdetect.CASCADE_SCALE_IMAGE,
                                new Size(30, 30), new Size());

                        int openHands = (int) openHandDetections.total();
                        int closeHands = (int) closeHandDetections.total();

                        if (openHands > 0) {
                            if (isHandOpen()) {
                                playPause.setValue(1);
                            }
                        }

                        if (closeHands > 0) {
                            if (isHandClose()) {
                                playPause.setValue(-1);
                            }
                        }
                    };

                    Thread tt = new Thread(rr);
                    tt.setDaemon(true);
                    tt.setName("conditonal");
                    tt.start();
                }
            }
        };

        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("show");
        t.start();
    }

    private static boolean isHandOpen(){
        System.out.println("calculating open" + totalOpenHands);
        totalCloseHands = 0;
        totalOpenHands += 1;
        if (totalOpenHands >= 2){
            totalOpenHands = 0;
            return true;
        }
        return false;
    }

    private static boolean isHandClose(){
        System.out.println("calculating close" + totalCloseHands);
        totalOpenHands = 0;
        totalCloseHands += 1;
        if (totalCloseHands >= 2){
            totalCloseHands = 0;
            return true;
        }
        return false;
    }

    private static int runnableThreads(){
        int nbRunning = 0;
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        System.out.println(threadGroup);
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.getThreadGroup().equals(threadGroup)
                    && t.getState() == Thread.State.RUNNABLE
                    && t.getState() != Thread.State.WAITING)

                nbRunning++;
        }
        return nbRunning;
    }

    public static void addListener(ChangeListener<Number> changeListener){
        playPause.addListener(changeListener);
    }
}
