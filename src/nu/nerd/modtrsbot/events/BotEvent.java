package nu.nerd.modtrsbot.events;

public abstract class BotEvent {
	public abstract Type getType();
	
	public enum Type {
		COMMAND
	};
}
