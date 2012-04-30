package edu.mayo.mprc.swift.report;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.ServletIntialization;
import edu.mayo.mprc.searchdb.dao.SearchDbDao;
import edu.mayo.mprc.searchdb.dao.TandemMassSpectrometrySample;
import edu.mayo.mprc.swift.SwiftWebContext;
import edu.mayo.mprc.utilities.FileUtilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Report about the .RAW files. Parses contents of {@link TandemMassSpectrometrySample}
 *
 * @author Roman Zenka
 */
public final class SampleReport extends HttpServlet {
	private static final long serialVersionUID = -5627714065838335L;
	private SearchDbDao searchDbDao;

	public void init() throws ServletException {
		if (ServletIntialization.initServletConfiguration(getServletConfig())) {
			if (SwiftWebContext.getServletConfig() != null) {
				searchDbDao = SwiftWebContext.getServletConfig().getSearchDbDao();
			}
		}
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) {
		resp.setContentType("text/plain");
		resp.setHeader("Content-Disposition", "attachment; filename=sample-report.csv");
		Writer writer = null;
		try {
			writer = new OutputStreamWriter(resp.getOutputStream());
			SampleReportData.writeCsv(writer, searchDbDao);
		} catch (Exception e) {
			throw new MprcException(e);
		} finally {
			FileUtilities.closeQuietly(writer);
		}
	}

}
