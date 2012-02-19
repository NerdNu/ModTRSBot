package nu.nerd.modtrsbot.events;

import java.util.ArrayList;
import java.util.List;

public class BotCommandEvent extends BotEvent {

	public String sender;
	public String sentFrom;
	public List<String> args = new ArrayList<String>();
	public String command;
	public boolean isOp;
	public boolean isVoice;
	
	@Override
	public Type getType() {
		return Type.COMMAND;
	}

	public String[] getArgs() {
		return args.toArray(new String[]{});
	}
}
