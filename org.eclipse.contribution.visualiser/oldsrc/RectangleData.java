/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial version
 *******************************************************************************/
package org.eclipse.contribution.visualiser.views.old;

import org.eclipse.contribution.visualiser.core.Stripe;
import org.eclipse.contribution.visualiser.interfaces.IMember;


/**
 * Stores data about rectangle figures representing stripes, which are
 * used in the Visualiser view
 */
public class RectangleData {
	protected IMember mem;
	protected Stripe stripe;
	protected String kind;
		
	/**
	 * The constructor - stores the associated Stripe, IMember and kind
	 * @param m - the IMember
	 * @param s - the Stripe
	 * @param a - the kind
	 */
	public RectangleData(IMember m, Stripe s, String a) {
		stripe = s;
		mem = m;
		kind = a;
	}

	
	/**
	 * Returns a String representation of this data
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(
			"RectangleData Member:"
				+ mem.getFullname()
				+ (kind != null ? "  Kind:" + kind : "")
				+ (stripe != null ? "  Stripe:" + stripe.toString() : ""));
		return sb.toString();
	}
}