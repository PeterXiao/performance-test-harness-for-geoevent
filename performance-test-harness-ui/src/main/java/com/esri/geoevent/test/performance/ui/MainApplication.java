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

public class MainApplication extends Application
{
	static Stage	primaryStage;

	static class AppCloser implements EventHandler<WindowEvent>
	{
		@Override
		public void handle(WindowEvent event)
		{
			Platform.exit();
			System.exit(0);
		}
	}

	@Override
	public void start(Stage primaryStage)
	{
		Parameters parameters = getParameters();
		Map<String, String> args = parameters.getNamed();
		String modeStr = args.get("mode");
		if( StringUtils.isEmpty( modeStr ) )
		{
			System.err.print( UIMessages.getMessage("STARTUP_ERROR_MODE_NULL") + UIMessages.getMessage("STARTUP_MODE_TYPES", Mode.getAllowableValues()) );
			Platform.exit();
			System.exit(0);
			return;
		}
		Mode mode = Mode.fromValue(modeStr);
		String fxmlFile = "";
		switch( mode )
		{
			case Producer:
				fxmlFile = "Producer.fxml";
				break;
			case Consumer:
				fxmlFile = "Consumer.fxml";
				break;
			case Orchestrator:
				fxmlFile = "Orchestrator.fxml";
				break;
			default:
				System.err.print( UIMessages.getMessage("STARTUP_ERROR_MODE_UNKNOWN", mode) + UIMessages.getMessage("STARTUP_MODE_TYPES", Mode.getAllowableValues()) );
				Platform.exit();
				System.exit(0);
				return;
		}
		
		Platform.setImplicitExit(true);
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
			Parent parent = (Parent) loader.load();
			Scene scene = new Scene(parent);

			primaryStage.setOnCloseRequest(new AppCloser());
			primaryStage.setTitle( UIMessages.getMessage("UI_TITLE", mode) );
			primaryStage.setScene(scene);
			primaryStage.show();
			MainApplication.primaryStage = primaryStage;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
