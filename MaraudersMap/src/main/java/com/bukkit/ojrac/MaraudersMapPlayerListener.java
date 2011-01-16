package com.bukkit.ojrac;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle player movement and pass it through to the PlayerListManager
 * @author ojrac
 */
public class MaraudersMapPlayerListener extends PlayerListener {
    private PlayerListManager listManager;

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
    	listManager.movePlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerJoin(PlayerEvent event) {
    	listManager.addPlayer(event.getPlayer());
    }
    
    @Override
    public void onPlayerQuit(PlayerEvent event) {
    	listManager.removePlayer(event.getPlayer());
    }

	public PlayerListManager getListManager() {
		return listManager;
	}

	public void setListManager(PlayerListManager listManager) {
		this.listManager = listManager;
	}
}

