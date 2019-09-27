package application.scenebuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;

import application.RunBash;
import application.VideoBar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
public class SetImagesController {

	@FXML
	private GridPane _mainPane;
	
	
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _Images = new ArrayList<String>();
	
	{
	RunBash bash = new RunBash("List=`ls ./resources/temp/images` ; List=${List//.???/} ; printf \"${List// /.\\\\n}\\n\"");
	_team.submit(bash);
	bash.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
		@Override
		public void handle(WorkerStateEvent event) {

			try {
				_Images = bash.get();
			} catch (Exception e) {
				e.printStackTrace();
			}

			{
				ObservableList<ImageView> imageList = FXCollections.observableArrayList();
				int i =0;
				for(String image:_Images) {
					i++;
					ImageElement displayImage = new ImageElement(image);
					_mainPane.add(displayImage, i%3, i/3);
					
				}
				
			}
		}
	});
	}
	
	  

}
