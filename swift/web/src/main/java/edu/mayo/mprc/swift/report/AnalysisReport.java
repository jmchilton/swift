package edu.mayo.mprc.swift.report;

import com.google.common.base.Charsets;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.ServletIntialization;
import edu.mayo.mprc.searchdb.Report;
import edu.mayo.mprc.searchdb.dao.Analysis;
import edu.mayo.mprc.searchdb.dao.SearchDbDao;
import edu.mayo.mprc.swift.SwiftWebContext;
import edu.mayo.mprc.utilities.FileUtilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Dumps all that was loaded from a Scaffold search into HTML.
 *
 * @author Roman Zenka
 */
public class AnalysisReport extends HttpServlet {
    private SearchDbDao searchDbDao;

    public void init() throws ServletException {
        if (ServletIntialization.initServletConfiguration(getServletConfig())) {
            if (SwiftWebContext.getServletConfig() != null) {
                searchDbDao = SwiftWebContext.getServletConfig().getSearchDbDao();
            }
        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(resp.getOutputStream(), Charsets.US_ASCII);

            String reportIdStr = req.getParameter("id");
            final long reportId;
            try {
                reportId = Long.parseLong(reportIdStr);
            } catch (NumberFormatException e) {
                throw new MprcException("Cannot process report id: " + reportIdStr, e);
            }

            searchDbDao.begin();
            try {
                Analysis analysis = searchDbDao.getAnalysis(reportId);

                writer.write("<html><head><title>Scaffold Report</title>" +
                        "<style>" +
                        "table { border-collapse: collapse }" +
                        "table td, table th { border: 1px solid black }" +
                        "</style>" +
                        "</head><body>");
                writer.write("<h1>Scaffold Report</h1>");
                analysis.htmlReport(new Report(writer), searchDbDao);

                searchDbDao.commit();
            } catch (Exception e) {
                searchDbDao.rollback();
                throw new MprcException("Could not obtain analysis data", e);
            }

            writer.write("</body></html>");
        } finally {
            FileUtilities.closeQuietly(writer);
        }
    }
}

