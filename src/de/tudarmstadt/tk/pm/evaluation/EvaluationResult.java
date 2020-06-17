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
package de.tudarmstadt.tk.pm.evaluation;

public class EvaluationResult {

	private double precision;

	private double recall;

	private double f1score;
	
	private double averageDistance;

	public EvaluationResult(double precision, double recall, double averageDistance) {
		this.precision = precision;
		this.recall = recall;
		this.averageDistance = averageDistance;

		this.f1score = 2 * (precision * recall) / (precision + recall);
	}
	
	@Override
	public String toString() {
		return String.format("%.4f;%.4f;%.4f;%.4f", f1score, precision, recall, averageDistance);
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public double getF1score() {
		return f1score;
	}

	public void setF1score(double f1score) {
		this.f1score = f1score;
	}
}
