import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.logging.Logger;


public class SavePositionsRunnable implements Runnable {
	protected static final Logger log = Logger.getLogger("Minecraft");

	private static final int CHARS_PER_PLAYER_ESTIMATE = 75;

	private MaraudersMap plugin;

	public SavePositionsRunnable(MaraudersMap plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		if (!plugin.isEnabled()) {
			plugin.onStopped();
			return;
		}
		
		doWrites();
		scheduleNext();
	}
	
	private void scheduleNext() {
		etc.getServer().addToServerQueue(this, MaraudersMap.DELAY_MILLIS);
	}
	
	private void doWrites() {
		RandomAccessFile file = plugin.getFile();
		if (file == null) {
			log.info("Couldn't get file for writing player positions");
			return;
		}
		
		try {
			file.seek(0);
		} catch (IOException ioException) {
			log.info("Couldn't seek to start of file -- " + ioException.toString());
		}
		
		
		List<Player> players = etc.getServer().getPlayerList();
		StringBuilder sb = new StringBuilder(players.size()
				* CHARS_PER_PLAYER_ESTIMATE);
		sb.append("[");
		for (int i = 0; i < players.size(); ++i) {
			Player player = players.get(i);
			sb.append("{\"name\": \"");
			sb.append(player.getName());
			sb.append("\", \"x\": ");
			sb.append(player.getX());
			sb.append(", \"y\": ");
			sb.append(player.getY());
			sb.append(", \"z\": ");
			sb.append(player.getZ());
			sb.append("}");
			if (i != players.size() - 1) {
				sb.append(",");
			}
		}
		sb.append("]");

		try {
			file.write(sb.toString().getBytes("UTF-8"));
			// truncate bytes after the current position
			file.setLength(file.getFilePointer());
		} catch (IOException ioException) {
			log.info("Couldn't write player data to file -- " + ioException.toString());
		}
	}
}
