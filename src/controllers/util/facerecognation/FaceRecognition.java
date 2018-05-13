package controllers.util.facerecognation;

import controllers.util.facerecognation.domain.FaceModel;
import controllers.util.facerecognation.utils.FaceUtils;
import factories.SceneFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.face.FaceRecognizer;
import org.opencv.face.FisherFaceRecognizer;
import org.opencv.videoio.VideoCapture;

import static org.opencv.imgcodecs.Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.opencv.imgcodecs.Imgcodecs.imencode;
import static org.opencv.imgcodecs.Imgcodecs.imread;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.resize;

public class FaceRecognition {
    private Stage stage = new Stage();
    private VideoCapture videoCapture = new VideoCapture(0);
    private ImageView imageView;
    private int occuracy;

    private synchronized void recognize() {
        Mat frame = new Mat();
        FaceRecognizer recognizer = createFaceRecognizer();
        Runnable r = () -> {
            while (true) {
                int[] label = {-1, -1, -1, -1, -1};
                double[] confidence = {-1, -1, -1, -1, -1};
                videoCapture.read(frame);

                MatOfByte buffer = new MatOfByte();
                imencode(".png", frame, buffer);
                Image image = new Image(new ByteArrayInputStream(buffer.toArray()));
                Platform.runLater(() -> imageView.setImage(image));
                FaceModel faceModel = FaceUtils.getFace(frame.clone());
                if (faceModel == null)
                    continue;

                Mat face = faceModel.getCroppedFace();
                Mat resizedFace = new Mat();
                cvtColor(face, face, COLOR_BGR2GRAY);
                resize(face, resizedFace, new Size(200, 200), 1.0, 1.0, INTER_CUBIC);

                recognizer.predict(resizedFace, label, confidence);
                if (confidence[0] > 1000) {
                    if (label[0] == 11) {
                        occuracy += label[0];
                    } else {
                        occuracy = 0;
                    }
                    if (occuracy == 33){
                        Platform.runLater(() -> stage.hide());
                        return;
                    }
                }
            }
        };

        Thread t = new Thread(r);
        t.setDaemon(true);
        t.start();
    }

    public void show(){
        stage.setTitle("Face Recognition");
        Pane pane = new Pane();
        imageView = new ImageView();
        imageView.setFitHeight(500);
        imageView.setFitWidth(500);
        pane.getChildren().add(imageView);
        Scene scene = new Scene(pane, 500, 500);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setOnHiding(event -> {
            Stage primaryStage = new Stage();
            Scene mainScene = SceneFactory.getScene("main");
            primaryStage.setScene(mainScene);
            primaryStage.setTitle("main scene");
            primaryStage.show();
        });
        resize200();
        recognize();
    }

    private FaceRecognizer createFaceRecognizer() {
        FaceRecognizer recognizer = FisherFaceRecognizer.create();
        List<Mat> src = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            src.add(imread(new File("resources/images/me/"+i+".jpg").getPath(), CV_LOAD_IMAGE_GRAYSCALE));
        }
        for (int i = 1; i <= 20; i++) {
            src.add(imread(new File("resources/images/not_me/"+i+".jpg").getPath(), CV_LOAD_IMAGE_GRAYSCALE));
        }

        Mat labels = new Mat(new Size(30, 1), CvType.CV_32SC1);
        double[] data  = new double[]{ 11, 11, 11, 11, 11, 11, 11, 11, 11, 11,
                                 22, 22, 22, 22, 22, 22, 22, 22, 22, 22,
                                 22, 22, 22, 22, 22, 22, 22, 22, 22, 22};
        labels.put(0, 0, data);

        recognizer.train(src, labels);
        return recognizer;
    }

    private void resize200() {
        Mat m;
        for (int i = 1; i <= 10; i++) {
            m = imread(new File("resources/images/me/"+i+".jpg").getPath());
            Mat temp = new Mat();
            resize(m, temp, new Size(200, 200), 1.0, 1.0, INTER_CUBIC);
            imwrite(new File("resources/images/me/"+i+".jpg").getPath(), temp);
        }
        for (int i = 1; i <= 20; i++) {
            m = imread(new File("resources/images/not_me/"+i+".jpg").getPath());
            Mat temp = new Mat();
            resize(m, temp, new Size(200, 200), 1.0, 1.0, INTER_CUBIC);
            imwrite(new File("resources/images/not_me/"+i+".jpg").getPath(), temp);
        }
    }
}
