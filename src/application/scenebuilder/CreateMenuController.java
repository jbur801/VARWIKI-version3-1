package application.scenebuilder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.AudioBar;
import application.RunBash;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class CreateMenuController {

	private String _term;
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _runningThread;
	private int audioCount=0;

	@FXML
	private AnchorPane Search;

	@FXML
	private TextField searchTextArea;

	@FXML
	private Button searchButton;

	@FXML
	private Button testButton;

	@FXML
	private Button saveButton;

	@FXML
	private TextArea displayTextArea;

	@FXML
	private VBox audioBox;

	@FXML
	void handleSaveAudio(ActionEvent event) {
		audioCount++;
		String selectedText = displayTextArea.getSelectedText();
		
		if(selectedText.isEmpty()) {
			return;
		}
		
		RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | text2wave -o ./temp/"+ audioCount + ".wav");
		_team.submit(audioCreation);
		_runningThread = true;
		audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				_runningThread=false;
			}
		});
		new AudioBar(selectedText,audioCount+".wav",audioBox);

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

		searchButton.setText("Searching...");

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
					searchButton.setText("search");

					displayTextArea.setText(text);
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
		RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | festival --tts");
		_team.submit(audioCreation);
		_runningThread = true;
		audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				_runningThread=false;
			}
		});

	}

	public void error(String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("ERROR "+msg);
		alert.setHeaderText("ERROR");
		alert.setContentText(msg);
		alert.showAndWait();
	}

}
