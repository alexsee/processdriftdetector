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

public class PValue {
	
	private double nodePvalue;
	private double arcPvalue;

	public PValue() {

	}

	public PValue(double nodePvalue, double arcPvalue) {
		this.nodePvalue = nodePvalue;
		this.arcPvalue = arcPvalue;
	}

	public double getNodePvalue() {
		return nodePvalue;
	}

	public void setNodePvalue(double nodePvalue) {
		this.nodePvalue = nodePvalue;
	}

	public double getArcPvalue() {
		return arcPvalue;
	}

	public void setArcPvalue(double arcPvalue) {
		this.arcPvalue = arcPvalue;
	}
	
}
