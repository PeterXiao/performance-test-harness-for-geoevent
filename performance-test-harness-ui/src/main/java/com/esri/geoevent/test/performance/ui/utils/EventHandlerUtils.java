package com.esri.geoevent.test.performance.ui.utils;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

public class EventHandlerUtils
{
	public static final EventHandler<KeyEvent> DIGITS_ONLY_EVENT_HANDLER = new DigitsOnlyEventHandler();
	
	static class DigitsOnlyEventHandler implements EventHandler<KeyEvent>
	{
		@Override
		public void handle(KeyEvent event)
		{
			if( event.getCode().isLetterKey() || event.getCode().isWhitespaceKey() )
				event.consume();
		}
	}
}
