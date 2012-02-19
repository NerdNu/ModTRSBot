package nu.nerd.modtrsbot;

import java.io.IOException;
import java.util.Random;

import nu.nerd.modtrsbot.events.BotCommandEvent;
import nu.nerd.modtrsbot.events.EventHandler;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class Bot extends PircBot {
	Random rnd = new Random();
	Configuration config;
	
	public Bot(Configuration config) {
		this.config = config;
	}
	
	public void setNick(String nick) {
		if (!this.isConnected()) {
			this.setName(nick);
		}
	}
	
	public Boolean Connect(String hostname, int port) {
		Boolean success = true;
		
		try {
			this.connect(hostname, port);
		} catch (NickAlreadyInUseException e) {
			this.changeNick(this.getName() + (rnd.nextInt(100) + 10));
		} catch (IOException e) {
			success = false;
		} catch (IrcException e) {
			success = false;
		}
		
		return success;
	}
	
	private User getUserFromChannel(String channel, String user) {
		User[] users = this.getUsers(channel);
		for (User u : users) {
			if (u.getNick().equalsIgnoreCase(user)) {
				return u;
			}
		}
		return null;
	}
	
	@Override
	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (channel.equalsIgnoreCase(config.IRC_CHANNEL)) {
			BotCommandEvent event = new BotCommandEvent();
			String[] args = message.split(" ");
			
			if (args[0].startsWith(config.COMMAND_PREFIX)) {
			
				event.command = args[0].replace(config.COMMAND_PREFIX, "");
				event.sender = sender;
				
				User u = getUserFromChannel(channel, sender);
				
				event.isOp = u.isOp();
				event.isVoice = u.hasVoice();
				
				event.sentFrom = channel;
			
				int i = 0;
				for (String s : args) {
					if (i > 0) {
						event.args.add(s);
					}
					i++;
				}
				
				EventHandler.getInstance().dispatch(event);
			}
		}
	}

	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		
	}
}
