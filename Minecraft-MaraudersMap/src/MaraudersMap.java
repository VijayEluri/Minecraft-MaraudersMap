import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;


public class MaraudersMap extends Plugin {
	public static final long DELAY_MILLIS = 1000;
	private static final String FILE_NAME = "player-locations.json";
	
	protected static final Logger log = Logger.getLogger("Minecraft");
	
	/** Is the plugin enabled? */
	private boolean enabled;
	
	/** Has the plugin been started? */
	private boolean started = false;
	
	/** The players' positions */
	private RandomAccessFile file = null;
	
	/**
	 * Start the plugin loop
	 */
	public synchronized void start() {
		if (started) {
			return;
		}
		
		started = true;
		SavePositionsRunnable runnable = new SavePositionsRunnable(this);
		runnable.run();
	}
	
	/**
	 * Returns whether the operation was successful
	 * @return
	 */
	protected boolean openFile() {
		if (file != null) {
			return true;
		}
		file = null;
		try {
			file = new RandomAccessFile(FILE_NAME, "rws");
		} catch (FileNotFoundException fileNotFoundException) {
			log.warning("Couldn't open file " + FILE_NAME + ": " + fileNotFoundException.toString());
		}
		
		return file != null;
	}
	
	@Override
	public void initialize() {
		if (!openFile()) {
			log.warning("Couldn't initialize Marauder's Map file");
			this.enabled = false;
			return;
		}
		
		this.enabled = true;
		start();
	}
	
	@Override
	public void disable() {
		enabled = false;
	}

	@Override
	public void enable() {
		enabled = true;
		
		if (file != null) {
			start();
		}
	}
	
	/**
	 * Call this from the SavePositionsRunnable when it stops looping 
	 */
	void onStopped() {
		started = false;
	}

	public boolean isEnabled() {
		return enabled;
	}



	/**
	 * Get the file for writing
	 * @return
	 */
	public RandomAccessFile getFile() {
		return file;
	}
}
