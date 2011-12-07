package edu.mayo.mprc.math;

import cern.colt.function.DoubleDoubleFunction;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.StringUtilities;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of Non-Negative Least Squares fitting that caches solution and hat matrices to make
 * repeated fits with the same base X matrix faster.  This isn't as clever as
 * Lawson and Hanson's method that fixes up the solution matrix rather than
 * redoing the inversion; that speedup could theoretically be added here, however.
 * <p/>
 * Also, the memory performance of this is not as good as it could be.
 */
public final class NNLS {
	private static final Logger LOGGER = Logger.getLogger(NNLS.class);

	private static final Algebra ALGEBRA = Algebra.ZERO;
	private static final DoubleFactory1D FACTORY_1D = DoubleFactory1D.dense;
	private static final DoubleFactory2D FACTORY_2D = DoubleFactory2D.dense;
	private static final DoubleDoubleFunction SUM_SQUARE_DELTAS = Functions.chain(Functions.square, Functions.minus);

	public NNLS() {
	}

	public NNLS(DoubleMatrix2D x, int allowedToBeNegative, int debug) {
		this.x = x;
		n = this.x.columns();
		m = this.x.rows();
		required = allowedToBeNegative;
		this.debug = debug;
		if (n > 31) {
			throw new MprcException("Too many columns.");
		}
		nn = 0;
		for (int i = 0; i < n; ++i) {
			if (0 == (required & (1 << i))) {
				nn++;
			}
		}
	}

	/**
	 * Fit results
	 */
	public static class Fit {

		public Fit() {
			rss = -1.;
			coefs = FACTORY_1D.make(0);
		}

		public Fit(int n) {
			coefs = FACTORY_1D.make(n);
			rss = -1.;
		}

		public DoubleMatrix1D yhat;
		public DoubleMatrix1D coefs;
		public double rss;
	}

	/**
	 * To store the cached hat and solve matrices
	 */
	private static final class HatSolve {
		private HatSolve(DoubleMatrix2D hat, DoubleMatrix2D solve) {
			this.hat = hat;
			this.solve = solve;
		}

		public DoubleMatrix2D hat;
		public DoubleMatrix2D solve;
	}

	public void fit(DoubleMatrix1D y, Fit f) {
		doNNLSRec(y, (1 << n) - 1, required, nn, 0, f);
		if (debug >= 3 && negfit.size() > 0 && f.rss != -1) {
			LOGGER.debug("Initial coefficents " + negfit.toString() + "\n");
		}
		if (debug >= 1) {
			LOGGER.debug(f.coefs.toString() + " =  " + f.rss + "\n");
		}
	}

	public String toFullString(DoubleMatrix1D coefs, int allowed, int bracket) {
		StringBuilder s = new StringBuilder();
		for (int j = 0, jj = 0; j < n; ++j) {
			if (0 != (allowed & (1 << j))) {
				if (bracket == j) {
					s.append('[');
				}
				s.append(coefs.get(jj));
				if (bracket == j) {
					s.append("]");
				}

				++jj;
			} else {
				s.append(Double.valueOf(0.));
			}
			s.append(' ');
		}
		return s.toString();
	}

	private HatSolve getHatSolve(int allowed) {
		HatSolve it = hatSolve.get(allowed);
		if (it == null) {

			int curnn = n;
			for (int i = 0; i < n; ++i) {
				if (0 == (allowed & (1 << i))) {
					--curnn;
				}
			}

			DoubleMatrix2D XX = FACTORY_2D.make(m, curnn);
			int ii = 0;
			for (int i = 0; i < n; ++i) {
				if (0 != (allowed & (1 << i))) {
					for (int j = 0; j < m; ++j) {
						XX.set(j, ii, x.get(j, i));
					}
					++ii;
				}
			}
			// possibly not very efficient. I think this is doing LU decomp.  Should I be doing QR instead?
			DoubleMatrix2D solve = ALGEBRA.mult(ALGEBRA.inverse(ALGEBRA.mult(ALGEBRA.transpose(XX), XX)), ALGEBRA.transpose(XX));
			DoubleMatrix2D hat = ALGEBRA.mult(XX, solve);

			it = new HatSolve(hat, solve);
			hatSolve.put(allowed, it);
		}
		return it;
	}

	/**
	 * Recursively perform the non-negative least square regression.
	 *
	 * @param allowed   Bitmask of coeficients that are not currently restricted to zero.
	 * @param required  Bitmask of coeficients that are never restricted to zero.
	 * @param dropcount total number of zeroed coefficients
	 */
	private void doNNLSRec(DoubleMatrix1D y, int allowed, int required, int nn, int dropcount, Fit fit) {
		if (dropcount > nn) {
			return;
			//throw RuntimeError("Dropped all coefficients.");
		}


		HatSolve pa = getHatSolve(allowed);
		DoubleMatrix2D hat = pa.hat;
		DoubleMatrix2D solve = pa.solve;

		DoubleMatrix1D coefs = ALGEBRA.mult(solve, y);
		DoubleMatrix1D yhat = ALGEBRA.mult(hat, y); // FIXME: this sucks: memory allocation at each recursive call, plus gets kept on stack unncessarily.

		// Sum of (y-yhat) squares
		double rss = y.aggregate(yhat, Functions.plus, SUM_SQUARE_DELTAS);

		int negcount = 0;
		for (int i = 0, ii = 0; i < n; ++i) {
			if (0 != (allowed & 1 << i)) {
				if (coefs.get(ii) < 0. && 0 == (this.required & 1 << i)) {
					negcount++;
				}
				ii++;
			}
		}

		if (negcount == nn) {
			if (debug >= 3) {
				LOGGER.warn("" + negcount + " coefficients negative: " + toFullString(coefs, allowed, -1) + "\n");
				negfit = coefs;
			}
			//return;  // FIXME: should this be optional?
		}

		if (negcount == 0 && ((fit.rss < 0.) || (rss < fit.rss))) {
			fit.rss = rss;
			if (fit.coefs.size() != n) {
				fit.coefs = setSize(fit.coefs, n);
			}
			for (int i = 0, ii = 0; i < n; ++i) {
				if (0 != (allowed & (1 << i))) {
					fit.coefs.set(i, coefs.get(ii));
					++ii;
				} else {
					fit.coefs.set(i, 0.);
				}
			}
			fit.yhat = yhat;
			return;
		}

		for (int i = 0, ii = 0; i < n; ++i) {
			int iibit = 1 << i;
			boolean a = (allowed & iibit) != 0;
			if (a && (coefs.get(ii) < 0.) && (0 == (required & iibit))) {
				if (debug >= 1) {
					LOGGER.debug(MessageFormat.format("{0}Dropping {1}\n",
							dropcount > 0 ? StringUtilities.repeat(' ', dropcount) : "",
							toFullString(coefs, allowed, i)));
				}

				allowed ^= iibit;

				doNNLSRec(y, allowed, required, nn, dropcount + 1, fit);

				allowed |= iibit;
				required |= iibit;
			}
			if (a) {
				ii++;
			}
		}
	}

	DoubleMatrix1D setSize(DoubleMatrix1D matrix, int n) {
		if (matrix.size() < n) {
			return DoubleFactory1D.dense.append(
					matrix,
					DoubleFactory1D.dense.make(n - matrix.size()));
		} else if (matrix.size() > n) {
			return matrix.viewPart(0, n).copy();
		}
		return matrix;
	}

	private DoubleMatrix2D x;
	/**
	 * Number of columns.
	 */
	private int n;
	/**
	 * Number of columns required to be positive
	 */
	private int nn;

	private int m;
	/**
	 * map from allowed coefficient bitmask -> (hat, solve) matricies
	 */
	private Map<Integer, HatSolve> hatSolve = new HashMap<Integer, HatSolve>();
	private int required;
	private int debug;
	private DoubleMatrix1D negfit = FACTORY_1D.make(0);

}
