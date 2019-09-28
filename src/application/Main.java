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

	private static ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private static Stage _stage;
	
	@Override
	public void start(Stage primaryStage) throws Exception {

		//Initiate the first Scene
	
		_stage=primaryStage;
		changeScene("scenebuilder/MainMenu.fxml",this);
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
	
	public static void changeScene(String fxml, Object location) {
		initiateFileSystem();
		try {
			 FXMLLoader loader = new FXMLLoader();
		        loader.setLocation(location.getClass().getResource(fxml));
		        Parent layout = loader.load();
		        Scene scene = new Scene(layout);
		        _stage.setScene(scene);
		        _stage.show();
			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	public static void initiateFileSystem() {
		_team.submit(new RunBash("rm -r ./resources/temp"));
		_team.submit(new RunBash("mkdir ./resources/VideoCreations ./resources/temp ./resources/temp/images"));

	}


	public static String getPathToResources() {
		
		return System.getProperty("user.dir") + "/bin/resources";
	}

	public static void clearImages() {
		_team.submit(new RunBash("rm -r ./resources/temp/images"));
		_team.submit(new RunBash("mkdir ./resources/temp/images"));
	}


}
