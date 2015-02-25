package com.esri.geoevent.test.performance.ui;

import java.io.IOException;

import com.esri.geoevent.test.performance.Messages;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

		Platform.setImplicitExit(true);
		try
		{
			FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
			Parent parent = (Parent) loader.load();
			Scene scene = new Scene(parent);

			primaryStage.setOnCloseRequest(new AppCloser());
			primaryStage.setTitle( Messages.getMessage("UI_TITLE") );
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
