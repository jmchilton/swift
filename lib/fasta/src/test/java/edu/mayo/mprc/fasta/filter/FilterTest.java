package edu.mayo.mprc.fasta.filter;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Random;

public final class FilterTest {

	@Test
	public static void shouldReverse() {
		final ReversalStringManipulator manipulator = new ReversalStringManipulator();
		Assert.assertNotNull(manipulator.getDescription());
		Assert.assertEquals(manipulator.manipulateString(""), "");
		Assert.assertEquals(manipulator.manipulateString("12345"), "54321");
		Assert.assertEquals(manipulator.manipulateString("A"), "A");
	}

	@Test
	public static void shouldScramble() {
		final ScrambleStringManipulator manipulator = new ScrambleStringManipulator();
		Assert.assertNotNull(manipulator.getDescription());
		manipulator.setRandom(new Random(0));
		Assert.assertEquals(manipulator.manipulateString(""), "");
		Assert.assertEquals(manipulator.manipulateString("12345"), "52134");
		Assert.assertEquals(manipulator.manipulateString("A"), "A");
	}

	@Test
	public static void shouldSimpleFilter() {
		final SimpleStringTextFilter filter = new SimpleStringTextFilter("HELLO world");

		Assert.assertEquals(filter.testCriteria(), TextFilter.VALID);
		Assert.assertTrue(filter.matches("Hello world"));
		Assert.assertTrue(filter.matches("Hello"));
		Assert.assertTrue(filter.matches("worlDLY"));
		Assert.assertTrue(filter.matches("this is hello test world"));

		Assert.assertFalse(filter.matches("Helo wold"));
		Assert.assertFalse(filter.matches("Helllo"));
		Assert.assertFalse(filter.matches("woorlDLY"));
		Assert.assertFalse(filter.matches("this is heello test wor1d"));

		filter.setMatchMode(MatchMode.ALL);

		Assert.assertTrue(filter.matches("Hello world"));
		Assert.assertFalse(filter.matches("Hello"));
		Assert.assertFalse(filter.matches("worlDLY"));
		Assert.assertTrue(filter.matches("this is hello test world"));

		Assert.assertFalse(filter.matches("Helo wold"));
		Assert.assertFalse(filter.matches("Helllo"));
		Assert.assertFalse(filter.matches("woorlDLY"));
		Assert.assertFalse(filter.matches("this is heello test wor1d"));

		filter.setMatchMode(MatchMode.NONE);

		Assert.assertFalse(filter.matches("Hello world"));
		Assert.assertFalse(filter.matches("Hello"));
		Assert.assertFalse(filter.matches("worlDLY"));
		Assert.assertFalse(filter.matches("this is hello test world"));

		Assert.assertTrue(filter.matches("Helo wold"));
		Assert.assertTrue(filter.matches("Helllo"));
		Assert.assertTrue(filter.matches("woorlDLY"));
		Assert.assertTrue(filter.matches("this is heello test wor1d"));
	}

	@Test
	public static void shouldEmptyFilter() {
		final SimpleStringTextFilter empty = new SimpleStringTextFilter("");
		Assert.assertNotSame(empty.testCriteria(), TextFilter.VALID);
	}


	@Test
	public static void shouldRegexFilter() {
		RegExTextFilter filter = new RegExTextFilter("hi[a");
		Assert.assertNotSame(filter.testCriteria(), TextFilter.VALID);

		filter = new RegExTextFilter("HELLO.*world");

		Assert.assertEquals(filter.testCriteria(), TextFilter.VALID);
		Assert.assertTrue(filter.matches("Hello world"));
		Assert.assertFalse(filter.matches("Hello"));
		Assert.assertFalse(filter.matches("worlDLY"));
		Assert.assertTrue(filter.matches("this is hello test world"));

		Assert.assertFalse(filter.matches("Helo wold"));
		Assert.assertFalse(filter.matches("Helllo"));
		Assert.assertFalse(filter.matches("woorlDLY"));
		Assert.assertFalse(filter.matches("this is heello test wor1d"));

		filter.setMatchMode(MatchMode.ALL);

		Assert.assertTrue(filter.matches("Hello world"));
		Assert.assertFalse(filter.matches("Hello"));
		Assert.assertFalse(filter.matches("worlDLY"));
		Assert.assertTrue(filter.matches("this is hello test world"));

		Assert.assertFalse(filter.matches("Helo wold"));
		Assert.assertFalse(filter.matches("Helllo"));
		Assert.assertFalse(filter.matches("woorlDLY"));
		Assert.assertFalse(filter.matches("this is heello test wor1d"));

		filter.setMatchMode(MatchMode.NONE);

		Assert.assertFalse(filter.matches("Hello world"));
		Assert.assertTrue(filter.matches("Hello"));
		Assert.assertTrue(filter.matches("worlDLY"));
		Assert.assertFalse(filter.matches("this is hello test world"));

		Assert.assertTrue(filter.matches("Helo wold"));
		Assert.assertTrue(filter.matches("Helllo"));
		Assert.assertTrue(filter.matches("woorlDLY"));
		Assert.assertTrue(filter.matches("this is heello test wor1d"));
	}


}

