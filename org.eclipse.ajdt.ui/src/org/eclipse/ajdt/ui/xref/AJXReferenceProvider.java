/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.ui.xref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.ajdt.core.model.AJComparator;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.internal.builder.AJNode;
import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.Member;

/**
 * @author hawkinsh
 *  
 */
public class AJXReferenceProvider implements IXReferenceProvider {

	private static final Class[] myClasses = new Class[] { IJavaElement.class };

	// array of relationships to show
	private AJRelationship[] showRels = new AJRelationship[] {
			AJRelationshipManager.ADVISES,
			AJRelationshipManager.ADVISED_BY,
			AJRelationshipManager.ASPECT_DECLARATIONS,
			AJRelationshipManager.DECLARED_ON,
			AJRelationshipManager.MATCHED_BY,
			AJRelationshipManager.MATCHES_DECLARE,
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getClasses()
	 */
	public Class[] getClasses() {
		return myClasses;
	}

	public IJavaElement[] getExtraChildren(IJavaElement je) {
		return AJModel.getInstance().getExtraChildren(je);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getXReferences(java.lang.Object)
	 */
	public Collection getXReferences(Object o) {
		if (!(o instanceof IJavaElement))
			return Collections.EMPTY_SET;

		List xrefs = new ArrayList();
		IJavaElement je = (IJavaElement) o;
		
		AJModel model = AJModel.getInstance();
		//System.out.println("je=" + je + " (" + je.hashCode() + ")");
		for (int i = 0; i < showRels.length; i++) {
			//System.out.println("relationship: " + showRels[i].getName());
			List associates = new ArrayList();
			List related = model.getRelatedElements(showRels[i], je);
			if (related != null) {
				for (Iterator iter = related.iterator(); iter.hasNext();) {
					IJavaElement javaElement = (IJavaElement) iter.next();
					//System.out.println("related: " + javaElement + " ("
					//		+ javaElement.hashCode() + ")");
					AJNode associate = new AJNode(javaElement, model
							.getJavaElementLinkName(javaElement));
					associates.add(associate);
				}
				Collections.sort(associates,new AJComparator());
				XRef xref = new XRef(showRels[i].getName(), associates);
				xrefs.add(xref);
			}
		}
		return xrefs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getProviderDescription()
	 */
	public String getProviderDescription() {
		return "Provides AspectJ cross-cutting structure references";
	}

	private static class XRef implements IXReference {

		private String name;

		private List associates;

		public XRef(String name, List associates) {
			this.name = name;
			this.associates = associates;
		}

		public String getName() {
			return name;
		}

		public Iterator getAssociates() {
			return associates.iterator();
		}
	}

	/**
	 * Get the line number for the given offset in the given
	 * AspectJMemberElement
	 */
	private int getLineNumFromOffset(AspectJMemberElement ajelement, int offSet) {
		try {
			IJavaElement je = ajelement.getParent().getParent();
			ICompilationUnit cu = null;
			if (je instanceof ICompilationUnit) {
				cu = (ICompilationUnit) je;
			}
			if (cu != null) {
				return getLineFromOffset(cu.getSource(), ajelement
						.getDeclaringType(), offSet);
			}
		} catch (JavaModelException jme) {
		}
		return 0;
	}

	/**
	 * Get the line number for the given offset in the given Member
	 */
	private int getLineNumFromOffset(Member m, int offSet) {
		try {
			IJavaElement je = m.getParent();
			ICompilationUnit cu = null;
			if (je instanceof ICompilationUnit) {
				cu = (ICompilationUnit) je;
			} else {
				IJavaElement j = je.getParent();
				if (j instanceof ICompilationUnit) {
					cu = (ICompilationUnit) j;
				}
			}
			if (cu != null) {
				return getLineFromOffset(cu.getSource(), m.getDeclaringType(),
						offSet);
			}
		} catch (JavaModelException jme) {
		}
		return 0;
	}

	/**
	 * Get the line number for the given offset in the given Source and type
	 */
	private int getLineFromOffset(String source, IType type, int offSet) {
		if (type != null) {
			String sourcetodeclaration = source.substring(0, offSet);
			int lines = 0;
			char[] chars = new char[sourcetodeclaration.length()];
			sourcetodeclaration.getChars(0, sourcetodeclaration.length(),
					chars, 0);
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] == '\n') {
					lines++;
				}
			}
			return lines + 1;
		}
		return 0;
	}

}