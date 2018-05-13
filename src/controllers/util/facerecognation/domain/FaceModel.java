package controllers.util.facerecognation.domain;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class FaceModel {
    private Point leftEye;
    private Point rightEye;
    private Mat croppedFace;

    public FaceModel(Point leftEye, Point rightEye) {
        this.leftEye = leftEye;
        this.rightEye = rightEye;
    }

    public Point getLeftEye() {
        return leftEye;
    }

    public void setLeftEye(Point leftEye) {
        this.leftEye = leftEye;
    }

    public Point getRightEye() {
        return rightEye;
    }

    public void setRightEye(Point rightEye) {
        this.rightEye = rightEye;
    }

    public Mat getCroppedFace() {
        return croppedFace;
    }

    public void setCroppedFace(Mat croppedFace) {
        this.croppedFace = croppedFace;
    }
}
