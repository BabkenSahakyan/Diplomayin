package controllers.util.facerecognation.utils;

import controllers.util.facerecognation.domain.FaceModel;

import java.io.File;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.equalizeHist;
import static org.opencv.imgproc.Imgproc.rectangle;
import static org.opencv.imgproc.Imgproc.getAffineTransform;
import static org.opencv.imgproc.Imgproc.warpAffine;
import static org.opencv.imgproc.Imgproc.getRotationMatrix2D;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.imgproc.Imgproc.INTER_LINEAR;

public class FaceUtils {
    private static FaceModel getFaceModel(Mat origin) {
        CascadeClassifier faceCascade = new CascadeClassifier(
                new File("resources/haarcascades/haarcascade_frontalface_alt.xml").getPath());
        CascadeClassifier eyesCascade = new CascadeClassifier(
                new File("resources/haarcascades/haarcascade_eye.xml").getPath());

        Mat image = origin.clone();
        if (!image.empty()) {
            MatOfRect faces = new MatOfRect();
            Mat grayFrame = new Mat();
            cvtColor(image, grayFrame, COLOR_BGR2GRAY);
            equalizeHist(grayFrame, grayFrame);
            faceCascade.detectMultiScale(grayFrame, faces);

            List<Rect> listRectFace = faces.toList();
            Rect face0 = new Rect();
            if (!listRectFace.isEmpty())
                face0 = listRectFace.get(0);
            if (listRectFace.size() == 1) {
                Rect _face = new Rect(face0.x, face0.y, face0.width, face0.height);
                rectangle(
                        image,
                        new Point(_face.x, _face.y),
                        new Point(_face.x + _face.width, _face.y + _face.height),
                        new Scalar(0, 255, 0, 0),
                        Math.max(1, Math.round(image.cols() / 150)), 8, 0
                );

                Point leftEye;
                Point rightEye;
                Mat faceROI = new Mat(grayFrame, face0);
                MatOfRect eyes = new MatOfRect();
                eyesCascade.detectMultiScale(faceROI, eyes);
                List<Rect> lstRectEyes = eyes.toList();

                if (lstRectEyes.size() == 2) {
                    Rect eye0 = lstRectEyes.get(0);
                    Rect eye1 = lstRectEyes.get(1);
                    leftEye = new Point(
                            (float) (face0.x+eye0.x+eye0.width*0.5),
                            (float) (face0.y+eye0.y+eye0.height*0.5));
                    rightEye = new Point(
                            (float) (face0.x+eye1.x+eye1.width*0.5),
                            (float) (face0.y+eye1.y+eye1.height*0.5));
                } else if (lstRectEyes.size() == 1) {
                    leftEye = new Point(
                            (float) (face0.x+face0.x+face0.width*0.5),
                            (float) (face0.y+face0.y+face0.height*0.5));
                    rightEye = new Point(image.cols()/2.0f, image.rows()/2.0f);
                } else {
                    leftEye = new Point(image.cols()/3.0f, image.rows()/2.0f);
                    rightEye = new Point(2.0f * image.cols()/3.0f, image.rows()/2.0f);
                }
                if (leftEye.x > rightEye.x) {
                    Point tmp = rightEye;
                    rightEye = leftEye;
                    leftEye = tmp;
                }
                return new FaceModel(leftEye, rightEye);
            }
        }
        return null;
    }

    public static FaceModel getFace(Mat origin) {
        FaceModel faceModel = getFaceModel(origin);
        if (faceModel == null) {
            return null;
        }
        Point left = faceModel.getLeftEye();
        Point right = faceModel.getRightEye();
        Mat image = origin.clone();
        image = cropFace(image, left, right, new Point(0.25f, 0.25f), new Size(200, 200));

        if (image != null && image.cols() > 0 && image.rows() > 0) {
            faceModel.setCroppedFace(image);
            return faceModel;
        }
        return null;
    }

    private static Mat scaleRotateTranslate(Mat image, double angle, Point center,
                                            Point newCenter, double scale) {
        Mat warp;
        Mat rot = new Mat();
        if (newCenter.x >= 0) {
            Point srcTri0 = new Point(center.x, center.y);
            Point srcTri1 = new Point(center.x+1, center.y);
            Point srcTri2 = new Point(center.x, center.y+1);
            MatOfPoint2f srcTri = new MatOfPoint2f(srcTri0, srcTri1, srcTri2);

            Point dstTri0 = new Point(newCenter.x, newCenter.y);
            Point dstTri1 = new Point(newCenter.x+1, newCenter.y);
            Point dstTri2 = new Point(newCenter.x, newCenter.y+1);
            MatOfPoint2f dstTri = new MatOfPoint2f(dstTri0, dstTri1, dstTri2);

            warp = new Mat(image.rows(), image.cols(), image.type());

            Mat warpMat = getAffineTransform(srcTri, dstTri);
            warpAffine(image, warp, warpMat, warp.size());
        } else {
            warp = image;
        }
        Mat rotMat = getRotationMatrix2D(center, angle, scale);
        warpAffine(warp, rot, rotMat, warp.size());
        return rot;
    }

    private static double distance(Point p1, Point p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        return Math.sqrt(dx*dx+dy*dy);
    }


    private static Mat cropFace(Mat image, Point leftEye, Point rightEye,
                        Point offsetPct, Size destSz) {
        try {
            // calculate offsets in original image
            double offsetH = Math.floor(offsetPct.x*destSz.width);
            double offsetV = Math.floor(offsetPct.y*destSz.height);
            // get the direction
            double[] eye_direction = new double[] { rightEye.x-leftEye.x, rightEye.y-leftEye.y };
            // calc rotation angle in radians
            double rotation = -Math.atan2(eye_direction[1], eye_direction[0]);
            // distance between them
            double dist = distance(leftEye, rightEye);
            // calculate the reference eye-width
            double reference = destSz.width - 2.0 * offsetH;
            // scale factor
            double scale = dist / reference;
            // rotate original around the left eye
            image = scaleRotateTranslate(image, rotation, leftEye, new Point(-1, -1), 1.0);

            Point cropXY = new Point(leftEye.x-scale*offsetH, leftEye.y-scale*offsetV);
            Point cropSize = new Point(destSz.width*scale, destSz.height*scale);

            if (cropSize.x < 50 || cropSize.y < 50)
                return null;

            image = new Mat(image, new Rect((int) cropXY.x, (int) cropXY.y,
                    (int) cropSize.x, (int) cropSize.y));

            resize(image, image, destSz, 0.0, 0.0, INTER_LINEAR);
            return image;
        } catch (Exception e) {}
        return null;
    }
}
