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
package org.eclipse.ajdt.internal.ui.xref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.model.AJComparator;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.internal.builder.AJNode;
import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.jdt.core.IJavaElement;

/**
 * @author hawkinsh
 *  
 */
public class AJXReferenceProvider implements IXReferenceProvider {

	private static final Class[] myClasses = new Class[] { IJavaElement.class };

	// array of relationships to show
	private AJRelationshipType[] showRels = new AJRelationshipType[] {
			AJRelationshipManager.ADVISES,
			AJRelationshipManager.ADVISED_BY,
			AJRelationshipManager.ASPECT_DECLARATIONS,
			AJRelationshipManager.DECLARED_ON,
			AJRelationshipManager.MATCHED_BY,
			AJRelationshipManager.MATCHES_DECLARE,
			AJRelationshipManager.ANNOTATES,
			AJRelationshipManager.ANNOTATED_BY
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
	    List l = AJModel.getInstance().getExtraChildren(je);
	    if (l == null) {
			return null;
		}
	    // ensuring that the children are sorted 
	    Collections.sort(l,new AJComparator());
	    return (IJavaElement[]) (l.toArray(new IJavaElement[] {}));
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
		for (int i = 0; i < showRels.length; i++) {
			List associates = new ArrayList();
			List related = model.getRelatedElements(showRels[i], je);
			if (related != null) {
				for (Iterator iter = related.iterator(); iter.hasNext();) {
					IJavaElement javaElement = (IJavaElement) iter.next();
					AJNode associate = new AJNode(javaElement, model
							.getJavaElementLinkName(javaElement));
					associates.add(associate);
				}
				Collections.sort(associates,new AJComparator());
				XRef xref = new XRef(showRels[i].getDisplayName(), associates);
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


}