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

public class AboutDialogController implements Initializable {

    @FXML
    private Label aboutLabel;
    @FXML
    private Button okBtn;

    // member vars
    private Stage dialogStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        aboutLabel.setText(UIMessages.getMessage("UI_ABOUT_LABEL"));
        okBtn.setText(UIMessages.getMessage("UI_OK_BTN_LABEL"));
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
     * handle the "Ok" button being pressed.
     *
     * @param event ActionEvent
     */
    @FXML
    private void handleOk(final ActionEvent event) {
        dialogStage.close();
    }
}
