package application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * 
 * @author davidxiao
 * This class creates a VideoBar that displays buttons that allows the user to play or delete a video. 
 * If they choose to delete a video a confirmation option appears to the right of the Video bar.
 */
public class VideoBar {

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _deleteOption;
	private String _name;
	private VBox _parent;
	private Button _playButton=new Button("Play");
	private Button _deleteButton=new Button("delete");
	private HBox _bar;


	public VideoBar(String name, VBox parent){
		_name=name;
		_parent=parent;

		_playButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				RunBash play = new RunBash("ffplay -autoexit ./VideoCreations/"+_name+".mp4");
				_team.submit(play);

				play.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
					}
				});
			}
		});

		_deleteButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if(!_deleteOption) {
					_deleteOption=true;
					Text confirm = new Text("  |  Are you sure?");

					Button yesButton = new Button("yes");
					Button noButton = new Button("no");

					yesButton.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							RunBash delete = new RunBash("rm -f ./VideoCreations/"+_name+".mp4");
							_team.submit(delete);
							_parent.getChildren().remove(_bar);	
							if(_parent.getChildren().isEmpty()) {
								_parent.getChildren().add(new Text("No more Creations"));
							}

						}
					});

					noButton.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							_bar.getChildren().removeAll(confirm,yesButton,noButton);						
							_deleteOption=false;
						}
					});

					_bar.getChildren().addAll(confirm,yesButton,noButton);
				}
			}
		});

		Text text = new Text(_name);
		_bar = new HBox(_playButton,_deleteButton,text);
		_bar.setSpacing(9);
		_parent.getChildren().add(_bar);
	}


}

