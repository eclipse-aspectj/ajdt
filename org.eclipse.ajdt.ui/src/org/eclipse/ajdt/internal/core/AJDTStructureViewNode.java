/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.ui.AbstractIcon;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.internal.core.resources.AJDTIcon;
import org.eclipse.ajdt.internal.core.resources.AspectJImages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class AJDTStructureViewNode 
implements IStructureViewNode, IAdaptable/*, IRelationshipAdapter*/{

	private static final String JDT_IMPORTS_LABEL = "import declarations";
	private static final String FWD_INTRO_REL_NAME = "declares member on";

	private IStructureViewNode.Kind kind = IStructureViewNode.Kind.DECLARATION;
	
	/**
	 * Used when the kind is IStructureViewNode.Kind.RELATIONSHIP
	 */
	private String relationshipName = null; 
	private List children = new ArrayList();
	private AJDTIcon icon;
	private IProgramElement node;
	private IRelationship relationship = null;
	private AJDTStructureViewNode parent = null;
	private IMarker marker = null;

	/**
	 * Constructor for AJDTSructureViewNode.  
	 */
	private AJDTStructureViewNode() {
		super();
	}
//
//	/**
//	 * Constructor used only to add dummy root node at top of tree
//	 */
//	public AJDTStructureViewNode( AJDTStructureViewNode child ) {
//		this.node = child.node;
//		this.icon = AJDTIcon.MISSING_ICON;
//		this.children = new ArrayList( );
//		children.add( child );
//		child.setParent( this );
//		this.label = "<root>";	
//	}

	/**
	 * Create a relationship node.
	 */	
	public AJDTStructureViewNode(IRelationship relationship, AbstractIcon icon) {
		this.icon = (AJDTIcon) icon;
		this.kind = Kind.RELATIONSHIP;
		this.relationshipName = 
			relationship.getName()+(relationship.hasRuntimeTest()?" (with runtime test)":"");
		this.relationship = relationship;
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
//				if (StructureViewNodeFactory.acceptNode(node, child.getStructureNode())) {
					child.setParent(this);
					if (isImportContainer) {
						child.icon = AspectJImages.JDT_IMPORTED;
					}	
//				}   
			}				
		}
		
//		for (Iterator it = node.getChildren().iterator(); it.hasNext();){
//			IProgramElement next = (IProgramElement)it.next();
//		}
		
//		System.out.println( "Created node " + node.getName());
//		if (node.getChildren() != null) {
//		System.out.println( "Children: " );
//			for (Iterator it = node.getChildren().iterator(); it.hasNext();){
//				Object next = it.next();
//				if ( next instanceof IProgramElement ) {
//					System.out.println( "    SN " + ((IProgramElement)next).getName() + " " + next.getClass());
//					if (next instanceof ProgramElementNode) {
//						ProgramElementNode pe = (ProgramElementNode) next;
//						System.out.println( "       Kind: " + pe.getProgramElementKind());
//						System.out.println( "       Member Kind?: " + pe.isMemberKind());					
//					}				
//				} else if ( node instanceof LinkNode ) {
//					System.out.println( "    LN " + ((LinkNode)next).getName());				
//				} else if ( node instanceof RelationNode ) {
//					System.out.println( "    RN " + ((RelationNode)next).getName());				
//				} else {
//					System.out.println( "    " + next.getClass());	
//				}
//			}
//		} 
//		System.out.println( "End Node.");
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
			ret = node.toLinkLabelString();	
		} else {
			ret = node.toLabelString();
//			if( getParent().kind == IStructureViewNode.Kind.RELATIONSHIP) {
//				if ( getParent().relationship.getKind().equals(IRelationship.Kind.ADVICE)) {
//				if ( !getParent().relationshipName.startsWith("declare") ) {
//					ret = node.getParent().toLabelString() + "." + ret;
//				}			
//			}
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
		} else /*if (adapter == IRelationshipAdapter.class){
			return this;
		} else */{
			return null;
		}
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
				IResource ir =
					AspectJUIPlugin.getDefault().getAjdtProjectProperties().findResource( sLoc.getSourceFile().getAbsolutePath(), currentProject);
				if (ir != null) {
					try {
						marker = ir.createMarker(IMarker.MARKER);
						marker.setAttribute(IMarker.LINE_NUMBER, sLoc.getLine());
						marker.setAttribute(IMarker.CHAR_START, sLoc.getColumn());
					} catch (CoreException coreEx) {
						System.err.println( coreEx );
						AspectJUIPlugin.getDefault().getLog().log(coreEx.getStatus());
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
			if ( node instanceof IProgramElement ) {
				IProgramElement pNode = (IProgramElement) node;
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
			}	
			if ( getLabel( ).equals( JDT_IMPORTS_LABEL )) {
				category = Category.IMPORTS;
			}
		}		
		return category;
	}


	public boolean isPublic( ) {
		boolean isPublic = true;
		if ( node instanceof IProgramElement ) {
			IProgramElement pNode = (IProgramElement) node;
			IProgramElement.Accessibility acc = pNode.getAccessibility();
			if ( acc != null ) {
				if ( !acc.equals( IProgramElement.Accessibility.PUBLIC ) ) {
					isPublic = false;
				 }
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

	// Relationships for Cross-Reference
	// ===============================================================

	/* (non-Javadoc)
	 * @see org.eclipse.aosd.relations.IRelationshipAdapter#getRelationshipsSource()
	 */
	public Object getRelationshipsSource() {
		return this;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.aosd.relations.IRelationshipAdapter#getRelationship(java.lang.String)
	 */
	//AJDT RELS
//	public org.eclipse.aosd.relations.IRelationship getRelationship(String name) {
//		return null;
//	}
//	/* (non-Javadoc)
//	 * @see org.eclipse.aosd.relations.IRelationshipAdapter#hasRelationships()
//	 */
//	public boolean hasRelationships() {
//		return false;
//	}
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

	static class AJDTPointcutMatcher /*
	implements IDeferredRelationship */{
		private AJDTStructureViewNode anchor = null;
		public AJDTPointcutMatcher(AJDTStructureViewNode anchor) {
			this.anchor = anchor;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.aosd.relations.IRelationship#getName()
		 */
		public String getName() {
			return "matches";
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
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			// TODO Get pointcut matches
			// Create a new BCELWorld.
			// Add all the classes from this projects classpath and output 
			// directory using world.addClass()
			org.aspectj.weaver.patterns.Pointcut p = 
				org.aspectj.weaver.patterns.Pointcut.fromString("execution(* *(..))");
			p.toString();
		}
	}

}
