/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Andy Clement IBM     initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

public class EnumAnnotationValue extends AnnotationValue {

	private String typeSignature;
	private String value;

	public EnumAnnotationValue(String typeSignature, String value) {
		super(AnnotationValue.ENUM_CONSTANT);
		this.typeSignature = typeSignature;
		this.value = value;
	}

	public String getType() {
		return typeSignature;
	}

	public String stringify() {
		return typeSignature+value;
	}

	public String getValue() {
		return value;
	}

	public String toString() {
		return "E(" + typeSignature + " " + value + ")";
	}

}
