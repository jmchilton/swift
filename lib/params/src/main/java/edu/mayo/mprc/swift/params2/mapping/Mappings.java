package edu.mayo.mprc.swift.params2.mapping;

import edu.mayo.mprc.swift.params2.Instrument;
import edu.mayo.mprc.swift.params2.Protease;
import edu.mayo.mprc.swift.params2.Tolerance;
import edu.mayo.mprc.unimod.ModSet;

import java.io.Reader;
import java.io.Writer;

/**
 * All parameter mappings for a given engine.
 * <p/>
 * The mappings class wraps a parameter set (stored in a parameter file, e. g. {@code mascot.params})
 * and allows the user to read and write the values using high-level access methods.
 * <p/>
 * For instance, mascot might store peptide tolerance as TOL and TOLU settings, but the method {@link #mapPeptideToleranceToNative}
 * takes only a single {@link #mapPeptideToleranceToNative} call to map the {@link Tolerance} back to the TOL, TOLU pair.
 * <p/>
 * The parameters should be validated as they are read/written. If all validations pass, the modified parameter set is saved
 * to disk using the {@link #write} method. This method gets the original parameter file as its input, so it can preserve
 * additional settings (and comments) that are not necessary for the mapping itself to function.
 */
public interface Mappings {
	/**
	 * @return Reader from which the base set of parameters can be read. This can be passed to {@link #read}
	 *         and {@link #write} for easy parameter mapping.
	 */
	Reader baseSettings();

	/**
	 * The read method is responsible for reading data from the native params file (e.g. mascot.params)
	 * and storing them into whatever internal structure of native parameters. This method does no longer make much sense,
	 * as all the native parameters should get overwritten by the mapped values.
	 *
	 * @param isr Reader for the params file
	 * @deprecated Remove completely.
	 */
	void read(Reader isr);

	/**
	 * This method is given the original native params file (same one that was used for {@link #read}
	 * and an output stream. The method should modify the original params file to reflect changes performed using {@code toNative}
	 * methods, such as {@link #mapEnzymeToNative}. The method should retain formatting and comments of the original
	 * params file, and retain all parameters that are not recognized.
	 * <p/>
	 * The resulting native param file is written into a given output stream.
	 *
	 * @param oldParams Original parameter file.
	 * @param out       Output stream to write the modified parameter file into
	 */
	void write(Reader oldParams, Writer out);

	void mapPeptideToleranceToNative(MappingContext context, Tolerance peptideTolerance);

	void mapFragmentToleranceToNative(MappingContext context, Tolerance fragmentTolerance);

	void mapVariableModsToNative(MappingContext context, ModSet variableMods);

	void mapFixedModsToNative(MappingContext context, ModSet fixedMods);

	void mapSequenceDatabaseToNative(MappingContext context, String shortDatabaseName);

	void mapEnzymeToNative(MappingContext context, Protease enzyme);

	void mapMissedCleavagesToNative(MappingContext context, Integer missedCleavages);

	void mapInstrumentToNative(MappingContext context, Instrument instrument);

	/**
	 * Used for testing purposes. Lets the user obtain a different native param value.
	 *
	 * @param name Native param name.
	 * @return Native param value.
	 */
	String getNativeParam(String name);

	/**
	 * Used for testing purposes. Lets the user change a native param value.
	 *
	 * @param name  Native param name.
	 * @param value Native param value.
	 */
	void setNativeParam(String name, String value);
}
