/*
 *  Process Drift Detection
 *  Copyright (C) 2018  Alexander Seeliger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.tudarmstadt.tk.pm.utils;

import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class MathUtils {

	public static final double log2 = Math.log(2);

	private static final double oneOverSqrtTwo = 1 / Math.sqrt(2);

	public static double klDivergence(double[] p1, double[] p2) {

		double klDiv = 0.0;

		for (int i = 0; i < p1.length; ++i) {
			if (p1[i] == 0) {
				continue;
			}
			if (p2[i] == 0.0) {
				continue;
			} // Limin

			klDiv += p1[i] * Math.log(p1[i] / p2[i]);
		}

		return klDiv / log2; // moved this division out of the loop -DM
	}

	public static double hellingerDistance(double[] v1, double[] v2) {
		double sum = 0.0;

		for (int i = 0; i < v1.length; i++) {
			sum += Math.pow(Math.sqrt(v1[i]) - Math.sqrt(v2[i]), 2d);
		}

		return Math.sqrt(sum) * oneOverSqrtTwo;
	}

	public static double cosineSimilarity(double[] V1, double[] V2) {
		int N = 0;
		N = ((V2.length < V1.length) ? V2.length : V1.length);

		double dot = 0.0d;
		double mag1 = 0.0d;
		double mag2 = 0.0d;

		for (int n = 0; n < N; n++) {
			dot += V1[n] * V2[n];
			mag1 += Math.pow(V1[n], 2);
			mag2 += Math.pow(V2[n], 2);
		}

		return dot / (Math.sqrt(mag1) * Math.sqrt(mag2));
	}

	public static double pearson(double[] v1, double[] v2) {
		PearsonsCorrelation cor = new PearsonsCorrelation();
		return 1 - cor.correlation(v1, v2);
	}

	public static double compare(double[] v1, double[] v2) {
		EuclideanDistance dist = new EuclideanDistance();
		return dist.compute(v1, v2);
	}

}
