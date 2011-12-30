package edu.mayo.mprc.scafml;

import edu.mayo.mprc.daemon.files.FileHolder;

import java.io.File;

public final class ScafmlExport extends FileHolder {
	private File scaffoldOutputDir;

	// Additional exports
	private boolean exportSpectra;
	private boolean exportPeptideReport;

	// Thresholds
	private double proteinProbability;
	private double peptideProbability;
	private int minimumPeptideCount;
	private int minimumNonTrypticTerminii;

	// Protein starring
	private String starred;
	private String delimiter;
	private boolean regularExpression;
	private boolean matchName;

	// Retaining spectra
	private boolean saveOnlyIdentifiedSpectra;
	private boolean saveNoSpectra;

	public ScafmlExport() {
	}

	public ScafmlExport(File scaffoldOutputDir, boolean exportSpectra, boolean exportPeptideReport, double proteinProbability, double peptideProbability, int minimumPeptideCount, int minimumNonTrypticTerminii, String starred, String delimiter, boolean regularExpression, boolean matchName, boolean saveOnlyIdentifiedSpectra, boolean saveNoSpectra) {
		this.scaffoldOutputDir = scaffoldOutputDir;
		this.exportSpectra = exportSpectra;
		this.exportPeptideReport = exportPeptideReport;
		this.proteinProbability = proteinProbability;
		this.peptideProbability = peptideProbability;
		this.minimumPeptideCount = minimumPeptideCount;
		this.minimumNonTrypticTerminii = minimumNonTrypticTerminii;
		this.starred = starred;
		this.delimiter = delimiter;
		this.regularExpression = regularExpression;
		this.matchName = matchName;
		this.saveOnlyIdentifiedSpectra = saveOnlyIdentifiedSpectra;
		this.saveNoSpectra = saveNoSpectra;
	}

	public void appendToDocument(StringBuilder result, String indent) {
		result
				.append(indent)
				.append("<DisplayThresholds " +
						"name=\"Some Thresholds\" " +
						"id=\"thresh\" " +
						"proteinProbability=\"" + proteinProbability + "\" " +
						"minimumPeptideCount=\"" + minimumPeptideCount + "\" " +
						"peptideProbability=\"" + peptideProbability + "\" " +
						"minimumNTT=\"" + minimumNonTrypticTerminii + "\" " +
						"useCharge=\"true,true,true\" " +
						"useMergedPeptideProbability=\"true\">" +
						"</DisplayThresholds>\n");

		appendScaffoldXmlExport(result, indent);

		result
				.append(indent)
				.append("<Export type=\"sfd\" thresholds=\"thresh\" path=\"")
				.append(scaffoldOutputDir.getAbsolutePath()).append("\"")
				.append(" saveOnlyIdentifiedSpectra=\"").append((saveOnlyIdentifiedSpectra || saveNoSpectra) ? "true" : "false").append("\"")
				.append(" saveNoSpectra=\"").append(saveNoSpectra ? "true" : "false").append("\"")
				.append("/>\n");

		if (exportPeptideReport) {
			result
					.append(indent)
					.append("<Export type=\"peptide-report\" thresholds=\"thresh\" path=\"").append(scaffoldOutputDir.getAbsolutePath()).append("\"/>\n");
		}

		if (exportSpectra) {
			result
					.append(indent)
					.append("<Export type=\"spectrum\" thresholds=\"thresh\" path=\"").append(scaffoldOutputDir.getAbsolutePath()).append("\"/>\n");
		}

		if (starred != null && starred.trim().length() != 0) {
			result
					.append(indent)
					.append("<Annotation id=\"stars\">\n")
					.append(indent)
					.append("\t<Star delimiter=\"" + delimiter + "\" ")
					.append("regEx=\"" + (regularExpression ? "true" : "false") + "\">\n")
					.append(starred).append("\n")
					.append(indent)
					.append("\t</Star>\n")
					.append(indent)
					.append("</Annotation>");
		}

		result.append("\n");
	}

	/**
	 * Appends an export for Scaffold's internal "scaffoldxml" format. This format is XStream-based
	 * dump of Scaffold internals.
	 *
	 * @param result Resulting XML to append the export to.
	 * @param indent How much to indent the XML.
	 */
	private void appendScaffoldXmlExport(StringBuilder result, String indent) {
		result
				.append(indent)
				.append("<Export type=\"scaffoldxml\" thresholds=\"thresh\" path=\"").append(scaffoldOutputDir.getAbsolutePath()).append("\"/>\n");
	}
}

