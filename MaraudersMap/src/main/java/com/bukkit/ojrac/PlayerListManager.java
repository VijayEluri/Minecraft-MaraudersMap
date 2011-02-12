package com.bukkit.ojrac;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class PlayerListManager implements Runnable {
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	/** The name of the output file */
	private static final String FILE_NAME = "player-locations.json";
	
	/** Sleep this long between updates */
	private static final int DEFAULT_DELAY_MILLIS = 1000;
	
	/** Minimum sleep time */
	private static final int MIN_DELAY_MILLIS = 50;

	/** How long do we expect each player to be in JSON? */
	private static final int CHARS_PER_PLAYER_ESTIMATE = 50;
	
	/** How long to wait between updates */
	public final int delayMillis;
	
	/** The controlling plugin */
	private MaraudersMap plugin;

	/** The map file; contains a json representation of the logged in players */
	private RandomAccessFile file;

	/** The hash code of the last JSON we saved to disk */
	private int lastHashCode = 0;
	
	/**
	 * Don't forget to call startUpdating
	 * @param plugin The plugin instance
	 */
	public PlayerListManager(MaraudersMap plugin) {
		this.plugin = plugin;
		int delayMillis = plugin.getConfiguration().getInt("mmap-delay", DEFAULT_DELAY_MILLIS);
		if (delayMillis < MIN_DELAY_MILLIS) {
			delayMillis = MIN_DELAY_MILLIS;
			log.warning("Marauder's Map: Configured delay too small. If you need a 60fps, recompile the plugin ;)");
		}
		this.delayMillis = delayMillis;
		
		log.info("Marauder's Map: Set polling delay to " + delayMillis + "ms");
		openMapFile();
	}
	
	@Override
	public void run() {
		if (file == null) {
			log.warning("No location file found; stopping.");
			plugin.getServer().getScheduler().cancelTasks(plugin);
			return;
		}
		
		refreshMapFile();
	}
	
	/**
	 * Tries to open the map file for writing. Returns whether the operation was successful
	 * @return
	 */
	private boolean openMapFile() {
		try {
			file = new RandomAccessFile(FILE_NAME, "rws");
		} catch (FileNotFoundException fileNotFoundException) {
			log.warning("Couldn't open file " + FILE_NAME + ": " + fileNotFoundException.toString());
		}
		
		return file != null;
	}
	
	private void refreshMapFile() {
		String json = generateJson();
		int newHashCode = json.hashCode();
		if (newHashCode == lastHashCode) {
			return;
		}
		lastHashCode = newHashCode;
		
		writeJson(json);
	}
	
	/** Converts the list of players to JSON */
	private synchronized String generateJson() {
		Player[] players = plugin.getServer().getOnlinePlayers();
		
		if (players.length == 0) {
			return "[]";
		}
		
		StringBuilder sb = new StringBuilder(players.length
				* CHARS_PER_PLAYER_ESTIMATE + 4);
		sb.append("[");
		for (Player player : players) {
			sb.append("{\"name\":\"");
			sb.append(player.getDisplayName());

			World world = player.getWorld();
			if (world != null) {
				sb.append("\", \"world\":\"");
				// This is starting to get dangerous; a JSON writer might be a good idea
				sb.append(player.getWorld().getName().replace('"', '\''));
			}
			
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
