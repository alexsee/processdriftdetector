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
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class XLogUtils {

	public static String getTraceType(XTrace trace) {
		String name = XConceptExtension.instance().extractName(trace);
		if (name.indexOf("_") < 0) {
			return "other";
		} else {
			return name.substring(0, name.indexOf("_"));
		}
	}

	public static String getTraceName(XTrace trace) {
		String name = XConceptExtension.instance().extractName(trace);
		return name;
	}

	/**
	 * Sorts the event log based on the first event.
	 * 
	 * @param log
	 * @return
	 */
	public static XLog sortLog(XLog log) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		XLog sortedLog = cloneButEmptyLog(factory, log);

		log.stream().sorted(new Comparator<XTrace>() {
			@Override
			public int compare(XTrace o1, XTrace o2) {
				if (o1.size() > 0 && o2.size() > 0) {
					Date d1 = XTimeExtension.instance().extractTimestamp(o1.get(0));
					Date d2 = XTimeExtension.instance().extractTimestamp(o2.get(0));

					return d1.compareTo(d2);
				}

				return -1;
			};
		}).forEach(x -> sortedLog.add(x));

		return sortedLog;
	}

	/**
	 * Create a new event log based on the classifiers from a given log.
	 * 
	 * @param factory
	 * @param log
	 * @return
	 */
	public static XLog cloneButEmptyLog(XFactory factory, XLog log) {
		XLog result = factory.createLog();
		result.getClassifiers().addAll(log.getClassifiers());

		for (XExtension extension : log.getExtensions())
			result.getExtensions().add(extension);

		// clone attributes
		result.setAttributes((XAttributeMap) log.getAttributes().clone());

		return result;
	}

	/**
	 * Returns the change string for given chunks.
	 * 
	 * @param chunks
	 * @param i1
	 * @param i2
	 * @return
	 */
	public static String getChangeString(List<XLog> chunks, int i1, int i2) {
		return XConceptExtension.instance().extractName(chunks.get(i1).get(chunks.get(i1).size() - 1)) + " - "
				+ XConceptExtension.instance().extractName(chunks.get(i2).get(0));
	}

	/**
	 * Splits the given event log into the given number of chunks.
	 * 
	 * @param log
	 * @param numberOfChunks
	 * @return
	 */
	public static List<XLog> createChunksByCount(XLog log, int numberOfTraces) {
		if (numberOfTraces > log.size()) {
			throw new IllegalArgumentException(
					"The number of traces per chunk must be smaller than the number of traces.");
		}

		XFactory factory = XFactoryRegistry.instance().currentDefault();

		// split the event log
		List<XLog> result = new ArrayList<XLog>();
		XLog resultLog = null;

		for (int i = 0; i < log.size(); i++) {
			if (i % numberOfTraces == 0) {
				if (resultLog != null) {
					result.add(resultLog);
				}
				resultLog = cloneButEmptyLog(factory, log);
			}

			resultLog.add(log.get(i));
		}

		result.add(resultLog);

		return result;
	}

	/**
	 * Splits the given event log into the given number of chunks.
	 * 
	 * @param log
	 * @param numberOfChunks
	 * @return
	 */
	public static List<XLog> createChunksByNumber(XLog log, int numberOfChunks) {
		if (numberOfChunks > log.size()) {
			throw new IllegalArgumentException("The number of chunks must be smaller than the number of traces.");
		}

		XFactory factory = XFactoryRegistry.instance().currentDefault();

		// calculate chunk size
		int chunkSize = log.size() / numberOfChunks;

		// split the event log
		List<XLog> result = new ArrayList<XLog>();

		for (int i = 0; i < numberOfChunks; i++) {
			XLog resultLog = cloneButEmptyLog(factory, log);

			for (int j = i * chunkSize; j < Math.min(((i + 1) * chunkSize), log.size()); j++) {
				resultLog.add(log.get(j));
			}

			result.add(resultLog);
		}

		return result;
	}

	/**
	 * Splits the event log into chunks based on the duration between start
	 * events.
	 * 
	 * @param log
	 * @param duration
	 * @return
	 */
	public static List<XLog> createChunksByTimeSpan(XLog log, long duration) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();

		// calculate chunk size
		Date startDate = null;
		XLog currentLog = cloneButEmptyLog(factory, log);

		// split the event log
		List<XLog> result = new ArrayList<XLog>();

		for (XTrace trace : log) {
			if (trace.size() == 0) {
				continue;
			}

			Date curDate = XTimeExtension.instance().extractTimestamp(trace.get(0));

			if (startDate == null) {
				startDate = curDate;
			}

			// check if date is larger than duration
			if ((curDate.getTime() - startDate.getTime()) > duration) {
				if (currentLog.size() > 0) {
					result.add(currentLog);
				}

				startDate = curDate;
				currentLog = cloneButEmptyLog(factory, log);
			}

			currentLog.add(trace);
		}

		if (currentLog.size() > 0) {
			result.add(currentLog);
		}

		return result;
	}
	
	/**
	 * Splits the given event log into the given number of chunks.
	 * 
	 * @param log
	 * @param numberOfChunks
	 * @return
	 */
	public static XLog subset(XLog log, int startIndex, int endIndex) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();

		// split the event log
		XLog resultLog = cloneButEmptyLog(factory, log);
		for (int i = Math.max(0, startIndex); i < Math.min(log.size(), endIndex); i++) {
			resultLog.add(log.get(i));
		}

		return resultLog;
	}
	
	/**
	 * Splits the given event log into the given number of chunks.
	 * 
	 * @param log
	 * @param numberOfChunks
	 * @return
	 */
	public static XLog filter(XLog log) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();

		// split the event log
		XLog resultLog = cloneButEmptyLog(factory, log);
		
		for (int i = 0; i < log.size(); i++) {
			XTrace trace = log.get(i);
			XTrace newTrace = factory.createTrace();
			
			for(int j = 0; j < trace.size(); j++) {
//				if(XLifecycleExtension.instance().extractTransition(trace.get(j)).contains("complete")) {
					newTrace.add(trace.get(j));
//				}
			}
			
			resultLog.add(newTrace);
		}

		return resultLog;
	}

}
