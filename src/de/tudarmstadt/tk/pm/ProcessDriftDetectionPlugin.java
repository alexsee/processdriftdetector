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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.stat.inference.GTest;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import de.tudarmstadt.tk.pm.evaluation.EvaluationCalculator;
import de.tudarmstadt.tk.pm.evaluation.EvaluationResult;
import de.tudarmstadt.tk.pm.utils.ListUtils;
import de.tudarmstadt.tk.pm.utils.MetricsUtils;
import de.tudarmstadt.tk.pm.utils.XLogUtils;

@Plugin(name = "Process Drift", parameterLabels = { "log", "ConceptDrift Input" }, returnLabels = {
		"Process Drifts" }, returnTypes = { ProcessDriftOutput.class }, userAccessible = true)
public class ProcessDriftDetectionPlugin {

	public boolean DEBUG = true;

	private List<String> eventNames = null;

	private final double pArcValueThreshold = 0.0001;

	private final double pNodeValueThreshold = 0.9;

	@UITopiaVariant(affiliation = "TU Darmstadt", author = "Alexander Seeliger", email = "seeliger@tk.tu-darmstadt.de")
	@PluginVariant(variantLabel = "Detects process drifts in event logs.", requiredParameterLabels = { 0 })
	public ProcessDriftOutput main(UIPluginContext context, XLog log) throws Exception {
		// measure time
		StopWatch sw = new StopWatch();
		sw.start();

		// sort the log
		XLog sortedLog = log; // XLogUtils.sortLog(log);
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(sortedLog);

		eventNames = logInfo.getEventClasses().getClasses().stream()
				.map(x -> x.getId().substring(0, x.getId().toLowerCase().indexOf("+"))).collect(Collectors.toList());

		// search for change points
		List<ChangePoint> changePoints = new ArrayList<>();

		int windowSize = 100;
		int maxWindowSize = 200;
		int index = 0;

		while (index < sortedLog.size() - windowSize) {
			PValue pvalue = calculatePValue(context, sortedLog, windowSize, index);

			int beginRefWindow = index;
			int endRefWindow = index + windowSize;
			int beginDetWindow = index + windowSize;
			int endDetWindow = index + windowSize * 2;

			if (DEBUG) {
				System.out.println(String.format("index\t %s\t p-Value\t %f\t %f",
						beginRefWindow + "-" + endRefWindow + ":" + beginDetWindow + "-" + endDetWindow,
						pvalue.getArcPvalue(), pvalue.getNodePvalue()));
			}

			// check if p-value of arc is smaller than threshold
			if (pvalue.getArcPvalue() < pArcValueThreshold) {

				// change point in detection window found
				boolean found = false;

				// determine new window size and last index
				int newWindowSize = windowSize / 2;
				int lastIndex = (index + windowSize * 2);

				PValue pvalue2before = new PValue(1, 1);

				// search in detection window for exact position of change point
				for (int i = beginDetWindow - newWindowSize; i < endDetWindow - newWindowSize; i = i + 10) {

					// larger than log size
					if (i + newWindowSize * 2 > sortedLog.size())
						break;

					lastIndex = (i + newWindowSize * 2);

					// calculate new pvalue
					PValue pvalue2 = calculatePValue(context, sortedLog, newWindowSize, i);

					if (DEBUG) {
						System.out.println(String.format(
								"  index\t %s\t p-Value\t %f\t %f", i + "-" + (i + newWindowSize) + ":"
										+ (i + newWindowSize) + "-" + (i + newWindowSize * 2),
								pvalue2.getArcPvalue(), pvalue2.getNodePvalue()));
					}

					// early stopping
					if (pvalue2before.getArcPvalue() - pvalue2.getArcPvalue() < -0.5)
						break;

					pvalue2before = pvalue2;

					// check for arc and p value
					if (pvalue2.getArcPvalue() < pArcValueThreshold && pvalue2.getNodePvalue() < pNodeValueThreshold) {

						// add change point to list
						ChangePoint ch = new ChangePoint((i - 10 + newWindowSize * 2));
						ch.setReasons(calculateReasons(context, sortedLog, newWindowSize, (i - 10 + newWindowSize * 2)));

						changePoints.add(ch);
						found = true;

						if (DEBUG) {
							System.out.println("Change Point at: " + (i - 10 + newWindowSize * 2));
						}

						// update window size
						windowSize *= (double) (i - 10 + newWindowSize * 2) / (double) (index + windowSize * 2);

						index = (i - 10 + newWindowSize * 2);
						found = true;

						break;

					}

				}

				if (!found) {
					windowSize *= (double) lastIndex / (double) (index + windowSize * 2);
					index += windowSize;
				}

			} else {
				windowSize *= 1.2;
			}

			if (windowSize >= maxWindowSize) {
				index = index + maxWindowSize;
				windowSize = 100;
			}
		}

		// stop time
		sw.stop();

		// calculate evaluation result
		EvaluationCalculator evaluation = new EvaluationCalculator(sortedLog);
		EvaluationResult result = evaluation.evaluate(changePoints);

		ProcessDriftOutput output = new ProcessDriftOutput(result, changePoints);
		output.setTimeElapsed(sw.getTime());

		System.out.println(result.toString());

		return output;
	}

	private Map<String, String> calculateReasons(UIPluginContext context, XLog log, int windowSize, int index) {
		XLog filtered = XLogUtils.filter(log);

		GraphMetrics reference = calculateMetrics(context, filtered, index - windowSize, index);
		GraphMetrics detection = calculateMetrics(context, filtered, index, index + windowSize);

		List<String> metrics = new ArrayList<>(reference.flatMetrics().keySet());

		List<Double> referenceValues = new ArrayList<Double>(reference.flatMetrics().values());
		List<Double> detectionValues = new ArrayList<Double>(detection.flatMetrics().values());

		Map<String, Double> reasons = new HashMap<>();
		Map<String, String> reasons2 = new HashMap<>();
		Map<String, Integer> indexMap = new HashMap<>();

		for (int j = 0; j < referenceValues.size(); j++) {
			if (Math.abs(referenceValues.get(j) - detectionValues.get(j)) > 0) {
				indexMap.put(metrics.get(j), j);
				reasons.put(metrics.get(j), detectionValues.get(j) - referenceValues.get(j));
			}
		}
		
		List<String> indegree = new ArrayList<>();
		List<String> outdegree = new ArrayList<>();
		
		for(Entry<String, Double> met : reasons.entrySet()) {
			if(met.getKey().contains(GraphMetrics.INDEGREE)) {
				indegree.add(met.getKey().replace(GraphMetrics.INDEGREE, ""));
			} else if(met.getKey().contains(GraphMetrics.OUTDEGREE)) {
				outdegree.add(met.getKey().replace(GraphMetrics.OUTDEGREE, ""));
			}
		}
		
		for (Entry<String, Double> met : reasons.entrySet()) {
			if(met.getKey().contains("->")) {
				for(String in : indegree) {
					if(met.getKey().contains("->" + in)) {
						reasons2.put(met.getKey(), met.getValue() + " (" + detectionValues.get(indexMap.get(met.getKey())) + ")");
					}
					
				}
				for(String out : outdegree) {
					if(met.getKey().contains(out + "->")) {
						 reasons2.put(met.getKey(), met.getValue() + " (" + detectionValues.get(indexMap.get(met.getKey())) + ")");
					}
				}
			} else {
				reasons2.put(met.getKey(), met.getValue().toString());
			}
		}

		return reasons2;
	}

	private GraphMetrics calculateMetrics(UIPluginContext context, XLog log, int startIndex, int endIndex) {
		XLog subset = XLogUtils.subset(log, startIndex, endIndex);
		GraphMetrics metrics = MetricsUtils.getGraphMetrics(MetricsUtils.generateHeuristicsNet(context, subset),
				eventNames);

		return metrics;
	}

	private PValue calculatePValue(UIPluginContext context, XLog log, int windowSize, int index) {
		GraphMetrics reference = calculateMetrics(context, log, index, index + windowSize);
		GraphMetrics detection = calculateMetrics(context, log, index + windowSize, index + windowSize * 2);

		PValue pvalue = new PValue();

		// calc arc
		List<Long> referenceValues = new ArrayList<>(reference.flatArcMetrics());
		List<Long> detectionValues = new ArrayList<>(detection.flatArcMetrics());

		pvalue.setArcPvalue(calculatePValue(referenceValues, detectionValues));

		// calc node
		referenceValues = new ArrayList<>(reference.flatNodeMetrics());
		detectionValues = new ArrayList<>(detection.flatNodeMetrics());
 
		pvalue.setNodePvalue(calculatePValue(referenceValues, detectionValues));

		return pvalue;
	}

	private double calculatePValue(List<Long> references, List<Long> detections) {
		List<Long> referenceValues = new ArrayList<>(references);
		List<Long> detectionValues = new ArrayList<>(detections);

		// remove zero values
		for (int j = 0; j < referenceValues.size(); j++) {
			if (referenceValues.get(j) == 0 && detectionValues.get(j) == 0) {
				referenceValues.remove(j);
				detectionValues.remove(j);

				j--;
			}
		}

		// perform G-test
		try {
			GTest gtest = new GTest();
			return gtest.gTestDataSetsComparison(ListUtils.toLongArray(referenceValues),
					ListUtils.toLongArray(detectionValues));
		} catch (ZeroException ex) {
			return 1;
		}
	}

}
