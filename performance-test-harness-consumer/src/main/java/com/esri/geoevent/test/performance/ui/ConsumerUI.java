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
import java.util.Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.apache.commons.lang3.StringUtils;

import com.esri.geoevent.test.performance.Mode;

public class ConsumerUI extends Application {

    static Stage primaryStage;

    static class AppCloser implements EventHandler<WindowEvent> {

        @Override
        public void handle(WindowEvent event) {
            Platform.exit();
            System.exit(0);
        }
    }

    @Override
    public void start(Stage primaryStage) {

        String modeStr = "Consumer";
        if (StringUtils.isEmpty(modeStr)) {
            System.err.print(UIMessages.getMessage("STARTUP_ERROR_MODE_NULL") + UIMessages.getMessage("STARTUP_MODE_TYPES", Mode.getAllowableValues()));
            Platform.exit();
            System.exit(0);
            return;
        }

        Mode mode = Mode.fromValue(modeStr);
        String fxmlFile = "";
        Object controller = null;
        switch (mode) {

            case Consumer:
                fxmlFile = "PerformanceCollector.fxml";
                controller = new ConsumerController();
                break;

            default:
                System.err.print(UIMessages.getMessage("STARTUP_ERROR_MODE_UNKNOWN", mode) + UIMessages.getMessage("STARTUP_MODE_TYPES", Mode.getAllowableValues()));
                Platform.exit();
                System.exit(0);
                return;
        }

        Platform.setImplicitExit(true);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            if (controller != null) {
                loader.setController(controller);
            }
            Parent parent = (Parent) loader.load();
            Scene scene = new Scene(parent);

            primaryStage.setOnCloseRequest(new AppCloser());
            primaryStage.setTitle(UIMessages.getMessage("UI_TITLE", mode));
            primaryStage.setScene(scene);
            primaryStage.show();
            ConsumerUI.primaryStage = primaryStage;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//	public static void main(String[] args)
//	{
//		launch(args);
//	}
    public void run() {
        launch();
    }
}
