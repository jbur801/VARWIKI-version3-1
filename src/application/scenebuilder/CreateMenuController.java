package application.scenebuilder;

import java.net.URL;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
import javafx.fxml.Initializable;

import application.*;

public class CreateMenuController implements Initializable {

	private String _term = "";
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _runningThread;
	private int audioCount=0;
	private ObservableList<HBox> _audioList = FXCollections.observableArrayList();

	@FXML
    private AnchorPane Search;

    @FXML
    private TextField searchTextArea;

    @FXML
    private Button _playButton;
    
    @FXML
    private Button _deleteButton;
    
    @FXML
    private Button _searchButton;

    @FXML
    private Button testButton;
    
    @FXML
    private Button _upButton;
    
    @FXML
    private Button _downButton;

    @FXML
    private Button saveButton;
    
    @FXML
    private ChoiceBox<String> _festivalVoice;

    @FXML
    private TextArea displayTextArea;

    @FXML
    private ListView<HBox> audioBox;

    @FXML
    private Button createButton;

    @FXML
    private Button returnButton;

    @FXML
    private TextField videoName;
    
    
    /**
     * this initialises choice box to allow for the selection of different festival voices
     */
    @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
    	ObservableList<String> voices = FXCollections.observableArrayList();
    	voices.addAll("Default","(voice_akl_nz_cw_cg_cg)","(voice_akl_nz_jdt_diphone)");
    	_festivalVoice.setItems(voices);
    }
    
    
	@FXML
	void handleCreate() {
		if(_runningThread) {
			return;
		}
		String name = videoName.getText();
		if(name.isEmpty()) {
			error("Please enter a name for your creation");
			return;
		}

		
		Object[] audioFiles = audioBox.getChildrenUnmodifiable().toArray();
		String audioFileNames="";
		for(Object audio:audioFiles) {
			audioFileNames = audioFileNames+audio.toString();
		}
		
		System.out.println(audioFileNames);
		RunBash mergeAudio = new RunBash("sox "+ audioFileNames +" ./temp/output.wav");
		_team.submit(mergeAudio);	
		mergeAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				RunBash audioLengthSoxi = new RunBash("soxi -D ./temp/output.wav");
				_team.submit(audioLengthSoxi);
				_runningThread = true;
				audioLengthSoxi.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						_runningThread=false;
						double audioLength;

						try {
							audioLength = Double.parseDouble(audioLengthSoxi.get().get(0));
							RunBash createVideo = new RunBash("ffmpeg -i ./temp/output.wav -vn -ar 44100 -ac 2 -b:a 192k ./temp/output.mp3 &> /dev/null "
									+ "; ffmpeg -f lavfi -i color=c=blue:s=320x240:d="+audioLength 
									+ " -vf \"drawtext=fontfile=/path/to/font.ttf:fontsize=30: "
									+ "fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text="+_term+"\" ./temp/"+name+".mp4 &> /dev/null "
									+ "; ffmpeg -i ./temp/"+name +".mp4 -i ./temp/output.mp3 -c:v copy -c:a aac -strict experimental "
									+ "./VideoCreations/"+name+".mp4  &> /dev/null");
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
	
	
	
	@FXML
	void handleSaveAudio(ActionEvent event) {
		audioCount++;
		String selectedText = displayTextArea.getSelectedText();
		String[] wordCount = selectedText.split("\\s+");
		
		if(selectedText.isEmpty()) {
			return;
		}else if(wordCount.length>20) {
			error("Can only save sections smaller than 20 words");
		}
		
		String voice = _festivalVoice.getSelectionModel().getSelectedItem();
		if(voice.contentEquals("Default") || voice.isEmpty()) {
		RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | text2wave -o ./temp/"+ audioCount + ".wav");
		_team.submit(audioCreation);
		_runningThread = true;
		audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				_runningThread=false;
			}
		});
		}else {
			RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | text2wave -o ./temp/"+ audioCount + ".wav " + "-eval \""+_festivalVoice.getSelectionModel().getSelectedItem()+"\"");
			_team.submit(audioCreation);
			_runningThread = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_runningThread=false;
				}
			});
		}
		new AudioBar(selectedText,audioCount+".wav",_audioList);
		audioBox.setItems(_audioList);

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
				_runningThread=false;
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
					_searchButton.setVisible(false);;
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	@FXML
	void handleTestAudio(ActionEvent event) {
		System.out.println(displayTextArea.getSelectedText());
		String selectedText = displayTextArea.getSelectedText();
		if(selectedText.isEmpty()) {
			return;
		}
		String voice = _festivalVoice.getSelectionModel().getSelectedItem();
		if(voice == null || voice.contentEquals("Default")) {
		_runningThread = true;	
		RunBash festivalFile = new RunBash( "echo '(SayText \"" +selectedText+"\")' > " +audioCount+".scm");	
		_team.submit(festivalFile);
		RunBash audioCreation = new RunBash("festival -b "+ audioCount+".scm");
		_team.submit(audioCreation);
		
		audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				_runningThread=false;
			}
		});
		}else {
			_runningThread = true;
			RunBash festivalFile = new RunBash("echo -e '"+voice+ "\n(SayText \"" +selectedText+"\")' > " +audioCount+".scm");	
			_team.submit(festivalFile);
			RunBash audioCreation = new RunBash("festival -b "+ audioCount+".scm");
			_team.submit(audioCreation);
		
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_runningThread=false;
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
