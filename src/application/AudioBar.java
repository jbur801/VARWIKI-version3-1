package application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.collections.ObservableList;
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
public class AudioBar extends HBox{

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _deleteOption;
	private String _name;
	private ObservableList<HBox> _parent;
	private Text _text;
	private HBox _bar =this;
	private static boolean _play=false;

	public AudioBar(String text,String fileName,ObservableList<HBox> parent){
		_parent=parent;
		_text= new Text(text);
		_text.wrappingWidthProperty().set(400);
		_name=fileName;
		this.getChildren().addAll(_text);
		this.setSpacing(2);	
		_parent.add(this);

	}

	
	public void delete() {
		if(!_deleteOption) {
			_deleteOption=true;
			Text confirm = new Text("Are you sure? ");

			Button yesButton = new Button("yes");
			Button noButton = new Button("no");

			yesButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					_parent.remove(_bar);
					}


		
			});

			noButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					_bar.getChildren().removeAll(confirm,yesButton,noButton);	
					_bar.getChildren().addAll(_text);
					_deleteOption=false;
				}
			});

			_bar.getChildren().removeAll(_text);
			_bar.getChildren().addAll(confirm,yesButton,noButton);
		}
		
	}
	public void moveDown() {
		int position = _parent.indexOf(_bar);
		if(position==_parent.size()-1) {
			return;
		}
		HBox temp = _parent.get(position+1);
		_parent.remove(position+1);
		_parent.remove(position);
		_parent.add(position,temp);
		_parent.add(position+1,_bar);
	}
	
	public void moveUp() {
		int position = _parent.indexOf(_bar);
		if(position==0) {
			return;
		}
		HBox temp = _parent.get(position-1);
		_parent.remove(position-1);
		_parent.remove(position-1);
		_parent.add(position-1,_bar);
		_parent.add(position,temp);
	}
	
	public void playAudio() {
		if(_play==true) {
			return;
		}
		_play=true;

		RunBash audioCreation = new RunBash("play ./temp/"+_name+".wav");
		_team.submit(audioCreation);
		audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				_play=false;
			}
		});
	}
	
	public String toString() {
		return " ./temp/" + _name;
	}


}
