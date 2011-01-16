package com.bukkit.ojrac;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerListManager extends Thread {
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	/** The name of the output file */
	private static final String FILE_NAME = "player-locations.json";
	
	/** Sleep this long between updates */
	private static final int DELAY_MILLIS = 500;
	
	private static PlayerListManager runningThread = null;
	
	/**
	 * Private constructor; use PLM.startUpdating to get a Manager
	 * @param plugin The plugin instance
	 */
	private PlayerListManager(MaraudersMap plugin) {
		this.plugin = plugin;
	}

	/**
	 * Start and register the PlayerListManager. If one is already running, returns null and exits.
	 * @param plugin The plugin to stay attached to
	 * @return The PlayerListManager
	 */
	public synchronized static PlayerListManager startUpdating(MaraudersMap plugin) {
		if (plugin == null) {
			throw new IllegalArgumentException("Plugin must not be null");
		}
		
		if (runningThread != null) {
			return runningThread;
		}
		
		log.info("Starting Marauder's Map thread");
		runningThread = new PlayerListManager(plugin);
		runningThread.start();
		
		try {
			runningThread.setPriority(MIN_PRIORITY);
		} catch(SecurityException e) {
			log.info("Couldn't set Marauder's Map thread to minimum priority");
		}
		
		return runningThread;
	}
	
	public synchronized static void stopUpdating() {
		runningThread.running = false;
	}
	
	private MaraudersMap plugin;
	
	private boolean running = false;
	
	@Override
	public void run() {
		if (plugin == null) {
			
		}
		if (!openMapFile()) {
			log.warning("Disabling Marauder's Map: Couldn't open map file");
			return;
		}
		running = true;
		
		while (running) {
			if (stale) {
				refreshMapFile();
			}
			
			try {
				Thread.sleep(DELAY_MILLIS);
			} catch (InterruptedException interruptedException) {
				log.warning("Marauder's Map just freaked out: " + interruptedException.toString());
			}
		}
	}

	/** The map file; contains a json representation of the logged in players */
	private RandomAccessFile file;
	
	/**
	 * Tries to open the map file for writing. Returns whether the operation was successful
	 * @return
	 */
	private boolean openMapFile() {
		stale = true;
		try {
			file = new RandomAccessFile(FILE_NAME, "rws");
		} catch (FileNotFoundException fileNotFoundException) {
			log.warning("Couldn't open file " + FILE_NAME + ": " + fileNotFoundException.toString());
		}
		
		return file != null;
	}

	/** A named list of players, keyed by EntityId */
	private Map<Integer, Player> players = new HashMap<Integer, Player>();
	
	/** Adds a player to the list */
	public synchronized void addPlayer(Player player) {
		players.put(player.getEntityId(), player);
		stale = true;
	}
	
	/** Removes a player */
	public synchronized void removePlayer(Player player) {
		players.remove(player.getEntityId());
		stale = true;
	}
	
	/** Notes that a player has moved */
	public synchronized void movePlayer(Player player) {
		//players.put(player.getEntityId(), player);
		stale = true;
	}

	/** The hash code of the last JSON we saved to disk */
	private int lastHashCode = 0;
	
	/** Do we think the file needs to be refreshed? */
	private boolean stale;
	
	private void refreshMapFile() {
		stale = false;
		
		String json = generateJson();
		int newHashCode = json.hashCode();
		if (newHashCode == lastHashCode) {
			return;
		}
		lastHashCode = newHashCode;
		
		writeJson(json);
	}

	/** How long do we expect each player to be in JSON? */
	private static final int CHARS_PER_PLAYER_ESTIMATE = 50;
	
	/** Converts the list of players to JSON */
	private synchronized String generateJson() {
		if (players.size() == 0) {
			return "[]";
		}
		
		StringBuilder sb = new StringBuilder(players.size()
				* CHARS_PER_PLAYER_ESTIMATE + 4);
		sb.append("[");
		for (Player player : players.values()) {
			sb.append("{\"name\":\"");
			sb.append(player.getDisplayName());
			
			Location location = player.getLocation();
			sb.append("\",\"x\":");
			sb.append(location.getX());
			sb.append(",\"y\":");
			sb.append(location.getY());
			sb.append(",\"z\":");
			sb.append(location.getZ());
			sb.append("},");
		}
		sb.setLength(sb.length() - 1);
		sb.append("]");
		
		return sb.toString();
	}
	
	/** Writes the JSON to a file */
	private void writeJson(String json) {
		try {
			file.seek(0);
			file.write(json.getBytes("UTF-8"));
			// truncate bytes after the current position
			file.setLength(file.getFilePointer());
		} catch (IOException ioException) {
			log.info("Couldn't write player data to file -- " + ioException.toString());
		}
	}
}
