/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceRange;

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
			System.out.println("getNameRange line="+line);
			setStartAndEnd(line);
		}
		return new SourceRange(this.nameStart, this.nameEnd-this.nameStart+1);
	}

	public int hashCode() {
		return name.hashCode();
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
					//System.out.println("line="+(lines+1)+" offset="+i);
					if (foundLine) {
						nameEnd=i-1;
						System.out.println("end="+nameEnd);
						return;
					}
					if ((lines+1)==targetLine) {
						nameStart=i+1;
						System.out.println("start="+nameStart);
						foundLine=true;
					}
				}
			}
		} catch (JavaModelException e) {
		}
	}
}
