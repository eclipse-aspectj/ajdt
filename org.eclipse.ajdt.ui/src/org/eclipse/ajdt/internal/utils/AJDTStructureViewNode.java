/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.ui.AbstractIcon;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class AJDTStructureViewNode 
implements IStructureViewNode, IAdaptable {

	private static final String JDT_IMPORTS_LABEL = UIMessages.AJDTStructureViewNode_import_declarations;

	private IStructureViewNode.Kind kind = IStructureViewNode.Kind.DECLARATION;
	
	/**
	 * Used when the kind is IStructureViewNode.Kind.RELATIONSHIP
	 */
	private String relationshipName = null; 
	private List children = new ArrayList();
	private AJDTIcon icon;
	private IProgramElement node;
	private AJDTStructureViewNode parent = null;
	private IMarker marker = null;

	/**
	 * Constructor for AJDTSructureViewNode.  
	 */
	private AJDTStructureViewNode() {
		super();
	}

	/**
	 * Create a relationship node.
	 */	
	public AJDTStructureViewNode(IRelationship relationship, AbstractIcon icon) {
		this.icon = (AJDTIcon) icon;
		this.kind = Kind.RELATIONSHIP;
		this.relationshipName = 
			relationship.getName()
				+ (relationship.hasRuntimeTest() ? " " //$NON-NLS-1$
						+ UIMessages.AJDTStructureViewNode_runtime_test
						: "");		 //$NON-NLS-1$
	}


	/**
	 * Create a link.
	 */	
	public AJDTStructureViewNode(IProgramElement node, AbstractIcon icon) {
		this.icon = (AJDTIcon) icon;
		this.kind = Kind.LINK;
		this.node = node;		
	}

	/**
	 * Create a declaration node.
	 */	
	public AJDTStructureViewNode(IProgramElement node, AbstractIcon icon, List children) {
		this.children = children;  
		this.icon = (AJDTIcon) icon;
		boolean isImportContainer = node.getName().equals(JDT_IMPORTS_LABEL); 
		if (isImportContainer) {
			this.icon = AspectJImages.JDT_IMPORT_CONTAINER;
		}
		this.kind = Kind.DECLARATION;
		this.node = node;
		
		// tell all the children who their parent is			
		if (children != null) {
			for (Iterator it = children.iterator(); it.hasNext(); ) {
				AJDTStructureViewNode child = (AJDTStructureViewNode)it.next();
				child.setParent(this);
				if (isImportContainer) {
					child.icon = AspectJImages.JDT_IMPORTED;
				}	
			}				
		}
	}

	/**
	 * @see StructureViewNode#getIcon()
	 */
	public AbstractIcon getIcon() {
		return icon;
	}

	/**
	 * @see StructureViewNode#getChildren()
	 */
	public List getChildren() {
		return children;
	}

	/**
	 * Return the parent of this node in the tree
	 */
	public AJDTStructureViewNode getParent( ) {
		return parent;
	}

	/**
	 * Get the label to use to describe this node
	 */
	public String getLabel( ) {
		String ret = null;
		if (kind == IStructureViewNode.Kind.RELATIONSHIP) {
			ret =  relationshipName;
		} else if (kind == IStructureViewNode.Kind.LINK) {
			ret = node.toLinkLabelString(false);	
		} else {
			ret = node.toLabelString();
		}
		return ret;
	}

	/**
	 * Set the parent of this node - called from parent's
	 * constructor
	 */
	private void setParent( AJDTStructureViewNode parent ) {
		this.parent = parent;
	}
	
	/**
	 * @see IAdaptable#getAdapter(Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return AJDTStructureViewNodeAdapter.getDefault( );
		}  
		return null;
	}


	/**
	 * Return an IMarker for the resource represented by this node
	 */
	public IMarker getMarker(IProject currentProject) {
		if ( marker != null ) return marker;	
		IProgramElement targetNode = node;
		
		if ( targetNode != null ) {		
			ISourceLocation sLoc = targetNode.getSourceLocation();
			if (sLoc != null && sLoc.getSourceFile().getAbsolutePath() != null) {
				IResource ir = AJDTUtils.findResource( sLoc.getSourceFile().getAbsolutePath(), currentProject);
				if (ir != null) {
					try {
						marker = ir.createMarker(IMarker.MARKER);
						marker.setAttribute(IMarker.LINE_NUMBER, sLoc.getLine());
						marker.setAttribute(IMarker.CHAR_START, sLoc.getColumn());
					} catch (CoreException coreEx) {
					}
				}
			}		
		}
		
		return marker;	
	}

	/**
	 * @see org.aspectj.ajde.ui.StructureViewNode#add(StructureViewNode)
	 */
	public void add(IStructureViewNode child) {
		children.add( child );
		((AJDTStructureViewNode)child).setParent( this );
	}

	/**
	 * @see org.aspectj.ajde.ui.StructureViewNode#remove(StructureViewNode)
	 */
	public void remove(IStructureViewNode child) {
		children.remove( child );
	}


	// -------------------------------------------------------------------------
	// support for outline view filtering and sorting
	
	// the order of categories for alphabetic sorting
	public interface Category {
			public final int PACKAGE      = 0;
			public final int IMPORTS      = 1;
			public final int CLASS        = 2;
			public final int INTERFACE    = 3;
			public final int ASPECT       = 4;
			public final int INTRODUCTION = 5;
			public final int FIELD        = 6;
			public final int POINTCUT     = 7;
			public final int DECLARATION  = 8;
			public final int ADVICE       = 9;	
			public final int CONSTRUCTOR  = 10;
			public final int METHOD       = 11;	
			public final int OTHER        = 12;
	};	


	private boolean computedCategory = false;
	private int category = Category.OTHER;
	
	public int category( ) {
		if ( !computedCategory ) {
			computedCategory = true;
			IProgramElement pNode =  node;
			IProgramElement.Kind kind = pNode.getKind();
			if ( kind.equals( IProgramElement.Kind.PACKAGE ) ) {
				category = Category.PACKAGE;
			} else if ( kind.equals( IProgramElement.Kind.CLASS ) ) {
				category = Category.CLASS;
			} else if ( kind.equals( IProgramElement.Kind.INTERFACE ) ) {
				category = Category.INTERFACE;
			} else if ( kind.equals( IProgramElement.Kind.ASPECT ) ) {
				category = Category.ASPECT;
			} else if ( kind.equals( IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR ) ) {
				category = Category.INTRODUCTION;
			} else if ( kind.equals( IProgramElement.Kind.INTER_TYPE_FIELD ) ) {
				category = Category.INTRODUCTION;
			} else if ( kind.equals( IProgramElement.Kind.INTER_TYPE_METHOD ) ) {
				category = Category.INTRODUCTION;
			} else if ( kind.equals( IProgramElement.Kind.POINTCUT ) ) {
				category = Category.POINTCUT;
			} else if ( kind.equals( IProgramElement.Kind.DECLARE_ERROR ) ||
						 kind.equals( IProgramElement.Kind.DECLARE_WARNING ) ||
						 kind.equals( IProgramElement.Kind.DECLARE_PARENTS ) ||
						 kind.equals( IProgramElement.Kind.DECLARE_SOFT )
				 ) {
					category = Category.DECLARATION;
			} else if ( kind.equals( IProgramElement.Kind.ADVICE ) ) {
				category = Category.ADVICE;
			} else if ( kind.equals( IProgramElement.Kind.FIELD ) ) {
				category = Category.FIELD;
			} else if ( kind.equals( IProgramElement.Kind.CONSTRUCTOR ) ) {
				category = Category.CONSTRUCTOR;
			} else if ( kind.equals( IProgramElement.Kind.METHOD ) ) {
				category = Category.METHOD;
			}
				
			if ( getLabel( ).equals( JDT_IMPORTS_LABEL )) {
				category = Category.IMPORTS;
			}
		}		
		return category;
	}


	public boolean isPublic( ) {
		boolean isPublic = true;
		IProgramElement.Accessibility acc = node.getAccessibility();
		if ( acc != null ) {
			if ( !acc.equals( IProgramElement.Accessibility.PUBLIC ) ) {
				isPublic = false;
			 }
		}
	
		return isPublic;
	}
	
	public void add(IStructureViewNode child, int position) {
		children.add(position, child);
		((AJDTStructureViewNode)child).setParent( this );
	}

	public Kind getKind() {
		return kind;
	}

	public String getRelationshipName() {
		return relationshipName;
	}

	public IProgramElement getStructureNode() {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.aosd.relations.IRelationshipAdapter#getRelationshipsSource()
	 */
	public Object getRelationshipsSource() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.aosd.relations.IRelationshipAdapter#getRelationships()
	 */
	public Collection getRelationships() {
		List rels = new ArrayList();
		for (Iterator c = children.iterator(); c.hasNext(); ) {
			AJDTStructureViewNode child = (AJDTStructureViewNode) c.next();
			if (child.kind == Kind.RELATIONSHIP) {
				rels.add(new AJDTNodeRelationship(child));
			} 
		}
		if (node.getKind() == IProgramElement.Kind.POINTCUT) {
			rels.add(new AJDTPointcutMatcher(this));
		}
		return rels;
	}

	static class AJDTNodeRelationship /*
	implements org.eclipse.aosd.relations.IRelationship*/ {
		private AJDTStructureViewNode anchor = null;
		public AJDTNodeRelationship(AJDTStructureViewNode anchor) {
			this.anchor = anchor;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.aosd.relations.IRelationship#getName()
		 */
		public String getName() {
			return anchor.relationshipName;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.aosd.relations.IRelationship#getAssociates()
		 */
		public Iterator getAssociates() {
			return anchor.children.iterator(); 
		}
	}

	static class AJDTPointcutMatcher {
		private AJDTStructureViewNode anchor = null;
		public AJDTPointcutMatcher(AJDTStructureViewNode anchor) {
			this.anchor = anchor;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.aosd.relations.IRelationship#getName()
		 */
		public String getName() {
			return UIMessages.AJDTStructureViewNode_matches;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.aosd.relations.IRelationship#getAssociates()
		 */
		public Iterator getAssociates() {
			List l = new ArrayList();
			l.add(anchor);
			return l.iterator(); 
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public void run() {
			// TODO Get pointcut matches
			// Create a new BCELWorld.
			// Add all the classes from this projects classpath and output 
			// directory using world.addClass()
			org.aspectj.weaver.patterns.Pointcut p = 
				org.aspectj.weaver.patterns.Pointcut.fromString("execution(* *(..))"); //$NON-NLS-1$
			p.toString();
		}
	}

}
