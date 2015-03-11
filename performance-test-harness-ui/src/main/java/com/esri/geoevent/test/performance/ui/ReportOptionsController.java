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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.jaxb.Report;
import com.esri.geoevent.test.performance.report.AbstractFileRollOverReportWriter;
import com.esri.geoevent.test.performance.report.ReportType;

public class ReportOptionsController implements Initializable
{
	@FXML
	private Label										reportTypeLabel;
	@FXML
	private ComboBox<ReportType>		reportType;
	@FXML
	private Label										reportFileLocationLabel;
	@FXML
	private Label										selectedReportFileLocationLabel;
	@FXML
	private Button									reportFileLocationBtn;

	@FXML
	private Label										allCoumnsLabel;
	@FXML
	private Label										selectedCoumnsLabel;
	@FXML
	private ListView<String>				allColumns;
	@FXML
	private ListView<String>				selectedColumns;
	@FXML
	private Button									moveRightBtn;
	@FXML
	private Button									moveLeftBtn;

	@FXML
	private Button									okBtn;
	@FXML
	private Button									cancelBtn;

	// statics
	private static final String			DEFAULT_REPORT_FILE_LOCATION		= ReportOptionsController.class.getResource(".").getFile() + "reports";
	private static final String			DEFAULT_REPORT_FILE_NAME				= "report";
	private static final ReportType	DEFAULT_REPORT_TYPE							= ReportType.XLSX;
	private static final int				DEFAULT_MAX_NUM_OF_REPORT_FILES	= 10;

	// member vars
	private Stage										dialogStage;
	private Report									report;
	private boolean									okClicked												= false;

	public ReportOptionsController()
	{
		// set some defaults
		report = new Report();
		report.setType(DEFAULT_REPORT_TYPE);
		report.setMaxNumberOfReportFiles(DEFAULT_MAX_NUM_OF_REPORT_FILES);
		report.setReportFile(DEFAULT_REPORT_FILE_LOCATION + "/" + DEFAULT_REPORT_FILE_NAME + "." + report.getType().toString().toLowerCase());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		reportTypeLabel.setText(UIMessages.getMessage("UI_REPORT_TYPE_LABEL"));
		reportType.setItems(getReportTypes());
		reportType.setValue(report.getType());
		reportFileLocationLabel.setText(UIMessages.getMessage("UI_REPORT_FILE_LOCATION_LABEL"));
		reportFileLocationBtn.setText(UIMessages.getMessage("UI_REPORT_FILE_BROWSE_LABEL"));
		reportFileLocationBtn.setTooltip(new Tooltip(UIMessages.getMessage("UI_REPORT_FILE_BROWSE_DESC")));
		allCoumnsLabel.setText(UIMessages.getMessage("UI_ALL_COLUMNS_LABEL"));
		selectedCoumnsLabel.setText(UIMessages.getMessage("UI_SELECTED_COLUMNS_LABEL"));
		allColumns.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		selectedColumns.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		okBtn.setText(UIMessages.getMessage("UI_OK_BTN_LABEL"));
		cancelBtn.setText(UIMessages.getMessage("UI_CANCEL_BTN_LABEL"));

		// lookup current directory and add "/report" to it
		updateSelectedReportFile(report.getReportFile());
		updateSelectedReportColumns(report.getReportColumns());
	}

	/**
	 * Sets the stage of this dialog.
	 * 
	 * @param dialogStage
	 */
	public void setDialogStage(Stage dialogStage)
	{
		this.dialogStage = dialogStage;
	}

	/**
	 * Returns true of the user clicked on the ok button
	 * 
	 * @return <code>true</code> if the user clicked on the "ok" button otherwise <code>false</code>.
	 */
	public boolean isOkClicked()
	{
		return this.okClicked;
	}

	/**
	 * Sets the report object to initialize the dialog
	 * 
	 * @param report
	 *          {@link Report}
	 */
	public void setReport(Report report)
	{
		if (report != null)
		{
			this.report = report;

			// update report type
			if (report.getType() == null)
				report.setType(reportType.getValue());
			reportType.setValue(report.getType());

			// update the report file location
			if (StringUtils.isEmpty(report.getReportFile()))
				report.setReportFile(selectedReportFileLocationLabel.getText());
			updateSelectedReportFile(report.getReportFile());
			
			// update the report columns
			HashSet<String> columns = new HashSet<String>();
			if( report.getReportColumns() != null )
				columns.addAll( report.getReportColumns() );
			if( report.getAdditionalReportColumns() != null )
				columns.addAll( report.getAdditionalReportColumns() );
			updateSelectedReportColumns( new ArrayList<String>(columns) );
		}
	}

	/**
	 * Returns the report object which is created by this dialog
	 * 
	 * @return {@link Report}
	 */
	public Report getReport()
	{
		return this.report;
	}

	@FXML
	private void toggleReportType(final ActionEvent event)
	{
		if (report.getType() != reportType.getValue())
		{
			report.setType(reportType.getValue());
			String newFileName = FilenameUtils.removeExtension(report.getReportFile()) + "." + report.getType().toString().toLowerCase();
			updateSelectedReportFile(newFileName);
		}
	}

	@FXML
	private void chooseReportFile(final ActionEvent event)
	{
		String reportTypeStr = report.getType().toString();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(UIMessages.getMessage("UI_REPORT_FILE_CHOOSER_TITLE"));
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(reportTypeStr, "*." + reportTypeStr.toLowerCase()));
		fileChooser.setInitialFileName(DEFAULT_REPORT_FILE_NAME + "." + reportTypeStr.toLowerCase());
		File file = fileChooser.showSaveDialog(dialogStage);
		if (file != null)
		{
			updateSelectedReportFile(file.getAbsolutePath());
		}
	}

	@FXML
  private void moveRight(final ActionEvent event)
  {
  	if( allColumns.getSelectionModel().getSelectedItems() != null )
  	{
  		List<String> selectedColumnsList = new ArrayList<String>( selectedColumns.getItems() );
  		List<String> columnsToAddList = new ArrayList<String>( allColumns.getSelectionModel().getSelectedItems() );
  		selectedColumnsList.addAll(columnsToAddList);
  		updateSelectedReportColumns(selectedColumnsList);
  	}
  }

	@FXML
  private void moveLeft(final ActionEvent event)
  {
  	if( selectedColumns.getSelectionModel().getSelectedItems() != null && selectedColumns.getItems().size() > 1 )
  	{
  		List<String> selectedColumnsList = new ArrayList<String>( selectedColumns.getItems() );
  		List<String> columnsToRemoveList = new ArrayList<String>( selectedColumns.getSelectionModel().getSelectedItems() );
  		selectedColumnsList.removeAll(columnsToRemoveList);
  		updateSelectedReportColumns(selectedColumnsList);
  	}
  }
	
	/**
	 * handle the "Ok" button being pressed.
	 * 
	 * @param event
	 *          ActionEvent
	 */
	@FXML
	private void handleOk(final ActionEvent event)
	{
		if (isInputValid())
		{
			okClicked = true;
			dialogStage.close();
		}
	}

	/**
	 * handle the "Cancel" button being pressed.
	 * 
	 * @param event
	 *          ActionEvent
	 */
	@FXML
	private void handleCancel(final ActionEvent event)
	{
		dialogStage.close();
	}

	/**
	 * Validates the user input
	 * 
	 * @return true if the input is valid
	 */
	private boolean isInputValid()
	{
		return true;
	}

	private void updateSelectedReportFile(String fileName)
	{
		if (!StringUtils.isEmpty(fileName))
		{
			selectedReportFileLocationLabel.setText(fileName);
			selectedReportFileLocationLabel.setTooltip(new Tooltip(fileName));
			report.setReportFile(fileName);
		}
	}

	private ObservableList<ReportType> getReportTypes()
	{
		ArrayList<ReportType> list = new ArrayList<ReportType>();
		list.add(ReportType.XLSX);
		list.add(ReportType.CSV);
		return FXCollections.observableList(list);
	}
	
	private void updateSelectedReportColumns(List<String> selectedColumnsList)
	{
		List<String> currentSelection = selectedColumnsList;
		if( currentSelection == null || currentSelection.size() == 0 )
			currentSelection = new ArrayList<String>(AbstractFileRollOverReportWriter.getDefaultColumnNames());
		
		// remove all items from the all list
		List<String> allColumnsList = new ArrayList<String>(AbstractFileRollOverReportWriter.getAllColumnNames());
		allColumnsList.removeAll(currentSelection);
		allColumns.setItems(FXCollections.observableList(allColumnsList));
		
		// update the selected list
		selectedColumns.setItems(FXCollections.observableList(currentSelection));
	}
}
