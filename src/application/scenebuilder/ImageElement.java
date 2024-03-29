package application.scenebuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import application.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.RadioButton;
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
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try { 
          fxmlLoader.load();
        } catch (IOException exception) { 
          throw new RuntimeException(exception); 
       } 
        	_name = imageName + ".jpg";
        	String imagePath = Main.getPathToResources() +"/temp/images/"+ _name;
        	System.out.println(imagePath);
        	URL imageURL;
			try {
				imageURL = new File(imagePath).toURI().toURL();
	            Image image = new Image(imageURL.toExternalForm(),200,200,false,false,true);
	            _imageHolder.setImage(image);
	            _isSelected=true;
			} catch (MalformedURLException e) {
				e.printStackTrace();	
			}
    }

    /**
     * returns boolean. If image is selected as video output
     * @return
     */
	public boolean isSelected() {
		return _isSelected;
	}
	
	/**
	 * returns the name of the image
	 */
	@Override
	public String toString() {
		return _name;
	}
    
	/**
	 * if the image is clicked, it is either elected or unselected
	 */
    @FXML
    public void handleImageClicked() {
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
