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
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/tab-separated-values");
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(resp.getOutputStream(), Charsets.US_ASCII);
			writer.write(AminoAcidSet.DEFAULT.report());
		} finally {
			FileUtilities.closeQuietly(writer);
		}
	}
}
