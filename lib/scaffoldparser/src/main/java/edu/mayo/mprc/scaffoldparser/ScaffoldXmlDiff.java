package edu.mayo.mprc.scaffoldparser;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import org.custommonkey.xmlunit.*;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.List;

/**
 * A method that can compare two scaffold report .xml files and see if they are similar enough disregarding some unimportant differences
 *
 * @author Eric Winter
 */
public final class ScaffoldXmlDiff {
	private DetailedDiff lastComparisonDetails;

	/**
	 * Checks to see if two XML files are "equivalent" meaning all elements and attributes have the comparable values
	 * This comparison can ignore the order of elements and actually navigates the document tree.  This comparison goes
	 * a step further and if any "differences" are detected they are validated since some detected differences may not be
	 * significant with regards to what scaffold uses.
	 * <p/>
	 * This is set to ignore the analysis date attribute as well as any node difference that can be attributed to floating
	 * point arithmetic issues. See {@link ScaffoldXMLDifferenceListener} for more details.
	 *
	 * @param f1 the xml we want to compare two to
	 * @param f2 the comparing xml file
	 * @return true if they are deemed "comparable" else return false or if either were null return false
	 * @throws FileNotFoundException if either of the files passed in are not equal
	 */
	public boolean areSimilarScaffoldXMLFiles(final File f1, final File f2) throws FileNotFoundException {
		if (f1 == null || f2 == null) {
			return false;
		}

		//if they have the same path
		if (f1.equals(f2)) {
			return true;
		}

		//check to see if the files exist and if not then throw a fit
		checkExistence(f1);
		checkExistence(f2);

		//setup the diff
		FileInputStream s1 = null;
		FileInputStream s2 = null;

		try {
			s1 = new FileInputStream(f1);
			s2 = new FileInputStream(f2);
			return this.compareScaffoldXml(s1, s2);
		} catch (Exception e) {
			throw new MprcException("Error creating Diff", e);
		} finally {
			FileUtilities.closeQuietly(s1);
			FileUtilities.closeQuietly(s2);
		}
	}

	private void checkExistence(final File file) throws FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
		}
	}

	public boolean compareScaffoldXml(final InputStream s1, final InputStream s2) throws IOException, ParserConfigurationException, SAXException {

		XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);

		final InputSource source1 = new InputSource(s1);
		final InputSource source2 = new InputSource(s2);


		final Diff diff = new Diff(source1, source2);

		diff.overrideDifferenceListener(new ScaffoldXMLDifferenceListener());


		diff.overrideElementQualifier(new ElementQualifier() {

			public boolean qualifyForComparison(final Element e1, final Element e2) {
				return e1.getNodeName().equals(e2.getNodeName());
			}
		});

		final boolean areSimilar = diff.similar();
		final boolean areIdentical = diff.identical();

		lastComparisonDetails = new DetailedDiff(diff);

		return areSimilar || areIdentical;
	}

	public List<Difference> getLastDiffDetails() {
		if (lastComparisonDetails == null) {
			throw new IllegalStateException("A comparison has not yet been made to get the details of.");
		}

		return this.lastComparisonDetails.getAllDifferences();
	}

	public String getDifferenceString() {
		final StringBuilder sb = new StringBuilder();
		for (final Difference diff : getLastDiffDetails()) {
			if (!diff.isRecoverable()) { //if is not a similarity
				sb.append(diff.getDescription()).append(" at\n\t").append(diff.getControlNodeDetail().getXpathLocation()).append("\n");
			}
		}
		return sb.toString();
	}

}
