/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.ajdt.core.javaelements.PointcutElement;
import org.eclipse.ajdt.pointcutdoctor.core.PointcutRelaxMungerFactory;
import org.eclipse.ajdt.pointcutdoctor.core.ShadowWrapper;
import org.eclipse.ajdt.pointcutdoctor.core.almost.AlmostJPSPluginFacade;
import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.contribution.xref.core.IXReferenceProvider;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;


public class AlmostMatchedJPSProvider implements IXReferenceProvider {
	
	private PointcutDoctorUIPlugin plugin;
	
	public AlmostMatchedJPSProvider() {
		this.plugin = PointcutDoctorUIPlugin.getDefault();
	}
	
	/**
	 * The set of classes/interfaces for which this provider will
	 * contribute references. The getXReferences method will be
	 * called on this provider any time that someone requests the
	 * references for an object that is an instance of one of the
	 * types returned from this call. 
	 * <p>A call to this method must always return the same class set
	 * (dynamic modification of the class set is not supported).</p>
	 */
	public Class<?>[] getClasses() { 
		if (isEnabled())
			return new Class<?>[]{IJavaElement.class};
		else
			return new Class<?>[0];
	}
	
	private boolean isEnabled() {
		return plugin.isEnabled();
	}

	/**
	 * Get the collection of IXReferences for the Object o. "o" is 
	 * guaranteed to be non-null and of a type returned by getClasses.
	 * This method will be called in "user time" and should have a 
	 * sub-second response time. To contribute cross references that cannot 
	 * guarantee to be computed in that timeframe, return an 
	 * <code>IDeferredXReference</code>. See the 
	 * <code>org.eclipse.contributions.xref.internal.providers.ProjectReferencesProvider</code> 
	 * for an example of a provider that uses this technique.
	 * @param o the object to get cross references for
	 * @return IXReference collection of cross references for "o". If there
	 * are no cross references to be contributed, either an empty collection or
	 * null is an acceptable return value.
	 */
	public Collection<IXReference> getXReferences(Object o, List<String> l) {
		if (o instanceof PointcutElement || o instanceof AdviceElement) {
			List<IXReference> xrefs = new ArrayList<IXReference>();
			XRef almostMatchRef = createAlmostMatchedJPSRef((AspectJMemberElement) o);
			if (almostMatchRef!=null) xrefs.add(almostMatchRef);
			if (o instanceof PointcutElement) {
				XRef matchRef = createMatchedJPSRef((AspectJMemberElement)o);
				if (matchRef!=null) xrefs.add(matchRef);
			}
			return xrefs;
		} else return null;
	}
	

	private XRef createMatchedJPSRef(AspectJMemberElement o) {
		AlmostJPSPluginFacade facade = plugin.getAlmostJPSPluginFacade();
		PointcutRelaxMungerFactory factory = facade
				.getPointcutRelaxMungerFactory(o.getParent().getJavaProject()
						.getProject());
		List<ShadowWrapper> shadows = facade.getMatchedJPSs(o, factory);
		if (shadows!=null && shadows.size() > 0) {
			String title = "matches";// "matched join points";
			if (o instanceof AdviceElement)
				title = "advises";
			XRef xref = new XRef(title, createAssociateForShadows(shadows, o));
			return xref;
		} else
			return null;
	}

	private XRef createAlmostMatchedJPSRef(AspectJMemberElement o) {
		AlmostJPSPluginFacade facade = plugin.getAlmostJPSPluginFacade();
		PointcutRelaxMungerFactory factory = facade
				.getPointcutRelaxMungerFactory(o.getParent().getJavaProject()
						.getProject());
		List<ShadowWrapper> shadows = facade.getAlmostMatchedJPSs(o, factory);
		if (shadows!=null && shadows.size() > 0) {
			String title = "almost matches"; // "almost matched join points";
			if (o instanceof AdviceElement)
				title = "almost advises";
			XRef xref = new XRef(title, createAssociateForShadows(shadows, o));
			return xref;
		} else
			return null;
	}

	private List<IAdaptable> createAssociateForShadows(List<ShadowWrapper> shadows, AspectJMemberElement o) {
		AlmostJPSPluginFacade facade = plugin.getAlmostJPSPluginFacade();
		List<IAdaptable> associates = new ArrayList<IAdaptable>();
//		Pointcut oldPointcut = facade.getOldPointcut(o);
		if (shadows!=null) {
			for (ShadowWrapper shw:shadows) {
				IJavaElement je = facade.findJavaElementForShadow(shw.getShadow(), o.getJavaProject());
				Pointcut ptc = facade.getMainPointcutForJavaElement(o);
				ShadowNode node = new ShadowNode(je, ptc, shw, o.getJavaProject().getProject());
				associates.add(node);
			}
		}
		return associates;
	}

//	private AJNode createAJNode(IProgramElement pe, ICompilationUnit unit) {
//		ISourceLocation sl = pe.getSourceLocation();
//
//		int offset = sl.getOffset();
//		// in some versions of ajde, code elements have an offset of
//		// zero - in cases like this, we go with the offset of the
//		// parent instead
//		if (offset == 0) {
//			offset = pe.getParent().getSourceLocation().getOffset();
//		}
//		IJavaElement el;
//		try {
//			el = unit.getElementAt(offset);
//			if (pe.getKind()==IProgramElement.Kind.CODE) {
//				IJavaElement parent = el;
//				el = new AJCodeElement((JavaElement) parent, sl
//						.getLine(), pe.toLabelString(false));
//			}
//		} catch (JavaModelException e) {
//			// TODO what to do with this?
//			e.printStackTrace();
//			return null;
//		}
//		return new AJNode(el, getJavaElementLinkName(el));
//	}
//	
//	private String getJavaElementLinkName(IJavaElement je) {
////		String name = (String) jeLinkNames.get(je);
////		if ((name != null) && (name.length() > 0)) {
////			return name;
////		}
//		// use element name instead, qualified with parent
//		if (je.getParent() != null) {
//			return je.getParent().getElementName() + '.' + je.getElementName();
//		}
//		return je.getElementName();
//	}


	public IJavaElement[] getExtraChildren(IJavaElement je) { 
		return null;
	}

	/**
	 * Returns a description of the provider suitable for display 
	 * in a user interface.
	 */
	public String getProviderDescription() { 
		return "Almost matched JPS Provider";
	}
	
	/**
	 * Enables the provider to handle the list of items to be filtered from the
	 * Cross References View
	 * 
	 * @param List of Strings corresponding to the items checked by the user
	 * to indicate the items to exclude in the Cross References View
	 */
	public void setCheckedFilters(List<String> l) { 
	}

	/**
	 * Enables the provider to handle the list of items to be filtered from the
	 * Cross References Inplace View
	 * 
	 * @param List of Strings corresponding to the items checked by the user
	 * to indicate the items to exclude in the Cross References Inplace View
	 */
	public void setCheckedInplaceFilters(List<String> l) { }
	
	/*
	 * Returns a List of Strings corresponding to the items previously checked
	 * by the user to populate the Cross References View
	 */
	public List<String> getFilterCheckedList() { 
		return null;

	}
	
	/**
	 * Returns a List of Strings corresponding to the items previously checked
	 * by the user to populate the Cross References Inplace View
	 */
	public List<String> getFilterCheckedInplaceList() {
		return null;

	}
	
	/**
	 * Returns a List of Strings corresponding to the items used to populate the checkBox with
	 */
	public List<String> getFilterList() { 
		return null;

	}
	
	/**
	 * Returns a List of Strings corresponding to the items specified to
	 * be checked by default in the Cross References Views
	 */
	public List<String> getFilterDefaultList() { 
		return null;

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

}
