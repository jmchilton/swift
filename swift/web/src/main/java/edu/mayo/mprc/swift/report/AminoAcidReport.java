package edu.mayo.mprc.swift.report;

import com.google.common.base.Charsets;
import edu.mayo.mprc.chem.AminoAcidSet;
import edu.mayo.mprc.utilities.FileUtilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * List all amino acids defined in Swift + their masses.
 *
 * @author Roman Zenka
 */
public final class AminoAcidReport extends HttpServlet {
	private static final long serialVersionUID = 6164359787369648483L;

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html");
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(resp.getOutputStream(), Charsets.US_ASCII);
			writer.write("<html><head><title>Amino Acids defined in Swift</title>" +
					"<style>" +
					"table { border-collapse: collapse }" +
					"table td, table th { border: 1px solid black }" +
					"</style>" +
					"</head><body>");
			writer.write("<h1>Amino acids defined in Swift</h1>");
			writer.write(AminoAcidSet.DEFAULT.report());
			writer.write("</body></html>");
		} finally {
			FileUtilities.closeQuietly(writer);
		}
	}
}
