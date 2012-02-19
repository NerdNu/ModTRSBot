package nu.nerd.modtrsbot;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import nu.nerd.modtrsbot.events.BotCommandEvent;
import nu.nerd.modtrsbot.events.BotEventListener;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;

import com.avaje.ebean.PagingList;

import yetanotherx.bukkitplugin.ModTRS.ModTRS;
import yetanotherx.bukkitplugin.ModTRS.api.ModTRSAPI;
import yetanotherx.bukkitplugin.ModTRS.command.CommandHandler;
import yetanotherx.bukkitplugin.ModTRS.command.CommandResult;
import yetanotherx.bukkitplugin.ModTRS.command.ModTRSCommandSender;
import yetanotherx.bukkitplugin.ModTRS.command.ModreqCommand;
import yetanotherx.bukkitplugin.ModTRS.event.EventHandler;
import yetanotherx.bukkitplugin.ModTRS.model.ModTRSRequest;
import yetanotherx.bukkitplugin.ModTRS.model.ModTRSUser;
import yetanotherx.bukkitplugin.ModTRS.util.ModTRSFunctions;
import yetanotherx.bukkitplugin.ModTRS.validator.ValidatorHandler;

public class ModTRSBot extends JavaPlugin implements Listener, BotEventListener {
    protected static final Logger log = Logger.getLogger("Minecraft");
    public final Configuration config = new Configuration(this);
    private ModTRSAPI modtrsAPI = null;
    private Bot ircbot = null;
    private ModTRSHandler modtrsHandler;

	@Override
    public void onEnable()
    {
        File config_file = new File(getDataFolder(), "config.yml");
        if (!config_file.exists()) {
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        config.load();
        
        //getServer().getPluginManager().registerEvents(this, this);
        nu.nerd.modtrsbot.events.EventHandler.getInstance().register(this);
        
        Plugin modtrsPlugin = this.getServer().getPluginManager().getPlugin("ModTRS");

        modtrsAPI = ((ModTRS)getServer().getPluginManager().getPlugin("ModTRS")).getAPI();
        modtrsHandler = new ModTRSHandler(this);
        
        if (modtrsPlugin != null) {
            this.getServer().getPluginManager().enablePlugin(modtrsPlugin);
            modtrsAPI = ((ModTRS) modtrsPlugin).getAPI();
        }
        
        EventHandler.getInstance().register(modtrsHandler);
        
        if (config.IRC_AUTO_CONNECT) {
        	Connect();
        }
        
        Log("Enabled.");
    }
	
	@Override
    public void onDisable()
    {
        Log("Disabled.");
    }
	
	public void Log(String message) {
        log.log(Level.INFO, "[" + getDescription().getName() + "] " + getDescription().getVersion() + ": " + message);
	}
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
	{
		boolean success = false;
		if (command.getName().equalsIgnoreCase("mtb")) {
			if ( args.length == 0 ) {
				success = false;
			}
			
			else if (args[0].equalsIgnoreCase("connect")) {
				if (sender.hasPermission(Permissions.CONNECT)) {
					sender.sendMessage(ChatColor.GREEN + "[ModTRSBot] Connecting...");
				}
				
				else {
					SendPermissionError(sender);
				}
				success = true;
			}
			
			else if (args[0].equalsIgnoreCase("disconnect")) {
				if (sender.equals(Permissions.DISCONNECT)) {
					sender.sendMessage(ChatColor.GREEN + "[ModTRSBot] Disconnecting...");
				}
				
				else {
					SendPermissionError(sender);
				}
				success = true;
			}
			
			else if (args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission(Permissions.RELOAD)) {
					config.load();
					sender.sendMessage(ChatColor.GREEN + "[ModTRSBot] Config reloaded...");
				}
				
				else {
					SendPermissionError(sender);
				}
				success = true;
			}
		}
		return success;
	}
	
	private void Connect() {
		if (ircbot == null) {
			ircbot = new Bot(config);
		}
		
		if (ircbot.isConnected()) {
			ircbot.disconnect();
		}
		
		ircbot.setNick(config.IRC_USERNAME);
		
		if (ircbot.Connect(config.IRC_SERVER, config.IRC_PORT)) {
			ircbot.joinChannel(config.IRC_CHANNEL);
		}
	}
	
	public void SendClaimMessage(int id, ModTRSCommandSender sender) {
		//ModTRSRequest req = modtrsAPI.getRequestTable().getRequestFromId(id);
		SendMessage(config.IRC_CHANNEL, String.format("%s[#%d] Claimed by%s %s%s", config.IRC_MSG_PREFIX, id, (ModTRSFunctions.isUserOnline(sender.getName(), getServer())?config.ONLINE_NAME:config.OFFLINE_NAME), sender.getName(), config.CLEAR));
	}
	
	public void SendCreateMessage(int sender, String request) {
		ModTRSUser user;
		try {
			user = modtrsAPI.getUserTable().getUserFromId(sender);
			SendMessage(config.IRC_CHANNEL, String.format("%s%s created request %s", config.IRC_MSG_PREFIX, user.getName(), request));
		} catch (SQLException e) {
			Log("Failed to send create message, there was a SQLException");
		}
	}
	
	public void SendModBroadcast(ModTRSCommandSender sender, String text) {
		SendMessage(config.IRC_CHANNEL, String.format("%s[MB - %s] %s", config.IRC_MSG_PREFIX, sender.getName(), text));
	}
	
	public void SendReopenMessage(ModTRSCommandSender sender, int id) {
		if (IsValidRequest(id)) {
			SendMessage(config.IRC_CHANNEL, String.format("%s%s reopened request #%d", config.IRC_MSG_PREFIX, sender.getName(), id));
		}
	}
	
	public void SendHoldMessage(ModTRSCommandSender sender, int id) {
		if (IsValidRequest(id)) {
			SendMessage(config.IRC_CHANNEL, String.format("%s%s held request #%d", config.IRC_MSG_PREFIX, sender.getName(), id));
		}
	}
	
	public void SendRequestMessage(ModTRSRequest request) {
		Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(CommandHandler.TIMEDATE_FORMAT);
        calendar.setTimeInMillis(request.getTstamp());
        
        String username;
        
        try {
        	ModTRSUser user = modtrsAPI.getUserTable().getUserFromId(request.getUserId());
        	username = user.getName();
        } catch (SQLException e) {
        	username = "[Unknown]";
        }
        
        SendMessage(config.IRC_CHANNEL, String.format("%s[#%s - %s] %s - %s", config.IRC_MSG_PREFIX, request.getId(), sdf.format(calendar.getTime()), request.getText(), username));
	}
	
	public void SendCompleteMessage(ModTRSCommandSender sender, int id) {
		if (IsValidRequest(id)) {
			SendMessage(config.IRC_CHANNEL, String.format("%s%s completed request #%d", config.IRC_MSG_PREFIX, sender.getName(), id));
		}
	}
	
	public void SendMessage(String target, String message) {
		if (ircbot != null && ircbot.isConnected()) {
			ircbot.sendMessage(target, message);
		}
	}
	
	public boolean IsValidRequest(int id) {
		ModTRSRequest req;
		boolean status = false;
		try {
			req = modtrsAPI.getRequestTable().getRequestFromId(id);
			if (req != null) {
				status = true;
			} else {
				status = false;
			}
		} catch (SQLException e) {
			status = false;
		}
		
		return status;
	}
	
	private void SendPermissionError(CommandSender sender) {
		sender.sendMessage(ChatColor.RED + "You do not have access to that command.");
	}

	public boolean onBotCommand(BotCommandEvent event) {
		boolean status = false;
		
		Log("Got command! Command: " + event.command);
		if (event.command.equalsIgnoreCase("check")) {
			String[] parameters = this.getParameters(event.getArgs());
			int page = Integer.parseInt(parameters[0]);
			String type = parameters[1];
			
			if (!parameters[2].isEmpty()) {

				int checkid = Integer.parseInt(parameters[2].trim());
				Log("The check id is " + checkid);
				if (IsValidRequest(checkid)) {
	            
		            try {
						SendRequestMessage(modtrsAPI.getRequestTable().getRequestFromId(checkid));
						status = true;
					} catch (SQLException e) {
					}
				}
	        }
			else {
				PagingList<ModTRSRequest> pager;
				
				try {
					pager = modtrsAPI.getRequestTable().getRequestsPager(type, 10);
					
					if ( pager.getTotalRowCount() == 0) {
						SendMessage(config.IRC_CHANNEL, config.IRC_MSG_PREFIX + "There are currently no mod requests.");
					} else {
						int count = 0;
						for (ModTRSRequest request : pager.getPage(page - 1).getList()) {
							count++;
							
							SendRequestMessage(request);
						}
					}
					
					status = true;
				} catch (SQLException e) {
					Log("Failed to get page.");
				}
			
			}
		}
		return status;
	}
	
	private String[] getParameters(String[] args) {
        int page = 1;
        String type = "open";
        String id = "";

        for (String arg : args) {
            if (arg.length() < 2) {
                arg += " ";
            }

            if (arg.substring(0, 2).equals("p:")) {
                page = Integer.parseInt(arg.substring(2));
            } else if (arg.substring(0, 2).equals("t:")) {
                type = arg.substring(2);
            } else {
                id = arg;
            }
        }

        return new String[]{Integer.toString(page), type, id};
    }
}
