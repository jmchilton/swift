package edu.mayo.mprc.sequest.core;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test(sequential = true)
public final class TestMgfFileParser {
	@Test(enabled = true, groups = {"linux"})
	public void testFindMatch() {
		final String BEGIN = "BEGIN IONS";
		final String NOT_BEGIN = "BEGINION";
		char[] withbegin = " BEGIN IONS".toCharArray();
		char[] tooshort = "BEGIN".toCharArray();

		int i = MgfIonsModeller.findMatch(BEGIN, withbegin, withbegin.length);
		Assert.assertTrue(i != -1, BEGIN + " == " + withbegin + " --> should have found a match");


		int j = MgfIonsModeller.findMatch(NOT_BEGIN, withbegin, withbegin.length);
		Assert.assertTrue(j == -1, NOT_BEGIN + " != " + withbegin + " --> should NOT have found a match");


		int K = MgfIonsModeller.findMatch(BEGIN, withbegin, withbegin.length - 1);
		Assert.assertTrue(K == -1, BEGIN + " != " + withbegin + " --> should NOT have found a match");


		int L = MgfIonsModeller.findMatch(BEGIN, tooshort, tooshort.length);
		Assert.assertTrue(L == -1, BEGIN + " != " + tooshort + " --> should NOT have found a match");


		int M = MgfIonsModeller.findMatch(BEGIN, BEGIN.toCharArray(), BEGIN.length());
		Assert.assertTrue(M != -1, BEGIN + " != " + BEGIN + " --> should have found a match");
	}
}
