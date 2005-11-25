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
 *     Ben Dalziel     - implementation of feature 95724 (Filter)
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
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.jdt.core.IJavaElement;

public class AJXReferenceProvider implements IXReferenceProvider {

	private static final Class[] myClasses = new Class[] { IJavaElement.class };

	private AJRelationshipType[] relationshipTypes = AJRelationshipManager.allRelationshipTypes;

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
		Collections.sort(l, new AJComparator());
		return (IJavaElement[]) (l.toArray(new IJavaElement[] {}));
	}

	private List /* AJRelationshipType */getAJRelationshipTypes(List relNames) {
		List visibleAJRelTypes = new ArrayList();
		for (int i = 0; i < relationshipTypes.length; i++) {
			String name = (String) relationshipTypes[i].getDisplayName();
			if (!relNames.contains(name)) {
				visibleAJRelTypes.add(relationshipTypes[i]);
			}
		}
		return visibleAJRelTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getXReferences(java.lang.Object)
	 */
	public Collection getXReferences(Object o, List checkedRelNames) {
		if (!(o instanceof IJavaElement))
			return Collections.EMPTY_SET;

		List visibleAJRelTypes = getAJRelationshipTypes(checkedRelNames);
		List xrefs = new ArrayList();
		IJavaElement je = (IJavaElement) o;

		AJModel model = AJModel.getInstance();
		for (Iterator it = visibleAJRelTypes.iterator(); it.hasNext();) {
			AJRelationshipType ajType = (AJRelationshipType) it.next();
			List associates = new ArrayList();
			List related = model.getRelatedElements(ajType, je);
			if (related != null) {
				for (Iterator iter = related.iterator(); iter.hasNext();) {
					IJavaElement javaElement = (IJavaElement) iter.next();
					AJNode associate = new AJNode(javaElement, model
							.getJavaElementLinkName(javaElement));
					associates.add(associate);
				}
				Collections.sort(associates, new AJComparator());
				XRef xref = new XRef(ajType.getDisplayName(), associates);
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
		return AspectJUIPlugin.getResourceString("AJXReferenceProvider.description"); //$NON-NLS-1$
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

	public void setCheckedFilters(List l) {
		AspectJPreferences.setCheckedFilters(l);
	}

	public List getFilterCheckedList() {
		List checked = AspectJPreferences.getFilterCheckedList();
		if (checked != null) {
			return checked;
		}
		// use defaults
		return getFilterDefaultList();
	}
	
	public void setCheckedInplaceFilters(List l) {
		AspectJPreferences.setCheckedInplaceFilters(l);
	}

	public List getFilterCheckedInplaceList() {
		List checked = AspectJPreferences.getFilterCheckedInplaceList();
		if (checked != null) {
			return checked;
		}
		// use defaults
		return getFilterDefaultList();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getFilterList()
	 */
	public List getFilterList() {
		List populatingList = new ArrayList();
		for (int i = 0; i < relationshipTypes.length; i++) {
			populatingList.add(relationshipTypes[i].getDisplayName());
		}
		return populatingList;
	}

	/*
	 * Returns the List of items to be filtered from the view by default.
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getFilterDefaultList()
	 */
	public List getFilterDefaultList() {
		List defaultFilterList = new ArrayList();
		
		// list of relationships to filter out by default
		defaultFilterList.add(AJRelationshipManager.USES_POINTCUT.getDisplayName());
		defaultFilterList.add(AJRelationshipManager.POINTCUT_USED_BY.getDisplayName());

		return defaultFilterList;
	}
}