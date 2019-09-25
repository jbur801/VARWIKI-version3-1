package application.scenebuilder;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import application.*;

public class MainMenuController {

	
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _creations = new ArrayList<String>();
    
    @FXML
    private Button createButton;

    @FXML
    private VBox videoBox;

    @FXML
    private Button backwardButton;

    @FXML
    private Button playButton;

    @FXML
    private Button forwardButton;

    @FXML
    private Button muteButton;
    
    @FXML
    private ListView<Node> videoListView;

	{
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
					List<Node> noVideos = new ArrayList<Node>();
					noVideos.add(noCreations);
					ObservableList<Node> noList = FXCollections.observableArrayList(noVideos);
					videoListView.setItems(noList);
				}else {
				ObservableList<Node> videoList = FXCollections.observableArrayList();
					for(String video:_creations) {
						new VideoBar(video,videoList);
					}
					videoListView.setItems(videoList);
				}
			}
		});
	}
    
	

    @FXML
    void handleForward(ActionEvent event) {

    }

    @FXML
    void handleMute(ActionEvent event) {

    }

    @FXML
    void handlePlay(ActionEvent event) {

    }
    
	@FXML
    void handleBackward(ActionEvent event) {

    }
    
    @FXML
    void handleCreate() {
    	Main.changeScene("CreateMenu.fxml", this);
    }
}