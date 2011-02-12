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
    	listManager.markStale();
    }
    
    @Override
    public void onPlayerJoin(PlayerEvent event) {
    	listManager.markStale();
    }
    
    @Override
    public void onPlayerQuit(PlayerEvent event) {
    	listManager.markStale();
    }

	public void setListManager(PlayerListManager listManager) {
		this.listManager = listManager;
	}
}

