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

import java.util.Iterator;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;

public class TypeUtil {
	public static boolean isSubTypeOf(ResolvedType t1, ResolvedType t2) {
		Iterator<ResolvedType> superIter = t1.getDirectSupertypes();
		while (superIter.hasNext()) {
			ResolvedType sup = superIter.next();
			if (sup.equals(t2)) return true;
		}
		return false;
	}

	public static boolean isApplicableToType(Member method, ResolvedType type) {
		Iterator<ResolvedMember> declaredMethodIter = type.getMethods(false, true); //FIXKDV: correct parameters here?
		while(declaredMethodIter.hasNext()) {
			Member m = declaredMethodIter.next();
			if (m.getSignature().equals(method.getSignature()))
				return true;
		}
		return false;
	}


}
