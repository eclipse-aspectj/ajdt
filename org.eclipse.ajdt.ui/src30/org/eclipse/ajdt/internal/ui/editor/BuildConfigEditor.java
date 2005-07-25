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
package org.eclipse.ajdt.internal.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.BuildConfigModel;
import org.aspectj.ajde.ui.BuildConfigNode;
import org.aspectj.bridge.IMessage;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.internal.ui.ajde.CompilerTaskListManager;
import org.eclipse.ajdt.internal.ui.resources.AJDTIcon;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.EditorPart;


public class BuildConfigEditor
	extends EditorPart
	implements SelectionListener {

	/** is a save required? */
	boolean isDirty = false;

	/**  The tree representation on the AJDT side */
	private Tree tree = null; 

	/** The model tree returned from AJDE */
	BuildConfigModel model = null;

	/** The file we are "editing" */
	IFileEditorInput fileInput;

	/** Map from IProgramElement to TreeItem used by gotoMarker */
	private Hashtable nodeMap;

	ImageDescriptor compilationUnitImgDesc;
	ImageDescriptor directoryImgDesc;
	
	/**
	 * Double clicking on an lst file opens it, double clicking on anything
	 * else selects/checks it.
	 * @see SelectionListener#widgetDefaultSelected(SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		TreeItem ti = (TreeItem) e.item;
		BuildConfigNode node = (BuildConfigNode) ti.getData( );
		if ( node != null && 
			 node.getBuildConfigNodeKind() == BuildConfigNode.Kind.FILE_LST ) {
			 String relativePath = node.getResourcePath();
			 IFile file = fileInput.getFile().getProject().getFile( relativePath );	
			 if ( file != null ) {
				 try {		 
				 	IDE.openEditor(
					 AspectJUIPlugin.getDefault().
					 	getWorkbench().
					 	getActiveWorkbenchWindow().
					 	getActivePage(),file,true );
				 } catch ( Exception ex ) { 
				 }
			 }
		} else {
			widgetSelected( e );
		}
	}

	/**
	 * @see SelectionListener#widgetSelected(SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		if ((e.detail & SWT.CHECK) > 0) {
			TreeItem ti = (TreeItem) e.item;
			boolean checked = ti.getChecked();
			setTreeNodeStatus(ti, checked);
			ensureTreeCheckboxConsistency(tree);
		}				
	}
	
	
	/**
	 * @see IEditorPart#doSave(IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		Ajde.getDefault().getConfigurationManager().writeModel( model );
		clearDirty( );
		try {
			// tell eclipse the file has been updated by Ajde	
			// bug 23954
			fileInput.getFile().refreshLocal( IResource.DEPTH_ZERO, monitor);
		} catch ( CoreException cEx ) {
			// resource update failed...
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(
				"Failed to update lst file resource", cEx );
		}

		// after a save we must clear markers and then reload the model to
		// get an updated set of errors and warnings
		clearMarkers( fileInput );
		buildModel( fileInput );
		CompilerTaskListManager.showOutstandingProblems( );
				
		tree.removeAll();
		populateTree();	
	}

	/**
	 * @see IEditorPart#doSaveAs()
	 */
	public void doSaveAs() {
		// save as not supported
		doSave( null );
	}

	/**
	 * @see IEditorPart#gotoMarker(IMarker)
	 */
	public void gotoMarker(IMarker marker) {
		try {
			Integer lineNumber = (Integer) marker.getAttribute( IMarker.LINE_NUMBER );
			String sourceFile = AJDTUtils.getResourcePath( marker.getResource() );
			BuildConfigNode node = model.findNodeForSourceLine( sourceFile, lineNumber.intValue() );
			if ( node != null ) { 
				// now find the TreeItem for this node and select it
				TreeItem tItem = (TreeItem) nodeMap.get( node );
				if ( tItem != null ) {
					TreeItem[] selectionTarget = new TreeItem[] { tItem };
					tree.setSelection( selectionTarget );
				}
			}
		} catch ( CoreException cEx ) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError( "Unable to go to marker", cEx);
		}
	}

	/**
	 * @see IEditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
		throws PartInitException {

		setSite( site );
		setInput( input );
		setContentDescription(input.getName());

		if (!(input instanceof IFileEditorInput)) {
			throw new PartInitException("InvalidInput");
		}
		
		fileInput = (IFileEditorInput) input;
		isDirty = false;

		clearMarkers( fileInput );
		buildModel( fileInput );
		CompilerTaskListManager.showOutstandingProblems( );
		
		AJLog.log("Editor opened on " + fileInput.getFile().getName());
	}

	/**
	 * @see IEditorPart#isDirty()
	 */
	public boolean isDirty() {
		return isDirty;
	}

	/**
	 * @see IEditorPart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * @see IWorkbenchPart#createPartControl(Composite)
	 */
	public void createPartControl(Composite parent) {
		tree = new Tree( parent, SWT.CHECK | SWT.CASCADE );
		tree.addSelectionListener( this );
		populateTree( );
	}

	/**
	 * @see IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		tree.setFocus();
	}


	public void dispose( ) {
		AJLog.log("Editor closed - " + fileInput.getFile().getName());
		super.dispose();	
	}
	
	/** clears any markers that exist for the given file */
	private void clearMarkers( IFileEditorInput input ) {
		try {
			((IResource) input.getFile()).deleteMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER,
				false,
				IResource.DEPTH_INFINITE);
			((IResource) input.getFile()).deleteMarkers(
					IAJModelMarker.AJDT_PROBLEM_MARKER,
					true,
					IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			AspectJUIPlugin.getDefault().getErrorHandler().handleError( e.getMessage(), e);
		}
	}
	
	/** build the AJDE model for this config file */
	private void buildModel( IFileEditorInput input ) {
		String filePath = input.getFile( ).getLocation( ).toOSString();
		nodeMap = new Hashtable( );
		model =
			Ajde.getDefault().getConfigurationManager().buildModel( filePath );
	}


	/**
	 * Prior to calling this method, the model should have
	 * been populated. Build a tree that represents it.
	 */
	private void populateTree( ) {
		if ( model != null ) {
			populateBranch( tree, model.getRoot() );
			ensureTreeCheckboxConsistency( tree );
		}
	}


	/**
	 * Populate a branch of the tree...
	 */	
	private void populateBranch(Widget parent, BuildConfigNode node) {
		TreeItem tItem;

		for (Iterator it = node.getChildren().iterator(); it.hasNext();) {
			BuildConfigNode childNode = (BuildConfigNode) it.next();
			if (parent instanceof Tree) {
				tItem = new TreeItem(((Tree) parent), SWT.NULL);

			} else {
				tItem = new TreeItem(((TreeItem) parent), SWT.NULL);
			}
			

			tItem.setImage( createNodeImage( childNode ) );
			tItem.setText(childNode.getName());
			tItem.setData( childNode );
			tItem.setChecked(childNode.isActive());
			tItem.setExpanded(true);
			nodeMap.put( childNode, tItem );
			populateBranch(tItem, childNode);
		}
	}

	/**
	 * Ensure the correct nodes are open and check marks are in place
	 */
	private boolean ensureTreeCheckboxConsistency(Widget where) {
		TreeItem[] tis;
		boolean oneselected = false;

		if (where instanceof TreeItem) {
			TreeItem ti = (TreeItem) where;
			tis = ti.getItems();
			oneselected = false;
			boolean graychildren = false;

			int count = 0;
			for (int i = 0; i < tis.length; i++)
				if (ensureTreeCheckboxConsistency(tis[i])) {
					ti.setChecked(true);
					ti.setExpanded(true);
					oneselected = true;
					if (tis[i].getGrayed())
						graychildren = true;
					count++;
				}

			if (oneselected) {
				if (count != tis.length || graychildren)
					ti.setGrayed(true);
				else
					ti.setGrayed(false);
			}

			if (count == 0 && tis.length != 0) {
				ti.setChecked(false);
				ti.setGrayed(false);
				ti.setExpanded(false);
			}

			return oneselected || ti.getChecked();
		} 
		tis = ((Tree) where).getItems();
		for (int i = 0; i < tis.length; i++)
			ensureTreeCheckboxConsistency(tis[i]);
		return false;
	}

	/**
	 * Check or uncheck a given tree node, with cascade into child nodes.
	 * Build model is updated accordingly
	 */
	private void setTreeNodeStatus( TreeItem item, boolean include ) {
		BuildConfigNode node = (BuildConfigNode) item.getData();
		if ( include ) { includeNodeInConfig( node ); }
		else { excludeNodeFromConfig( node ); }		
		// recurse
		TreeItem[] children = item.getItems();
		for ( int i = 0; i < children.length; i++ ) {
			children[i].setChecked( include );
			setTreeNodeStatus( children[i], include );
		}	
	}


	/**
	 * Change event needed to force update of display to include 
	 * dirty marker in title
	 */
	private void setDirty( ) {
		isDirty = true;
		firePropertyChange(PROP_DIRTY);
	}
	
	/**
	 * Change event needed to force update of display to exclude 
	 * dirty marker in title
	 */
	private void clearDirty( ) {
		isDirty = false;
		firePropertyChange(PROP_DIRTY);
	}
	
	/**
	 * This node should be included in the config if it is not already
	 * present. Mark dirty if a change is required
	 */
	private void includeNodeInConfig( BuildConfigNode node ) {
		if ( !node.isActive() ) {
			node.setActive( true );
			setDirty( );
		}
	}
	
	/**
	 * This node should be excluded from the config if it is not already
	 * absent. Mark dirty if a change is required
	 */
	private void excludeNodeFromConfig( BuildConfigNode node ) {
		if ( node.isActive( ) ) {
			node.setActive( false );
			setDirty( );	
		}	
	}

	/**
	 * Create the image for a node, taking into account any
	 * possible error or warning overlays.
	 */		
	private Image createNodeImage( BuildConfigNode node ) {
		ImageDescriptor baseDescriptor;
		BuildConfigNode.Kind kind = node.getBuildConfigNodeKind();

		// now for overlays...
		int overlayFlags = 0;
		if ( kind == BuildConfigNode.Kind.ERROR ) {
			overlayFlags = JavaElementImageDescriptor.ERROR;
		}
		List linkedsrcfolders = getLinkedSourceFolders();
		IMessage sMessage = node.getMessage();
		if ( sMessage != null ) {
			if ( sMessage.getKind() == IMessage.ERROR ) {
				overlayFlags = JavaElementImageDescriptor.ERROR;
			} else if ( sMessage.getKind() == IMessage.WARNING ) {
				overlayFlags = JavaElementImageDescriptor.WARNING;	
			}
		}
		String nodename=node.getName();
		if (overlayFlags!=0 && nodename.startsWith("Use relative paths only, omitting: ")) {
			// Check if its inside a linked source folder...
			String realLocation = node.getName().substring("Use relative paths only, omitting: ".length());

			realLocation = realLocation.replace('/',File.separatorChar);
			realLocation = realLocation.replace('\\',File.separatorChar);
			for (Iterator fldr = linkedsrcfolders.iterator(); fldr.hasNext();) {
				String element = (String) fldr.next();
				element = element.replace('/',File.separatorChar);
				element = element.replace('\\',File.separatorChar);
	
				if (realLocation.startsWith(element)) {
					node.setName("Resource from linked source folder: "+realLocation);
					overlayFlags = 0;
				}
			}
		}
		
		// Key into the image 'cache' is node.getBuildConfigNodeKind + overlayFlags (int)
		// For example "Java source file:::0" or "build configuration file:::0"
		String key = new String(kind.toString() + ":::" + overlayFlags);
		
		// Initialize the map if it hasn't already been setup
		if (reusableImageMap == null)
			reusableImageMap = new Hashtable();
			
	
		if (reusableImageMap.get(key) != null) {
			return (Image) reusableImageMap.get(key);
		} 
		AJDTIcon icon =
			(AJDTIcon) AspectJImages.registry().getIcon(kind);
		baseDescriptor = icon.getImageDescriptor();
		Image newImage =
			AJDTUtils.decorate(baseDescriptor, overlayFlags).createImage();
		reusableImageMap.put(key, newImage);
		return newImage;
				
	}
	
	// Map of nodekind+overflags -> image
	private static Hashtable reusableImageMap;
	
	
	public List getLinkedSourceFolders() {
		List linkedSourceFolders = new ArrayList();
		IProject proj = fileInput.getFile().getProject();
		IJavaProject jProject = JavaCore.create(proj);
		try {
			IClasspathEntry[] classpathEntries = jProject.getResolvedClasspath(false);
			for (int i = 0; i < classpathEntries.length; i++) {
				if (classpathEntries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IClasspathEntry sourceEntry = classpathEntries[i];
					IPath sourcePath = sourceEntry.getPath();
					// remove the first segment because the findMember call following
					// always adds it back in under the covers (doh!) and we end up
					// with two first segments otherwise!
					sourcePath = sourcePath.removeFirstSegments(1);
					IResource[] srcContainer = new IResource[] { proj.findMember(sourcePath)};
					linkedSourceFolders.add(srcContainer[0].getLocation().toOSString());
				}
			}
		} catch (JavaModelException jmEx) {
		}

		return linkedSourceFolders;
	}
	
}
