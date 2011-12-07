package edu.mayo.mprc.utilities;

import edu.mayo.mprc.MprcException;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.*;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.List;

/**
 * A method that can compare two scaffold report .xml files and see if they are similar enough disregarding some unimportant differences
 */
public final class ScaffoldXmlComparer {
	private static final Logger LOGGER = Logger.getLogger(ScaffoldXmlComparer.class);

	/**
	 * Checks to see if two XML files are "equivalent" meaning all elements and attributes have the comparable values
	 * This comparison can ignore the order of elements and actually navigates the document tree.  This comparison goes
	 * a step further and if any "differences" are detected they are validated since some detected differences may not be
	 * signficant with regards to what scaffold uses.
	 * <p/>
	 * This is set to ignore the analysis date attribute as well as any node difference that can be attributed to floating
	 * point arithmetic issues.
	 * <p/>
	 * <p/>
	 * This method depends on XMLUnit so that needs to be installed
	 *
	 * @param f1 the xml we want to compare two to
	 * @param f2 the comaring xml file
	 * @return true if they are deamed "comparable" else return false or if either were null return false
	 * @throws java.io.FileNotFoundException if either of the files passed in are not equal
	 */
	public boolean areSimilarScaffoldXMLFiles(File f1, File f2) throws FileNotFoundException {
		if (f1 == null || f2 == null) {
			return false;
		}

		//if they are the same object or have the same path
		if (f1 == f2 || f1.equals(f2)) {
			return true;
		}

		//check to see if the files exist and if not then throw a fit
		if (!f1.exists()) {
			throw new FileNotFoundException("File not found: " + f1.getAbsolutePath());
		}
		if (!f2.exists()) {
			throw new FileNotFoundException("File not found: " + f2.getAbsolutePath());
		}

		//setup the diff
		FileInputStream s1 = null;
		FileInputStream s2 = null;

		try {
			s1 = new FileInputStream(f1);
			s2 = new FileInputStream(f2);
			return this.compareScafml(s1, s2);
		} catch (Exception e) {
			throw new MprcException("Error creating Diff", e);
		} finally {
			FileUtilities.closeQuietly(s1);
			FileUtilities.closeQuietly(s2);
		}
	}


	private DetailedDiff lastComparisonDetails;

	public boolean compareScafml(InputStream s1, InputStream s2) throws IOException, ParserConfigurationException, SAXException {

		XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
		XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
		XMLUnit.setIgnoreWhitespace(true);

		InputSource source1 = new InputSource(s1);
		InputSource source2 = new InputSource(s2);


		Diff diff = new Diff(source1, source2);

		diff.overrideDifferenceListener(new ScaffoldXMLDifferenceListener());

		diff.overrideElementQualifier(new ElementQualifier() {

			public boolean qualifyForComparison(Element e1, Element e2) {
				if (!e1.getNodeName().equals(e2.getNodeName())) {
					return false;
				}
				String id1 = e1.getAttribute("id");
				String id2 = e2.getAttribute("id");
				return id1.equals(id2);
			}
		});

		final boolean areSimilar = diff.similar();
		final boolean areIdentical = diff.identical();

		lastComparisonDetails = new DetailedDiff(diff);

		return (areSimilar || areIdentical);
	}

	public List<Difference> getLastDiffDetails() {
		if (lastComparisonDetails == null) {
			throw new IllegalStateException("A comparison has not yet been made to get the details of.");
		}

		return this.lastComparisonDetails.getAllDifferences();
	}

	public String getDifferenceString() {
		StringBuilder sb = new StringBuilder();
		for (Difference diff : getLastDiffDetails()) {
			if (!diff.isRecoverable()) { //if is not a simularity
				sb.append("Difference at Xpath location: ").append(diff.getControlNodeDetail().getXpathLocation()).append("\n");
			}
		}
		return sb.toString();
	}

}
