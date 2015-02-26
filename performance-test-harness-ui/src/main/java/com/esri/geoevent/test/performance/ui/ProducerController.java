package com.esri.geoevent.test.performance.ui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import com.esri.geoevent.test.performance.PerformanceCollector;
import com.esri.geoevent.test.performance.Protocol;
import com.esri.geoevent.test.performance.activemq.ActiveMQEventProducer;
import com.esri.geoevent.test.performance.kafka.KafkaEventProducer;
import com.esri.geoevent.test.performance.rabbitmq.RabbitMQEventProducer;
import com.esri.geoevent.test.performance.streamservice.StreamServiceEventProducer;
import com.esri.geoevent.test.performance.tcp.TcpEventProducer;
import com.esri.geoevent.test.performance.ui.controls.RestrictiveTextField;
import com.esri.geoevent.test.performance.ui.controls.TitledBorder;
import com.esri.geoevent.test.performance.websocket.WebsocketEventProducer;
import com.esri.geoevent.test.performance.websocket.server.WebsocketServerEverntProducer;

public class ProducerController implements Initializable
{
	// UI Elements
	@FXML
	private TitledBorder commandListenerBox;
	@FXML
	private Label protocolLabel;
	@FXML
	private ComboBox<Protocol> protocol;
	@FXML
	private ToggleButton connectButton;
	@FXML
	private Label portLabel;
	@FXML
	private RestrictiveTextField port;
	@FXML
	private TitledBorder loggerBox;
	@FXML
	private TextArea logger;
	@FXML
	private Button clearBtn;
	@FXML
	private Button copyBtn;
	
	// statics
	private static final String START_IMAGE_SOURCE = "images/play.png"; 
	private static final String STOP_IMAGE_SOURCE = "images/stop.png"; 
	private static final ImageView START_IMAGE = new ImageView(new Image(ProducerController.class.getResourceAsStream(START_IMAGE_SOURCE)));
	private static final ImageView STOP_IMAGE = new ImageView(new Image(ProducerController.class.getResourceAsStream(STOP_IMAGE_SOURCE)));
	private static final int DEFAULT_COMMAND_PORT = 5010;
	
	// our producer worker
	private PerformanceCollector producer;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{		
		commandListenerBox.setTitle( UIMessages.getMessage("UI_COMMAND_BOX_LABEL") );
		protocolLabel.setText( UIMessages.getMessage("UI_PROTOCOL_LABEL") );
		protocol.setItems( getProtocolList());
		protocol.setValue( Protocol.TCP );
		portLabel.setText( UIMessages.getMessage("UI_PORT_LABEL") );
		port.setPromptText( UIMessages.getMessage("UI_PORT_PROMPT") );
		port.setText(String.valueOf(DEFAULT_COMMAND_PORT));
		loggerBox.setTitle( UIMessages.getMessage("UI_LOGGER_BOX_LABEL") );
		copyBtn.setText( UIMessages.getMessage("UI_COPY_BUTTON_LABEL") );
		copyBtn.setTooltip( new Tooltip(UIMessages.getMessage("UI_COPY_BUTTON_DESC")) );
		clearBtn.setText( UIMessages.getMessage("UI_CLEAR_BUTTON_LABEL") );
		clearBtn.setTooltip( new Tooltip(UIMessages.getMessage("UI_CLEAR_BUTTON_DESC")) );
		
		toggleRunningState(null);
		redirectSystemOutAndErrToTextArea();
	}
	
	@FXML
	public void toggleRunningState(ActionEvent event)
	{
		// if its selected then we are started 
		if( connectButton.isSelected() )
		{
			connectButton.setTooltip( new Tooltip( UIMessages.getMessage("UI_STOP_DESC")) );
			connectButton.setGraphic(STOP_IMAGE);
			
			// disable
			protocol.setDisable(true);
			port.setDisable(true);
			start();
		}
		// else we are stopped
		else
		{
			connectButton.setTooltip( new Tooltip( UIMessages.getMessage("UI_START_DESC")) );
			connectButton.setGraphic(START_IMAGE);
			
			// enable
			protocol.setDisable(false);
			port.setDisable(false);
			stop();
		}
	}
	
	@FXML
	public void clearLogger(ActionEvent event)
	{
		logger.clear();
	}
	
	@FXML
	public void copyLogger(ActionEvent event)
	{
		final Clipboard clipboard = Clipboard.getSystemClipboard();
    final ClipboardContent content = new ClipboardContent();
    content.putString( logger.getText() );
    clipboard.setContent(content);
	}
	
	private void start()
	{
		// stop the producer if we are not stopped already
		if( producer != null )
			stop();
		
		switch (protocol.getValue())
		{
			case TCP:
//				if(isClusterable)
//				{
//					int connectionPort = NumberUtils.toInt(cmd.getOptionValue("cp"), 5775);
//					producer = new ClusterableTcpEventProducer(connectionPort);
//				}
//				else 
					producer = new TcpEventProducer();
				break;
			case WEBSOCKETS:
				producer = new WebsocketEventProducer();
				break;
			case ACTIVE_MQ:
				producer = new ActiveMQEventProducer();
				break;
			case RABBIT_MQ:
				producer = new RabbitMQEventProducer();
				break;
			case STREAM_SERVICE:
				producer = new StreamServiceEventProducer();
				break;
			case KAFKA:
			  producer = new KafkaEventProducer();
			  break;
			case WEBSOCKET_SERVER:
        final WebsocketServerEverntProducer wsProducer = new WebsocketServerEverntProducer();
        producer = wsProducer;
        Runtime.getRuntime().addShutdownHook(new Thread() {
          @Override
          public void run()
          {
            wsProducer.shutdown();
          }
        });
        break;
			default:
				return;
		}
		Integer portNumber = DEFAULT_COMMAND_PORT;
		try
		{
			portNumber = Integer.parseInt( port.getText() );
		}
		catch( NumberFormatException ignored )
		{
			//ignored
		}
		
		producer.listenOnCommandPort(portNumber, true);
	}
	
	private void stop()
	{
		if( producer != null )
		{
			producer.disconnectCommandPort();
			producer = null;
		}
	}
	
	private void redirectSystemOutAndErrToTextArea()
	{
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
	
	private ObservableList<Protocol> getProtocolList()
	{
		ArrayList<Protocol> list = new ArrayList<Protocol>();
		list.add(Protocol.ACTIVE_MQ);
		list.add(Protocol.KAFKA);
		list.add(Protocol.RABBIT_MQ);
		list.add(Protocol.STREAM_SERVICE);
		list.add(Protocol.TCP);
		list.add(Protocol.WEBSOCKETS);
		list.add(Protocol.WEBSOCKET_SERVER);
		return FXCollections.observableList(list);
	}
}
