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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GraphMetrics {
	
	public static final String NUMBER_OF_NODES = "NUMBER_OF_NODES";
	
	public static final String NUMBER_OF_EDGES = "NUMBER_OF_EDGES";

	public static final String NETWORK_DEGREE = "NETWORK_DEGREE";

	public static final String INDEGREE = "INDEGREE_";

	public static final String OUTDEGREE = "OUTDEGREE_";

	public static final String DEGREE = "DEGREE_";
	
	public static final String DENSITY = "DENSITY";
	
	
	private int numberOfNodes;
	
	private int numberOfEdges;

	private Map<String, Integer> outgoingEdges = new HashMap<>();
	
	private Map<String, Integer> incomingEdges = new HashMap<>();
	
	private Map<String, Integer> nodeDegree = new HashMap<>();
	
	private Map<String, Integer> nodeCount = new HashMap<>();
	
	private Map<String, Integer> arcCount = new HashMap<>();
	
	public Map<String, Integer> getArcCount() {
		return arcCount;
	}

	public void setArcCount(Map<String, Integer> arcCount) {
		this.arcCount = arcCount;
	}

	public Map<String, Integer> getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(Map<String, Integer> nodeCount) {
		this.nodeCount = nodeCount;
	}

	private int networkDegree;
	
	public Collection<Long> flatArcMetrics() {
		Map<String, Long> result = new HashMap<>();
		
		for (String node : getArcCount().keySet()) {
			result.put(node, (long) getArcCount().get(node));
		}

		return result.values();
	}
	
	public Collection<Long> flatNodeMetrics() {
		Map<String, Long> result = new HashMap<>();
		
		for (String node : getNodeCount().keySet()) {
			result.put(node, (long) getNodeCount().get(node));
		}

		return result.values();
	}
	
	public Map<String, Double> flatMetrics() {
		Map<String, Double> result = new HashMap<>();
		result.put(NUMBER_OF_NODES, (double) getNumberOfNodes());
		result.put(NUMBER_OF_EDGES, (double) getNumberOfEdges());
		result.put(NETWORK_DEGREE, (double) getNetworkDegree());
		
		for (String node : getIncomingEdges().keySet()) {
			result.put(INDEGREE + node, (double) getIncomingEdges().get(node));
		}
		
		for (String node : getOutgoingEdges().keySet()) {
			result.put(OUTDEGREE + node, (double) getOutgoingEdges().get(node));
		}
		
//		for (String node : getNodeDegree().keySet()) {
//			result.put(DEGREE + node, (double) getNodeDegree().get(node));
//		}
		
		for (String node : getArcCount().keySet()) {
			result.put(node, (double) getArcCount().get(node));
		}
		
		result.put(DENSITY, getNumberOfEdges() / (getNumberOfNodes() * (getNumberOfNodes() - 1D)));

		return result;
	}
	
	public int getNetworkDegree() {
		return networkDegree;
	}

	public void setNetworkDegree(int networkDegree) {
		this.networkDegree = networkDegree;
	}

	public Map<String, Integer> getNodeDegree() {
		return nodeDegree;
	}

	public Map<String, Integer> getIncomingEdges() {
		return incomingEdges;
	}
	
	public void putOutgoingEdges(String activity, Integer count) {
		this.outgoingEdges.put(activity, count);
	}
	
	public void putIncomingEdges(String activity, Integer count) {
		this.incomingEdges.put(activity, count);
	}
	
	public void putNodeDegree(String activity, Integer count) {
		this.nodeDegree.put(activity, count);
	}

	public Map<String, Integer> getOutgoingEdges() {
		return outgoingEdges;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}
	
	public int getNumberOfEdges() {
		return numberOfEdges;
	}

	public void setNumberOfEdges(int numberOfEdges) {
		this.numberOfEdges = numberOfEdges;
	}

}
