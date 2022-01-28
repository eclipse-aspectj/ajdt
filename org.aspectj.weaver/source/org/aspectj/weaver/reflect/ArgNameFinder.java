/* *******************************************************************
 * Copyright (c) 2005-2017 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import java.lang.reflect.Member;

/**
 * @author Adrian Colyer
 * @author Andy Clement
 */
public interface ArgNameFinder {

	/**
	 * Attempt to discover the parameter names for a reflectively obtained member.
	 * @param forMember the member for which parameter names are being looked up
	 * @return parameter names or null if names can't be determined
	 */
	String[] getParameterNames(Member forMember);

}
