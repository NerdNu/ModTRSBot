package nu.nerd.modtrsbot.events;

import java.util.ArrayList;
import java.util.List;

public class EventHandler {
	private List<BotEventListener> listeners = new ArrayList<BotEventListener>();
	
	private EventHandler() {
		
	}
	
	public void register(BotEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
	
	public void dispatch(BotEvent event) {
		for (BotEventListener listener : this.listeners) {
            switch (event.getType()) {
            case COMMAND:
            	listener.onBotCommand((BotCommandEvent) event);
            	break;
            }
		}
	}
	
	public static EventHandler getInstance() {
        return EventHandlerHolder.INSTANCE;
    }

    private static class EventHandlerHolder {
        private static final EventHandler INSTANCE = new EventHandler();
    }
}
