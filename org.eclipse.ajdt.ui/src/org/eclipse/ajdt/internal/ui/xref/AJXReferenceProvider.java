/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.SortedSet;
import java.util.TreeSet;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.model.AJComparator;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.internal.builder.AJNode;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.contribution.xref.core.IXReferenceProviderExtension;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;

public class AJXReferenceProvider implements IXReferenceProvider, IXReferenceProviderExtension {

	private static final Class<?>[] myClasses = new Class[] { IJavaElement.class };

	private AJRelationshipType[] relationshipTypes = AJRelationshipManager.getAllRelationshipTypes();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.contribution.xref.core.IXReferenceProvider#getClasses()
	 */
	public Class<?>[] getClasses() {
		return myClasses;
	}

	public IJavaElement[] getExtraChildren(IJavaElement je) {
	    AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(je);
	    IProgramElement ipe = model.javaElementToProgramElement(je);
	    if (ipe != null) {
    	    List<IProgramElement> ipeChildren = ipe.getChildren();
    	    if (ipeChildren != null) {
    	        SortedSet<IJavaElement> jeChildren = new TreeSet<IJavaElement>(new AJComparator());
        	    for (IProgramElement ipeChild : ipeChildren) {
                    if (ipeChild.getKind() == IProgramElement.Kind.CODE) {
                        jeChildren.add(model.programElementToJavaElement(ipeChild));
                    }
                }
        	    
                return (IJavaElement[]) (jeChildren.toArray(new IJavaElement[0]));
    	    }
	    }
	    return null;
	}

	private List<AJRelationshipType> getAJRelationshipTypes(List<String> relNames) {
		List<AJRelationshipType> visibleAJRelTypes = new ArrayList<AJRelationshipType>();
		for (int i = 0; i < relationshipTypes.length; i++) {
			String name = relationshipTypes[i].getDisplayName();
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
	public Collection<IXReference> getXReferences(IAdaptable o, List<String> checkedRelNames) {
		if (!(o instanceof IJavaElement))
			return Collections.emptySet();

		List<AJRelationshipType> visibleAJRelTypes = getAJRelationshipTypes(checkedRelNames);
		List<IXReference> xrefs = new ArrayList<IXReference>();
		IJavaElement je = (IJavaElement) o;
        AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(je);

		for (AJRelationshipType ajType : visibleAJRelTypes) {
			List<IAdaptable> associates = new ArrayList<IAdaptable>();
			List<IJavaElement> related = model.getRelationshipsForElement(je, ajType);
			if (related != null && related.size() > 0) {
				for (IJavaElement javaElement : related) {
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
		return UIMessages.AJXReferenceProvider_description;
	}

	private static class XRef implements IXReference {

		private String name;

		private List<IAdaptable> associates;

		public XRef(String name, List<IAdaptable> associates) {
			this.name = name;
			this.associates = associates;
		}

		public String getName() {
			return name;
		}

		public Iterator<IAdaptable> getAssociates() {
			return associates.iterator();
		}
	}

	public void setCheckedFilters(List<String> l) {
		AspectJPreferences.setCheckedFilters(l);
	}

	public List<String> getFilterCheckedList() {
		List<String> checked = AspectJPreferences.getFilterCheckedList();
		if (checked != null) {
			return checked;
		}
		// use defaults
		return getFilterDefaultList();
	}
	
	public void setCheckedInplaceFilters(List<String> l) {
		AspectJPreferences.setCheckedInplaceFilters(l);
	}

	public List<String> getFilterCheckedInplaceList() {
		List<String> checked = AspectJPreferences.getFilterCheckedInplaceList();
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
	public List<String> getFilterList() {
		List<String> populatingList = new ArrayList<String>();
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
	public List<String> getFilterDefaultList() {
		List<String> defaultFilterList = new ArrayList<String>();
		
		// list of relationships to filter out by default
		defaultFilterList.add(AJRelationshipManager.USES_POINTCUT.getDisplayName());
		defaultFilterList.add(AJRelationshipManager.POINTCUT_USED_BY.getDisplayName());

		return defaultFilterList;
	}

    public Collection<IXReference> getXReferences(Object o, List<String> l) {
        Assert.isLegal(o instanceof IAdaptable, "Object should be of type IAdaptable: " + o);
        return getXReferences((IAdaptable) o, l);
    }
}