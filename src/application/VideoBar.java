package application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/**
 * 
 * @author davidxiao
 * This class creates a VideoBar that displays buttons that allows the user to play or delete a video. 
 * If they choose to delete a video a confirmation option appears to the right of the Video bar.
 */
public class VideoBar extends HBox{

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _deleteOption;
	private Text _name;
	private ObservableList<HBox> _parent;
	private VideoBar _bar=this;


	public VideoBar(String name, ObservableList<HBox> videoList){
		_name= new Text(name);
		_parent=videoList;

		_bar.getChildren().add(_name);
		_bar.setSpacing(9);
		_parent.add(_bar);
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
					RunBash delete = new RunBash("rm -f ./resources/VideoCreations/"+_name.getText()+".mp4");
					_team.submit(delete);
					_parent.remove(_bar);	

					if(_parent.isEmpty()) {
						_parent.add(new HBox(new Text("No more Creations")));
					}


				}
			});

			noButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					_bar.getChildren().removeAll(confirm,yesButton,noButton);	
					_bar.getChildren().addAll(_name);
					_deleteOption=false;
				}
			});

			_bar.getChildren().removeAll(_name);
			_bar.getChildren().addAll(confirm,yesButton,noButton);
		}
	}

}


