package nu.nerd.modtrsbot;

public class Configuration {
	private final ModTRSBot plugin;
	
	public String IRC_SERVER;
	public int IRC_PORT;
	public String IRC_USERNAME;
	public String IRC_REALNAME;
	public String IRC_CONNECT_COMMAND;
	public Boolean IRC_AUTO_CONNECT;
	public String IRC_CHANNEL;
	public String IRC_MSG_PREFIX;
	private char ColorStart = 3;
	//public String OFFLINE_NAME =  ColorStart + "4";
	//public String ONLINE_NAME =  ColorStart + "3";
	public String OFFLINE_NAME =  "";
	public String ONLINE_NAME =  "";
	public String CLEAR = ColorStart + "0,0";
	public String COMMAND_PREFIX = "";
	
	public Configuration(ModTRSBot plugin) {
		this.plugin = plugin;
	}
	
	public void save() {
		plugin.saveConfig();
	}
	
	public void load()
    {
        plugin.reloadConfig();

        IRC_SERVER = plugin.getConfig().getString("irc.server", "");
        IRC_PORT = plugin.getConfig().getInt("irc.port", 0);
        IRC_USERNAME = plugin.getConfig().getString("irc.username", "");
        IRC_REALNAME = plugin.getConfig().getString("irc.realname", "");
        IRC_CONNECT_COMMAND = plugin.getConfig().getString("irc.connect_command", "");
        IRC_AUTO_CONNECT = plugin.getConfig().getBoolean("irc.auto_connect", false);
        IRC_CHANNEL = plugin.getConfig().getString("irc.channel", "");
        IRC_MSG_PREFIX = plugin.getConfig().getString("irc.msgprefix", "");
        COMMAND_PREFIX = plugin.getConfig().getString("command.prefix", "@");
    }
}
