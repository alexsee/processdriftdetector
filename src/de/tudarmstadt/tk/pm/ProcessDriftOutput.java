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
package de.tudarmstadt.tk.pm;

import java.util.List;

import de.tudarmstadt.tk.pm.evaluation.EvaluationResult;

public class ProcessDriftOutput {

	private EvaluationResult result;
	
	private List<ChangePoint> changePoints;
	
	private long timeElapsed = 0;
	
	private int splitSize = 0;

	public ProcessDriftOutput(EvaluationResult result, List<ChangePoint> changePoints) {
		this.result = result;
		this.changePoints = changePoints;
	}

	public EvaluationResult getResult() {
		return result;
	}

	public List<ChangePoint> getChangePoints() {
		return changePoints;
	}
	
	public long getTimeElapsed() {
		return timeElapsed;
	}

	public void setTimeElapsed(long timeElapsed) {
		this.timeElapsed = timeElapsed;
	}

	public int getSplitSize() {
		return splitSize;
	}

	public void setSplitSize(int splitSize) {
		this.splitSize = splitSize;
	}

}
