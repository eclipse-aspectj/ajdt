/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.utils;


import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class AJDTStructureViewNodeAdapter implements IWorkbenchAdapter {

	private static AJDTStructureViewNodeAdapter instance = null;		

	public static AJDTStructureViewNodeAdapter getDefault( ) {
		if ( instance == null ) { instance = new AJDTStructureViewNodeAdapter( ); }
		return instance;	
	}

	/**
	 * Constructor for AJDTStructureViewNodeAdapter.
	 */
	private AJDTStructureViewNodeAdapter() {
		super();
	}

//	/**
//	 * @see IWorkbenchAdapter#getChildren(Object)
//	 */
//	public Object[] getChildren(Object o) {
//		if ( o instanceof AJDTStructureViewNode ) {  
//			AJDTStructureViewNode node = (AJDTStructureViewNode) o;	
//			List acceptedChidren = new ArrayList();
//			for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
//				AJDTStructureViewNode child = (AJDTStructureViewNode)it.next();
//				if (StructureViewNodeFactory.acceptNode(node.getStructureNode(), child.getStructureNode())) {
//					acceptedChidren.add(child);
//				} 
//			}  
//			return acceptedChidren.toArray();
//		} else { 
//			return new Object[0];
//		}
//	}  

	/**
	 * @see IWorkbenchAdapter#getChildren(Object)
	 */
	public Object[] getChildren(Object o) {
		if ( o instanceof AJDTStructureViewNode ) {
			AJDTStructureViewNode node = (AJDTStructureViewNode) o;	
			return node.getChildren().toArray();
		} 
		return new Object[0];
	}

	/**
	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
	 */
	public ImageDescriptor getImageDescriptor(Object o) {
		ImageDescriptor retVal = ImageDescriptor.getMissingImageDescriptor();
		if ( o instanceof AJDTStructureViewNode ) {
			AJDTStructureViewNode node = (AJDTStructureViewNode) o;	
			ImageDescriptor baseDesc = ((AJDTIcon)node.getIcon()).getImageDescriptor();
			retVal = AJDTUtils.decorate( baseDesc, node.getStructureNode() );
		}
		return retVal;
	}

	/**
	 * @see IWorkbenchAdapter#getLabel(Object)
	 */
	public String getLabel(Object o) {
		String label = "<>";
		if ( o instanceof AJDTStructureViewNode ) {
			AJDTStructureViewNode node = (AJDTStructureViewNode) o;	
			label = node.getLabel();
		}
		return label;
	}

	/**
	 * @see IWorkbenchAdapter#getParent(Object)
	 */
	public Object getParent(Object o) {
		if ( o instanceof AJDTStructureViewNode ) {
			AJDTStructureViewNode node = (AJDTStructureViewNode) o;	
			return  node.getParent();
		}
		return null;
	}
		
}

