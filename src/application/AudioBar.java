package application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
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
public class AudioBar{

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _deleteOption;
	private String _name;
	private VBox _parent;
	private Button _playButton=new Button("Play");
	private Button _deleteButton=new Button("Delete");
	private Button _upButton=new Button("/\\");
	private Button _downButton=new Button("\\/");
	private Text _text;
	private HBox _bar;
	private static boolean _play=false;

	public AudioBar(String text,String fileName,VBox parent){
		_parent=parent;
		_playButton.setMinWidth(75);
		_deleteButton.setMinWidth(75);
		_text= new Text(text);
		_text.wrappingWidthProperty().set(400);
		_name=fileName;
		_bar = new HBox(_playButton,_deleteButton,_upButton,_downButton,_text);
		_bar.setSpacing(2);	
		_parent.getChildren().add(_bar);
		buttonSetup();

	}

	public void buttonSetup(){

		_playButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {	
				if(_play==true) {
					return;
				}
				_play=true;

				RunBash audioCreation = new RunBash("echo \"" + _text.getText() + "\" | festival --tts");
				_team.submit(audioCreation);
				audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						_play=false;
					}
				});

			}
		});
		_deleteButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {	
				_parent.getChildren().remove(_bar);
			}
		});
		_upButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {	
				int position = _parent.getChildren().indexOf(_bar);
				if(position==0) {
					return;
				}
				Node temp = _parent.getChildren().get(position-1);
				_parent.getChildren().remove(position-1);
				_parent.getChildren().remove(position-1);
				_parent.getChildren().add(position-1,_bar);
				_parent.getChildren().add(position,temp);
			}
		});
		_downButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {	
				int position = _parent.getChildren().indexOf(_bar);
				if(position==_parent.getChildren().size()-1) {
					return;
				}
				Node temp = _parent.getChildren().get(position+1);
				_parent.getChildren().remove(position+1);
				_parent.getChildren().remove(position);
				_parent.getChildren().add(position,temp);
				_parent.getChildren().add(position+1,_bar);

			}
		});
	}


}
