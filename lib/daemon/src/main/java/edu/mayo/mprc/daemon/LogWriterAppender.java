package edu.mayo.mprc.daemon;

import org.apache.log4j.*;
import org.apache.log4j.spi.LoggingEvent;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * Class appends filtered log to a given file.
 */
class LogWriterAppender extends WriterAppender {

	private String allowedMDCKey;
	private Object mdcValue;
	private HashSet<String> allowedThreadNames;
	private HashSet<Level> allowedLevels;
	private HashSet<Level> notAllowedLevels;

	private static Layout appenderLayout;

	static {
		Enumeration appenders = Logger.getRootLogger().getAllAppenders();

		if (appenders.hasMoreElements()) {
			Appender appender = (Appender) appenders.nextElement();
			appenderLayout = appender.getLayout();
		} else {
			appenderLayout = new SimpleLayout();
		}
	}

	public LogWriterAppender(OutputStream outputStream) {
		this(new OutputStreamWriter(outputStream));
	}

	public LogWriterAppender(Writer writer) {
		super(appenderLayout, writer);

		allowedThreadNames = new HashSet<String>(4);
		allowedLevels = new HashSet<Level>(10);
		notAllowedLevels = new HashSet<Level>(10);
	}

	public void setAllowedMDCKey(String key, Object value) {
		this.allowedMDCKey = key;
		this.mdcValue = value;
	}

	public void clearAllowedMDCKey() {
		this.allowedMDCKey = null;
		this.mdcValue = null;
	}

	public void addAllowedThreadName(String threadName) {
		allowedThreadNames.add(threadName);
	}

	public void removeAllowedThreadName(String threadName) {
		allowedThreadNames.remove(threadName);
	}

	public void addAllowedLevel(Level level) {
		allowedLevels.add(level);
	}

	public void removeAllowedLevel(Level level) {
		allowedLevels.remove(level);
	}

	public void addNotAllowedLevel(Level level) {
		notAllowedLevels.add(level);
	}

	public void removeNotAllowedLevel(Level level) {
		notAllowedLevels.remove(level);
	}

	@Override
	public void append(LoggingEvent loggingEvent) {
		if (allowedThreadNames.size() == 0 || allowedThreadNames.contains(loggingEvent.getThreadName())) {
			if ((allowedLevels.size() == 0 || allowedLevels.contains(loggingEvent.getLevel())) && (notAllowedLevels.size() == 0 || !notAllowedLevels.contains(loggingEvent.getLevel()))) {
				if (allowedMDCKey == null || mdcValue.equals(loggingEvent.getMDC(allowedMDCKey))) {
					super.append(loggingEvent);
				}
			}
		}
	}
}
