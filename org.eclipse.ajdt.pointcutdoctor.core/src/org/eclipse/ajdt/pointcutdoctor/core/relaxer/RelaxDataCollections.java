/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.core.relaxer;

import java.util.ArrayList;
import java.util.List;

public class RelaxDataCollections {

	int currentRowIdx = 0;
	List<List<RelaxData>> rows = new ArrayList<List<RelaxData>>();
	
	public void addToCurrentRow(RelaxData d) {
		while (currentRowIdx>=rows.size()) 
			rows.add(new ArrayList<RelaxData>());
		List<RelaxData> row = rows.get(currentRowIdx);
		row.add(d);
	}

	public void nextRow() {
		currentRowIdx++;
	}
	
	public void clear() {
		rows.clear();
	}

	public List<List<RelaxData>> getRows() {
		return rows;
	}

}
