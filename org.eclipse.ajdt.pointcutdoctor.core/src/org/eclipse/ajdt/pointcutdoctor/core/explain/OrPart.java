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
package org.eclipse.ajdt.pointcutdoctor.core.explain;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Shadow;

public class OrPart extends AbstractPart {

	private Part left;
	private Part right;
	
	public OrPart(Part left, Part right) {
		this.left = left;
		this.right = right;
	}

	public Reason explain(Shadow shadow, FuzzyBoolean matchResult) {
		Reason leftReason = left.explain(shadow, matchResult);
		Reason rightReason = right.explain(shadow, matchResult);
		if (matchResult.alwaysTrue())
			return leftReason.union(rightReason);
		else if(matchResult.alwaysFalse())
			return leftReason.and(rightReason);
		else {
//		case Maybe:
			Reason r1 = leftReason.and(rightReason);   // Am^Bm
			Reason r2 = left.explain(shadow, FuzzyBoolean.NO).and(rightReason); // Af^Bm
			Reason r3 = leftReason.and(right.explain(shadow, FuzzyBoolean.NO)); // Am^Bf
			return r1.union(r2.union(r3));
		}
	}

	public String toString() {
		return "("+left.toString()+" v "+right.toString()+")";
	}

}
