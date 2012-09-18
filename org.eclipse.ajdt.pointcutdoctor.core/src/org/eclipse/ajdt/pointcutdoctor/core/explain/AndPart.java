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

public class AndPart extends AbstractPart {
	private Part left;
	private Part right;
	
	public AndPart(Part left, Part right) {
		this.left = left;
		this.right = right;
	}

	public Reason explain(Shadow shadow, FuzzyBoolean matchResult) {
		Reason leftReason = left.explain(shadow, matchResult);
		Reason rightReason = right.explain(shadow, matchResult);
		
		if (matchResult.alwaysTrue())
			return leftReason.and(rightReason);
		if (matchResult.alwaysFalse())
			return leftReason.union(rightReason);
		else {
//			case Maybe:
				Reason r1 = leftReason.and(rightReason);   // Am^Bm
				Reason leftT = left.explain(shadow, FuzzyBoolean.YES);
				Reason r2 = leftT.and(rightReason); // At^Bm
				Reason rightT = right.explain(shadow, FuzzyBoolean.YES);
				Reason r3 = leftReason.and(rightT); // Am^Bt
				return r1.union(r2.union(r3));
			}
	}
	
	public String toString() {
		return "("+left.toString()+" ^ "+right.toString()+")";
	}

}
