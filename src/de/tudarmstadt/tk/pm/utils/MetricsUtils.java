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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.heuristics.HeuristicsNetGraph;
import org.processmining.models.heuristics.elements.Activity;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.HeuristicsMiner;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;

import de.tudarmstadt.tk.pm.GraphMetrics;

public class MetricsUtils {

	/**
	 * Calculate metrics for the given heuristics graph.
	 * 
	 * @param graph
	 * @param log
	 * @return
	 */
	public static GraphMetrics getGraphMetrics(HeuristicsNet net, List<String> eventNames) {
		HeuristicsNetGraph graph = convertToGraph(net);

		GraphMetrics metric = new GraphMetrics();
		metric.setNumberOfNodes(graph.getNodes().size());
		metric.setNumberOfEdges(graph.getEdges().size());

		List<String> evtNames = new ArrayList<>(eventNames);
		Map<String, Integer> nodeFire = new HashMap<>();
		Map<String, Integer> arcFire = new HashMap<>();

		// clear all metrics
		for (String eventName : evtNames) {
			metric.putOutgoingEdges(eventName, 0);
			metric.putIncomingEdges(eventName, 0);
			metric.putNodeDegree(eventName, 0);
			
			nodeFire.put(eventName, 0);
			for (String eventName2 : evtNames) {
				arcFire.put(eventName + "->" + eventName2, 0);
			}
		}

		// calculate node and arc distributions
		for (XEventClass cl1 : net.getActivitiesMappingStructures().getActivitiesMapping()) {
			nodeFire.put(cl1.getId(), net.getActivitiesActualFiring()[cl1.getIndex()]);

			for (XEventClass cl2 : net.getActivitiesMappingStructures().getActivitiesMapping()) {
				arcFire.put(cl1.getId() + "->" + cl2.getId(), (int) net.getArcUsage().get(cl1.getIndex(), cl2.getIndex()));
			}
		}
		
		metric.setNodeCount(nodeFire);
		metric.setArcCount(arcFire);

		// now update activity information
		for (Activity activity : graph.getActivities()) {
			int outEdges = graph.getOutEdges(activity).size();
			int inEdges = graph.getInEdges(activity).size();

			metric.putOutgoingEdges(activity.getLabel(), outEdges);
			metric.putIncomingEdges(activity.getLabel(), inEdges);
			metric.putNodeDegree(activity.getLabel(), outEdges + inEdges);

			if (outEdges + inEdges > metric.getNetworkDegree()) {
				metric.setNetworkDegree(outEdges + inEdges);
			}

			evtNames.remove(activity.getLabel());
		}

		return metric;
	}

	public static HeuristicsNet generateHeuristicsNet(PluginContext context, XLog log) {
		XEventClassifier selectedClassifier = new XEventNameClassifier();
//				for(XEventClassifier classifier : log.getClassifiers()) {
//					if(classifier.name().equals("MXML Legacy Classifier")) {
//						selectedClassifier = classifier;
//						break;
//					}
//				}

		HeuristicsMinerSettings settings = new HeuristicsMinerSettings();
		settings.setClassifier(selectedClassifier);
		settings.setL1lThreshold(0.0);
		settings.setL2lThreshold(0.0);
		settings.setLongDistanceThreshold(0.0);
		settings.setAndThreshold(0.0);

		HeuristicsMiner miner = new HeuristicsMiner(context, log, settings);
		HeuristicsNet net = miner.mine();

		return net;
	}

	/**
	 * Create a new heuristics net from a given event log.
	 * 
	 * @param context
	 * @param log
	 * @return
	 */
	public static HeuristicsNetGraph generateHeuristicsNetGraph(PluginContext context, XLog log) {
		HeuristicsNet net = generateHeuristicsNet(context, log);
		return convertToGraph(net);
	}

	private static HeuristicsNetGraph convertToGraph(HeuristicsNet net) {
		HeuristicsNetGraph graph = new HeuristicsNetGraph(net, "", false);

		return graph;
	}

}
