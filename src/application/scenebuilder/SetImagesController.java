package application.scenebuilder;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.flickr4java.flickr.*;
import com.flickr4java.flickr.photos.*;

import application.Main;
import application.RunBash;
import application.VideoBar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
public class SetImagesController implements Initializable{

	@FXML
	private GridPane _mainPane;
	
	
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _images = new ArrayList<String>();
	private ObservableList<ImageElement> _imageList;
	


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		RunBash bash = new RunBash("ls ./resources/temp/images | cut -f1 -d'.'");
		_team.submit(bash);
		bash.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {

				try {
					_images = bash.get();		
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
					_imageList = FXCollections.observableArrayList();
					int i =0;
					for(String image:_images) {
						ImageElement displayImage = new ImageElement(image);
						_mainPane.add(displayImage, i%3, i/3);
						i++;	
					}
					
				
			}
		});
	}
	
	public List<ImageElement> getSelectedImages(){
		List<ImageElement> selected = new ArrayList<ImageElement>();
		for(ImageElement i:_imageList) {
			if(i.isSelected()) {
				selected.add(i);
			}
		}
		return selected;
	}
	
	  

}
