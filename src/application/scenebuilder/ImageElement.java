package application.scenebuilder;

import java.io.IOException;
import java.net.URL;

import application.Main;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
    private RadioButton _isSelected;
    private Parent root;

    public ImageElement(String imageName) {
    	FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("ImageElement.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
        	String imageURL = Main.getPathToResources() + "/temp/" + imageName + ".jpg";
            fxmlLoader.load();
            Image image = new Image(imageURL,200,200,false,false,true);
            _imageHolder.setImage(image);
            /**
            parentProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object oldP, Object newP) {
                    root = (Parent) newP;
                    registerDragEvent();
                }
            });
            **/
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    public void handleImageClicked() {
    _isSelected.fire();
    	if(!_isSelected.isSelected())
     _imageHolder.setEffect(new Glow());
    	else {
    		_imageHolder.setEffect(null); //________________________________---------------------------------
    	}
     
    }
    
	protected void registerDragEvent() {
		// TODO Auto-generated method stub
		
	}
}
