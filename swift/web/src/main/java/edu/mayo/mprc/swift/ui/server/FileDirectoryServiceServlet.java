package edu.mayo.mprc.swift.ui.server;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.swift.SwiftWebContext;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public final class FileDirectoryServiceServlet extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(FileDirectoryServiceServlet.class);

	private File basePath;
	private static final long serialVersionUID = 1557269803152490571L;
	public static final String DIRECTORY_PATH_ATTRIBUTE_NAME = "d";
	public static final String EXPANDED_PATHS_ATTRIBUTE_NAME = "e";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init();
		try {
			basePath = SwiftWebContext.getServletConfig().getBrowseRoot();
		} catch (Exception t) {
			throw new ServletException("Failed to initialize " + this.getClass().getSimpleName() + " - cannot retrieve base path", t);
		}
		if (basePath == null) {
			throw new ServletException("No base-path specified");
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws
			ServletException, IOException {

		resp.setContentType("text/xml");
		resp.setHeader("Cache-Control", "no-cache");
		PrintWriter out = resp.getWriter();
		String directory_path;
		String expanded_paths;
		try {

			directory_path = req.getParameter(DIRECTORY_PATH_ATTRIBUTE_NAME);
			if (directory_path == null) {
				directory_path = "";
			}
			directory_path = removePathParentUsage(directory_path);
			expanded_paths = req.getParameter(EXPANDED_PATHS_ATTRIBUTE_NAME);
			if (expanded_paths == null) {
				expanded_paths = "";
			}

			FileSearchBean fileBean = new FileSearchBean(basePath.getAbsolutePath());
			fileBean.setPath(fixFileSeparators(directory_path));
			fileBean.setExpandedPaths(expanded_paths);
			fileBean.writeFolderContent(out);
		} catch (MprcException e) {
			throw new ServletException("Problem in FileDirectoryServiceServlet, " + e.getMessage(), e);
		} catch (Exception e) {
			throw new ServletException("Problem in login session:" + e.getMessage(), e);
		} finally {
			out.close();
		}
	}  // end doGet


	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws
			ServletException, IOException {
		doGet(req, resp);
	}

	/**
	 * replace the file separators with the localhost file separators
	 *
	 * @param filename - the filename
	 * @return - modified file name
	 */
	public static String fixFileSeparators(String filename) {
		if (filename == null) {
			return null;
		}
		switch (File.separatorChar) {
			case '/':
				return filename.replace('\\', File.separatorChar);
			case '\\':
				return filename.replace('/', File.separatorChar);
			default:
				LOGGER.warn("warning, unrecognized file separator=" + File.separatorChar);
				filename = filename.replace('/', File.separatorChar);
				filename = filename.replace('\\', File.separatorChar);
				return filename;
		}
	}

	public static String removePathParentUsage(String path) {
		return path != null ? path.replace("\\.\\.", "") : null;
	}
}
