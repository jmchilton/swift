/**
 * Classes for processing reports from Swift that enable analysis of spectrum quality.
 *
 * We are putting together multiple sources of information:
 * <ul>
 * <li>Information from the headers of the .RAW file</li>
 * <li>Information from the resulting .mgf files (parent mass, charge state)</li>
 * <li>Spectrum quality values from msmsEval, computed from the .mgf files</li>
 * <li>Identifications from Scaffold</li>
 * </ul>
 * We are merging four different data sources into a single output that is to be analyzed. Any input
 * can be missing, in which case we will output empty columns.
 *
 * The main class in this package is {@link edu.mayo.mprc.qa.SpectrumInfoJoiner}.
 */
package edu.mayo.mprc.qa;