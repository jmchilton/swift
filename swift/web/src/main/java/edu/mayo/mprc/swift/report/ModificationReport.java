package edu.mayo.mprc.swift.report;

import com.google.common.base.Charsets;
import edu.mayo.mprc.ServletIntialization;
import edu.mayo.mprc.swift.SwiftWebContext;
import edu.mayo.mprc.unimod.Unimod;
import edu.mayo.mprc.unimod.UnimodDao;
import edu.mayo.mprc.utilities.FileUtilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Dumps all the modifications Swift is currently operating with.
 *
 * @author Roman Zenka
 */
public final class ModificationReport extends HttpServlet {
	private static final long serialVersionUID = -7725592285967200178L;
	private static final String TITLE = "Modifications defined in Swift";
	private transient UnimodDao unimodDao;

	public void init() throws ServletException {
		if (ServletIntialization.initServletConfiguration(getServletConfig())) {
			if (SwiftWebContext.getServletConfig() != null) {
				unimodDao = SwiftWebContext.getServletConfig().getUnimodDao();
			}
		}
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html");
		OutputStreamWriter writer = null;
		try {
			unimodDao.begin();
			final Unimod unimod = unimodDao.load();
			final String report = unimod.report();
			unimodDao.commit();

			writer = new OutputStreamWriter(resp.getOutputStream(), Charsets.US_ASCII);
			writer.write("<html><head><title>" + TITLE + "</title>" +
					"<style>" +
					"table { border-collapse: collapse }" +
					"table td, table th { border: 1px solid black }" +
					"</style>" +
					"</head><body>");
			writer.write("<h1>" + TITLE + "</h1>");
			writer.write(report);
			writer.write("</body></html>");

		} finally {
			FileUtilities.closeQuietly(writer);
		}
	}
}
