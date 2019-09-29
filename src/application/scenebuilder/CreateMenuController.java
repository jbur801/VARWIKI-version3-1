package application.scenebuilder;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import application.*;


/**
 * 
 * @author Student
 * This contains all functionality for creating a video.
 * 
 *
 */
public class CreateMenuController implements Initializable {

	private ObservableList<HBox> _audioList = FXCollections.observableArrayList();
	private String _term = "";
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _runningThread;
	private int audioCount=0;
	private SetImagesController _controller;
	private String __videoName;

	@FXML
	private Button _playButton;

	@FXML
	private TextField _searchTextArea;

	@FXML
	private Button _imageButton;

	@FXML
	private Button _deleteButton;

	@FXML
	private ListView<HBox> _audioBox;

	@FXML
	private Button _upButton;

	@FXML
	private Button _downButton;

	@FXML
	private Button _searchButton;

	@FXML 
	private CheckBox _imageSelection;

	@FXML
	private Button _testButton;

	@FXML
	private Button _saveButton;

	@FXML
	private TextArea _displayTextArea;

	@FXML
	private ChoiceBox<String> _festivalVoice;

	@FXML
	private Button _createButton;

	@FXML
	private Button _returnButton;

	@FXML
	private TextField _videoName;


	private Stage _stage;

	
	/**
	 * this initialises choice box to allow for the selection of different festival voices
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		ObservableList<String> voices = FXCollections.observableArrayList();
		voices.addAll("Default","(voice_akl_nz_cw_cg_cg)","(voice_akl_nz_jdt_diphone)");
		_festivalVoice.setItems(voices);
	}

	/**
	 * creation button used to build the final video
	 */
	@FXML
	void handleCreate(ActionEvent event) {
		
		//ERROR checking
		if(_runningThread) {
			error("Please Wait for Processes to Finish");
			return;
		}
		
		if(_term.isEmpty()) {
			error("No topic Searched");
			return;
		}else if(_audioList.isEmpty()){
			error("No Audio selected for Creation");
			return;
		}

		__videoName = _videoName.getText();

		if(__videoName.isEmpty()) {
			error("Creation must have a name");
			return;
		}else if((!__videoName.matches("[a-zA-Z0-9_-]*"))) {
			error("name can only contain letter, numbers, _ and - ");
			return;
		}else{
			//checks if file already exists
			RunBash f = new RunBash("[ -e ./resources/VideoCreations/"+__videoName+".mp4 ]");
			_team.submit(f);
			f.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent arg0) {
					if(f.getExitStatus()== 0) {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle("ERROR ");
						alert.setHeaderText("File already exists");
						alert.setContentText("would you like to overwrite?");
						Optional<ButtonType> result = alert.showAndWait();

						if(result.get() != ButtonType.OK) {
							return;
						}else {
							RunBash remove = new RunBash("rm ./resources/VideoCreations/"+__videoName+".mp4");
							_team.submit(remove);
							createVideo();
						}
					}else {
						createVideo();
					}
				}

			});
			return;
		}
	}


	/**
	 * THis method contains most/all of the bash and ffmpeg commands used in video creation
	 */
	public void createVideo() {
		List<String> images = getSelectedImages();
		String name = _videoName.getText();
		String audioFileNames="";
		for(Node audio:_audioList) {
			audioFileNames = audioFileNames+audio.toString()+".wav ";
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
							RunBash createVideoAudio = new RunBash("ffmpeg -i ./resources/temp/output.wav -vn -ar 44100 -ac 2 -b:a 192k ./resources/temp/output.mp3 &> /dev/null "
									+ "; ffmpeg -f lavfi -i color=c=blue:s=320x240:d="+audioLength 
									+ " -vf \"drawtext=fontfile=/path/to/font.ttf:fontsize=30: "
									+ "fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text="+_term+"\" ./resources/temp/"+name+"noImage.mp4 &> /dev/null ;");

							_team.submit(createVideoAudio);
							RunBash createVideo2;
							if(!_imageSelection.isSelected()) {
								createVideo2 = new RunBash("ffmpeg -i ./resources/temp/"+name +"noImage.mp4 -i ./resources/temp/output.mp3 -c:v copy -c:a aac -strict experimental "
										+ "./resources/VideoCreations/"+name+".mp4  &> /dev/null");
							} else {
								markImages(images);
								textFileBuilder(images,audioLength);
								videoMaker();
								createVideo2 = new RunBash("ffmpeg -i ./resources/temp/"+name +".mp4 -i ./resources/temp/output.mp3 -c:v copy -c:a aac -strict experimental "
										+ "./resources/VideoCreations/"+name+".mp4  &> /dev/null");

							}
							_team.submit(createVideo2);
							_runningThread = true;
							createVideo2.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
								@Override
								public void handle(WorkerStateEvent event) {
									_runningThread=false;
									Main.changeScene("MainMenu.fxml", this);
								}
							});
						} catch (NumberFormatException | InterruptedException | ExecutionException e) {
							error("Video Creation Failed");
							_runningThread=false;
							Main.changeScene("MainMenu.fxml", this);
						}
					}
				});

			}
		});
	}

	/**
	 * this method marks the images with the topic text
	 * @param images
	 */
	private void markImages(List<String> images) {
		for (String path: images) {
			System.out.println(path);
			RunBash mark= new RunBash("ffmpeg -i ./resources/temp/images/" + path + " -vf \"drawtext=text='"+ _term + "':fontcolor=white:fontsize=75:x=(w-text_w)/2: y=(h-text_h-line_h)/2:\" ./resources/temp/" + path);
			_team.submit(mark);
		}
	}



	/**
	 * if the user wises to return to the main menu they can, but are prompted with a confirmation msg
	 */
	@FXML
	void handleReturn(ActionEvent event) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Are you sure?");
		alert.setHeaderText("Unsaved work will be lost");
		alert.setContentText("Do you still want to EXIT?");
		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == ButtonType.OK) {
			Main.changeScene("MainMenu.fxml", this);
		}

	}

	/**
	 * creates the popup that is used to select images
	 */
	private void initializeSetImages() {
		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(getClass().getResource("SetImages.fxml"));
		Parent layout; 
		try {

			layout = loader.load();
			_controller=loader.getController();

			_controller.construct(this);
			Scene scene = new Scene(layout);
			_stage = new Stage();
			_stage.setScene(scene);
			_stage.initModality(Modality.APPLICATION_MODAL);
			_stage.initStyle(StageStyle.UNDECORATED);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Opens image selection menu
	 */
	@FXML
	void handleImages(ActionEvent event) {
		if(_searchButton.isVisible()==true) {
			error("please search for a subject first");
			return;
		} else if (_runningThread) {
			error("A Process is currently running");
			return;
		}
		popupSetImages();
	}
	
	
	public void popdownSetImages() {
		_stage.hide();;
	}
	
	private void popupSetImages() {
		_stage.show();
	}

	/**
	 * Saves highlighted text as audio file
	 * @param event
	 */
	@FXML
	void handleSaveAudio(ActionEvent event) {
		audioCount++;
		String selectedText = _displayTextArea.getSelectedText();
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
					_audioBox.setItems(_audioList);
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
					if(audioCreation.returnError() != null && audioCreation.returnError().substring(0, 10).contentEquals("SIOD ERROR")) {
						error("some words selected cannot be converted by selcted voice package");
						return;
					}
					new AudioBar(selectedText,audioCount+"",_audioList);
					_audioBox.setItems(_audioList);

				}
			});
		}

	}

	/**
	 * If there is an entered term, the term is searched on wikipedia
	 * @param event
	 */
	@FXML
	void handleSearch(ActionEvent event) {
		_term = _searchTextArea.getCharacters().toString();

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

					_displayTextArea.setText(text);
					_searchTextArea.setEditable(false);
					_searchButton.setVisible(false);
					_videoName.setText(_term);
					_imageButton.setText("loading...");
					GetFlickr imageDown = new GetFlickr(_searchTextArea.getText(), 9);
					_team.submit(imageDown);
					imageDown.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							_runningThread = false;
							initializeSetImages();
							_imageButton.setText("Modify display");
						}
					});
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * plays a preview of the selected audio text
	 * @param event
	 */
	@FXML
	void handleTestAudio(ActionEvent event) {
		String selectedText = _displayTextArea.getSelectedText();
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
					if(audioCreation.returnError() != null && audioCreation.returnError().substring(0, 10).contentEquals("SIOD ERROR")) {
						error("some words selected cannot be converted by selcted voice package");
						return;
					}
				}
			});
		}
	}

	/*
	 * 
	 * The following four methods are used to edit play existing creations
	 */
	@FXML
	void handlePlayAudio(ActionEvent event) {
		HBox audio = _audioBox.getSelectionModel().getSelectedItem();
		((AudioBar) audio).playAudio();
	}

	@FXML
	void handleDeleteAudio(ActionEvent event) {
		HBox audio = _audioBox.getSelectionModel().getSelectedItem();
		((AudioBar) audio).delete();
	}

	@FXML
	void handleMoveAudioDown(ActionEvent event) {
		HBox audio = _audioBox.getSelectionModel().getSelectedItem();
		((AudioBar) audio).moveDown();
	}

	@FXML
	void handleMoveAudioUp(ActionEvent event) {
		HBox audio = _audioBox.getSelectionModel().getSelectedItem();
		((AudioBar) audio).moveUp();
	}


	/**
	 * 
	 * @return currently selected images (default is all images selected)
	 */
	private List<String> getSelectedImages() {
		List<String> images = new ArrayList<String>();
		List<ImageElement> elements = _controller.getSelectedImages();
		System.out.println( elements.size());
		for(ImageElement name: elements) {
			images.add(name.toString());
		}
		return images;
	}

	/**
	 * builds text file used for ffmpeg command to create slideshow video
	 * @param images
	 * @param totalDuration
	 */
	private void textFileBuilder(List<String> images, double totalDuration) {
		double duration = totalDuration/images.size();
		String stringDuration = Double.toString(duration);
		String text = ""; 	
		String lastImage="";
		for(String name:images) {
			text= text +"file '" + name +"'\nduration " + stringDuration + "\n";
			lastImage=name;
		}
		text=text+"file '"+lastImage+"'";

		RunBash createFile = new RunBash("touch ./resources/temp/cmd.txt ; echo -e \""+text+ "\" > ./resources/temp/cmd.txt");
		_team.submit(createFile);



	}
	
	/**
	 * creates slideshow from stored and selected images
	 */
	private void videoMaker() {
		RunBash makeVideo = new RunBash("ffmpeg -f concat -safe 0 -i ./resources/temp/cmd.txt -r 25 -pix_fmt yuv420p -vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2'  ./resources/temp/"+ __videoName +".mp4");
		_team.submit(makeVideo);
	}



	/**
	 * resets current Create Menu (deletes everything)
	 */
	@FXML
	void handleReset(ActionEvent event) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Are you sure?");
		alert.setHeaderText("Unsaved work will be lost");
		alert.setContentText("Do you still want to RESET?");
		Optional<ButtonType> result = alert.showAndWait();

		if(result.get() == ButtonType.OK) {
			Main.changeScene("CreateMenu.fxml", this);
		}

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
