package application;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {

		//Initiate the first Scene
		MainMenu menu = new MainMenu(primaryStage);
		menu.initiateFileSystem();
		menu.displayMenu();

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
}
