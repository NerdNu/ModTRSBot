package nu.nerd.modtrsbot.events;

import java.util.EventListener;

public interface BotEventListener extends EventListener {
	public boolean onBotCommand(BotCommandEvent event);
}
