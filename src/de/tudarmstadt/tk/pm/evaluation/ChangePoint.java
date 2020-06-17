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

public class ChangePoint {

	private String changePointTraceName;

	private Integer index;

	private Integer distance;

	public ChangePoint(String name, Integer index, Integer distance) {
		this.changePointTraceName = name;
		this.index = index;
		this.distance = distance;
	}

	public String toString() {
		return changePointTraceName + " (Index: " + index + "; Distance: " + distance + ")";
	}

	public String getChangePointTraceName() {
		return changePointTraceName;
	}

	public void setChangePointTraceName(String changePointTraceName) {
		this.changePointTraceName = changePointTraceName;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

}
