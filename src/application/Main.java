package application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	
	@Override
	public void start(Stage primaryStage) throws Exception {

		//Initiate the first Scene
		initiateFileSystem();

		
		try {
			 FXMLLoader loader = new FXMLLoader();
		        loader.setLocation(this.getClass().getResource("CreateMenu.fxml"));
		        Parent layout = loader.load();
		        Scene scene = new Scene(layout);
		        primaryStage.setScene(scene);
		        primaryStage.show();
			}catch(Exception e) {
				
			}
		//Sets the whole program to close when application window is closed
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent t) {
				System.exit(0);
			}
		});		
	}

	public static void main(String[] args) {
		launch();
	}
	
	public void initiateFileSystem() {
		_team.submit(new RunBash("rm -r ./temp"));
		_team.submit(new RunBash("mkdir ./VideoCreations ./temp"));

	}
}
