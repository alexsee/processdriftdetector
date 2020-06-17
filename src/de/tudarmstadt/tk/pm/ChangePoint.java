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

import java.util.Map;

public class ChangePoint {
	
	private int index;
	
	private int indexBeforeChunk;
	
	private int indexAfterChunk;
	
	private Map<String, String> reasons;
	
	public ChangePoint(int index) {
		this.index = index;
	}
	
	public ChangePoint(int index, int indexBeforeChunk, int indexAfterChunk) {
		this.index = index;
		this.indexBeforeChunk = indexBeforeChunk;
		this.indexAfterChunk = indexAfterChunk;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndexBeforeChunk() {
		return indexBeforeChunk;
	}

	public void setIndexBeforeChunk(int indexBeforeChunk) {
		this.indexBeforeChunk = indexBeforeChunk;
	}

	public int getIndexAfterChunk() {
		return indexAfterChunk;
	}

	public void setIndexAfterChunk(int indexAfterChunk) {
		this.indexAfterChunk = indexAfterChunk;
	}

	public Map<String, String> getReasons() {
		return reasons;
	}

	public void setReasons(Map<String, String> reasons) {
		this.reasons = reasons;
	}

	
}
