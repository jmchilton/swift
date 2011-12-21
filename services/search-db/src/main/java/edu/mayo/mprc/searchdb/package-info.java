/**
 * A database of all previous search results.
 *
 * The user can search for protein, peptide or specific modification and find out in which searches it was seen before.
 *
 * The implementation of this feature is somewhat tricky.
 *
 * For Scaffold 2, we can use the Scaffold .xml export files
 * to load this information. See {@link ScafmlExport#appendScaffoldXmlExport} for exact syntax.
 * These files were being generated from the "begging of time" so virtually all legacy Swift searches will provide this file.
 *
 * Scaffold 3 scaffoldxml export is different and also appears to be incomplete.
 * For this reason, we utilize the Scaffold spectrum report if available. The spectrum report is preferred.
 *
 * For backward compatibility, this package supports loading both types of files. Going forward, it should be only
 * the spectrum report.
 */
package edu.mayo.mprc.searchdb;

import edu.mayo.mprc.scafml.ScafmlExport;