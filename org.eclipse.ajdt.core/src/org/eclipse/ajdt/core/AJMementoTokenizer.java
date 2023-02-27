/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core;

import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;

public class AJMementoTokenizer extends MementoTokenizer {
	public static final String COUNT = Character.toString(JavaElement.JEM_COUNT);
	public static final String JAVAPROJECT = Character.toString(JavaElement.JEM_JAVAPROJECT);
	public static final String PACKAGEFRAGMENTROOT = Character.toString(JavaElement.JEM_PACKAGEFRAGMENTROOT);
	public static final String PACKAGEFRAGMENT = Character.toString(JavaElement.JEM_PACKAGEFRAGMENT);
	public static final String FIELD = Character.toString(JavaElement.JEM_FIELD);
	public static final String METHOD = Character.toString(JavaElement.JEM_METHOD);
	public static final String INITIALIZER = Character.toString(JavaElement.JEM_INITIALIZER);
	public static final String COMPILATIONUNIT = Character.toString(JavaElement.JEM_COMPILATIONUNIT);
	public static final String CLASSFILE = Character.toString(JavaElement.JEM_CLASSFILE);
	public static final String MODULAR_CLASSFILE = Character.toString(JavaElement.JEM_MODULAR_CLASSFILE);
	public static final String TYPE = Character.toString(JavaElement.JEM_TYPE);
	public static final String MODULE = Character.toString(JavaElement.JEM_MODULE);
	public static final String PACKAGEDECLARATION = Character.toString(JavaElement.JEM_PACKAGEDECLARATION);
	public static final String IMPORTDECLARATION = Character.toString(JavaElement.JEM_IMPORTDECLARATION);
	public static final String LOCALVARIABLE = Character.toString(JavaElement.JEM_LOCALVARIABLE);
	public static final String TYPE_PARAMETER = Character.toString(JavaElement.JEM_TYPE_PARAMETER);
	public static final String ANNOTATION = Character.toString(JavaElement.JEM_ANNOTATION);
	public static final String LAMBDA_EXPRESSION = Character.toString(JavaElement.JEM_LAMBDA_EXPRESSION);
	public static final String LAMBDA_METHOD = Character.toString(JavaElement.JEM_LAMBDA_METHOD);
	public static final String STRING = Character.toString(JavaElement.JEM_STRING);
	public static final String CLASSPATH_ATTRIBUTE = JAVAPROJECT + PACKAGEFRAGMENTROOT;

	// begin AspectJ change
	public static final String ASPECT_CU = Character.toString(AspectElement.JEM_ASPECT_CU);
	public static final String ADVICE = Character.toString(AspectElement.JEM_ADVICE);
	public static final String ASPECT_TYPE = Character.toString(AspectElement.JEM_ASPECT_TYPE);
	public static final String CODEELEMENT = Character.toString(AspectElement.JEM_CODEELEMENT);
	public static final String ITD_METHOD = Character.toString(AspectElement.JEM_ITD_METHOD);
	public static final String ITD_FIELD = Character.toString(AspectElement.JEM_ITD_FIELD);
	public static final String DECLARE = Character.toString(AspectElement.JEM_DECLARE);
	public static final String POINTCUT = Character.toString(AspectElement.JEM_POINTCUT);
	// end AspectJ change

	private final char[] memento;
	private final int length;
	private int index = 0;

	public AJMementoTokenizer(String memento) {
		super(memento);
		this.memento = memento.toCharArray();
		this.length = this.memento.length;
	}

	public AJMementoTokenizer(MementoTokenizer tokenizer) {
		super(String.valueOf((char[]) ReflectionUtils.getPrivateField(MementoTokenizer.class, "memento", tokenizer)));
		memento = (char[]) ReflectionUtils.getPrivateField(MementoTokenizer.class, "memento", tokenizer);
		length = memento.length;
		index = (Integer) ReflectionUtils.getPrivateField(MementoTokenizer.class, "index", tokenizer);
		ReflectionUtils.setPrivateField(MementoTokenizer.class, "index", this, index);
	}

	/**
	 * create a memento tokenizer that is reset to the token after the given name
	 */
	public AJMementoTokenizer(MementoTokenizer tokenizer, String resetToName) {
		super(String.valueOf((char[]) ReflectionUtils.getPrivateField(MementoTokenizer.class, "memento", tokenizer)));
		memento = (char[]) ReflectionUtils.getPrivateField(MementoTokenizer.class, "memento", tokenizer);
		length = memento.length;

		// only reset to the given name if it is found
		int nameIndex = CharOperation.indexOf(resetToName.toCharArray(), memento, true);
		if (index >= 0)
			index = nameIndex + resetToName.length();
		else
			index = (Integer) ReflectionUtils.getPrivateField(MementoTokenizer.class, "index", tokenizer);
		ReflectionUtils.setPrivateField(MementoTokenizer.class, "index", this, index);
	}

	public boolean hasMoreTokens() {
		return this.index < this.length;
	}

	public String nextToken() {
		int start = this.index;
		StringBuilder buffer = null;
		switch (this.memento[this.index++]) {
			case JavaElement.JEM_ESCAPE:
				buffer = new StringBuilder();
				buffer.append(this.memento[this.index]);
				start = ++this.index;
				break;
			case JavaElement.JEM_COUNT:
				return COUNT;
			case JavaElement.JEM_JAVAPROJECT:
				// Also covers JavaElement#JEM_DELIMITER_ESCAPE, in which case,
				// we seek ahead by one char and check if it's an escaped delimiter
				// and if that's true, we return that as the token.
				// Else, we decide that JEM_JAVAPROJECT is the current token.
				if (this.index < this.length) {
					char nextChar = this.memento[this.index++];
					switch(nextChar) {
						case JavaElement.JEM_LAMBDA_EXPRESSION:
							return LAMBDA_EXPRESSION;
						case JavaElement.JEM_LAMBDA_METHOD:
							return LAMBDA_METHOD;
						case JavaElement.JEM_STRING:
							return STRING;
						case JavaElement.JEM_PACKAGEFRAGMENTROOT:
							return CLASSPATH_ATTRIBUTE;
						default:
							this.index--;
							break;
					}
				}
				return JAVAPROJECT;
			case JavaElement.JEM_PACKAGEFRAGMENTROOT:
				return PACKAGEFRAGMENTROOT;
			case JavaElement.JEM_PACKAGEFRAGMENT:
				return PACKAGEFRAGMENT;
			case JavaElement.JEM_FIELD:
				return FIELD;
			case JavaElement.JEM_METHOD:
				return METHOD;
			case JavaElement.JEM_INITIALIZER:
				return INITIALIZER;
			case JavaElement.JEM_COMPILATIONUNIT:
				return COMPILATIONUNIT;
			case JavaElement.JEM_CLASSFILE:
				return CLASSFILE;
			case JavaElement.JEM_MODULAR_CLASSFILE:
				return MODULAR_CLASSFILE;
			case JavaElement.JEM_TYPE:
				return TYPE;
			case JavaElement.JEM_MODULE:
				return MODULE;
			case JavaElement.JEM_PACKAGEDECLARATION:
				return PACKAGEDECLARATION;
			case JavaElement.JEM_IMPORTDECLARATION:
				return IMPORTDECLARATION;
			case JavaElement.JEM_LOCALVARIABLE:
				return LOCALVARIABLE;
			case JavaElement.JEM_TYPE_PARAMETER:
				return TYPE_PARAMETER;
			case JavaElement.JEM_ANNOTATION:
				return ANNOTATION;
			// begin AspectJ change
			case AspectElement.JEM_ADVICE:
				return ADVICE;
			case AspectElement.JEM_ASPECT_TYPE:
				return ASPECT_TYPE;
			case AspectElement.JEM_CODEELEMENT:
				return CODEELEMENT;
			case AspectElement.JEM_ITD_METHOD:
				return ITD_METHOD;
			case AspectElement.JEM_ITD_FIELD:
				return ITD_FIELD;
			case AspectElement.JEM_DECLARE:
				return DECLARE;
			case AspectElement.JEM_POINTCUT:
				return POINTCUT;
			case AspectElement.JEM_ASPECT_CU:
				// only return here if JDT weaving is off
				// if JDT weaving is on, then a * here
				// means that we are in an on demand import declaration
				if (!AspectJPlugin.USING_CU_PROVIDER) {
					return ASPECT_CU;
				}
				// end AspectJ change
		}
		loop: while (this.index < this.length) {
			switch (this.memento[this.index]) {
				case JavaElement.JEM_ESCAPE:
					if (buffer == null) buffer = new StringBuilder();
					buffer.append(this.memento, start, this.index - start);
					start = ++this.index;
					break;
				case JavaElement.JEM_COUNT:
				case JavaElement.JEM_JAVAPROJECT:
				case JavaElement.JEM_PACKAGEFRAGMENTROOT:
				case JavaElement.JEM_PACKAGEFRAGMENT:
				case JavaElement.JEM_FIELD:
				case JavaElement.JEM_METHOD:
				case JavaElement.JEM_INITIALIZER:
				case JavaElement.JEM_COMPILATIONUNIT:
				case JavaElement.JEM_CLASSFILE:
				case JavaElement.JEM_TYPE:
				case JavaElement.JEM_MODULE:
				case JavaElement.JEM_PACKAGEDECLARATION:
				case JavaElement.JEM_IMPORTDECLARATION:
				case JavaElement.JEM_LOCALVARIABLE:
				case JavaElement.JEM_TYPE_PARAMETER:
				case JavaElement.JEM_ANNOTATION:
					// begin AspectJ change
				case AspectElement.JEM_ADVICE:
				case AspectElement.JEM_ASPECT_TYPE:
				case AspectElement.JEM_CODEELEMENT:
				case AspectElement.JEM_ITD_METHOD:
				case AspectElement.JEM_ITD_FIELD:
				case AspectElement.JEM_DECLARE:
				case AspectElement.JEM_POINTCUT:
					break loop;
				// end AspectJ change
				// begin AspectJ change
				case AspectElement.JEM_ASPECT_CU:
					// only break here if JDT weaving is off
					// if JDT weaving is on, then a * here
					// means that we are in an on demand import declaration
					if (!AspectJPlugin.USING_CU_PROVIDER) {
						break loop;
					}
					// end AspectJ change
			}
			this.index++;
		}
		if (buffer != null) {
			buffer.append(this.memento, start, this.index - start);
			return buffer.toString();
		} else {
			return new String(this.memento, start, this.index - start);
		}
	}

	void setIndexTo(int newIndex) {
		this.index = newIndex;
	}
}
