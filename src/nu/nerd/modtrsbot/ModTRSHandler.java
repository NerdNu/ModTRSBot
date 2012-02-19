package nu.nerd.modtrsbot;

import java.sql.SQLException;
import java.util.logging.Level;

import yetanotherx.bukkitplugin.ModTRS.event.CommandClaimEvent;
import yetanotherx.bukkitplugin.ModTRS.event.CommandCompleteEvent;
import yetanotherx.bukkitplugin.ModTRS.event.CommandHoldEvent;
import yetanotherx.bukkitplugin.ModTRS.event.CommandModBroadcastEvent;
import yetanotherx.bukkitplugin.ModTRS.event.CommandReopenEvent;
import yetanotherx.bukkitplugin.ModTRS.event.Listener;
import yetanotherx.bukkitplugin.ModTRS.event.SaveRowEvent;
import yetanotherx.bukkitplugin.ModTRS.model.ModTRSRequest;

public class ModTRSHandler extends Listener {
	ModTRSBot plugin;
	
	public ModTRSHandler(ModTRSBot plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onCommandClaim(CommandClaimEvent event) {
		plugin.SendClaimMessage(event.getId(), event.getSender());
	}
	
	public void onCommandComplete(CommandCompleteEvent event) {
		plugin.SendCompleteMessage(event.getSender(), event.getId());
	}
	
	public void onCommandHold(CommandHoldEvent event) {
		plugin.SendHoldMessage(event.getSender(), event.getId());
	}
	
	public void onCommandReopen(CommandReopenEvent event) {
		plugin.SendReopenMessage(event.getSender(), event.getId());
	}
	
	public void onSaveRow(SaveRowEvent event) {
		if (event.getModel() instanceof ModTRSRequest) {
			ModTRSRequest req = (ModTRSRequest) event.getModel();
			if (req.getStatus() == 0 && req.getId() == 0) {
				plugin.SendCreateMessage(req.getUserId(), req.getText());
			}
		}
	}
	
	public void onCommandModBroadcast(CommandModBroadcastEvent event) {
		plugin.SendModBroadcast(event.getSender(), event.getText());
	}
}
