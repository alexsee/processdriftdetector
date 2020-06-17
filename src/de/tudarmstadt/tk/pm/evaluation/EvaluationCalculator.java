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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import de.tudarmstadt.tk.pm.utils.XLogUtils;

public class EvaluationCalculator {

	private List<Integer> originalChangePoints;

	private XLog log;

	public EvaluationCalculator(XLog log) {
		this.log = log;

		// determine original change points
		this.originalChangePoints = new ArrayList<>();
		String lastTraceType = XLogUtils.getTraceType(log.get(0));
		String lastTraceType2 = XLogUtils.getTraceType(log.get(1));

		for (XTrace trace : log) {
			String curTraceType = XLogUtils.getTraceType(trace);

			if (!lastTraceType.equals(curTraceType) && lastTraceType2.equals(curTraceType)) {
				// no change point
			} else if (!lastTraceType.equals(curTraceType)) {
				// change point
//				originalChangePoints.add(log.indexOf(trace));
			} else if (lastTraceType.equals(curTraceType) && !lastTraceType2.equals(curTraceType)) {
				// change point
				originalChangePoints.add(log.indexOf(trace) - 1);
			} 

			lastTraceType2 = lastTraceType;
			lastTraceType = curTraceType;
		}

	}

	public EvaluationResult evaluate(List<de.tudarmstadt.tk.pm.ChangePoint> changePoints) {

		// map change points to nearest neighbor
		Map<Integer, ChangePoint> detectedChangePoints = new HashMap<>();

		for (de.tudarmstadt.tk.pm.ChangePoint detectedPoint : changePoints) {

			int minimalIndex = -1;
			int minimalDistance = Integer.MAX_VALUE;

			for (Integer originalPoint : originalChangePoints) {

				int distance = Math.abs(originalPoint - detectedPoint.getIndex());

				if (!detectedChangePoints.containsKey(originalPoint)
						|| detectedChangePoints.get(originalPoint).getDistance() > distance) {

					if (minimalDistance > distance) {
						minimalIndex = originalPoint;
						minimalDistance = distance;
					}
				}

			}

			if (minimalIndex > -1) {
				detectedChangePoints.put(minimalIndex, new ChangePoint(XLogUtils.getTraceName(log.get(detectedPoint.getIndex())),
						detectedPoint.getIndex(), minimalDistance));
			}

		}

		// calculate precision, recall, f1-score
		double tp = 0.0, fp = 0.0, fn = 0.0;
		double distance = 0.0;

		for (Integer originalChange : originalChangePoints) {
			if (detectedChangePoints.containsKey(originalChange)) {
				tp += 1;
				distance += detectedChangePoints.get(originalChange).getDistance();
			} else {
				fn += 1;
			}
		}

		fp = changePoints.size() - tp;

		// return evaluation result
		EvaluationResult result = new EvaluationResult(tp / (tp + fp), tp / (tp + fn), distance / tp);
		return result;

	}

}
