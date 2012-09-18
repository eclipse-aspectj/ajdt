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
package org.eclipse.ajdt.pointcutdoctor.core;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Shadow;

public class ShadowWrapper {
	@Override
	public String toString() {
		return (matchResult.maybeTrue()? "?":"")+shadow.toString();
	}

	private Shadow shadow;
	private FuzzyBoolean matchResult;
//	private boolean maybe;

	public ShadowWrapper(Shadow shadow, FuzzyBoolean matchResult) {
		this.shadow = shadow;
		this.matchResult = matchResult;
	}

	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof ShadowWrapper && shadow!=null)
			return shadow.equals(((ShadowWrapper)arg0).getShadow());
		else return false;
	}

	@Override
	public int hashCode() {
		if (shadow!=null)
			return shadow.hashCode();
		else return 0;
	}

	public Shadow getShadow() {
		return shadow;
	}

	public void setShadow(Shadow shadow) {
		this.shadow = shadow;
	}

	public FuzzyBoolean getMatchResult() {
		return matchResult;
	}
	
}
