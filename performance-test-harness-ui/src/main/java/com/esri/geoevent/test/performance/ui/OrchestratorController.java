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

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import jfxtras.labs.scene.control.BigDecimalField;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.esri.geoevent.test.performance.Mode;
import com.esri.geoevent.test.performance.jaxb.RampTest;
import com.esri.geoevent.test.performance.jaxb.StressTest;
import com.esri.geoevent.test.performance.jaxb.Test;
import com.esri.geoevent.test.performance.jaxb.TestType;
import com.esri.geoevent.test.performance.jaxb.TimeTest;
import com.esri.geoevent.test.performance.ui.controls.RestrictiveTextField;

public class OrchestratorController implements Initializable
{
	//UI Elements
	@FXML
	public Label titleLabel;
	@FXML
	public MenuBar menuBar;
	@FXML
	public Menu fileMenu;
	@FXML
	public MenuItem fileOpenMenuItem;
	@FXML
	public MenuItem fileSaveMenuItem;
	@FXML
	public Menu optionsMenu;
	@FXML
	public MenuItem optionsReportMenuItem;
	@FXML
	public Menu helpMenu;
	@FXML
	public MenuItem helpAboutMenuItem;
	@FXML
	public VBox collectorBox;
	
	// producers
	@FXML
	public Label producersLabel;
	@FXML
	public TableView<ConnectableRemoteHost> producersTable;
	@FXML
	public TableColumn<ConnectableRemoteHost,String> producersNameColumn;
	@FXML
	public TableColumn<ConnectableRemoteHost,Boolean> producersConnectedColumn;
	@FXML
	public TextField producerHostName;
	@FXML
	public RestrictiveTextField producerPort;
	@FXML
	public Button producerAddBtn;
	
	// consumers
	@FXML
	public Label consumersLabel;
	@FXML
	public TableView<ConnectableRemoteHost> consumersTable;
	@FXML
	public TableColumn<ConnectableRemoteHost,String> consumersNameColumn;
	@FXML
	public TableColumn<ConnectableRemoteHost,Boolean> consumersConnectedColumn;
	@FXML
	public TextField consumerHostName;
	@FXML
	public RestrictiveTextField consumerPort;
	@FXML
	public Button consumerAddBtn;
	
	// simulation
	@FXML
	public VBox simulationBox;
	@FXML
	public Label testTypeLabel;
	@FXML
	public ComboBox<TestType> testType;
	
	// time test
	@FXML
	public GridPane timeTestGroup;
	@FXML
	public Label eventsPerSecLabel;
	@FXML
	public BigDecimalField eventsPerSec;
	@FXML
	public Label totalTimeInSecLabel;
	@FXML
	public BigDecimalField totalTimeInSec;
	@FXML
	public Label expectedResultCountPerSecLabel;
	@FXML
	public BigDecimalField expectedResultCountPerSec;
	@FXML
	public Label staggeringIntervalLabel;
	@FXML
	public BigDecimalField staggeringInterval;
	
	//stress test
	@FXML
	public GridPane stressTestGroup;
	@FXML
	public Label numOfEventsLabel;
	@FXML
	public BigDecimalField numOfEvents;
	@FXML
	public Label iterationsLabel;
	@FXML
	public BigDecimalField iterations;
	@FXML
	public Label expectedResultCountLabel;
	@FXML
	public BigDecimalField expectedResultCount;
	
	// ramp test
	@FXML
	public GridPane rampTestGroup;
	@FXML
	public Label minEventsLabel;
	@FXML
	public BigDecimalField minEvents;
	@FXML
	public Label maxEventsLabel;
	@FXML
	public BigDecimalField maxEvents;
	@FXML
	public Label eventsToAddPerTestLabel;
	@FXML
	public BigDecimalField eventsToAddPerTest;
	@FXML
	public Label expectedResultCountPerTestLabel;
	@FXML
	public BigDecimalField expectedResultCountPerTest;
	
	@FXML
	public Button applySimulationBtn;
	
	// member vars
	private Test test;
	
	// statics
	private static final Callback<TableColumn<ConnectableRemoteHost,Boolean>,TableCell<ConnectableRemoteHost,Boolean>> CONNECTABLE_CELL_FACTORY = (p) -> new ConnectedTableCell<>();
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		titleLabel.setText( Mode.Orchestrator.toString() );
		fileMenu.setText( UIMessages.getMessage("UI_FILE_MENU_LABEL") );
		fileOpenMenuItem.setText( UIMessages.getMessage("UI_FILE_OPEN_MENU_ITEM_LABEL") );
		fileSaveMenuItem.setText( UIMessages.getMessage("UI_FILE_SAVE_MENU_ITEM_LABEL") );
		optionsMenu.setText( UIMessages.getMessage("UI_OPTIONS_MENU_LABEL") );
		optionsReportMenuItem.setText( UIMessages.getMessage("UI_OPTIONS_REPORT_MENU_ITEM_LABEL") );
		helpMenu.setText( UIMessages.getMessage("UI_HELP_MENU_LABEL") );
		helpAboutMenuItem.setText( UIMessages.getMessage("UI_HELP_ABOUT_MENU_ITEM_LABEL") );
		
		//set up the tables
		//producers
		producersLabel.setText( UIMessages.getMessage("UI_PRODUCERS_LABEL") );
		producersNameColumn.setText( UIMessages.getMessage("UI_PRODUCERS_NAME_COL_LABEL") );
		producersNameColumn.prefWidthProperty().bind(producersTable.widthProperty().multiply(0.83));
		producersConnectedColumn.prefWidthProperty().bind(producersTable.widthProperty().multiply(0.16));
		producersConnectedColumn.setCellFactory(CONNECTABLE_CELL_FACTORY);
		producerHostName.setPromptText( UIMessages.getMessage("UI_PRODUCER_HOST_NAME_DESC") );
		producerPort.setPromptText( UIMessages.getMessage("UI_PRODUCER_PORT_DESC") );
		producerAddBtn.setTooltip( new Tooltip(UIMessages.getMessage("UI_PRODUCER_ADD_BTN_DESC")));
		
		//consumers
		consumersLabel.setText( UIMessages.getMessage("UI_CONSUMERS_LABEL") );
		consumersNameColumn.setText( UIMessages.getMessage("UI_CONSUMERS_NAME_COL_LABEL") );
		consumersNameColumn.prefWidthProperty().bind(consumersTable.widthProperty().multiply(0.83));
		consumersConnectedColumn.prefWidthProperty().bind(consumersTable.widthProperty().multiply(0.16));
		consumersConnectedColumn.setCellFactory(CONNECTABLE_CELL_FACTORY);
		consumerHostName.setPromptText( UIMessages.getMessage("UI_CONSUMER_HOST_NAME_DESC") );
		consumerPort.setPromptText( UIMessages.getMessage("UI_CONSUMER_PORT_DESC") );
		consumerAddBtn.setTooltip( new Tooltip(UIMessages.getMessage("UI_CONSUMER_ADD_BTN_DESC")));
		
		// simulation
		testTypeLabel.setText( UIMessages.getMessage("UI_TEST_TYPE_LABEL") );
		testType.setTooltip(new Tooltip(UIMessages.getMessage("UI_TEST_TYPE_DESC")) );
		testType.setItems(getTestTypes());
		testType.setValue(TestType.TIME);
		// time
		eventsPerSecLabel.setText( UIMessages.getMessage("UI_EVENTS_PER_SEC_LABEL") );
		totalTimeInSecLabel.setText( UIMessages.getMessage("UI_TOTAL_TIME_IN_SEC_LABEL") );
		expectedResultCountPerSecLabel.setText( UIMessages.getMessage("UI_EXPECTED_EVENTS_PER_SEC_LABEL") );
		staggeringIntervalLabel.setText( UIMessages.getMessage("UI_STAGGERING_INTERVAL_LABEL") );
		// stress
		numOfEventsLabel.setText( UIMessages.getMessage("UI_NUM_OF_EVENTS_LABEL") );
		iterationsLabel.setText( UIMessages.getMessage("UI_ITERATIONS_LABEL") );
		expectedResultCountLabel.setText( UIMessages.getMessage("UI_EXPECTED_EVENT_COUNT_LABEL") );
		// ramp
		minEventsLabel.setText( UIMessages.getMessage("UI_MIN_EVENTS_LABEL") );
		maxEventsLabel.setText( UIMessages.getMessage("UI_MAX_EVENTS_LABEL") );
		eventsToAddPerTestLabel.setText( UIMessages.getMessage("UI_EVENTS_TO_ADD_LABEL") );
		expectedResultCountPerTestLabel.setText( UIMessages.getMessage("UI_EXPECTED_EVENTS_PER_TEST_LABEL") );
		
		applySimulationBtn.setText( UIMessages.getMessage("UI_APPLY_SIMULATION_LABEL") );
		//set up state
		toggleTestType(null);
	}
	
	/**
   * Handle action related to input (in this case specifically only responds to
   * keyboard event CTRL-A).
   * 
   * @param event Input event.
   */
  @FXML
  private void handleKeyInput(final InputEvent event)
  {
     if (event instanceof KeyEvent)
     {
        final KeyEvent keyEvent = (KeyEvent) event;
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A)
        {
           provideAboutFunctionality();
        }
     }
  }
  
  /**
   * Handle action related to "About" menu item.
   * 
   * @param event Event on "About" menu item.
   */
  @FXML
  private void handleAboutAction(final ActionEvent event)
  {
     provideAboutFunctionality();
  }
  
  @FXML
  private void addProducer(final ActionEvent event)
  {
  	String hostName = producerHostName.getText();
  	int port = NumberUtils.toInt(producerPort.getText(), 0);
  	if( StringUtils.isEmpty(hostName) || port == 0 )
  	{
  		//TODO: Validation error
  	}
  	else
  	{
  		producersTable.getItems().add(new ConnectableRemoteHost(hostName, port));
  	}
  }
  
  @FXML
  private void addConsumer(final ActionEvent event)
  {
  	String hostName = consumerHostName.getText();
  	int port = NumberUtils.toInt(consumerPort.getText(), 0);
  	if( StringUtils.isEmpty(hostName) || port == 0 )
  	{
  		//TODO: Validation error
  	}
  	else
  	{
  		consumersTable.getItems().add(new ConnectableRemoteHost(hostName, port));
  	}
  }
  
  @FXML
  private void toggleTestType(final ActionEvent event)
  {
  	rampTestGroup.setVisible(false);
  	stressTestGroup.setVisible(false);
  	timeTestGroup.setVisible(false);
  	
  	TestType selectedTestType = testType.getValue();
  	switch( selectedTestType )
  	{
  		case RAMP:
  			rampTestGroup.setVisible(true);
  			break;
  		case STRESS:
  			stressTestGroup.setVisible(true);
  			break;
  		case TIME:
  			timeTestGroup.setVisible(true);
  			break;
  		default:
  			break;
  	}
  }
  
  @FXML
  private void applySimulation(final ActionEvent event)
  {
  	TestType type = testType.getValue();
  	if( ! isSimulationValid(type) )
  	{
  		//TODO: Display validation message
  		return;
  	}
  	
  	switch(type)
  	{
  		case RAMP:
  			RampTest rampTest = new RampTest();
  			rampTest.setEventsToAddPerTest(eventsToAddPerTest.getNumber().intValue());
  			rampTest.setMaxEvents(maxEvents.getNumber().intValue());
  			rampTest.setMinEvents(minEvents.getNumber().intValue());
  			if( expectedResultCountPerTest.getNumber() != null && expectedResultCountPerTest.getNumber().intValue() > 0 )
  				rampTest.setExpectedResultCountPerTest( expectedResultCountPerTest.getNumber().intValue() );
  			this.test = rampTest;
  			break;
  			
  		case STRESS:
  			StressTest stressTest = new StressTest();
  			if( expectedResultCount.getNumber() != null && expectedResultCount.getNumber().intValue() > 0 )
  				stressTest.setExpectedResultCount( expectedResultCount.getNumber().intValue() );
  			stressTest.setIterations(iterations.getNumber().intValue());
  			stressTest.setNumOfEvents(numOfEvents.getNumber().intValue());
  			this.test = stressTest;
  			break;
  			
  		case TIME:
  			TimeTest timeTest = new TimeTest();
  			if( expectedResultCountPerSec.getNumber() != null && expectedResultCountPerSec.getNumber().intValue() > 0 )
  				timeTest.setExpectedResultCountPerSec( expectedResultCountPerSec.getNumber().intValue() );
  			if( staggeringInterval.getNumber() != null && staggeringInterval.getNumber().intValue() > 0 )
  				timeTest.setExpectedResultCountPerSec( staggeringInterval.getNumber().intValue() );
  			timeTest.setEventsPerSec(eventsPerSec.getNumber().intValue());
  			timeTest.setTotalTimeInSec(totalTimeInSec.getNumber().intValue());
  			this.test = timeTest;
  			break;
  			
  		default:
  			break;
  	}
  }
  
  private boolean isSimulationValid(TestType type)
  {
  	switch(testType.getValue())
  	{
  		case RAMP:
  			if( eventsToAddPerTest.getNumber().intValue() <= 0)
  				return false;
  			if( maxEvents.getNumber().intValue() <= 0)
  				return false;
  			if( minEvents.getNumber().intValue() <= 0)
  				return false;
  			return true;
  			
  		case STRESS:
  			if( numOfEvents.getNumber().intValue() <= 0)
  				return false;
  			if( iterations.getNumber().intValue() <= 0)
  				return false;
  			return true;
  			
  		case TIME:
  			if( eventsPerSec.getNumber().intValue() <= 0)
  				return false;
  			if( totalTimeInSec.getNumber().intValue() <= 0)
  				return false;
  			return true;
  			
  		default:
  			break;
  	}
  	return false;
  }
  
  /**
   * Perform functionality associated with "About" menu selection or CTRL-A.
   */
  private void provideAboutFunctionality()
  {
     System.out.println("You clicked on About!");      
  }

  private ObservableList<TestType> getTestTypes()
	{
		ArrayList<TestType> list = new ArrayList<TestType>();
		list.add(TestType.TIME);
		list.add(TestType.STRESS);
		list.add(TestType.RAMP);
		return FXCollections.observableList(list);
	}
  
  /**
   * Helper static class to create connected/disconnected buttons based on the a boolean property
   * 
   * @param <T>
   */
  static class ConnectedTableCell<T> extends TableCell<T, Boolean> 
  {	
  	//statics
  	private static final String CONNECTED_IMAGE_SOURCE = "images/connected.png"; 
  	private static final String DISCONNECTED_IMAGE_SOURCE = "images/disconnected.png"; 
    
  	@Override
  	protected void updateItem(Boolean item, boolean empty)
  	{
  		super.updateItem(item, empty);
  		setAlignment(Pos.CENTER);
  		
  		if( empty || item == null )
  		{
  			setGraphic(null);
  			return;
  		}
  		
  		if(!item)
  		{
  			setGraphic(getConnectedButton());
  		}
  		else
  		{
  			setGraphic(getDisconnectedButton());
  		}
  	}
  	
  	private Button getConnectedButton()
  	{
  		Button connectedButton = new Button("", new ImageView(new Image(OrchestratorController.class.getResourceAsStream(CONNECTED_IMAGE_SOURCE))));
    	connectedButton.getStyleClass().add("buttons");
    	connectedButton.setTooltip(new Tooltip(UIMessages.getMessage("UI_CONNECT_DESC")));
    	return connectedButton;
  	}
  	
  	private Button getDisconnectedButton()
  	{
  		Button disconnectedButton = new Button("", new ImageView(new Image(OrchestratorController.class.getResourceAsStream(DISCONNECTED_IMAGE_SOURCE))));
  		disconnectedButton.getStyleClass().add("buttons");
    	disconnectedButton.setTooltip(new Tooltip(UIMessages.getMessage("UI_DISCONNECT_DESC")));
    	return disconnectedButton;
  	}
  }
}
