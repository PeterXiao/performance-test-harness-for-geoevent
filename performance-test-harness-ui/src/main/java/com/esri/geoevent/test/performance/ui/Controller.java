package com.esri.geoevent.test.performance.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import com.esri.geoevent.test.performance.ui.controls.RestrictiveTextField;
import com.esri.geoevent.test.performance.ui.controls.TitledBorder;

public class Controller implements Initializable
{
	// UI Elements
	@FXML
	private TitledBorder modeBox;
	@FXML
	private ToggleGroup mode;
	@FXML
	private RadioButton producerMode;
	@FXML
	private RadioButton orchestratorMode;
	@FXML
	private RadioButton consumerMode;
	@FXML
	private ToggleButton connectButton;
	@FXML
	private Label portLabel;
	@FXML
	private RestrictiveTextField port;
	
	// statics
	private static final String DISCONNECTED_IMAGE_SOURCE = "images/disconnected.png"; 
	private static final String CONNECTED_IMAGE_SOURCE = "images/connected.png"; 
	private static final ImageView DISCONNECTED_IMAGE = new ImageView(new Image(Controller.class.getResourceAsStream(DISCONNECTED_IMAGE_SOURCE)));
	private static final ImageView CONNECTED_IMAGE = new ImageView(new Image(Controller.class.getResourceAsStream(CONNECTED_IMAGE_SOURCE)));
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{		
		modeBox.setTitle( UIMessages.getMessage("UI_MODE_LABEL") );
		producerMode.setText( UIMessages.getMessage("UI_PRODUCER_MODE_LABEL") );
		orchestratorMode.setText( UIMessages.getMessage("UI_ORCHESTRATOR_MODE_LABEL") );
		consumerMode.setText( UIMessages.getMessage("UI_CONSUMER_MODE_LABEL") );
		portLabel.setText( UIMessages.getMessage("UI_PORT_LABEL") );
		port.setPromptText( UIMessages.getMessage("UI_PORT_PROMPT") );
		setConnectButtonState(null);
	}
	
	@FXML
	public void setConnectButtonState(ActionEvent event)
	{
		// if its selected then we are connected 
		if( connectButton.isSelected() )
		{
			connectButton.setTooltip( new Tooltip( UIMessages.getMessage("UI_DISCONNECT_DESC")) );
			connectButton.setGraphic(CONNECTED_IMAGE);
		}
		// else we are disconnected
		else
		{
			connectButton.setTooltip( new Tooltip( UIMessages.getMessage("UI_CONNECT_DESC")) );
			connectButton.setGraphic(DISCONNECTED_IMAGE);
		}
	}
}
