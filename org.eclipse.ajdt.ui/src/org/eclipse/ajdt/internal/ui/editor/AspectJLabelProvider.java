/* *******************************************************************
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * ******************************************************************/
 
package org.eclipse.ajdt.internal.ui.editor;

import org.aspectj.ajde.ui.IStructureViewNode;
import org.eclipse.ajdt.internal.core.AJDTStructureViewNode;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Colors labels using the following scheme:
 * <UL>
 *   <LI>declarations: black
 *   <LI>relationships: black
 *   <LI>links: blue (gray if no source available)
 * </UL>
 * @author Mik Kersten
 */
public class AspectJLabelProvider extends WorkbenchLabelProvider implements IColorProvider {
	
	private Composite parent;

	public AspectJLabelProvider(Composite parent) { 
		this.parent = parent;
	}
	
	
	public Color getBackground(Object element) { 
		return null;
	}

	public Color getForeground(Object element) {
		if (!(element instanceof AJDTStructureViewNode)) return null;
		
		AJDTStructureViewNode node = (AJDTStructureViewNode)element;
		if (node.getKind().equals(IStructureViewNode.Kind.LINK)) {
			  
			if (node.getStructureNode().getParent() == null) {  // not in containment hierarchy
				return new Color(parent.getDisplay(), 155, 155, 155);  
			} else {			
				return new Color(parent.getDisplay(), 0, 0, 255);
			}
		} else {
			return null;
		}
	} 
}
