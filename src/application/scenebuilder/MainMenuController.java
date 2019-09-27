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

/**
 * 
 *
 */
public class MainMenuController implements Initializable{


	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _creations = new ArrayList<String>();
	private enum State{
		EMPTY,
		PLAYING,
		PAUSED,
		FINISHED
	};

	private HBox _lastSelected;
	private State _state = State.EMPTY;
	private boolean _muted = false;
	private ObservableList<HBox> _videoList = FXCollections.observableArrayList();
	
	
	@FXML
	private Text videoTime;
	
	@FXML
	private Button createButton;
	
	@FXML
	private Button _deleteButton;
	
	@FXML 
	private Slider _slider;

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
	

	@FXML
	void handleSlider(ActionEvent event) {
		
	}
	

	/*
	 * fastforwards the current video
	 */
	@FXML
	void handleForward(ActionEvent event) {
		_player.getMediaPlayer().seek( _player.getMediaPlayer().getCurrentTime().add( Duration.seconds(3)) );
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
	void handleVideoMultiButton(ActionEvent event) {
		HBox currentSelection = (HBox) videoListView.getSelectionModel().getSelectedItem();
		if(currentSelection == null){
			currentSelection = videoListView.getItems().get(0);
		}
		switch (_state) {
		case EMPTY:
			play(currentSelection);
			break;
		case PLAYING:
			_multiButton.setText("Play");
			_player.getMediaPlayer().pause();
			_state=State.PAUSED;
			break;
		case PAUSED:
			if(_lastSelected==null) {
				//can fix if you care to
				_lastSelected = currentSelection;
			}
			
			if(currentSelection.equals(_lastSelected)){
				_multiButton.setText("Pause");
				_player.getMediaPlayer().play();
				_state=State.PLAYING;
			}else {
				_state=State.EMPTY;
				handleVideoMultiButton(event);
			}
			break;
		case FINISHED:
			
			if(currentSelection.equals(_lastSelected)){
				_multiButton.setText("Pause");
				//I think this might need smth else
				_player.getMediaPlayer().play();
				_state=State.PLAYING;
			}else {
				_state=State.EMPTY;
				handleVideoMultiButton(event);
			}

		}
	}

	private void play(HBox creationToPlay) {
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
			mediaUrl = new File(System.getProperty("user.dir") + "/bin/VideoCreations/"+asText.getText()+".mp4").toURI().toURL();
			_lastSelected=creationToPlay;
			Media media = new Media(mediaUrl.toExternalForm());
			//Create the player and set to play.
			MediaPlayer mediaPlayer = new MediaPlayer(media);
			_player.setMediaPlayer(mediaPlayer);
			_player.getMediaPlayer().setMute(_muted);
			mediaPlayer.play();
			/**
		 //this needs to be done
		Runnable finished = new Task() {
			@Override
			protected List<String> call() throws Exception {
			List <String> out=new ArrayList<String>();
			out.add("done");
				return out;
			}

		};
		mediaPlayer.setOnEndOfMedia(finished);
			 **/
			//mediaPlayer.setOnReady(arg0);;
			_multiButton.setText("Pause");
			_state= State.PLAYING;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@FXML
	void handleBackward(ActionEvent event) {
		_player.getMediaPlayer().seek( _player.getMediaPlayer().getCurrentTime().add( Duration.seconds(-3)) );
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
		RunBash bash = new RunBash("List=`ls ./VideoCreations` ; List=${List//.???/} ; printf \"${List// /.\\\\n}\\n\"");
		_team.submit(bash);
		bash.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {

				try {
					_creations = bash.get();
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