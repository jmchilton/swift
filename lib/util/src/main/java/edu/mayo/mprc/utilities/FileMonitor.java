package edu.mayo.mprc.utilities;

import edu.mayo.mprc.MprcException;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Monitors disk files for changes.
 * <ul>
 * <li>A file is considered changed if its last modification date changes (even if contents stay identical)</li>
 * <li>Deleting the file does not fire the change event.</li>
 * <li>Creating a previously nonexistent file fires the change event, unless the file is recreated with identical modification time it had before.</li>
 * </ul>
 * <p/>
 * Usage:
 * <ul>
 * <li>Implement FileListener interface</li>
 * <li>Create new instance of file monitor.</li>
 * <li>Add files/directories to watch</li>
 * </ul>
 */
public final class FileMonitor {
	private Timer timer;
	private Map<File, Long> files;
	private Collection<WeakReference<FileListener>> listeners;

	/**
	 * Create a file monitor instance with specified polling interval.
	 *
	 * @param pollingInterval Polling interval in milliseconds.
	 */
	public FileMonitor(final long pollingInterval) {
		files = new HashMap<File, Long>();
		listeners = new ArrayList<WeakReference<FileListener>>();

		timer = new Timer(true);
		timer.schedule(new FileMonitorNotifier(), 0, pollingInterval);
	}


	/**
	 * Stop the file monitor polling.
	 */
	public void stop() {
		timer.cancel();
	}


	/**
	 * Add file to listen for. File may be any java.io.File (including a
	 * directory) and may well be a non-existing file in the case where the
	 * creating of the file is to be trepped.
	 * <p/>
	 * More than one file can be listened for. When the specified file is
	 * created, modified or deleted, listeners are notified.
	 *
	 * @param file File to listen for.
	 */
	public void addFile(final File file) {
		if (!files.containsKey(file)) {
			final long modifiedTime = file.exists() ? file.lastModified() : -1;
			files.put(file, modifiedTime);
		}
	}


	/**
	 * Remove specified file for listening.
	 *
	 * @param file File to remove.
	 */
	public void removeFile(final File file) {
		files.remove(file);
	}


	/**
	 * Add listener to this file monitor.
	 *
	 * @param fileListener Listener to add.
	 */
	public void addListener(final FileListener fileListener) {
		if (fileListener == null) {
			throw new MprcException("Cannot add null listener to file monitor");
		}
		// Don't add if its already there
		for (final WeakReference<FileListener> reference : listeners) {
			final FileListener listener = reference.get();
			if (listener != null && listener.equals(fileListener)) {
				return;
			}
		}

		// Use WeakReference to avoid memory leak if this becomes the
		// sole reference to the object.
		listeners.add(new WeakReference<FileListener>(fileListener));
	}


	/**
	 * Remove listener from this file monitor.
	 *
	 * @param fileListener Listener to remove.
	 */
	public void removeListener(final FileListener fileListener) {
		for (Iterator<WeakReference<FileListener>> iterator = listeners.iterator(); iterator.hasNext(); ) {
			final WeakReference<FileListener> reference = iterator.next();
			final FileListener listener = reference.get();
			if (listener != null && listener.equals(fileListener)) {
				iterator.remove();
				break;
			}
		}
	}


	/**
	 * This is the timer thread which is executed every n milliseconds
	 * according to the setting of the file monitor. It investigates the
	 * file in question and notify listeners if changed.
	 */
	private class FileMonitorNotifier extends TimerTask {
		public void run() {
			// Loop over the registered files and see which have changed.
			// Use a copy of the list in case listener wants to alter the
			// list within its fileChanged method.
			final Collection<File> filesKeysCopy = new ArrayList<File>(FileMonitor.this.files.keySet());

			for (final File file : filesKeysCopy) {
				final long lastModifiedTime = FileMonitor.this.files.get(file);
				final long newModifiedTime = file.exists() ? file.lastModified() : -1;

				// Check if file has changed (but it must not have been deleted)
				if (newModifiedTime != lastModifiedTime && newModifiedTime != -1) {

					// Register new modified time
					FileMonitor.this.files.put(file, newModifiedTime);

					// Notify listeners
					for (Iterator<WeakReference<FileListener>> listenerIterator = listeners.iterator(); listenerIterator.hasNext(); ) {
						final WeakReference<FileListener> reference = listenerIterator.next();
						final FileListener listener = reference.get();

						// Remove from list if the back-end object has been GC'd
						if (listener == null) {
							listenerIterator.remove();
						} else {
							listener.fileChanged(file);
						}
					}
				}
			}
		}
	}
}
