/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseSourceLocation;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.util.Util;

/**
 *
 * @author mchapman
 */
public class AJCodeElement extends LocalVariable implements IAJCodeElement {
	private final String name;

	private int startLine;

	public AJCodeElement(JavaElement parent, String name) {
		// The 3 calls to 'initializeLocations' are a horrible hack, see comment there
		super(
			parent, name, 0, 0,
			initializeLocations(parent, name)[0],
			initializeLocations(parent, name)[1],
			"I", new org.eclipse.jdt.internal.compiler.ast.Annotation[0], 0, false //$NON-NLS-1$
		);
		this.name = name;
		startLine = initializeLocations(parent, name)[2];
	}

    public AJCodeElement(JavaElement parent, String name, int occurrence) {
        this(parent,name);
        this.setOccurrenceCount(occurrence);
    }

	public ISourceRange getNameRange() {
		return new SourceRange(this.nameStart, this.nameEnd-this.nameStart+1);
	}

	public boolean equals(Object o) {
		if (!(o instanceof AJCodeElement)) {
			return super.equals(o);
		}
		AJCodeElement ajce = (AJCodeElement)o;
		return super.equals(o) && (getOccurrenceCount() == ajce.getOccurrenceCount());
	}

	public static int[] initializeLocations(JavaElement parent, String name) {
		// This is a horrible hack to accomodate the fact that LocalVariable.nameStart, and .nameEnd were made final in
		// Eclipse 2024-12. Instead of initialising the variables after creation, the values must now be passed directly
		// into the super constructor. Therefore, this method is called 3x, once for each of the 3 values returned. This is
		// inefficient, but keeps us from caching the array in a static thread-local variable, possibly creating a memory
		// leak in doing so.
		LocalVariable dummyInstance = new LocalVariable(
			parent, name, 0, 0, 0, 0, "I", new org.eclipse.jdt.internal.compiler.ast.Annotation[0], 0, false //$NON-NLS-1$
		);

		// Return values: [0] nameStart, [1] nameEnd, [2] startLine
		final int[] locations = new int[3];

		// Try the easy way first
		IProgramElement ipe =
			AJProjectModelFactory.getInstance().getModelForJavaElement(dummyInstance).javaElementToProgramElement(dummyInstance);
		ISourceLocation sloc = ipe.getSourceLocation();
		if (sloc != null) {
			locations[2] = sloc.getLine();
			locations[0] = sloc.getOffset();
			if (sloc instanceof EclipseSourceLocation) {
				EclipseSourceLocation esloc = (EclipseSourceLocation) sloc;
				locations[1] = esloc.getEndPos();
			}
		}

		// Sometimes, the start and end values are not set. So, do it the hard way and calculate them from the line.
		if (locations[0] <= 0 || locations[1] <= 0) {
			try {
				IOpenable openable = parent.getOpenableParent();
				IBuffer buffer;
				if (openable instanceof AJCompilationUnit) {
					AJCompilationUnit ajCompUnit = (AJCompilationUnit) openable;
					ajCompUnit.requestOriginalContentMode();
					buffer = openable.getBuffer();
					ajCompUnit.discardOriginalContentMode();
				}
				else {
					buffer = openable.getBuffer();
				}
				String source = buffer.getContents();

				int lines = 0;
				for (int i = 0; i < source.length(); i++) {
					if (source.charAt(i) == '\n') {
						lines++;
						if (lines == locations[2] - 1) {
							// starting remove white space
							i++;
							while (i < source.length() && Character.isWhitespace(source.charAt(i)) && source.charAt(i) != '\n') {
								i++;
							}
							locations[0] = i;
							break;
						}
					}
				}

				for (int i = locations[0] + 1; i < source.length(); i++) {
					if (source.charAt(i) == '\n' || source.charAt(i) == ';') {
						locations[1] = i - 1;
						break;
					}
				}

				locations[0] = Math.min(locations[0], locations[1]);
			}
			catch (JavaModelException ignored) {}
		}

		return locations;
	}

	/**
	 * @return Returns the line in the file of this AJCodeElement.
	 */
	public int getLine() {
	    return startLine;
	}

	/**
	 * @return Returns the name for this AJCodeElement
	 */
	public String getName() {
		return name;
	}

	protected char getHandleMementoDelimiter() {
		return AspectElement.JEM_CODEELEMENT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.JavaElement#getChildren()
	 */
	// Workaround for bug 94401 - JavaElement expects the parent
	// to be an openable, but it is not for an AJCodeElement
	public IJavaElement[] getChildren() {
		return JavaElement.NO_ELEMENTS;
	}

}
