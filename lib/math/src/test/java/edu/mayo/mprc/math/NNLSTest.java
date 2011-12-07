package edu.mayo.mprc.math;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class NNLSTest {
	private static final DoubleMatrix2D unit3x3 = DoubleFactory2D.dense.make(new double[][]{
			{1, 0, 0},
			{0, 1, 0},
			{0, 0, 1}
	});

	private static final DoubleMatrix2D test1 = DoubleFactory2D.dense.make(new double[][]{
			{1, 2, 3},
			{-1, 1, -2},
			{3, -1, 1}
	});

	@Test
	public void shouldSolveIdentityMatrix() {
		NNLS nnls = new NNLS(unit3x3, 0, 5);

		DoubleMatrix1D y = DoubleFactory1D.dense.make(new double[]{1, 2, 3});

		NNLS.Fit f = new NNLS.Fit();
		nnls.fit(y, f);
		assertMatrixEquals(f.coefs, new double[]{1, 2, 3}, 0);
	}

	@Test
	public void shouldSolveWithNegativeNumbers() {
		NNLS nnls = new NNLS(unit3x3, 0x5, 5);

		DoubleMatrix1D y = DoubleFactory1D.dense.make(new double[]{1, -200, 3});

		NNLS.Fit f = new NNLS.Fit();
		nnls.fit(y, f);
		assertMatrixEquals(f.coefs, new double[]{1, 0, 3}, 0);
	}

	@Test
	public void shouldSolveWithAllowedNegativeNumbers() {
		NNLS nnls = new NNLS(unit3x3, 1 << 1, 5); // Second allowed to be negative

		DoubleMatrix1D y = DoubleFactory1D.dense.make(new double[]{1, -200, 3});

		NNLS.Fit f = new NNLS.Fit();
		nnls.fit(y, f);
		assertMatrixEquals(f.coefs, new double[]{1, -200, 3}, 0);
	}

	@Test
	public void shouldSolveWithAllowedNegativeNumbers2() {
		NNLS nnls = new NNLS(unit3x3, 1 << 2, 5); // Third allowed to be negative

		DoubleMatrix1D y = DoubleFactory1D.dense.make(new double[]{1, -200, -100});

		NNLS.Fit f = new NNLS.Fit();
		nnls.fit(y, f);
		assertMatrixEquals(f.coefs, new double[]{1, 0, -100}, 0);
	}

	@Test
	public void shouldSolveWithAllNegative() {
		NNLS nnls = new NNLS(unit3x3, 0x7, 5); // All can be negative

		DoubleMatrix1D y = DoubleFactory1D.dense.make(new double[]{1, -200, 3});

		NNLS.Fit f = new NNLS.Fit();
		nnls.fit(y, f);
		assertMatrixEquals(f.coefs, new double[]{1, -200, 3}, 0);
	}

	@Test
	public void shouldSolveWithAllNegative2() {
		NNLS nnls = new NNLS(test1, 0x7, 5); // All can be negative

		DoubleMatrix1D y = DoubleFactory1D.dense.make(new double[]{3, -5, 8});

		NNLS.Fit f = new NNLS.Fit();
		nnls.fit(y, f);
		assertMatrixEquals(f.coefs, new double[]{2, -1, 1}, 1E-10);
	}

	private static final DoubleMatrix2D test2 = DoubleFactory2D.dense.make(new double[][]{
			{1, -1, 3},
			{2, 1, -1},
			{3, -2, 1},
			{4, -1, 3}
	});

	@Test
	public void shouldSolveNonSquare() {
		NNLS nnls = new NNLS(test2, 0x7, 5); // All can be negative

		DoubleMatrix1D y = DoubleFactory1D.dense.make(new double[]{6, 2, 9, 12});

		NNLS.Fit f = new NNLS.Fit();
		nnls.fit(y, f);
		assertMatrixEquals(f.coefs, new double[]{2, -1, 1}, 1E-10);
	}

	public static void assertMatrixEquals(DoubleMatrix1D matrix, double[] expected, double precision) {
		StringBuilder expectedString = new StringBuilder();
		for (double anExpected : expected) {
			expectedString.append(anExpected).append(' ');
		}
		String difference = "Expected:\n\t" + expectedString.toString() + "\nActual:\n\t" + matrix.toString();
		Assert.assertEquals(matrix.size(), expected.length, "The expected dimensions do not match:\n" + difference);
		for (int i = 0; i < expected.length; i++) {
			Assert.assertEquals(matrix.get(i), expected[i], precision, "Mismatched element #" + (i + 1) + " when comparing:\n" + difference);
		}
	}


}
