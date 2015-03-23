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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.Mode;
import com.esri.geoevent.test.performance.RunningException;
import com.esri.geoevent.test.performance.RunningState;
import com.esri.geoevent.test.performance.RunningStateListener;
import com.esri.geoevent.test.performance.TestHarnessExecutor;
import com.esri.geoevent.test.performance.jaxb.Fixture;
import com.esri.geoevent.test.performance.jaxb.Fixtures;
import com.esri.geoevent.test.performance.jaxb.Report;

public class OrchestratorController implements Initializable, RunningStateListener
{
	//UI Elements
	@FXML
	public Label titleLabel;
	
	// menus
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
	
	// fixtures
	@FXML
	public TabPane fixtureTabPane;
	@FXML
	public Button addFixtureBtn;
	@FXML
	public Button startBtn;
	@FXML 
	public Label statusLabel;
	
	// member vars
	private Fixtures fixtures = new Fixtures();
	private Stage	stage;
	private boolean	isRunning;
	private TestHarnessExecutor executor = null; 
	
	// statics
	private static final String START_IMAGE_SOURCE = "images/play.png"; 
	private static final String STOP_IMAGE_SOURCE = "images/stop.png"; 
  
	public static final String DEFAULT_FIXTURES_FILE = "fixtures.xml";
	
	/**
	 * Set up some defaults
	 */
	public OrchestratorController()
	{
		this.fixtures = new Fixtures();
		this.fixtures.setDefaultFixture(new Fixture("Default"));
		this.fixtures.setReport(new Report());
		this.fixtures.setFixtures(new ArrayList<Fixture>());
	}
	
	/**
	 * Sets the stage of this controller.
	 * 
	 * @param dialogStage
	 */
	public void setStage(Stage stage)
	{
		this.stage = stage;
	}
	
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
		addFixtureBtn.setTooltip( new Tooltip(UIMessages.getMessage("UI_ADD_FIXTURE_DESC")) );
		startBtn.setText( UIMessages.getMessage("UI_START_BTN_LABEL") );
		startBtn.setTooltip( new Tooltip(UIMessages.getMessage("UI_START_BTN_DESC")) );
		statusLabel.setText( "This is where the status changes will be placed." );
		// setup the tabs
		setupTabs();
	}
	
	/**
   * Handle action related to input (in this case specifically only responds to
   * keyboard event CTRL-A).
   * 
   * @param event Input event.
   */
  @FXML
  public void handleKeyInput(final InputEvent event)
  {
     if (event instanceof KeyEvent)
     {
        final KeyEvent keyEvent = (KeyEvent) event;
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A)
        {
           provideAboutFunctionality();
        }
        else if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.R)
        {
           showReportOptionsDialog();
        }
        else if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.O)
        {
           openFixturesFile();
        }
        else if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.S)
        {
           saveFixturesFile();
        }
     }
  }
  
  /**
   * Handle action related to "About" menu item.
   * 
   * @param event Event on "About" menu item.
   */
  @FXML
  public void handleAboutAction(final ActionEvent event)
  {
     provideAboutFunctionality();
  }
  
  /**
   * Opens a dialog to edit the report options. If the user clicks OK, 
   * the changes are saved into the main configuration.
   * 
   * @param ActionEvent
   */
  @FXML
  public void handleReportOptionsAction(final ActionEvent event) 
  {
  	showReportOptionsDialog();
  }
  
  /**
   * Opens a file chooser dialog to load a fixtures.xml configuration file.
   * 
   * @param event {@link ActionEvent}
   */
  @FXML
  public void handleOpenAction(final ActionEvent event)
  {
  	openFixturesFile();
  }
  
  /**
   * Saves the current configuration as an xml file.
   * 
   * @param event {@link ActionEvent}
   */
  @FXML
  public void handleSaveAction(final ActionEvent event)
  {
  	saveFixturesFile();
  }
  
  /**
   * Adds a new fixture to the TabPane and to the fixtures model
   * 
   * @param event ActionEvent
   */
  @FXML
  public void addFixture(final ActionEvent event)
  {
  	Fixture defaultFixture = fixtures.getDefaultFixture();
  	Fixture newFixture = new Fixture("NewFixture" + (fixtures.getFixtures().size()+1));
  	newFixture.apply(defaultFixture);
  	fixtures.getFixtures().add(newFixture);
  	addFixtureTab(newFixture, false);
  }
  
  /**
   * This is the main method which is used to start the simulation test
   * 
   * @param event {@link ActionEvent} not used.
   */
  @FXML
  public void startTest(final ActionEvent event)
  {
  	toggleRunningState( ! isRunning );
  }
  
  /**
   * Helper method to toggle the UI running state
   */
  private void toggleRunningState(boolean newRunningState)
  {
  	isRunning = newRunningState;
		if( isRunning )
  	{
			// start the executor
			try
			{
				this.executor = new TestHarnessExecutor(fixtures);
		  	this.executor.start();
		  	this.executor.setRunningStateListener(this);
			} 
			catch( RunningException error )
			{
				//TODO: error handling
				error.printStackTrace();
			}
	  	
			// toggle the ui
  		startBtn.setText( UIMessages.getMessage("UI_STOP_BTN_LABEL") );
  		startBtn.setTooltip( new Tooltip( UIMessages.getMessage("UI_STOP_BTN_DESC") ) );
  		startBtn.setGraphic(new ImageView(new Image(FixtureController.class.getResourceAsStream(STOP_IMAGE_SOURCE))));
  	}
  	else
  	{
  		// stop execution
  		if( this.executor != null )
  		{
  			if( this.executor.isRunning() )
  			{
  				this.executor.stop();
  			}
  			else
  			{
  				this.executor = null;
  			}
  		}
  		
  		startBtn.setText( UIMessages.getMessage("UI_START_BTN_LABEL") );
  		startBtn.setTooltip( new Tooltip( UIMessages.getMessage("UI_START_BTN_DESC") ) );
  		startBtn.setGraphic(new ImageView(new Image(FixtureController.class.getResourceAsStream(START_IMAGE_SOURCE))));
  	}
  }
  
  /**
   * Displays a file chooser to select a file to open
   */
  private void openFixturesFile()
  {
  	FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(UIMessages.getMessage("UI_OPEN_FILE_CHOOSER_TITLE"));
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
		fileChooser.setInitialFileName(DEFAULT_FIXTURES_FILE);
		File file = fileChooser.showOpenDialog(stage);
		if (file != null)
		{
			try
			{
				loadFile(file);
				setupTabs();
			} 
			catch( Exception error )
			{
				//TODO: Improve error handling and reporting
				error.printStackTrace();
			}
		}
  }
  
  /**
   * Helper method to load a file and unmarshall it using JAXB
   * 
   * @param file File to load
   * @throws JAXBException if there is a unmarshalling problem
   */
  private void loadFile(File file) throws JAXBException
  {
 		JAXBContext jaxbContext = JAXBContext.newInstance(Fixtures.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		StreamSource xml = new StreamSource(file.getAbsolutePath());
		this.fixtures = (Fixtures) unmarshaller.unmarshal(xml);
  }
  
  /**
   * Displays a file chooser to select a file location to save the configuration file.
   */
  private void saveFixturesFile()
  {
  	FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(UIMessages.getMessage("UI_SAVE_FILE_CHOOSER_TITLE"));
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
		fileChooser.setInitialFileName(DEFAULT_FIXTURES_FILE);
		File file = fileChooser.showSaveDialog(stage);
		if (file != null)
		{
			try
			{
				saveFile(file);
			} 
			catch( Exception error )
			{
				//TODO: Improve error handling and reporting
				error.printStackTrace();
			}
		}
  }
  
  /**
   * Saves the current fixtures file to disk.
   * 
   * @param file File where to save the configuration file.
   * @throws JAXBException
   * @throws XMLStreamException
   * @throws IOException
   */
  private void saveFile(File file) throws JAXBException, XMLStreamException, IOException
  {
  	JAXBContext jaxbContext = JAXBContext.newInstance(Fixtures.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		FileWriter fileWriter = new FileWriter(file);
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xmlWritter = factory.createXMLStreamWriter(fileWriter);
		marshaller.marshal(fixtures, xmlWritter);
		fileWriter.flush();
		fileWriter.close();
  }
  
  /**
   * Shows the report options dialog
   */
  private void showReportOptionsDialog() 
  {
    try 
    {
      // Load the fxml file and create a new stage for the popup
      FXMLLoader loader = new FXMLLoader(getClass().getResource("ReportOptions.fxml"));
      AnchorPane page = (AnchorPane) loader.load();
      Stage dialogStage = new Stage();
      dialogStage.setTitle( UIMessages.getMessage( "UI_REPORT_OPTIONS_TITLE" ) );
      dialogStage.initModality(Modality.APPLICATION_MODAL);
      dialogStage.initOwner( stage );
      Scene scene = new Scene(page);
      dialogStage.setScene(scene);

      // Set the person into the controller
      ReportOptionsController controller = loader.getController();
      controller.setDialogStage(dialogStage);
      controller.setReport(this.fixtures.getReport());	//TODO: we need to clone our Report Object Here

      // Show the dialog and wait until the user closes it
      dialogStage.showAndWait();
      if( controller.isOkClicked() )
      {
      	this.fixtures.setReport( controller.getReport() );
      }
    } 
    catch (IOException e) 
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Shows the confirmation dialog
   */
  private boolean showConfirmationDialog(String msg) 
  {
    try 
    {
      // Load the fxml file and create a new stage for the popup
      FXMLLoader loader = new FXMLLoader(getClass().getResource("ConfirmationDialog.fxml"));
      Parent page = (Parent) loader.load();
      Stage dialogStage = new Stage();
      dialogStage.setTitle( UIMessages.getMessage( "UI_CLOSE_TAB_TITLE" ) );
      dialogStage.initModality(Modality.APPLICATION_MODAL);
      dialogStage.initOwner( stage );
      Scene scene = new Scene(page);
      dialogStage.setScene(scene);

      // Set the person into the controller
      ConfirmationDialogController controller = loader.getController();
      controller.setDialogStage(dialogStage);
      controller.setConfirmationMsg( msg );
      
      // Show the dialog and wait until the user closes it
      dialogStage.showAndWait();
      return controller.isOkClicked();
    } 
    catch (IOException e) 
    {
      e.printStackTrace();
    }
    return false;
  }
  
  /**
   * Perform functionality associated with "About" menu selection or CTRL-A.
   */
  private void provideAboutFunctionality()
  {
  	try 
    {
      // Load the fxml file and create a new stage for the popup
      FXMLLoader loader = new FXMLLoader(getClass().getResource("AboutDialog.fxml"));
      Parent page = (Parent) loader.load();
      Stage dialogStage = new Stage();
      dialogStage.setTitle( UIMessages.getMessage( "UI_HELP_ABOUT_MENU_ITEM_LABEL" ) );
      dialogStage.initModality(Modality.APPLICATION_MODAL);
      dialogStage.initOwner( stage );
      Scene scene = new Scene(page);
      dialogStage.setScene(scene);

      // Set the person into the controller
      AboutDialogController controller = loader.getController();
      controller.setDialogStage(dialogStage);
      
      // Show the dialog and wait until the user closes it
      dialogStage.showAndWait();
    } 
    catch (IOException e) 
    {
      e.printStackTrace();
    }     
  }
  
  private void setupTabs()
  {
  	//clear them all out
  	fixtureTabPane.getTabs().clear();
  	
  	// setup the tabs
 		if( fixtures.getDefaultFixture() != null )
 		{
 			if( StringUtils.isEmpty(fixtures.getDefaultFixture().getName()) )
 				fixtures.getDefaultFixture().setName("Default");
 			addFixtureTab( fixtures.getDefaultFixture(), true );
 		}
 		if( fixtures.getFixtures() != null )
 		{
 			for( Fixture fixture : fixtures.getFixtures() )
 			{
 				fixture.apply(fixtures.getDefaultFixture());
 				addFixtureTab( fixture, false );
 			}
 		}
  }
  
  private void addFixtureTab(final Fixture fixture, boolean isDefault)
  {
  	try
  	{
	  	FXMLLoader loader = new FXMLLoader(getClass().getResource("Fixture.fxml"));	  	
	  	Parent fixtureTab = (Parent) loader.load();

	  	FixtureController controller = loader.getController();
	  	controller.setFixture(fixture);
	  	controller.setIsDefault(isDefault);

	  	Tab newTab = new Tab( fixture.getName() );
	  	newTab.setContent( fixtureTab );
	  	newTab.closableProperty().setValue(!isDefault);
	  	newTab.setOnCloseRequest(event->
	  	{
	  		boolean isOkClicked = showConfirmationDialog(UIMessages.getMessage("UI_CLOSE_TAB_LABEL", fixture.getName()));
	  		if ( ! isOkClicked)
	  		{
	  			event.consume();
	  		}
	  		else
	  		{
	  			fixtures.getFixtures().remove(fixture);
	  		}

	  		//TODO: Style the dialog
	  	});
			fixtureTabPane.getTabs().add(newTab);
  	}
		catch (IOException e)
		{
			e.printStackTrace();
		}
  }
  
  /**
   * Running State Listener - this is used so we get notified when the orchestration is complete
   */
  @Override
  public void onStateChange(RunningState newState)
  {
  	switch( newState.getType() )
  	{
  		case STOPPED:
  			toggleRunningState(false);
  			break;
  			
			default:
				break;
  	}
  }
}
