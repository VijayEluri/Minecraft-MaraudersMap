package com.bukkit.ojrac;
import java.io.File;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MaraudersMap for Bukkit
 *
 * @author ojrac
 */
public class MaraudersMap extends JavaPlugin {
    private final MaraudersMapPlayerListener playerListener = new MaraudersMapPlayerListener();

    private PlayerListManager listManager;
    
    public MaraudersMap(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc,
    		File folder, File plugin, ClassLoader cLoader) {
    	super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    @Override
    public void onEnable() {
    	listManager = PlayerListManager.startUpdating(this);
    	playerListener.setListManager(listManager);
    	
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
    	pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
    	pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
    }

	public void onDisable() {
    	PlayerListManager.stopUpdating();
    }
}

