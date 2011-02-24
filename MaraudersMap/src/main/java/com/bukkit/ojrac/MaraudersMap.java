package com.bukkit.ojrac;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MaraudersMap for Bukkit
 *
 * @author ojrac
 */
public class MaraudersMap extends JavaPlugin {
	/** Guessed from the Bukkit source */
	private static final int MS_PER_TICK = 50;
	
	/** The plugin's runnable */
    private PlayerListManager listManager;
    
    @Override
    public void onEnable() {
    	if (listManager == null) {
    		listManager = new PlayerListManager(this);
    	}
    	
    	int tickDelay = listManager.delayMillis / MS_PER_TICK;
    	getServer().getScheduler().scheduleSyncRepeatingTask(this, listManager, tickDelay, tickDelay);
    }

	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
    }
}

