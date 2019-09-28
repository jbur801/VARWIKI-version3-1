package application.scenebuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import application.Main;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class ImageElement extends AnchorPane{
	
    @FXML
    private ImageView _imageHolder;
    @FXML
    private RadioButton _button;
    private boolean _isSelected;
    private String _name;

    public ImageElement(String imageName) {
    	super();
    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ImageElement.fxml"));
        //fxmlLoader.setLocation(getClass().getResource("ImageElement.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try { 
          fxmlLoader.load();
        } catch (IOException exception) { 
          throw new RuntimeException(exception); 
       } 
        	_name = "/temp/images/" + imageName + ".jpg";
        	String imagePath = Main.getPathToResources() + _name;
        	URL imageURL;
			try {
				imageURL = new File(imagePath).toURI().toURL();
	            Image image = new Image(imageURL.toExternalForm(),200,200,false,false,true);
	            _imageHolder.setImage(image);
	            _isSelected=true;
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
            /**
            parentProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object oldP, Object newP) {
                    root = (Parent) newP;
                    registerDragEvent();
                }
            });
            **/
    }

	public boolean isSelected() {
		return _isSelected;
	}
	
	@Override
	public String toString() {
		return _name;
	}
    
    @FXML
    public void handleImageClicked() {
    	//idk
    _isSelected = !_isSelected;
    System.out.println(_isSelected);
    _button.setSelected(_isSelected);
    	if(_isSelected){
    _imageHolder.setOpacity(1);
    }else {
    		_imageHolder.setOpacity(0.2); 
    	}
     
    }
    
	protected void registerDragEvent() {
		// TODO Auto-generated method stub
		
	}
}
