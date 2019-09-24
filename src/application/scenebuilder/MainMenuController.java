package application.scenebuilder;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import application.*;

public class MainMenuController {

	
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _creations = new ArrayList<String>();

    @FXML
    private VBox videoBox;

    @FXML
    private Button createButton;

    
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
					videoBox.getChildren().add(noCreations);
				}else {
					for(String video:_creations) {
						new VideoBar(video,videoBox);
					}
				}
			}
		});
	}
    
    
    @FXML
    void handleCreate() {
    	Main.changeScene("CreateMenu.fxml", this);
    }
}