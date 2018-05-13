package controllers;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import controllers.util.playpause.PlayPauseByHand;
import model.Model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class VideoController implements Initializable {
    public ProgressBar videoProgress;
    public MediaView mediaView;
    public Label timeLabel;
    public Slider volumeSlider;
    public AnchorPane anchorPane;
    public AreaChart chart;
    public NumberAxis xAxis;
    public NumberAxis yAxis;
    public Button handButton;

    private MediaPlayer mediaPlayer;

    private boolean simpleTimeFormat = true;
    private boolean playing = true;
    private boolean fullScreen = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mediaPlayer = new MediaPlayer(new Media(new File(Model.getURL()).toURI().toString()));

        mediaPlayer.setAutoPlay(true);
        mediaView.setMediaPlayer(mediaPlayer);

        mediaPlayer.setOnReady(() -> {
            Media media = mediaPlayer.getMedia();
            Stage stage = (Stage) mediaView.getScene().getWindow();
            stage.setWidth(media.getWidth());
            stage.setHeight(media.getWidth());
            xAxis.setUpperBound(Model.getFramesCount() - 1);
        });

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            videoProgress.setProgress(newValue.toMillis() / mediaPlayer.getTotalDuration().toMillis());
            long total = TimeUnit.MILLISECONDS.toSeconds((long) mediaPlayer.getTotalDuration().toMillis());
            long current = TimeUnit.MILLISECONDS.toSeconds((long) newValue.toMillis());

            if (simpleTimeFormat) {
                timeLabel.setText(total + "/" + current);
            } else {
                timeLabel.setText(total + (total - current > 0 ? "/-" : "/") + (total - current));
            }
        });

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) ->
                mediaPlayer.setVolume(newValue.doubleValue() / 100));

        mediaPlayer.setOnPlaying(() -> {
            if (mediaView.getScene() != null) {
                double width = mediaView.getScene().widthProperty().doubleValue();
                double height = mediaView.getScene().heightProperty().doubleValue();
                mediaView.setFitWidth(width);
                mediaView.setFitHeight(height);

                mediaView.getScene().widthProperty().addListener((observable, oldValue, newValue) -> {
                    mediaView.setFitWidth(newValue.doubleValue());
                    anchorPane.resize(newValue.doubleValue(), mediaView.getFitHeight());
                    centralize();
                });
                mediaView.getScene().heightProperty().addListener((observable, oldValue, newValue) -> {
                    mediaView.setFitHeight(newValue.doubleValue());
                    anchorPane.resize(mediaView.getFitWidth(), newValue.doubleValue());
                    centralize();
                });
            }
        });

        Model.getValues().addListener((ListChangeListener<? super Integer>) c -> {
            XYChart.Series facesChart = new XYChart.Series();
            List<Integer> list = new ArrayList<>(c.getList());
            if (!list.isEmpty()){
                int start = list.get(0);
                for (int i = 1; i < list.size(); i++) {
                    facesChart.getData().add(new XYChart.Data(i + start, list.get(i)));
                }

                chart.getData().add(facesChart);
            }
        });
    }

    @FXML
    private void seek(MouseEvent event) {
        Duration totalDuration = mediaPlayer.getTotalDuration();
        double seek = (event.getSceneX() - 8) / (((Node) event.getSource()).getScene().getWidth() - 20);
        mediaPlayer.seek(new Duration(totalDuration.toMillis() * seek));
        videoProgress.setProgress(seek);
    }

    @FXML
    private void timeFormat(MouseEvent event) {
        simpleTimeFormat = !simpleTimeFormat;
    }

    @FXML
    private void playPause(MouseEvent event) {
        playing = !playing;
        if (playing) {
            mediaPlayer.play();
            //set play button image
        } else {
            mediaPlayer.pause();
            //set pause button image
        }
    }

    @FXML
    private void fullScreen(MouseEvent event) {
        fullScreen = !fullScreen;
        ((Stage)((Node)event.getSource()).getScene().getWindow()).setFullScreen(fullScreen);
    }

    private void centralize() {
        double x = (mediaView.getScene().getWidth() - mediaView.prefWidth(0)) / 2;
        double y = (mediaView.getScene().getHeight() - mediaView.prefHeight(0)) / 2;
        mediaView.setX(x);
        mediaView.setY(y);
    }

    public void handWindow(MouseEvent event) {
        PlayPauseByHand.addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(1)){
                mediaPlayer.play();
                playing = true;
                System.out.println("playing");
            }

            if (newValue.equals(-1)){
                mediaPlayer.pause();
                playing = false;
                System.out.println("pause");
            }
        });

        if (!Model.isHandsWindowOpen()) {
            PlayPauseByHand.show();
            Model.setHandsWindowOpen(true);
        } else {
            PlayPauseByHand.close();
            Model.setHandsWindowOpen(false);
        }

    }
}
