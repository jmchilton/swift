package edu.mayo.mprc.swift.ui.client.widgets.validation;

import edu.mayo.mprc.swift.ui.client.rpc.ClientModSpecificity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;


/**
 * This is used to test the modification search class {@link edu.mayo.mprc.swift.ui.client.widgets.validation.ModificationSearch}
 */
@Test(sequential = true)
public final class TestModificationSearch {
	/**
	 * test search by mass with a positive mass and precision, ie "42.010565-1.1"
	 * With mass search
	 * precisionSpec - specifies mass window
	 * <p>
	 * <ul>
	 * <li> '-1' then search -1 and +1 Dalton delta mass
	 * <li> '-.1' search for 0.9 Dalton to 1.1 delta mass Dalton window
	 * </ul>
	 * </p>
	 */
	@Test(enabled = true, groups = {"fast"})
	public void TestSearchByMassPositiveMass() {
		massSearch("" + 42.010565 + "-" + "1.1", 3);
	}

	@Test(enabled = true, groups = {"fast"})
	public void TestSearchByMassEmptyCase() {
		final ModificationSearch m = ModificationSearch.createInstance();
		// try the empty case
		final String spec = "";
		final List<ClientModSpecificity> result = m.searchByMass(spec);
		Assert.assertNull(result);
	}

	/**
	 * Performs the mass search.
	 *
	 * @param spec        - specification for the search for example "42.0-1.1" ie <mass><negative><precision>
	 *                    It uses a test data set from {@link #getTestDataforModsPopup()}
	 * @param numExpected - the number of hits expected within the test data
	 */
	private void massSearch(final String spec, final int numExpected) {
		final ModificationSearch m = ModificationSearch.createInstance();

		// get some data
		final List<ClientModSpecificity> modSpecs = getTestDataforModsPopup();
		m.setAllowedValues(modSpecs);

		final List<ClientModSpecificity> result = m.searchByMass(spec);
		int numfound = 0;
		if (result != null) {
			numfound = result.size();
		}
		Assert.assertTrue(result != null && result.size() == numExpected, "should have been " + numExpected + " records found, found " + numfound);
	}

	@Test(enabled = true, groups = {"fast"})
	public void TestSearchByMassNegativeMassDecimalPoint() {
		massSearch("" + -42.010565 + "-" + "1.1", 1);
	}


	@Test(enabled = true, groups = {"fast"})
	public void TestSearchByMassNegativeMassNothingAfterDecimalPoint() {
		massSearch("" + -42.010565 + "-" + "1.", 1);
	}


	@Test(enabled = true, groups = {"fast"})
	public void TestSearchByMassNegativeMassNoDecimalPoint() {
		massSearch("" + -42.010565 + "-" + "1", 1);
	}

	@Test(enabled = true, groups = {"fast"})
	public void TestSearchByMassPositiveMassNoDecimalPoint() {
		massSearch("" + 42.010565 + "-" + "1", 3);
	}

	@Test(enabled = true, groups = {"fast"})
	public void TestSearchByMassPositiveMassNothingAfterDecimalPoint() {
		massSearch("" + 42.010565 + "-" + "1.", 3);
	}

	@Test(enabled = true, groups = {"fast"})
	public void TestSearchByMassNegativeMassNoPrecision() {
		massSearch("" + -42.010565, 1);
	}

	@Test(enabled = true, groups = {"fast"})
	public void TestSearchByWord() {
		final ModificationSearch m = ModificationSearch.createInstance();
		// get some data
		final List<ClientModSpecificity> modSpecs = getTestDataforModsPopup();
		m.setAllowedValues(modSpecs);
		// try the empty case
		final String spec = "Acetyl";
		final List<ClientModSpecificity> result = m.searchByWord(spec);
		final int numfound = result.size();
		Assert.assertEquals(numfound, 2, "Expected acetylation C and K");
	}


	@Test(enabled = true, groups = {"fast"})
	public void TestSearchByWordCaseInsensitive() {
		final ModificationSearch m = ModificationSearch.createInstance();
		// get some data
		final List<ClientModSpecificity> modSpecs = getTestDataforModsPopup();
		m.setAllowedValues(modSpecs);
		// try the empty case
		final String spec = "acetyl";
		final List<ClientModSpecificity> result = m.searchByWord(spec);
		final int numfound = result.size();
		Assert.assertEquals(numfound, 2, "Expected acetylation C and K");
	}

	public List<ClientModSpecificity> getTestDataforModsPopup() {

		final List<String> altnames = Arrays.asList("none");
		return Arrays.asList(
				new ClientModSpecificity(
						"Acetylation",
						"Anywhere",
						'C',
						false,
						"Post-translational",
						42.010565,
						altnames,
						"H(2) C(2) O",
						"none",
						112,
						false
				),
				new ClientModSpecificity(
						"6-N-biotinylaminohexyl isopropyl phosphate",
						"Anywhere",
						'C',
						false,
						"Post-translational",
						41.015565,
						altnames,
						"H(2) C(2) O",
						"none",
						113,
						false),
				new ClientModSpecificity(
						"reduced 4-Hydroxynonenal",
						"Anywhere",
						'C',
						false,
						"Post-translational",
						40.011565,
						altnames,
						"H(2) C(2) O",
						"none",
						114,
						false
				),
				new ClientModSpecificity(
						"Acetylation",
						"Anywhere",
						'K',
						false,
						"Post-translational",
						42.010565,
						altnames,
						"H(2) C(2) O",
						"none",
						112,
						false
				),
				new ClientModSpecificity(
						"Acetylation",
						"Anywhere",
						'K',
						false,
						"Post-translational",
						-42.010565,
						altnames,
						"H(2) C(2) O",
						"none",
						112,
						false));
	}

	/*

			<umod:mod title="Acetyl" full_name="Acetylation" username_of_poster="unimod"
					  group_of_poster="admin"
					  date_time_posted="2002-08-19 19:17:11"
					  date_time_modified="2006-10-15 19:52:06"
					  approved="1"
					  record_id="1">
				<umod:specificity hidden="1" site="C" position="Anywhere" classification="Post-translational"
								  spec_group="3"/>
				<umod:specificity hidden="0" site="N-term" position="Protein N-term"
								  classification="Post-translational"
								  spec_group="5"/>
				<umod:specificity hidden="1" site="S" position="Anywhere" classification="Post-translational"
								  spec_group="4"/>
				<umod:specificity hidden="0" site="N-term" position="Any N-term" classification="Multiple"
								  spec_group="2">
					<umod:misc_notes>GIST acetyl light</umod:misc_notes>
				</umod:specificity>
				<umod:specificity hidden="0" site="K" position="Anywhere" classification="Multiple"
								  spec_group="1">
					<umod:misc_notes>PT and GIST acetyl light</umod:misc_notes>
				</umod:specificity>
				<umod:delta mono_mass="42.010565" avge_mass="42.0367" composition="H(2) C(2) O">
					<umod:element symbol="H" number="2"/>
					<umod:element symbol="C" number="2"/>
					<umod:element symbol="O" number="1"/>
				</umod:delta>
		*/
}
