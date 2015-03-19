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

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import jfxtras.labs.scene.control.BigDecimalField;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.esri.geoevent.test.performance.jaxb.Fixture;
import com.esri.geoevent.test.performance.jaxb.RampTest;
import com.esri.geoevent.test.performance.jaxb.RemoteHost;
import com.esri.geoevent.test.performance.jaxb.Simulation;
import com.esri.geoevent.test.performance.jaxb.StressTest;
import com.esri.geoevent.test.performance.jaxb.Test;
import com.esri.geoevent.test.performance.jaxb.TestType;
import com.esri.geoevent.test.performance.jaxb.TimeTest;
import com.esri.geoevent.test.performance.ui.controls.RestrictiveTextField;

public class FixtureController implements Initializable
{
	//UI Elements
	@FXML
	public Label nameLabel;
	@FXML
	public TextField nameField;
	@FXML
	public Button editNameBtn;
	
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
	private Fixture fixture;
	private boolean isDefault;
	private boolean isEditingName;
	
	// statics
	private static final Callback<TableColumn<ConnectableRemoteHost,Boolean>,TableCell<ConnectableRemoteHost,Boolean>> CONNECTABLE_CELL_FACTORY = (p) -> new ConnectedTableCell<>();
	private static final String SAVE_IMAGE_SOURCE = "images/save.png"; 
	private static final String EDIT_IMAGE_SOURCE = "images/pencil.png"; 
  
	
	/**
	 * Sets the isDefault flag.
	 * 
	 * @param fixture of type {@link Fixture}
	 */
	public void setIsDefault(boolean isDefault)
	{
		this.isDefault = isDefault;
		applyIsDefault(isDefault);
	}
	
	/**
	 * Sets the Fixture for this controller.
	 * 
	 * @param fixture of type {@link Fixture}
	 */
	public void setFixture(Fixture fixture)
	{
		this.fixture = fixture;
		applyFixture(fixture);
	}
	
	/**
	 * retrieves the fixture - populating it as needed
	 * 
	 * @return
	 */
	public Fixture getFixture()
	{
		return this.fixture;
	}
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		nameLabel.setText( UIMessages.getMessage("UI_FIXTURE_NAME_LABEL")  );
		nameField.setText( fixture != null ? fixture.getName() : "" );
		editNameBtn.setTooltip( new Tooltip( UIMessages.getMessage("UI_FIXTURE_EDIT_NAME_DESC") ) );
		
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
		toggleEditName(null);
		setEditNameState(isEditingName);
	}
  
	@FXML
	private void onNameFieldKeyPressed(final KeyEvent event)
	{
		if( event.getCode() == KeyCode.ENTER )
		{
			setEditNameState( ! isEditingName);
		}
	}
	
	@FXML
  private void toggleEditName(final ActionEvent event)
  {
  	setEditNameState( ! isEditingName);
  }
	
	private void setEditNameState(boolean newState)
	{
		if( newState != isEditingName )
		{
			isEditingName = newState;
			if( isEditingName )
	  	{
	  		editNameBtn.setTooltip( new Tooltip( UIMessages.getMessage("UI_FIXTURE_SAVE_NAME_DESC") ) );
	  		editNameBtn.setGraphic(new ImageView(new Image(FixtureController.class.getResourceAsStream(SAVE_IMAGE_SOURCE))));
	  		nameField.setEditable( true );
	  	}
	  	else
	  	{
	   		editNameBtn.setTooltip( new Tooltip( UIMessages.getMessage("UI_FIXTURE_EDIT_NAME_DESC") ) );
	  		editNameBtn.setGraphic(new ImageView(new Image(FixtureController.class.getResourceAsStream(EDIT_IMAGE_SOURCE))));
	  		nameField.setEditable( false );
	  	}
		}
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
  			setTest( rampTest );
  			break;
  			
  		case STRESS:
  			StressTest stressTest = new StressTest();
  			if( expectedResultCount.getNumber() != null && expectedResultCount.getNumber().intValue() > 0 )
  				stressTest.setExpectedResultCount( expectedResultCount.getNumber().intValue() );
  			stressTest.setIterations(iterations.getNumber().intValue());
  			stressTest.setNumOfEvents(numOfEvents.getNumber().intValue());
  			setTest( stressTest );
  			break;
  			
  		case TIME:
  			TimeTest timeTest = new TimeTest();
  			if( expectedResultCountPerSec.getNumber() != null && expectedResultCountPerSec.getNumber().intValue() > 0 )
  				timeTest.setExpectedResultCountPerSec( expectedResultCountPerSec.getNumber().intValue() );
  			if( staggeringInterval.getNumber() != null && staggeringInterval.getNumber().intValue() > 0 )
  				timeTest.setExpectedResultCountPerSec( staggeringInterval.getNumber().intValue() );
  			timeTest.setEventsPerSec(eventsPerSec.getNumber().intValue());
  			timeTest.setTotalTimeInSec(totalTimeInSec.getNumber().intValue());
  			setTest( timeTest );
  			break;
  			
  		default:
  			break;
  	}
  }
  
  /**
   * Validates the simulation inputs
   * 
   * @param type
   * @return
   */
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

  private ObservableList<TestType> getTestTypes()
	{
		ArrayList<TestType> list = new ArrayList<TestType>();
		list.add(TestType.TIME);
		list.add(TestType.STRESS);
		list.add(TestType.RAMP);
		return FXCollections.observableList(list);
	}
  
  /**
   * Helper method used to set the Test on the Fixture object
   * @param test
   */
  private void setTest(Test test)
  {
  	if( fixture == null )
  	{
  		fixture = new Fixture();
  	}
  	if( fixture.getSimulation() == null )
  	{
  		fixture.setSimulation( new Simulation() );
  	}
  	fixture.getSimulation().setTest(test);
  }
  
  private void applyIsDefault(boolean isDefault)
  {
  	if( isDefault )
  	{
  		editNameBtn.setVisible(false);
  		nameField.setEditable(false);
  	}
  	else
  	{
  		editNameBtn.setVisible(true);
  		nameField.setEditable(true);  		
  	}
  }
  
  /**
   * Helper method that is used to set the fixture object and apply it to all of the ui elements
   * 
   * @param fixture
   */
  private void applyFixture(Fixture fixture)
  {
  	if( fixture == null )
  		return;
  	
  	if( isDefault )
  	{
  		nameField.setEditable(false);
  		editNameBtn.setVisible(false);
  	}
  	else
  	{
  		nameField.setText( fixture.getName() );
  		nameField.setEditable(true);
  		editNameBtn.setVisible(true);  		
  	}
  	//populate the configs
  	if( fixture.getDefaultConfig() != null )
  	{
  		// apply if necessary
  		if( fixture.getConsumerConfig() != null )
  			fixture.getConsumerConfig().apply(fixture.getDefaultConfig());
  		if( fixture.getProducerConfig() != null )
  			fixture.getProducerConfig().apply(fixture.getDefaultConfig());
  	}
  	
  	// get the consumers
  	List<RemoteHost> consumers = new ArrayList<RemoteHost>();
  	if( fixture.getConsumerConfig() != null )
  	{
  		if( fixture.getConsumerConfig().getConsumers() != null )
  		{
  			consumers = fixture.getConsumerConfig().getConsumers();
  		}
  		else
  		{
  			consumers.add(fixture.getConsumerConfig().getDefaultRemoteHost());
  		}
  	}
  	List<ConnectableRemoteHost> connectableConsumers = convert(consumers);
  	consumersTable.getItems().clear();
  	consumersTable.getItems().addAll(connectableConsumers);
  	
  	// get the producers
   	List<RemoteHost> producers = new ArrayList<RemoteHost>();
   	if( fixture.getProducerConfig() != null )
   	{
   		if( fixture.getProducerConfig().getProducers() != null )
   		{
   			producers = fixture.getProducerConfig().getProducers();
   		}
   		else
   		{
   			producers.add(fixture.getProducerConfig().getDefaultRemoteHost());
   		}
   	}
   	List<ConnectableRemoteHost> connectableProducers = convert(producers);
   	producersTable.getItems().clear();
   	producersTable.getItems().addAll(connectableProducers);
   	
   	// set the test
   	if( fixture.getSimulation() != null && fixture.getSimulation().getTest() != null )
   	{
   		Test test = fixture.getSimulation().getTest();
   		testType.setValue(test.getType());
   		switch( test.getType() )
   		{
   			case RAMP:
   				RampTest rampTest = (RampTest) test;
   				eventsToAddPerTest.setNumber(new BigDecimal(rampTest.getEventsToAddPerTest()));
   				maxEvents.setNumber(new BigDecimal(rampTest.getMaxEvents()));
   				minEvents.setNumber(new BigDecimal(rampTest.getMinEvents()));
   				expectedResultCountPerTest.setNumber(new BigDecimal(rampTest.getExpectedResultCountPerTest()));
   				break;
    			
    		case STRESS:
    			StressTest stressTest = (StressTest) test;
    			numOfEvents.setNumber(new BigDecimal(stressTest.getNumOfEvents()));
    			iterations.setNumber(new BigDecimal(stressTest.getIterations()));
    			expectedResultCount.setNumber(new BigDecimal(stressTest.getExpectedResultCount()));
    			break;
    			
    		case TIME:
    			TimeTest timeTest = (TimeTest) test;
    			eventsPerSec.setNumber(new BigDecimal(timeTest.getEventsPerSec()));
    			totalTimeInSec.setNumber(new BigDecimal(timeTest.getTotalTimeInSec()));
    			expectedResultCountPerSec.setNumber(new BigDecimal(timeTest.getExpectedResultCountPerSec()));
    			staggeringInterval.setNumber(new BigDecimal(timeTest.getStaggeringInterval()));
    			break;
    			
    		default:
    			break;
   		}
   	}
  }
  
  private List<ConnectableRemoteHost> convert(List<RemoteHost> remoteHosts)
  {
  	if( remoteHosts == null )
  		return null;
  	
  	return remoteHosts.stream().map(host -> new ConnectableRemoteHost(host)).collect(Collectors.toList());
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
  		Button connectedButton = new Button("", new ImageView(new Image(FixtureController.class.getResourceAsStream(CONNECTED_IMAGE_SOURCE))));
    	connectedButton.getStyleClass().add("buttons");
    	connectedButton.setTooltip(new Tooltip(UIMessages.getMessage("UI_CONNECT_DESC")));
    	return connectedButton;
  	}
  	
  	private Button getDisconnectedButton()
  	{
  		Button disconnectedButton = new Button("", new ImageView(new Image(FixtureController.class.getResourceAsStream(DISCONNECTED_IMAGE_SOURCE))));
  		disconnectedButton.getStyleClass().add("buttons");
    	disconnectedButton.setTooltip(new Tooltip(UIMessages.getMessage("UI_DISCONNECT_DESC")));
    	return disconnectedButton;
  	}
  }
}
