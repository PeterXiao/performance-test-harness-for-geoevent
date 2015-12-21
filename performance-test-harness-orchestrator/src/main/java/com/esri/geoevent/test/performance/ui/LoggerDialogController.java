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
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

public class LoggerDialogController implements Initializable {

    @FXML
    protected TextArea logger;
    @FXML
    protected Button clearBtn;
    @FXML
    protected Button copyBtn;
    @FXML
    protected Button closeBtn;

    // member vars
    private Stage dialogStage;
    private ClearLoggerEventListener listener;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        copyBtn.setText(UIMessages.getMessage("UI_COPY_BUTTON_LABEL"));
        copyBtn.setTooltip(new Tooltip(UIMessages.getMessage("UI_COPY_BUTTON_DESC")));
        clearBtn.setText(UIMessages.getMessage("UI_CLEAR_BUTTON_LABEL"));
        clearBtn.setTooltip(new Tooltip(UIMessages.getMessage("UI_CLEAR_BUTTON_DESC")));
        closeBtn.setText(UIMessages.getMessage("UI_CLOSE_BUTTON_LABEL"));
        closeBtn.setTooltip(new Tooltip(UIMessages.getMessage("UI_CLOSE_BUTTON_DESC")));
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
     * Sets the stage of this dialog.
     *
     * @param output Output Dialog
     */
    public void setOutputBuffer(final String output) {
        logger.setText(output);
        redirectSystemOutAndErrToTextArea();
    }

    /**
     * Set up a event handler when the logger area is cleared
     *
     * @param listener {@link ClearLoggerEventListener}
     */
    public void onClearLoggerEvent(final ClearLoggerEventListener listener) {
        this.listener = listener;
    }

    /**
     * handle the "Close" button being pressed.
     *
     * @param event ActionEvent
     */
    @FXML
    private void handleClose(final ActionEvent event) {
        dialogStage.close();
    }

    @FXML
    public void clearLogger(final ActionEvent event) {
        logger.clear();
        if (listener != null) {
            listener.handleClearLoggerEvent();
        }
    }

    @FXML
    public void copyLogger(final ActionEvent event) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(logger.getText());
        clipboard.setContent(content);
    }

    private void redirectSystemOutAndErrToTextArea() {
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

    interface ClearLoggerEventListener {

        void handleClearLoggerEvent();
    }
}
