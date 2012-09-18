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

public class NotPart extends AbstractPart {
	private Part other;
	
	public NotPart(Part other) {
		this.other = other;
	}

	public Reason explain(Shadow shadow, FuzzyBoolean matchResult) {
		return other.explain(shadow, matchResult.not());
//		if (matchResult.alwaysTrue())
//			return other.explain(shadow, ExplainOption.False);
//		case False:
//			return other.explain(shadow, ExplainOption.True);
//		default:  // Maybe
//			return other.explain(shadow, ExplainOption.Maybe);
//		}
		
	}

	public String toString() {
		return "~("+other.toString()+")";
	}

}
