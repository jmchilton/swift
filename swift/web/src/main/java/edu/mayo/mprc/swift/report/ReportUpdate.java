package edu.mayo.mprc.swift.report;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.ServletIntialization;
import edu.mayo.mprc.daemon.DaemonConnection;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.daemon.progress.ProgressInfo;
import edu.mayo.mprc.daemon.progress.ProgressListener;
import edu.mayo.mprc.qstat.QstatOutput;
import edu.mayo.mprc.qstat.QstatWorkPacket;
import edu.mayo.mprc.searchdb.dao.SearchDbDao;
import edu.mayo.mprc.swift.SwiftWebContext;
import edu.mayo.mprc.swift.db.SearchRunFilter;
import edu.mayo.mprc.swift.db.SwiftDao;
import edu.mayo.mprc.swift.dbmapping.ReportData;
import edu.mayo.mprc.swift.dbmapping.SearchRun;
import edu.mayo.mprc.swift.search.AssignedSearchRunId;
import edu.mayo.mprc.swift.search.SwiftSearcherCaller;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// TODO: Ideally refactor into RESTful interface - would provide clean access to Swift

public final class ReportUpdate extends HttpServlet {
	private static final long serialVersionUID = 20071220L;
	private static final String CONTENT_TYPE = "application/javascript; charset=utf-8";
	private static final Logger LOGGER = Logger.getLogger(ReportUpdate.class);
	private transient SwiftDao swiftDao;
	private transient SearchDbDao searchDbDao;
	private transient FileTokenFactory fileTokenFactory;
	/**
	 * How many milliseconds to wait till qstat considered down.
	 */
	private static final int QSTAT_TIMEOUT = 30 * 1000;

	public void init() throws ServletException {
		if (ServletIntialization.initServletConfiguration(getServletConfig())) {
			if (SwiftWebContext.getServletConfig() != null) {
				swiftDao = SwiftWebContext.getServletConfig().getSwiftDao();
				searchDbDao = SwiftWebContext.getServletConfig().getSearchDbDao();
				fileTokenFactory = SwiftWebContext.getServletConfig().getFileTokenFactory();
			}
		}
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		this.doGet(req, resp);
	}

	private void printError(PrintWriter output, String message, Throwable t) {
		LOGGER.error(message, t);
		output.println(message);
		if (t != null) {
			t.printStackTrace(output);
		}
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		resp.setHeader("Cache-Control", "no-cache");

		// If there is a rerun parameter, we want to rerun the given search run, otherwise we produce data
		String rerun = req.getParameter("rerun");
		if (rerun != null) {
			swiftDao.begin(); // Transaction-per-request
			rerunSearch(req, resp, rerun);
			swiftDao.commit();
			return;
		}

		// If there is a hide parameter, we want to hide the given search run, otherwise we produce data
		String hide = req.getParameter("hide");
		if (hide != null) {
			swiftDao.begin(); // Transaction-per-request
			hideSearch(req, resp, hide);
			swiftDao.commit();
			return;
		}

		// Qstat causes qstat daemon info to be printed out
		String qstatJobId = req.getParameter("qstat");
		if (qstatJobId != null) {
			DaemonConnection connection = SwiftWebContext.getServletConfig().getQstatDaemonConnection();
			resp.setContentType("text/plain");
			SgeStatusProgressListener listener = new SgeStatusProgressListener(resp);
			connection.sendWork(new QstatWorkPacket(Integer.parseInt(qstatJobId)), listener);
			try {
				listener.waitForEvent(QSTAT_TIMEOUT);
			} catch (InterruptedException ignore) {
				// SWALLOWED: We just exit
			}
			return;
		}

		// Action field defines what to do next
		String action = req.getParameter("action");

		// This action does not make much sense, not sure if it is actually used
		if ("open".equals(action)) {
			String file = req.getParameter("file");
			file = SwiftWebContext.getServletConfig().getBrowseWebRoot() + file.substring(SwiftWebContext.getServletConfig().getBrowseRoot().getAbsolutePath().length());
			try {
				resp.sendRedirect(file);
			} catch (IOException e) {
				throw new ServletException(e);
			}
			return;
		}

		JsonWriter out = null;
		try {
			// All following actions require a search run
			swiftDao.begin(); // Transaction-per-request

			String timestamp = req.getParameter("timestamp");

			SearchRunFilter searchRunFilter = new SearchRunFilter();
			searchRunFilter.setStart(req.getParameter("start"));
			searchRunFilter.setCount(req.getParameter("count"));
			searchRunFilter.setUserFilter(req.getParameter("userfilter"));

			PrintWriter printOut = resp.getWriter();
			out = new JsonWriter(printOut);
			resp.setContentType(CONTENT_TYPE);

			// No action - clear everything, get fresh copy of data
			if (action == null || action.length() == 0) {
				out.clearAll();
				printSearchRuns(out, searchRunFilter, "insert");
			} else if ("rewrite".equals(action)) {
				// Rewrite given range, do not erase/modify anything else
				printSearchRuns(out, searchRunFilter, "rewrite");
			} else if ("expand".equals(action)) {
				// Expand - provide detailed task info for one task
				int id = Integer.parseInt(req.getParameter("id"));
				out.rewriteTaskDataList(id, swiftDao.getTaskDataList(id));
			} else if ("update".equals(action)) {
				// We print out all new search runs + updates to the ones that changed
				Date time = new Date();
				if (null != timestamp) {
					time.setTime(Long.parseLong(timestamp));
				} else {
					time.setTime(0);
				}
				updateSearchRuns(out, searchRunFilter, time);

				// TODO: Do not output all expanded task lists, only the changed ones
				String expanded = req.getParameter("expanded");
				if (null != expanded) {
					String[] expandedIds = expanded.split(",");
					for (String idString : expandedIds) {
						if (null != idString && idString.length() != 0) {
							int id = Integer.parseInt(idString);
							out.rewriteTaskDataList(id, swiftDao.getTaskDataList(id));
						}
					}
				}
			}
			swiftDao.commit();
		} catch (Exception t) {
			swiftDao.rollback();
			throw new ServletException(t);
		} finally {
			if (null != out) {
				out.close();
			}
		}
	}

	private void rerunSearch(HttpServletRequest req, HttpServletResponse resp, String rerun) throws ServletException {
		PrintWriter output;
		try {
			output = resp.getWriter();
		} catch (IOException ignore) {
			// SWALLOWED: We can live even if this fails.
			output = new PrintWriter(System.out);
		}
		final SearchRun td = getSearchRunForId(rerun);

		ResubmitProgressListener listener = new ResubmitProgressListener();

		try {
			SwiftSearcherCaller.resubmitSearchRun(td, SwiftWebContext.getServletConfig().getSwiftSearcherDaemonConnection(), listener);
		} catch (Exception t) {
			throw new ServletException(t);
		}

		try {
			listener.waitForResubmit(60 * 1000);
		} catch (InterruptedException e) {
			throw new ServletException("Resubmit was interrupted", e);
		}

		if (listener.getAssignedId() == -1) {
			if (listener.getLastException() != null) {
				printError(output, "Rerun failed", listener.getLastException());
			} else {
				printError(output, "Timeout passed and rerun did not report success", null);
			}
			return;
		}
		forwardToReportPage(req, resp);
	}

	private void forwardToReportPage(HttpServletRequest req, HttpServletResponse resp) {
		try {
			// Forward the viewer back to the report page
			resp.sendRedirect("/report/report.jsp");
		} catch (Exception ignore) {
			// SWALLOWED: This is not essential, if it fails, no big deal
		}
	}

	private void hideSearch(HttpServletRequest req, HttpServletResponse resp, String hide) throws ServletException {
		final SearchRun td = getSearchRunForId(hide);
		td.setHidden(1);
		forwardToReportPage(req, resp);
	}

	private SearchRun getSearchRunForId(String id) {
		int searchRunId = Integer.parseInt(id);

		final SearchRun td;
		try {
			td = swiftDao.getSearchRunForId(searchRunId);
		} catch (Exception t) {
			throw new MprcException("Failure looking up search run with id=" + searchRunId, t);
		}
		return td;
	}

	/**
	 * Prints search runs in given range to given writer. The search run data is passed as a parameter to a specified
	 * function, such as "insert", "rewrite" or "update".
	 *
	 * @param out    Where to write the search runs to.
	 * @param method Method to use on the search runs. "update" will send a command to update search run data, while "insert" will insert new search runs.
	 * @param filter Filter defining what search runs and how sorted to output.
	 */
	private void printSearchRuns(JsonWriter out, SearchRunFilter filter, String method) {
		List<SearchRun> searchRuns = swiftDao.getSearchRunList(filter);
		int firstSearchRun = 0;
		int lastSearchRun = Math.min(
				filter.getCount() != null ? Integer.parseInt(filter.getCount()) : 0,
				searchRuns.size());

		Date newTimestamp = new Date();
		newTimestamp.setTime(0);
		for (int i = firstSearchRun; i < lastSearchRun; i++) {
			SearchRun searchRun = searchRuns.get(i);
			ArrayList<ReportInfo> reports = getReportsForSearchRun(searchRun);
			if (null != searchRun.getStartTimestamp() && searchRun.getStartTimestamp().compareTo(newTimestamp) > 0) {
				newTimestamp = searchRun.getStartTimestamp();
			}
			if (null != searchRun.getEndTimestamp() && searchRun.getEndTimestamp().compareTo(newTimestamp) > 0) {
				newTimestamp = searchRun.getEndTimestamp();
			}

			int runningTasks = swiftDao.getNumberRunningTasksForSearchRun(searchRun);
			out.processSearchRun(i, searchRun, runningTasks, reports, method);
		}
		out.setTimestamp(newTimestamp);
	}

	private ArrayList<ReportInfo> getReportsForSearchRun(SearchRun searchRun) {
		ArrayList<ReportInfo> reports = new ArrayList<ReportInfo>();
		for (ReportData report : searchRun.getReports()) {
			reports.add(
					new ReportInfo(report.getId(),
							fileTokenFactory.fileToTaggedDatabaseToken(report.getReportFileId()),
							searchDbDao.hasAnalysis(report.getId())
					)
			);
		}
		Collections.sort(reports);
		return reports;
	}

	/**
	 * Produces code to update given range of search runs. The update affects only data changed since last timestamp.
	 * The produced code contains instruction for setting a new timestamp.
	 *
	 * @param out       Where to write the search runs to.
	 * @param timestamp Time when the last update was performed.
	 * @param filter    Filter defining what search runs and how sorted to output.
	 */
	private void updateSearchRuns(JsonWriter out, SearchRunFilter filter, Date timestamp) {
		List<SearchRun> searchRuns = swiftDao.getSearchRunList(filter);
		int firstSearchRun = filter.getStart() != null ? Integer.parseInt(filter.getStart()) : 0;
		int lastSearchRun = Math.min(firstSearchRun + (filter.getCount() != null ? Integer.parseInt(filter.getCount()) : searchRuns.size()), searchRuns.size());

		Date newTimestamp = timestamp;
		for (int i = firstSearchRun; i < lastSearchRun; i++) {
			SearchRun searchRun = searchRuns.get(i);
			ArrayList<ReportInfo> reports = getReportsForSearchRun(searchRun);
			if (null != searchRun.getStartTimestamp() && searchRun.getStartTimestamp().compareTo(newTimestamp) > 0) {
				newTimestamp = searchRun.getStartTimestamp();
			}
			if (null != searchRun.getEndTimestamp() && searchRun.getEndTimestamp().compareTo(newTimestamp) > 0) {
				newTimestamp = searchRun.getEndTimestamp();
			}

			int runningTasks = swiftDao.getNumberRunningTasksForSearchRun(searchRun);
			boolean doInsert = null != searchRun.getStartTimestamp() && searchRun.getStartTimestamp().compareTo(timestamp) > 0;
			out.processSearchRun(i, searchRun, runningTasks, reports, doInsert ? "insert" : "update");
		}
		out.setTimestamp(newTimestamp);
	}

	private static final class SgeStatusProgressListener implements ProgressListener {
		// TODO: This directly prints out messages - unclean
		private HttpServletResponse response;
		private boolean finished;
		private final Object lock = new Object();

		public SgeStatusProgressListener(HttpServletResponse response) {
			this.response = response;
		}

		public void requestEnqueued(String hostString) {
		}

		public void requestProcessingStarted() {
		}

		public void waitForEvent(long timeout) throws InterruptedException {
			// TODO: This suffers from spurious wakeups, however this function can safely fail once in a while
			synchronized (lock) {
				if (!finished) {
					lock.wait(timeout);
				}
			}
		}

		public void requestProcessingFinished() {
			signalFinished();
		}

		private void signalFinished() {
			synchronized (lock) {
				finished = true;
				lock.notifyAll();
			}
		}

		public void requestTerminated(DaemonException e) {
			String info = e.getMessage();
			try {
				ServletOutputStream output = response.getOutputStream();
				output.print(info);
				output.close();
			} catch (IOException ignore) {
				// SWALLOWED: Not much we can do
			}
			signalFinished();
		}

		public void userProgressInformation(ProgressInfo progressInfo) {
			if (progressInfo instanceof QstatOutput) {
				QstatOutput info = (QstatOutput) progressInfo;
				try {
					ServletOutputStream output = response.getOutputStream();
					output.print(info.getQstatOutput());
					output.close();
				} catch (IOException ignore) {
					// SWALLOWED: Not much we can do
				}
			}
		}
	}

	private class ResubmitProgressListener implements ProgressListener {
		private Throwable lastException = null;
		private long assignedId = -1;
		private final Object lock = new Object();

		public ResubmitProgressListener() {
		}

		private boolean isComplete() {
			return assignedId != -1 || lastException != null;
		}

		public void waitForResubmit(long timeout) throws InterruptedException {
			long currentTime = System.currentTimeMillis();
			long finalTime = currentTime + timeout;
			synchronized (lock) {
				while (!isComplete()) {
					lock.wait(Math.max(finalTime - currentTime, timeout));
					currentTime = System.currentTimeMillis();
					if (currentTime >= finalTime) {
						// Timed out
						break;
					}
				}
			}
		}

		public Throwable getLastException() {
			synchronized (lock) {
				return lastException;
			}
		}

		public long getAssignedId() {
			synchronized (lock) {
				return assignedId;
			}
		}

		public void requestEnqueued(String hostString) {
		}

		public void requestProcessingStarted() {
		}

		public void requestProcessingFinished() {
		}

		public void requestTerminated(DaemonException e) {
			synchronized (lock) {
				this.lastException = e;
				lock.notifyAll();
			}
		}

		public void userProgressInformation(ProgressInfo progressInfo) {
			if (progressInfo instanceof AssignedSearchRunId) {
				synchronized (lock) {
					this.assignedId = ((AssignedSearchRunId) progressInfo).getSearchRunId();
					lock.notifyAll();
				}
			}
		}
	}
}
