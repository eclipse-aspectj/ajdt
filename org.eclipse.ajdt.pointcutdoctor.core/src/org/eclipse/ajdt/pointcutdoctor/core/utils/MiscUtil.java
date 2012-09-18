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
package org.eclipse.ajdt.pointcutdoctor.core.utils;

public class MiscUtil {

	public static boolean equalsOrBothNull(Object v1, Object v2) {
		if (v1!=null)
			return v1.equals(v2);
		else if (v2!=null)
			return v2.equals(v1);
		else return true;
	}

}
