/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceRange;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * 
 * @author mchapman
 */
public class AJCodeElement extends LocalVariable implements IAJCodeElement {
	private String name;
	private int line;
	
	/**
	 * @param parent
	 * @param name
	 * @param parameterTypes
	 */
	public AJCodeElement(JavaElement parent, int line, String name) {
		super(parent,name,0,0,0,0,"I");
		this.name=name;
		this.line=line;
	}

	public ISourceRange getNameRange() {
		if (nameStart==0) {
			setStartAndEnd(line);
		}
		return new SourceRange(this.nameStart, this.nameEnd-this.nameStart+1);
	}

	/**
	 * Overriding LocalVariable.hashcode() to include
	 * the line number of the AJCodeElement (since two
	 * different AJCodeElements can have the same name and 
	 * parent - must always have different line numbers.
	 */
	public int hashCode() {
		return Util.combineHashCodes(name.hashCode(),line);
	}

	/**
	 * Overriding LocalVariable.equals to include the line number.
	 * An object is equal to this one if super.equals(o)
	 * returns true AND the line numbers are the same.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof AJCodeElement)) {
			return super.equals(o);
		}
		AJCodeElement ajce = (AJCodeElement)o;
		return super.equals(o) && (line == ajce.line);
	}
	
	private void setStartAndEnd(int targetLine) {
		try {
			IOpenable openable = this.parent.getOpenableParent();
			IBuffer buffer = openable.getBuffer();
			String source = buffer.getContents();
			int lines = 0;
			boolean foundLine=false;
			for (int i = 0; i < source.length(); i++) {
				if (source.charAt(i) == '\n') {
					lines++;
					if (foundLine) {
						nameEnd=i-1;
						//System.out.println("end="+nameEnd);
						return;
					}
					if ((lines+1)==targetLine) {
						nameStart=i+1;
						//System.out.println("start="+nameStart);
						foundLine=true;
					}
				}
			}
		} catch (JavaModelException e) {
		}
	}
	
	/**
	 * @return Returns the line in the file of this AJCodeElement.
	 */
	public int getLine() {
		return line;
	}
	
	/**
	 * @return Returns the name for this AJCodeElement
	 */
	public String getName() {
		return name;
	}
}
