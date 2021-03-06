package edu.mayo.mprc.swift.benchmark;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.ServletIntialization;
import edu.mayo.mprc.common.client.StringUtilities;
import edu.mayo.mprc.swift.SwiftWebContext;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.dbmapping.TaskData;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.workflow.persistence.TaskState;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Benchmark extends HttpServlet {

	private transient SwiftDao swiftDao;

	public Benchmark() {
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (ServletIntialization.initServletConfiguration(getServletConfig())) {
			if (SwiftWebContext.getServletConfig() != null) {
				swiftDao = SwiftWebContext.getServletConfig().getSwiftDao();
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String idString = req.getParameter("id");
		final int searchId = Integer.parseInt(idString);
		if (idString != null) {
			swiftDao.begin();
			ServletOutputStream outputStream = null;
			try {
				final List<TaskData> taskDataList = swiftDao.getTaskDataList(searchId);
				resp.setContentType("text/csv");
				resp.setHeader("Content-Disposition", "attachment; filename=\"report_" + searchId + ".csv\"");
				outputStream = resp.getOutputStream();
				printTaskTable(outputStream, taskDataList);
				swiftDao.commit();
			} catch (Throwable t) {
				swiftDao.rollback();
				throw new MprcException("Could not create the report", t);
			} finally {
				FileUtilities.closeQuietly(outputStream);
			}
		}
	}

	/**
	 * Output the table of tasks. The columns correspond to different task types, below are
	 * times (in seconds) corresponding
	 *
	 * @param outputStream
	 * @param taskDataList
	 * @throws IOException
	 */
	static void printTaskTable(ServletOutputStream outputStream, List<TaskData> taskDataList) throws IOException {
		Map<String, Column> table = makeColumnMap(taskDataList);
		String[] types = getTaskTypes(table);
		if (types.length > 0) {
			outputStream.println(StringUtilities.join(types, ","));
			printTableData(outputStream, table, types);
		}
	}

	/**
	 * Print the table of all collected data.
	 *
	 * @param outputStream Where to print data to.
	 * @param table        Input data, sorted into columns.
	 * @param types        Order in which to output the task types.
	 * @throws IOException When writing goes wrong.
	 */
	private static void printTableData(ServletOutputStream outputStream, Map<String, Column> table, String[] types) throws IOException {
		int i = 0;
		boolean hasData = true;
		StringBuilder line = new StringBuilder(types.length * 5);
		while (hasData) {
			hasData = false;
			boolean comma = false;
			for (String type : types) {
				String data = table.get(type).getData(i);
				if (!hasData && !"".equals(data)) {
					hasData = true;
				}
				if (comma) {
					line.append(",");
				}
				line.append(data);
				comma = true;
			}
			if (hasData) {
				outputStream.println(line.toString());
				line.setLength(0);
			}
			i++;
		}
	}

	private static String[] getTaskTypes(Map<String, Column> taskTypes) {
		String[] types = new String[taskTypes.size()];
		taskTypes.keySet().toArray(types);
		Arrays.sort(types);
		return types;
	}

	private static Map<String, Column> makeColumnMap(List<TaskData> taskDataList) {
		Map<String, Column> taskTypes = new HashMap<String, Column>(10);

		// Create list of types
		for (TaskData data : taskDataList) {
			if (TaskState.COMPLETED_SUCCESFULLY.getText().equals(data.getTaskState().getDescription())) {
				final String key = data.getTaskName();
				Column column = taskTypes.get(key);
				if (column == null) {
					column = new Column(key);
					taskTypes.put(key, column);
				}

				String value = getValue(data);
				column.addData(value);
			}
		}
		return taskTypes;
	}

	/**
	 * Return value to be reported per task.
	 *
	 * @param data Task information.
	 * @return String value for the table - currently the number of elapsed seconds.
	 */
	private static String getValue(TaskData data) {
		String value = "";
		if (data.getEndTimestamp() != null && data.getStartTimestamp() != null) {
			value = String.valueOf((data.getEndTimestamp().getTime() - data.getStartTimestamp().getTime()) / 1000.0);
		}
		return value;
	}
}
