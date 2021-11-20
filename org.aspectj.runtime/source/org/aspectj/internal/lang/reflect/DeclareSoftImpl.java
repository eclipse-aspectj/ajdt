/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.internal.lang.reflect;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.DeclareSoft;
import org.aspectj.lang.reflect.PointcutExpression;

/**
 * @author colyer
 *
 */
public class DeclareSoftImpl implements DeclareSoft {

	private AjType<?> declaringType;
	private PointcutExpression pointcut;
	private AjType<?> exceptionType;
	private String missingTypeName;


	public DeclareSoftImpl(AjType<?> declaringType, String pcut, String exceptionTypeName) {
		this.declaringType = declaringType;
		this.pointcut = new PointcutExpressionImpl(pcut);
		try {
			ClassLoader cl = declaringType.getJavaClass().getClassLoader();
			this.exceptionType = AjTypeSystem.getAjType(Class.forName(exceptionTypeName,false,cl));
		} catch (ClassNotFoundException ex) {
			this.missingTypeName = exceptionTypeName;
		}
	}

	public AjType getDeclaringType() {
		return this.declaringType;
	}

	public AjType getSoftenedExceptionType() throws ClassNotFoundException {
		if (this.missingTypeName != null) throw new ClassNotFoundException(this.missingTypeName);
		return this.exceptionType;
	}

	public PointcutExpression getPointcutExpression() {
		return this.pointcut;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("declare soft : ");
		if (this.missingTypeName != null) {
			sb.append(this.exceptionType.getName());
		} else {
			sb.append(this.missingTypeName);
		}
		sb.append(" : ");
		sb.append(getPointcutExpression().asString());
		return sb.toString();
	}
}
