/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer - initial version
Sian Whiting - added support for different Advice icons
...
**********************************************************************/
package org.eclipse.ajdt.internal.core;

import java.util.List;

import org.aspectj.ajde.ui.AbstractIcon;
import org.aspectj.ajde.ui.AbstractIconRegistry;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.ajde.ui.StructureViewNodeFactory;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;

public class AJDTStructureViewNodeFactory extends StructureViewNodeFactory {
	
	// store the latest relationship so that its children can be created with the correct icon
	// if they are advice with a runtime test.
	IRelationship latestRelationship;

	private AJDTStructureViewNodeFactory( ) { super( null ); }

	/**
	 * Constructor for AJDTStructureViewNodeFatory.
	 * @param arg0
	 */
	public AJDTStructureViewNodeFactory(AbstractIconRegistry iconRegistry) {
		super(iconRegistry);
	}

	/**
	 * This method is called from AJDE to build the nodes of the structure
	 * tre, starting from the leaves up.
	 */
	protected IStructureViewNode createDeclaration(
		IProgramElement node,
		AbstractIcon icon,
		List children) {
			
		// look out for special case "null" icon and replace it with
		// Eclipse's MissingImageDescriptor
		if (icon == null || icon.getIconResource() == null) {
			icon = AJDTIcon.MISSING_ICON;	
		}
		
		icon = changeIconIfAdviceNode(node, icon, false);
					
		return new AJDTStructureViewNode(node, icon, children);
	}

 
	protected IStructureViewNode createLink(
		IProgramElement node,
		AbstractIcon icon) {
		if(latestRelationship != null) {
			icon = changeIconIfAdviceNode(node, icon, latestRelationship.hasRuntimeTest());
		} else {
			icon = changeIconIfAdviceNode(node, icon, false);
		}
		return new AJDTStructureViewNode(node, icon);
	}

	protected IStructureViewNode createRelationship(
		IRelationship relationship,
		AbstractIcon icon) {
		latestRelationship = relationship;
		
		return new AJDTStructureViewNode(relationship, icon);
	}

	
	/**
	 * Helper method.  If the node is advice use the extra information to provide the 
	 * correct icon
	 * @param node - IProgramElement
	 * @param defaultIcon - icon to use if node is not advice
	 * @return correct advice icon if node is advice, otherwise defaultIcon is returned
	 */
	private AbstractIcon changeIconIfAdviceNode(IProgramElement node, AbstractIcon defaultIcon, boolean hasDynamicTests) {
		if(node.getKind() == IProgramElement.Kind.ADVICE) {
			if (node.getExtraInfo()!=null && node.getExtraInfo().getExtraAdviceInformation()!=null) {				
				if(node.getExtraInfo().getExtraAdviceInformation().equals("before")) {
					if(hasDynamicTests) {
						defaultIcon = AspectJImages.DYNAMIC_BEFORE_ADVICE;
					} else {
						defaultIcon = AspectJImages.BEFORE_ADVICE;
					}
				} else if (node.getExtraInfo().getExtraAdviceInformation().equals("around")) {
					if(hasDynamicTests) {
						defaultIcon = AspectJImages.DYNAMIC_AROUND_ADVICE;
					} else {
						defaultIcon = AspectJImages.AROUND_ADVICE;
					}
				} else {
					if(hasDynamicTests) {
						defaultIcon = AspectJImages.DYNAMIC_AFTER_ADVICE;
					} else {
						defaultIcon = AspectJImages.AFTER_ADVICE;	
					}
				}
			}
		}
		return defaultIcon;
	}
	
}