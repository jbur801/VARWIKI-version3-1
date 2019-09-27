package application.scenebuilder;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import application.*;

public class MainMenuController implements Initializable{


	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _creations = new ArrayList<String>();
	private enum State{
		EMPTY,
		PLAYING,
		PAUSED,
		FINISHED
	};
	
	private ObservableList<HBox> _videoList = FXCollections.observableArrayList();

	private HBox _lastSelected;
	private State _state = State.EMPTY;
	@FXML
	private Button createButton;

	@FXML 
	private Slider _slider;

	@FXML
	private VBox videoBox;

	@FXML
	private MediaView _player;

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

	@FXML
	void handleSlider(ActionEvent event) {

	}

	@FXML
	void handleForward(ActionEvent event) {
		List<HBox> creations =  videoListView.getItems();
		int i = creations.indexOf(_lastSelected)+1;
		if (i>=creations.size()) {
			i=0;
		}
		setup(creations.get(i));
	}

	@FXML
	void handleMute(ActionEvent event) {
		if(existingPlayer()) {
			_muted=!_muted;
			_player.getMediaPlayer().setMute(_muted);
		}
	}


	private boolean existingPlayer() {

		return _player.getMediaPlayer()!=null;
	}

	@FXML
	void handleSelectionChange() {
		HBox currentSelection = (HBox) videoListView.getSelectionModel().getSelectedItem();
		setup(currentSelection);	
	}

	@FXML
	void handleVideoMultiButton() {		
		switch (_state) {
		case EMPTY:
			setup(videoListView.getItems().get(0));
			play();
			break;
		case PLAYING:
			pause();
			break;
		case PAUSED:
			play();
			break;
		case FINISHED:
			//todo: needs a real implementation, this currently does nothing
			//_player.getMediaPlayer().setOnEndOfMedia(Runnable);

			play();
			break;
		}

	}

	private void pause() {
		_multiButton.setText("Play");
		Duration time=_player.getMediaPlayer().getCurrentTime();
		_player.getMediaPlayer().stop();
		_player.getMediaPlayer().setStartTime(time);
		_state=State.PAUSED;

	}

	class countTime extends Task <Void>{

		@Override
		protected Void call() throws Exception {

			//updateProgress(1,Main.getDuration());
			return null;

		}

	}

	private void play() {
		_player.getMediaPlayer().play();
		_multiButton.setText("Pause");
		_state= State.PLAYING;
	}

	private void setup(HBox creationToPlay) {
		if(existingPlayer()) {
			_player.getMediaPlayer().dispose();
		}
		URL mediaUrl;
		try {
			//find media
			Text asText = (Text) creationToPlay.getChildren().get(0);
			if(asText.getText().contentEquals("Are you sure? ")|asText.getText().contentEquals("No more Creations")) {
				return;
			}
			mediaUrl = new File(Main.getPathToResources() + "/VideoCreations/"+asText.getText()+".mp4").toURI().toURL();
			_lastSelected=creationToPlay;
			Media media = new Media(mediaUrl.toExternalForm());
			//Create the player and set to play.
			MediaPlayer mediaPlayer = new MediaPlayer(media);
			mediaPlayer.setAutoPlay(false);
			_player.setMediaPlayer(mediaPlayer);
			_player.getMediaPlayer().setMute(_muted);

			Duration time=_player.getMediaPlayer().getCurrentTime();
			Duration totalDuration = _player.getMediaPlayer().getTotalDuration();
			_multiButton.setText("Play");
			_state= State.PAUSED;
			_player.getMediaPlayer().setOnEndOfMedia(new RunBash("") {

			});
			//_slider.valueProperty().bind(task);

			//_player.getMediaPlayer().setStartTime(arg0);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@FXML
	void handleBackward(ActionEvent event) {
		List<HBox> creations =  videoListView.getItems();
		int i = creations.indexOf(_lastSelected)-1;
		if (i<0) {
			i=creations.size()-1;
		}
		setup(creations.get(i));
	}

	@FXML
	void handleCreate() {
		Main.changeScene("CreateMenu.fxml", this);
	}

	/**
	 * universal delete button
	 * @param event
	 */
	@FXML
	void handleDeleteVideo(ActionEvent event) {
		HBox selectedItem = videoListView.getSelectionModel().getSelectedItem();
		if(selectedItem instanceof VideoBar) {
			((VideoBar) selectedItem).delete();
		}
	}


	//maybe unnecessary

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		_state = State.EMPTY;

		/*
		 * this loads the current stored videos
		 */
		RunBash bash = new RunBash("List=`ls ./resources/VideoCreations` ; List=${List//.???/} ; printf \"${List// /.\\\\n}\\n\"");
		_team.submit(bash);
		bash.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {

				try {
					_creations = bash.get();
					
						for(String image:_creations) {
						System.out.println(image);
						}
				
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(_creations.get(0).isEmpty()) {

					Text noCreations = new Text("No Current Creations");

					_videoList.add(new HBox(noCreations));
					videoListView.setItems(_videoList);

				}else {

					for(String video:_creations) {
						new VideoBar(video,_videoList);
					}
					videoListView.setItems(_videoList);
				}
			}
		});
	}
}
