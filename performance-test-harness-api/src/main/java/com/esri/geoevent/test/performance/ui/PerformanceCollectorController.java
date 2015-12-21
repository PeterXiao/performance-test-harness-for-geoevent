/*
  Copyright 1995-2015 Esri

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

  For additional information, contact:
  Environmental Systems Research Institute, Inc.
  Attn: Contracts Dept
  380 New York Street
  Redlands, California, USA 92373

  email: contracts@esri.com
 */
package com.esri.geoevent.test.performance.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import com.esri.geoevent.test.performance.Protocol;
import com.esri.geoevent.test.performance.ui.controls.RestrictiveTextField;
import com.esri.geoevent.test.performance.ui.controls.TitledBorder;

public abstract class PerformanceCollectorController implements Initializable
{
	//UI Elements
	@FXML
	protected Label titleLabel;
	@FXML
	protected TitledBorder commandListenerBox;
	@FXML
	protected Label protocolLabel;
	@FXML
	protected ComboBox<Protocol> protocol;
	@FXML
	protected ToggleButton connectButton;
	@FXML
	protected Label portLabel;
	@FXML
	protected RestrictiveTextField port;
	@FXML
	protected TitledBorder loggerBox;
	@FXML
	protected TextArea logger;
	@FXML
	protected Button clearBtn;
	@FXML
	protected Button copyBtn;
	@FXML
	protected RestrictiveTextField serverPort;
	
	//statics
	private static final String START_IMAGE_SOURCE = "images/play.png"; 
	private static final String STOP_IMAGE_SOURCE = "images/stop.png"; 
	private static final ImageView START_IMAGE = new ImageView(new Image(PerformanceCollectorController.class.getResourceAsStream(START_IMAGE_SOURCE)));
	private static final ImageView STOP_IMAGE = new ImageView(new Image(PerformanceCollectorController.class.getResourceAsStream(STOP_IMAGE_SOURCE)));
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{		
		commandListenerBox.setTitle( UIMessages.getMessage("UI_COMMAND_BOX_LABEL") );
		protocolLabel.setText( UIMessages.getMessage("UI_PROTOCOL_LABEL") );
		protocol.setValue( Protocol.TCP );
		serverPort.setPromptText( UIMessages.getMessage("UI_SERVER_PORT_PROMPT") );
		serverPort.setTooltip( new Tooltip(UIMessages.getMessage("UI_SERVER_PORT_DESC")) );
		// add a focus out event to save the state
		serverPort.focusedProperty().addListener((observable,oldValue,newValue)->{
			if( ! newValue )
			{
				saveState();
			}
		});
		portLabel.setText( UIMessages.getMessage("UI_PORT_LABEL") );
		port.setPromptText( UIMessages.getMessage("UI_PORT_PROMPT") );
		port.setTooltip( new Tooltip(UIMessages.getMessage("UI_PORT_DESC")) );
		// add a focus out event to save the state
		port.focusedProperty().addListener((observable,oldValue,newValue)->{
			if( ! newValue )
			{
				saveState();
			}
		});
		loggerBox.setTitle( UIMessages.getMessage("UI_LOGGER_BOX_LABEL") );
		copyBtn.setText( UIMessages.getMessage("UI_COPY_BUTTON_LABEL") );
		copyBtn.setTooltip( new Tooltip(UIMessages.getMessage("UI_COPY_BUTTON_DESC")) );
		clearBtn.setText( UIMessages.getMessage("UI_CLEAR_BUTTON_LABEL") );
		clearBtn.setTooltip( new Tooltip(UIMessages.getMessage("UI_CLEAR_BUTTON_DESC")) );
		
		toggleServerPortState(null);
		toggleRunningState(null);
		redirectSystemOutAndErrToTextArea();
	}
	
	@FXML
	public void toggleRunningState(final ActionEvent event)
	{
		// if its selected then we are started 
		if( connectButton.isSelected() )
		{
			connectButton.setTooltip( new Tooltip( UIMessages.getMessage("UI_STOP_DESC")) );
			connectButton.setGraphic(STOP_IMAGE);
			
			// disable
			protocol.setDisable(true);
			port.setDisable(true);
			serverPort.setDisable(true);
			start();
			saveState();
		}
		// else we are stopped
		else
		{
			connectButton.setTooltip( new Tooltip( UIMessages.getMessage("UI_START_DESC")) );
			connectButton.setGraphic(START_IMAGE);
			
			// enable
			protocol.setDisable(false);
			port.setDisable(false);
			serverPort.setDisable(false);
			stop();
		}
	}
	
	@FXML
	public void toggleServerPortState(final ActionEvent event)
	{
		if( protocol.getValue() == Protocol.TCP_SERVER || protocol.getValue() == Protocol.WEBSOCKET_SERVER )
		{
			serverPort.setVisible(true);
		}
		else
		{
			serverPort.setVisible(false);
		}
		saveState();
	}
	
	@FXML
	public void clearLogger(final ActionEvent event)
	{
		logger.clear();
	}
	
	@FXML
	public void copyLogger(final ActionEvent event)
	{
		final Clipboard clipboard = Clipboard.getSystemClipboard();
    final ClipboardContent content = new ClipboardContent();
    content.putString( logger.getText() );
    clipboard.setContent(content);
	}
	
	@FXML
	public void saveState(final ActionEvent event)
	{
		saveState();
	}
	
	private void redirectSystemOutAndErrToTextArea()
	{
		OutputStream out = new OutputStream() {
	      @Override
	      public void write(int b) throws IOException {
	      		Platform.runLater(() -> logger.appendText(String.valueOf((char) b)));
	      }
	  };
	  System.setOut(new PrintStream(out, true));
		OutputStream err = new OutputStream() {
	      @Override
	      public void write(int b) throws IOException {
	      		Platform.runLater(() -> logger.appendText(String.valueOf((char) b)));
	      }
	  };
	  System.setErr(new PrintStream(err, true));
	}
	
	protected abstract void start();
	
	protected abstract void stop();
	
	protected abstract void saveState();
	
	protected abstract void loadState();
}
