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
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.esri.geoevent.test.performance.Mode;
import com.esri.geoevent.test.performance.jaxb.Fixture;
import com.esri.geoevent.test.performance.jaxb.Fixtures;
import com.esri.geoevent.test.performance.jaxb.Report;

public class OrchestratorController implements Initializable
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
	
	// member vars
	private Fixtures fixtures = new Fixtures();
	private Stage	stage;
	
	// statics
	
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
  private void handleKeyInput(final InputEvent event)
  {
     if (event instanceof KeyEvent)
     {
        final KeyEvent keyEvent = (KeyEvent) event;
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A)
        {
           provideAboutFunctionality();
        }
        if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.R)
        {
           showReportOptionsDialog();
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
  
  /**
   * Opens a dialog to edit the report options. If the user clicks OK, 
   * the changes are saved into the main configuration.
   * 
   * @param ActionEvent
   * @return true if the user clicked OK, false otherwise.
   */
  @FXML
  public void handleReportOptionsAction(final ActionEvent event) 
  {
  	showReportOptionsDialog();
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
   * Perform functionality associated with "About" menu selection or CTRL-A.
   */
  private void provideAboutFunctionality()
  {
     System.out.println("You clicked on About!");      
  }
  
  private void setupTabs()
  {
  	// setup the tabs
 		if( fixtures.getDefaultFixture() != null )
 			addFixtureTab( fixtures.getDefaultFixture(), true );
 		if( fixtures.getFixtures() != null )
 		{
 			for( Fixture fixture : fixtures.getFixtures() )
 				addFixtureTab( fixture, false );
 		}
  }
  
  private void addFixtureTab(Fixture fixture, boolean isDefault)
  {
  	try
  	{
	  	FXMLLoader loader = new FXMLLoader(getClass().getResource("Fixture.fxml"));	  	
	  	Parent fixtureTab = (Parent) loader.load();

	  	FixtureController controller = loader.getController();
	  	controller.setFixture(fixture);
	  	controller.setIsDefault( isDefault );

	  	Tab newTab = new Tab( fixture.getName() );
	  	newTab.setContent( fixtureTab );
	  	newTab.closableProperty().setValue(!isDefault);
			fixtureTabPane.getTabs().add(newTab);
  	}
		catch (IOException e)
		{
			e.printStackTrace();
		}
  }
}
