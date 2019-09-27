package application.scenebuilder;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.AudioBar;
import application.RunBash;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import application.*;

public class CreateMenuController implements Initializable {



	private ObservableList<HBox> _audioList = FXCollections.observableArrayList();
	private String _term = "";
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _runningThread;
	private int audioCount=0;
	private SetImagesController _controller;

	@FXML
	private Button _playButton;

	@FXML
	private TextField searchTextArea;

	@FXML
	private Button _imageButton;

	@FXML
	private Button _deleteButton;

	@FXML
	private ListView<HBox> audioBox;

	@FXML
	private Button _upButton;

	@FXML
	private Button _downButton;

	@FXML
	private Button _searchButton;

	@FXML 
	private CheckBox _images;

	@FXML
	private Button testButton;

	@FXML
	private Button saveButton;

	@FXML
	private TextArea displayTextArea;

	@FXML
	private ChoiceBox<String> _festivalVoice;

	@FXML
	private Button createButton;

	@FXML
	private Button returnButton;

	@FXML
	private TextField videoName;
	private boolean _manual;
	private Scene _scene;

	/**
	 * this initialises choice box to allow for the selection of different festival voices
	 */

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		ObservableList<String> voices = FXCollections.observableArrayList();
		voices.addAll("Default","(voice_akl_nz_cw_cg_cg)","(voice_akl_nz_jdt_diphone)");
		_festivalVoice.setItems(voices);
		_manual=false;
	}

	@FXML
	void handleCreate() {
		if(_runningThread) {
			return;
		}
		String name = videoName.getText();
		if(name.isEmpty()) {
			error("Creation must have a name");
			return;
		}
		String audioFileNames="";
		for(Node audio:_audioList) {
			audioFileNames = audioFileNames+audio.toString();
		}		

		RunBash mergeAudio = new RunBash("sox "+ audioFileNames +" ./resources/temp/output.wav");
		_team.submit(mergeAudio);	
		mergeAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				RunBash audioLengthSoxi = new RunBash("soxi -D ./resources/temp/output.wav");
				_team.submit(audioLengthSoxi);
				_runningThread = true;
				audioLengthSoxi.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						_runningThread=false;
						double audioLength;

						try {
							audioLength = Double.parseDouble(audioLengthSoxi.get().get(0));
							RunBash createVideo = new RunBash("ffmpeg -i ./resources/temp/output.wav -vn -ar 44100 -ac 2 -b:a 192k ./resources/temp/output.mp3 &> /dev/null "
									+ "; ffmpeg -f lavfi -i color=c=blue:s=320x240:d="+audioLength 
									+ " -vf \"drawtext=fontfile=/path/to/font.ttf:fontsize=30: "
									+ "fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text="+_term+"\" ./resources/temp/"+name+".mp4 &> /dev/null "
									+ "; ffmpeg -i ./resources/temp/"+name +".mp4 -i ./resources/temp/output.mp3 -c:v copy -c:a aac -strict experimental "
									+ "./resources/VideoCreations/"+name+".mp4  &> /dev/null");

							_team.submit(createVideo);
							_runningThread = true;
							createVideo.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
								@Override
								public void handle(WorkerStateEvent event) {
									_runningThread=false;
									Main.changeScene("MainMenu.fxml", this);
								}
							});
						} catch (NumberFormatException | InterruptedException | ExecutionException e) {
							error("Video Creation Failed");
							Main.changeScene("MainMenu.fxml", this);
						}
					}
				});

			}
		});
	}

	@FXML
	void handleReturn() {
		Main.changeScene("MainMenu.fxml", this);
	}

	private void initializeSetImages() {
		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(getClass().getResource("SetImages.fxml"));
		Parent layout; 
		try {
			_controller=loader.getController();
			layout = loader.load();
			_scene = new Scene(layout);

			_manual = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@FXML
	void handleImages() {
		//yeap i really did it
		if(_searchButton.isVisible()==true) {
			error("please search for a subject first");
			return;
		} else if (_runningThread) {
			error("busy");
			return;
		}
		if (_controller==null) {
			initializeSetImages();
		}
		popupSetImages();
	}

	private void popupSetImages() {
		Stage imageStage = new Stage();
		imageStage.setScene(_scene);
		imageStage.show();
	}

	@FXML
	void handleSaveAudio(ActionEvent event) {
		audioCount++;
		String selectedText = displayTextArea.getSelectedText();
		String[] wordCount = selectedText.split("\\s+");

		if(selectedText.isEmpty()) {
			return;
		}else if(wordCount.length>20) {
			error("Can only save sections smaller than 20 words");
			return;
		}
		String voice = _festivalVoice.getSelectionModel().getSelectedItem();

		if( voice ==null || voice.contentEquals("Default") ) {
			RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | text2wave -o ./resources/temp/"+ audioCount + ".wav");

			_team.submit(audioCreation);
			_runningThread = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_runningThread=false;
					new AudioBar(selectedText,audioCount+"",_audioList);
					audioBox.setItems(_audioList);
				}
			});
		}else {
			RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | text2wave -o ./resources/temp/"+ audioCount + ".wav " + "-eval \""+_festivalVoice.getSelectionModel().getSelectedItem()+"\"");
			_team.submit(audioCreation);
			_runningThread = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_runningThread=false;
					if(audioCreation.returnError().substring(0, 10).contentEquals("SIOD ERROR")) {
						error("some words selected cannot be converted by selcted voice package");
						return;
					}
					new AudioBar(selectedText,audioCount+"",_audioList);
					audioBox.setItems(_audioList);

				}
			});
		}

	}

	@FXML
	void handleSearch(ActionEvent event) {
		_term = searchTextArea.getCharacters().toString();

		if(_term.isEmpty()) {
			error("please enter a term");
			return;
		}else if(_runningThread) {
			error("please wait for process to finish");
			return;
		}

		_searchButton.setText("Searching...");

		//wiki search bash command is created and run on another thread
		RunBash command = new RunBash("wikit "+ _term);
		_team.submit(command);
		_runningThread = true;
		command.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				String text;


				try {
					text = command.get().get(0);
					//checks if search was successful or not
					if(text.contentEquals(_term + " not found :^(" )) {
						error("search term not found");
					}
					_searchButton.setText("search");

					displayTextArea.setText(text);
					searchTextArea.setEditable(false);
					_searchButton.setVisible(false);
					videoName.setText(_term);
					GetFlickr imageDown = new GetFlickr(searchTextArea.getText(), 9);
					_team.submit(imageDown);
					imageDown.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							_runningThread = false;
						}
					});
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	@FXML
	void handleTestAudio(ActionEvent event) {
		String selectedText = displayTextArea.getSelectedText();
		if(selectedText.isEmpty() || _runningThread) {
			return;
		}
		String voice = _festivalVoice.getSelectionModel().getSelectedItem();
		if(voice == null ||voice.contentEquals("Default")) {

			RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | festival --tts");
			_team.submit(audioCreation);
			_runningThread = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_runningThread=false;
				}
			});

		}else {
			RunBash audioCreation = new RunBash("echo {\""+voice+"\",'(SayText \""+selectedText+"\")'} | bash -c festival");
			_team.submit(audioCreation);
			_runningThread = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_runningThread=false;
					if(audioCreation.returnError().substring(0, 10).contentEquals("SIOD ERROR")) {
						error("some words selected cannot be converted by selcted voice package");
						return;
					}
				}
			});
		}


	}

	@FXML
	void handlePlayAudio(ActionEvent event) {
		HBox audio = audioBox.getSelectionModel().getSelectedItem();
		((AudioBar) audio).playAudio();
	}

	@FXML
	void handleDeleteAudio(ActionEvent event) {
		HBox audio = audioBox.getSelectionModel().getSelectedItem();
		((AudioBar) audio).delete();
	}

	@FXML
	void handleMoveAudioDown(ActionEvent event) {
		HBox audio = audioBox.getSelectionModel().getSelectedItem();
		((AudioBar) audio).moveDown();
	}

	@FXML
	void handleMoveAudioUp(ActionEvent event) {
		HBox audio = audioBox.getSelectionModel().getSelectedItem();
		((AudioBar) audio).moveUp();
	}


	/**
	 * helper method that creates a popup when an error occurs
	 * @param msg
	 */
	public void error(String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("ERROR "+msg);
		alert.setHeaderText("ERROR");
		alert.setContentText(msg);
		alert.showAndWait();
	}

}
