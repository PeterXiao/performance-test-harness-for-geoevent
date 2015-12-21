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
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmationDialogController implements Initializable {

    @FXML
    private Label confirmMsgLabel;
    @FXML
    private Button okBtn;
    @FXML
    private Button cancelBtn;

    //member vars
    private Stage dialogStage;
    private boolean okClicked = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        okBtn.setText(UIMessages.getMessage("UI_OK_BTN_LABEL"));
        cancelBtn.setText(UIMessages.getMessage("UI_CANCEL_BTN_LABEL"));
    }

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage Dialog Stage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns true of the user clicked on the ok button
     *
     * @return <code>true</code> if the user clicked on the "ok" button
     * otherwise <code>false</code>.
     */
    public boolean isOkClicked() {
        return this.okClicked;
    }

    /**
     * Sets the confirmation message label to be displayed
     *
     * @param msg Message to be displayed
     */
    public void setConfirmationMsg(String msg) {
        if (confirmMsgLabel != null) {
            confirmMsgLabel.setText(msg);
        }
    }

    /**
     * handle the "Ok" button being pressed.
     *
     * @param event ActionEvent
     */
    @FXML
    private void handleOk(final ActionEvent event) {
        okClicked = true;
        dialogStage.close();
    }

    /**
     * handle the "Cancel" button being pressed.
     *
     * @param event ActionEvent
     */
    @FXML
    private void handleCancel(final ActionEvent event) {
        dialogStage.close();
    }
}
