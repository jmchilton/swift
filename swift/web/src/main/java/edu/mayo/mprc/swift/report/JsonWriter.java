package edu.mayo.mprc.swift.report;

import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.swift.db.LogInfo;
import edu.mayo.mprc.swift.dbmapping.SearchRun;
import edu.mayo.mprc.swift.dbmapping.TaskData;
import edu.mayo.mprc.utilities.StringUtilities;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Produces JSON that transmits data to the report table. The JSON is printed directly to a specified {@link PrintWriter}.
 */
public final class JsonWriter {
	/**
	 * Target object to send the data to.
	 */
	public static final String TARGET = "root";

	private PrintWriter out;

	/**
	 * The method that is currently being populated.
	 * The JSON interface supports several methods. The methods take an array of inputs.
	 * In order to save space, all the subsequent calls to the same method should be packed into a single
	 * call with an array as parameter.
	 *
	 * @see #currentMethodArray
	 */
	private String currentMethod;
	/**
	 * Array of parameters for currently produced method.
	 *
	 * @see #currentMethod
	 */
	private StringBuilder currentMethodArray = new StringBuilder(100);
	private static final int SEARCH_RUN_BUILDER_CAPACITY = 200;
	private static final int TASK_STRINGBUILDER_CAPACITY = 150;
	private static final long MILLIS_PER_SEC = 1000L;
	private static final long SECS_PER_MIN = 60L;
	private static final long SECS_PER_HOUR = 3600L;

	public JsonWriter(PrintWriter out) {
		this.out = out;
	}

	public void close() {
		dumpMethod(null, null);
		out.print(TARGET + ".fireOnChange();");
		out.close();
	}

	/**
	 * Appends code that clears all data on the client.
	 */
	public void clearAll() {
		dumpMethod(null, null);
		out.print(TARGET + ".clearAll();");
	}

	public void setTimestamp(Date date) {
		dumpMethod(null, null);
		out.print(MessageFormat.format("window.timestamp={0};", Long.toString(date.getTime())));
	}

	/**
	 * Appends code that inserts a search run at given position into the stream.
	 *
	 * @param order        Position at which is the searchRun inserted
	 * @param searchRun    Search run data
	 * @param method       Method to be executed on the searchRun. Can be "insert", "rewrite" or "update". Only "insert" sends order information.
	 * @param reports      A listing of searchRun output files. Can be null in case the searchRun did not finish yet.
	 * @param runningTasks Number of tasks currently running.
	 */
	public void processSearchRun(int order, SearchRun searchRun, int runningTasks, Iterable<ReportInfo> reports, String method) {
		StringBuilder parameter = new StringBuilder(SEARCH_RUN_BUILDER_CAPACITY);
		// Only insert method supports the order parameter (others define items by id)
		// Only update method outputs just "live" data (data that change during lifetime of the searchRun)
		appendSearchRunJson(parameter, "insert".equals(method) ? order : -1, searchRun, runningTasks, reports, "update".equals(method));
		dumpMethod(method, parameter.toString());
	}

	/**
	 * Appends JSON code for search run to given string.
	 *
	 * @param builder      StringBuilder to append to
	 * @param order        Position of the searchRun within the searchRun array. When -1, the order information is not transferred.
	 * @param searchRun    Search run data
	 * @param runningTasks Amount currently running tasks.
	 * @param reports      A listing of output files the searchRun produced.
	 * @param justLiveData When true, only "live data" are reported (data that changes during the lifetime of the searchRun)
	 * @return Original StringBuilder with searchRun appended.
	 */
	public static StringBuilder appendSearchRunJson(StringBuilder builder, int order, SearchRun searchRun, int runningTasks, Iterable<ReportInfo> reports, boolean justLiveData) {
		builder.append('{');

		if (0 <= order) {
			appendKeyNumber(builder, "_", (long) order);
		}
		appendKeyNumberNull(builder, "id", searchRun.getId());
		if (!justLiveData) {
			appendKeyString(builder, "title", searchRun.getTitle());
			appendKeyString(builder, "user", searchRun.getSubmittingUser().getFirstName() + ' ' + searchRun.getSubmittingUser().getLastName());
			appendKeyString(builder, "submitted", formatDateCompact(searchRun.getStartTimestamp()));
		}
		Date endStamp = searchRun.getEndTimestamp();
		if (endStamp == null) {
			appendKeyString(builder, "duration", formatTimeSpan(new Date().getTime() - searchRun.getStartTimestamp().getTime()) + " (running)");
		} else {
			appendKeyString(builder, "duration", formatTimeSpan(endStamp.getTime() - searchRun.getStartTimestamp().getTime()));
		}
		if (!justLiveData) {
			appendKeyNumber(builder, "subtasks", (long) searchRun.getNumTasks());
		}

		appendKeyNumber(builder, "search", searchRun.getSwiftSearch() == null ? 0 : searchRun.getSwiftSearch());
		appendKeyString(builder, "errormsg", searchRun.getErrorMessage());
		appendKeyNumber(builder, "ok", (long) searchRun.getTasksCompleted());
		appendKeyNumber(builder, "failures", (long) searchRun.getTasksFailed());
		appendKeyNumber(builder, "warnings", (long) searchRun.getTasksWithWarning());
		appendKeyNumber(builder, "running", (long) runningTasks);
		if (reports != null) {
			appendKeyReportInfo(builder, "results", reports);
		}
		appendComma(builder);
		builder.append("\"details\":{");
		appendKeyNumber(builder, "total", (long) searchRun.getNumTasks());
		builder.append('}');

		builder.append('}');
		return builder;
	}

	/**
	 * Formats given time span (in milliseconds) in a human readable way (xx days, hh:mm:ss)
	 *
	 * @param l Number of milliseconds elapsed.
	 * @return Human readable time information (xx days, hh:mm:ss)
	 */
	private static String formatTimeSpan(long l) {
		long seconds = l / 1000 % 60;
		long minutes = l / 1000 / 60 % 60;
		long hours = l / 1000 / 60 / 60 % 24;
		long days = l / 1000 / 60 / 60 / 24;

		StringBuilder result = new StringBuilder(10);

		if (days > 0) {
			result.append(days);
			result.append(days > 0 ? " days, " : " day, ");
		}

		result.append(hours)
				.append(":")
				.append(minutes < 10 ? "0" : "")
				.append(minutes)
				.append(":")
				.append(seconds < 10 ? "0" : "")
				.append(seconds);

		return result.toString();
	}

	public void rewriteTaskDataList(int searchRunId, List<TaskData> statusList) {
		dumpMethod(null, null);
		StringBuilder parameter = new StringBuilder(TASK_STRINGBUILDER_CAPACITY);
		for (int i = 0; i < statusList.size(); i++) {
			appendComma(parameter);
			appendTaskDataJson(parameter, i, statusList.get(i));
		}
		out.print(MessageFormat.format("{0}.findId({1}).details.clearAll().rewrite([{2}]);", TARGET, Integer.toString(searchRunId), parameter.toString()));
	}

	/**
	 * Appends task status JSON to given string.
	 *
	 * @param builder StringBuilder to append to.
	 * @param order   Position of the task status within the task status array
	 * @param status  Task status data
	 * @return Original StringBuilder with the task status appended.
	 */
	public static StringBuilder appendTaskDataJson(StringBuilder builder, int order, TaskData status) {
		builder.append('{');
		appendKeyNumber(builder, "_", (long) order);
		appendKeyNumber(builder, "taskid", status.getId());
		appendKeyString(builder, "title", status.getDescriptionLong());
		appendKeyString(builder, "status", status.getTaskState().getDescription());
		Date end = status.getEndTimestamp();
		Date start = status.getStartTimestamp();
		Date queue = status.getQueueTimestamp();
		if (end == null) {
			end = new Date();
		}
		if (start == null) {
			start = end;
		}
		if (queue == null) {
			queue = start;
		}
		long queueTotal = start.getTime() - queue.getTime();
		long runTotal = end.getTime() - start.getTime();
		String timeStr = "";
		if (runTotal > 0) {
			timeStr = "Actual time: " + millisToTimeString(runTotal);
			if (queueTotal > 0) {
				timeStr += " after " + millisToTimeString(queueTotal) + " spent in queue";
			}
		} else if (queueTotal > 0) {
			timeStr = "Queuing time: " + millisToTimeString(queueTotal);
		} else {
			timeStr = "Did get neither queued nor started yet";
		}

		appendKeyString(builder, "time", timeStr);
		appendKeyNumber(builder, "queuestamp", queue.getTime());
		appendKeyNumber(builder, "startstamp", start.getTime());
		appendKeyNumber(builder, "endstamp", end.getTime());
		appendKeyString(builder, "errormsg", status.getErrorMessage());
		appendKeyString(builder, "jobid", status.getGridJobId());
		appendKeyString(builder, "host", status.getHostString());
		appendKeyNumberNull(builder, "percentDone", status.getPercentDone());

		if (status.getOutputLogDatabaseToken() != null || status.getErrorLogDatabaseToken() != null) {
			appendComma(builder);
			builder.append("\"logs\":[");

			if (status.getOutputLogDatabaseToken() != null) {
				appendLogInfo(builder, new LogInfo(LogInfo.STD_OUT_LOG_TYPE, FileTokenFactory.tagDatabaseToken(status.getOutputLogDatabaseToken())));
			}

			if (status.getErrorLogDatabaseToken() != null) {
				appendLogInfo(builder, new LogInfo(LogInfo.STD_ERR_LOG_TYPE, FileTokenFactory.tagDatabaseToken(status.getErrorLogDatabaseToken())));
			}

			builder.append(']');
		}

		builder.append('}');
		return builder;
	}

	private static String millisToTimeString(long millis) {
		long sec = millis / MILLIS_PER_SEC;
		long min = (sec / SECS_PER_MIN) % SECS_PER_MIN;
		long hour = sec / SECS_PER_HOUR;
		sec %= SECS_PER_MIN;
		return MessageFormat.format("{0,number}:{1,number,00}:{2,number,00}", hour, min, sec);
	}

	private static void appendComma(StringBuilder parameter) {
		if (1 < parameter.length() && '{' != parameter.charAt(parameter.length() - 1) && '[' != parameter.charAt(parameter.length() - 1)) {
			parameter.append(", ");
		}
	}

	private static void appendKeyNumber(StringBuilder parameter, String key, long value) {
		appendComma(parameter);
		parameter.append('"').append(key).append("\":").append(value);
	}

	private static void appendKeyNumberNull(StringBuilder parameter, String key, Integer value) {
		appendComma(parameter);
		if (value != null) {
			parameter.append('"').append(key).append("\":").append(value);
		} else {
			parameter.append('"').append(key).append("\":null");
		}
	}

	private static void appendKeyNumberNull(StringBuilder parameter, String key, Float value) {
		appendComma(parameter);
		if (value != null) {
			parameter.append('"').append(key).append("\":").append(value);
		} else {
			parameter.append('"').append(key).append("\":null");
		}
	}

	private static void appendKeyString(StringBuilder parameter, String key, String value) {
		appendComma(parameter);
		if (value != null) {
			parameter.append('"').append(key).append("\":\"").append(StringUtilities.toUnicodeEscapeString(escapeDoubleQuoteJavascript(value))).append('"');
		} else {
			parameter.append('"').append(key).append("\":null");
		}
	}

	private static void appendKeyStringArray(StringBuilder parameter, String key, Iterable<String> value) {
		appendComma(parameter);
		if (value == null) {
			parameter.append('"').append(key).append("\":null");
		} else {
			parameter.append('"').append(key).append("\":[");
			boolean first = true;
			for (String v : value) {
				if (!first) {
					parameter.append(',');
				}
				first = false;
				parameter.append('"');
				parameter.append(escapeDoubleQuoteJavascript(v));
				parameter.append('"');
			}
			parameter.append(']');
		}
	}

	private static void appendKeyReportInfo(StringBuilder parameter, String key, Iterable<ReportInfo> value) {
		appendComma(parameter);
		if (value == null) {
			parameter.append('"').append(key).append("\":null");
		} else {
			parameter.append('"').append(key).append("\":[");
			boolean first = true;
			for (ReportInfo v : value) {
				if (!first) {
					parameter.append(", ");
				}
				first = false;

				appendReportInfo(parameter, v);
			}
			parameter.append(']');
		}
	}

	private static void appendReportInfo(StringBuilder builder, ReportInfo info) {
		builder.append('{');
		appendKeyNumber(builder, "reportId", info.getReportId());
		appendKeyString(builder, "path", info.getFilePath());
		appendKeyNumber(builder, "analysis", info.isHasAnalysis() ? 1 : 0);
		builder.append('}');
	}

	private static void appendLogInfo(StringBuilder builder, LogInfo info) {
		appendComma(builder);
		builder.append('{');
		appendKeyString(builder, "type", info.getType());
		appendKeyString(builder, "longname", info.getTaggedDatabaseToken());
		builder.append('}');
	}

	private static final Pattern JAVASCRIPT_ESCAPE = Pattern.compile("'|\\\\");

	/**
	 * Escapes a string to bre output into JavaScript string delimited by apostrophes
	 *
	 * @param text Text to be escaped.
	 * @return Escaped string (add enclosing apostrophes to use).
	 */
	public static String escapeSingleQuoteJavascript(String text) {
		String replaced = JAVASCRIPT_ESCAPE.matcher(text).replaceAll("\\\\$0");
		replaced = replaced.replaceAll("\n", "\\\\n");
		replaced = replaced.replaceAll("\r", "\\\\r");
		replaced = replaced.replaceAll("\t", "\\\\t");
		return replaced;
	}

	private static final Pattern JAVASCRIPT_ESCAPE_QUOTE = Pattern.compile("\"|\\\\");

	/**
	 * Escapes a string to bre output into JavaScript string delimited by apostrophes
	 *
	 * @param text Text to be escaped.
	 * @return Escaped string (add enclosing apostrophes to use).
	 */
	public static String escapeDoubleQuoteJavascript(String text) {
		String replaced = JAVASCRIPT_ESCAPE_QUOTE.matcher(text).replaceAll("\\\\$0");
		replaced = replaced.replaceAll("\n", "\\\\n");
		replaced = replaced.replaceAll("\r", "\\\\r");
		replaced = replaced.replaceAll("\t", "\\\\t");
		return replaced;
	}

	/**
	 * Formats date into a compact date-time format, using "today" and "yesterday" for recent data.
	 *
	 * @param date Date to be formatted.
	 * @return Formatted date string.
	 */
	public static String formatDateCompact(Date date) {
		// Get current date/time
		Calendar calendar = Calendar.getInstance();
		Calendar formatData = Calendar.getInstance();
		formatData.setTime(date);
		return formatDateCompact(formatData, calendar);
	}

	public static String formatDateCompact(Calendar date, Calendar now) {
		// Reset it to midnight of this day
		now.set(Calendar.AM_PM, Calendar.AM);
		now.set(Calendar.HOUR, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);

		if (0 <= date.compareTo(now)) {
			// Date is today
			return "Today at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date.getTime());
		}
		now.add(Calendar.DAY_OF_MONTH, -1);
		if (0 <= date.compareTo(now)) {
			return "Yesterday at " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date.getTime());
		}
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date.getTime());
	}

	private void dumpMethod(String method, String parameter) {
		if (isMethodChanging(method)) {
			if (this.currentMethod != null) {
				out.print(MessageFormat.format("root.{0}([{1}]);", this.currentMethod, currentMethodArray.toString()));
			}
			this.currentMethod = method;
			currentMethodArray.setLength(0);
		}
		if (parameter != null) {
			if (0 != currentMethodArray.length()) {
				currentMethodArray.append(", ");
			}
			currentMethodArray.append(parameter);
		}
	}

	private boolean isMethodChanging(String method) {
		if (this.currentMethod == null && method != null) {
			return true;
		}
		return this.currentMethod != null && !this.currentMethod.equals(method);
	}
}
