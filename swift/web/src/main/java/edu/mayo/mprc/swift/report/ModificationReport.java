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
	private transient UnimodDao unimodDao;

	public void init() throws ServletException {
		if (ServletIntialization.initServletConfiguration(getServletConfig())) {
			if (SwiftWebContext.getServletConfig() != null) {
				unimodDao = SwiftWebContext.getServletConfig().getUnimodDao();
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/tab-separated-values");
		OutputStreamWriter writer = null;
		try {
			final Unimod unimod = unimodDao.load();
			writer = new OutputStreamWriter(resp.getOutputStream(), Charsets.US_ASCII);
			writer.write(unimod.report());
		} finally {
			FileUtilities.closeQuietly(writer);
		}
	}
}
