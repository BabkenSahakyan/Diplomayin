package analyzers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import static model.Model.changeValues;
import static model.Model.getFramesCount;
import static model.Model.setFramesCount;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.equalizeHist;

public class VideoAnalyzer{
    private Mat[] matFaces;
    private final CascadeClassifier faceDetector;

    private static VideoAnalyzer videoAnalyzer;
    private VideoAnalyzer(String path) {
        String classifierFilePath = "resources/haarcascades/haarcascade_frontalface_alt.xml";
        faceDetector = new CascadeClassifier(new File(classifierFilePath).getPath());

        VideoCapture videoCapture = new VideoCapture(path);
        int frameCount = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
        setFramesCount(frameCount);

        this.matFaces = new Mat[getFramesCount()];
        Runnable r = () -> {
            for (int i = 0; i < getFramesCount(); i++) {
                if (i % 12 == 0){
                    matFaces[i] = new Mat();
                    videoCapture.read(matFaces[i]);
                } else {
                    videoCapture.grab();
                }
            }
            videoCapture.release();
        };

        Thread t1 = new Thread(r);
        t1.setDaemon(true);
        t1.start();
    }

    public static VideoAnalyzer getVideoAnalyzer(String path){
        if (videoAnalyzer == null){
            videoAnalyzer = new VideoAnalyzer(path);
        }
        return videoAnalyzer;
    }

    public void analyze(){
        Runnable r = () -> {
            int size = getFramesCount();
            int lastValue;
            int end;
            List<Integer> values = new ArrayList<>();
            values.add(1);
            for (int i = 0; i < size; i += 20) {
                end = i + 20 < size ? i + 20 : size;
                job(values, i, end);
                lastValue = values.get(values.size() - 1);
                Platform.runLater(() -> {
                    changeValues(values);
                    values.clear();
                });
                try {
                    while (!values.isEmpty()) {
                        Thread.sleep(0, 1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                values.add(end);
                if (i != 0) {
                    values.add(lastValue);
                }
            }
            Platform.runLater(() -> {
                changeValues(values);
                values.clear();
            });
        };
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("analyze");
        t.start();
    }

    private void job(List<Integer> values, int start, int end){
        Mat grayFrame = new Mat();
        for (int i = start; i < end; i++) {
            if (i % 12 == 0) {
                try {
                    while (matFaces[i] == null || matFaces[i].empty()) {
                        Thread.sleep(0, 1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                cvtColor(matFaces[i], grayFrame, COLOR_BGR2GRAY);
                equalizeHist(grayFrame, grayFrame);
                MatOfRect faceDetections = new MatOfRect();
                faceDetector.detectMultiScale(grayFrame, faceDetections, 1.1, 2,
                        Objdetect.CASCADE_SCALE_IMAGE, new Size(), new Size());
                values.add((int)faceDetections.total() * 100);
            }
        }
    }
}
