package com.esri.geoevent.test.performance.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.apache.commons.lang3.math.NumberUtils;

import com.esri.geoevent.test.performance.PerformanceCollector;
import com.esri.geoevent.test.performance.Protocol;
import com.esri.geoevent.test.performance.activemq.ActiveMQEventConsumer;
import com.esri.geoevent.test.performance.rabbitmq.RabbitMQEventConsumer;
import com.esri.geoevent.test.performance.streamservice.StreamServiceEventConsumer;
import com.esri.geoevent.test.performance.tcp.ClusterableTcpEventConsumer;
import com.esri.geoevent.test.performance.tcp.TcpEventConsumer;
import com.esri.geoevent.test.performance.websocket.WebsocketEventConsumer;

public class ConsumerController extends PerformanceCollectorController
{
	// statics
	private static final int DEFAULT_COMMAND_PORT = 5020;
	
	// our producer worker
	private PerformanceCollector consumer;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{		
		super.initialize(location, resources);
		
		protocol.setItems( getProtocolList());
		port.setText(String.valueOf(DEFAULT_COMMAND_PORT));
	}
	
	@Override
	protected void start()
	{
		// stop the producer if we are not stopped already
		if( consumer != null )
			stop();
		
		switch (protocol.getValue())
		{
			case TCP:
				consumer = new TcpEventConsumer();
				break;
			case TCP_SERVER:
				int connectionPort = NumberUtils.toInt(serverPort.getText(), 5775);
				consumer = new ClusterableTcpEventConsumer(connectionPort);
			break;
			case WEBSOCKETS:
				consumer = new WebsocketEventConsumer();
				break;
			case ACTIVE_MQ:
				consumer = new ActiveMQEventConsumer();
				break;
			case RABBIT_MQ:
				consumer = new RabbitMQEventConsumer();
				break;
			case STREAM_SERVICE:
				consumer = new StreamServiceEventConsumer();
				break;
			default:
				return;
		}
		
		int portNumber = NumberUtils.toInt(port.getText(), DEFAULT_COMMAND_PORT);
		consumer.listenOnCommandPort(portNumber, true);
	}
	
	@Override
	protected void stop()
	{
		if( consumer != null )
		{
			consumer.disconnectCommandPort();
			consumer = null;
		}
	}
	
	private ObservableList<Protocol> getProtocolList()
	{
		ArrayList<Protocol> list = new ArrayList<Protocol>();
		list.add(Protocol.ACTIVE_MQ);
		list.add(Protocol.RABBIT_MQ);
		list.add(Protocol.STREAM_SERVICE);
		list.add(Protocol.TCP);
		list.add(Protocol.TCP_SERVER);
		list.add(Protocol.WEBSOCKETS);
		return FXCollections.observableList(list);
	}
}
