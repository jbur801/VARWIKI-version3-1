package application.scenebuilder;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import application.*;

public class MediaBox {



	@FXML
	private Button createButton;

	@FXML 
	private Slider _slider;

	@FXML
	private VBox videoBox;

	@FXML
	private Text _videoTime;
	
	@FXML
	private MediaView view;
	
	private MediaPlayer _player;

	@FXML
	private Button backwardButton;

	@FXML
	private Button _multiButton;

	@FXML
	private Button forwardButton;

	@FXML
	private Button muteButton;

	@FXML
	private ListView<HBox> videoListView;
	private boolean _muted = false;
	private boolean _playing;

	public MediaBox(Media media,boolean muted) {

		//Create the player and set to play.
		_player = new MediaPlayer(media);
		_player.setAutoPlay(false);
		_player.setMute(_muted);
		_player.setOnReady(new Runnable() {
			@Override
			public void run() {
				setupSlider();
			}
		});
		_multiButton.setText("Play");
		_playing=false;

		_player.setOnEndOfMedia(new Runnable() {
			public void run() {
				_playing=false;
				_player.stop();
				_multiButton.setText("Replay");

			}
		});
		_player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
			@Override
			public void changed(ObservableValue<? extends Duration> observable, Duration oldValue,
					Duration newValue) {				
				_slider.setValue(newValue.toMillis());
			}
		});
	}

	@FXML
	void handleForward(ActionEvent event) {
		_player.seek(_player.getCurrentTime().add( Duration.seconds(3)));
	}
	@FXML
	void handleBackward(ActionEvent event) {
		_player.seek( _player.getCurrentTime().add( Duration.seconds(-3)));
	}
	

	@FXML
	void handleMute(ActionEvent event) {
		if(!_muted) {
			muteButton.setText("Unmute");
		}else {
			muteButton.setText("Mute");
		}
		_muted=!_muted;
		_player.setMute(_muted);

	}




	@FXML
	void handleVideoMultiButton() {		
		if(_playing) {
			pause();
		}else {
			play();
		}
	}

	private void pause() {
		_multiButton.setText("Play");
		_player.pause();
		_playing =false;

	}

	private void play() {

		_player.play();

		_multiButton.setText("Pause");
		_playing = true;
	}

	
	
	private void checkPlay() {
		if (_playing) {
			_player.play();
		}
	}

	private void setupSlider() {
		Pane _thumb = (Pane) _slider.lookup(".thumb");
		_thumb.setOnDragDetected(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				_player.pause();
			}
		});	
		_thumb.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				System.out.println(_player.getCurrentTime().toSeconds());
				_player.seek(Duration.millis(_slider.getValue()));
				System.out.println(_player.getCurrentTime().toSeconds());
				checkPlay();
			}
		});
		_slider.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				System.out.println(_player.getCurrentTime().toSeconds());
				_player.seek(Duration.millis(_slider.getValue()));
				System.out.println(_player.getCurrentTime().toSeconds());
				checkPlay();
			}
		});
		_slider.setMax(_player.getTotalDuration().toMillis());
		_slider.valueProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				String time = "";
				Double newValue = (Double)arg1;
				time += String.format("%02d", (int)newValue.doubleValue()/60000);
				time += ":";
				time += String.format("%02d", (int)newValue.doubleValue()/1000);
				_videoTime.setText(time);
			}

		});
	}






}
