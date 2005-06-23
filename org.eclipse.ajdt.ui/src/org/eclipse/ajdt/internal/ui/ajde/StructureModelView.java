/**********************************************************************
Copyright (c) 2002-2005 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer - initial version
Sian January - removed advice marker update methods as part of the fix 
	for bug 70658
Matt Chapman - converted old custom outline view into debugging view
**********************************************************************/

package org.eclipse.ajdt.internal.ui.ajde;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.FileStructureView;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.ajde.ui.StructureView;
import org.aspectj.ajde.ui.StructureViewManager;
import org.aspectj.ajde.ui.StructureViewProperties;
import org.aspectj.ajde.ui.StructureViewRenderer;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.builder.AJBuilder;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.editor.AspectJLabelProvider;
import org.eclipse.ajdt.internal.utils.AJDTEventTrace;
import org.eclipse.ajdt.internal.utils.AJDTStructureViewNode;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.part.ViewPart;

public class StructureModelView extends ViewPart implements ISelectionListener,
	StructureViewRenderer {

	private TreeViewer viewer;
	
	private Action fLinkWithEditor;
	
	private boolean fDoLinkWithEditor = true;
	
	/**
	 * StructureViewManager that manages the views on our behalf
	 */
	private StructureViewManager  svManager;
	
	/**
	 * The view that we are displaying
	 */
	private StructureView view;

	/**
	 * Properties controlling the tree view display
	 */
	private StructureViewProperties viewProperties;

	protected IFile input;
	
	public void createPartControl(Composite parent) {
		this.svManager = Ajde.getDefault().getStructureViewManager();
		this.viewProperties = svManager.getDefaultViewProperties();
		viewProperties.setGranularity(StructureViewProperties.Granularity.DECLARED_ELEMENTS);

		viewer =
			new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		makeActions();
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
		hookDoubleClickAction();

		viewer.setContentProvider(new WorkbenchContentProvider());
		viewer.setLabelProvider(new AspectJLabelProvider(parent));
//		IAdaptable outline = getContentOutline(input,true);
//		viewer.setInput(outline);
		expandTreeView( );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		super.setSite(site);
		ISelectionService service= site.getWorkbenchWindow().getSelectionService();
		service.addPostSelectionListener(this);
	}
	
	private void makeActions() {
		fLinkWithEditor = new Action() {
			public void run() {
				fDoLinkWithEditor= fLinkWithEditor.isChecked();
			}
		};
		fLinkWithEditor.setChecked(fDoLinkWithEditor);
		fLinkWithEditor.setText("&Link with Editor"); //$NON-NLS-1$
		fLinkWithEditor.setToolTipText("Link With Editor"); //$NON-NLS-1$
		JavaPluginImages.setLocalImageDescriptors(fLinkWithEditor, "synced.gif"); //$NON-NLS-1$
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick();
			}
		});
	}
	
	private void handleDoubleClick() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object target = ((IStructuredSelection) selection)
					.getFirstElement();
			if (target instanceof AJDTStructureViewNode) {
				AJDTStructureViewNode ajdtNode = (AJDTStructureViewNode) target;
				IMarker marker = ajdtNode.getMarker(input.getProject());
				// System.out.println( "marker = " + marker );
				if (ajdtNode.getStructureNode() != null) {
					AJDTEventTrace.nodeClicked(ajdtNode.getStructureNode()
							.getName(), marker);
					if (marker != null) {
						try {
							IDE.openEditor(AspectJUIPlugin
									.getDefault().getActiveWorkbenchWindow()
									.getActivePage(), marker, false);
						} catch (PartInitException ex) {
						}
					}
				} else {
					AJDTEventTrace
							.generalEvent("Problem in outline view: Editor input is not a file");
				}
			}
		}
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(fLinkWithEditor);
	}
	
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!fDoLinkWithEditor || !(selection instanceof ITextSelection)) {
			return;
		}
		if (part instanceof IEditorPart) {
			IEditorInput editorInput = ((IEditorPart)part).getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				IFileEditorInput fe = (IFileEditorInput)editorInput;
				input = fe.getFile();
				IAdaptable outline = getContentOutline(input,true);
				viewer.setInput(outline);
			}
		
		}
	}

	/**
	 * Gets the content outline for a given input element.
	 * Returns the outline (a list of MarkElements), or null
	 * if the outline could not be generated.
	 */
	private IAdaptable getContentOutline(IFile input, boolean registerForUpdates) {
		String filePath = AJDTUtils.getResourcePath( input );
		AJDTEventTrace.structureViewRequested( input.getName() );
		// Memory leak fix, we must delete the view we currently have
		// so that we don't leak storage over time in the List of structure views maintained
		// inside the svManager
		if (this.view!=null) svManager.deleteView(this.view);
		
		FileStructureView structureView = svManager.
			createViewForSourceFile( filePath, viewProperties );
		structureView.setRenderer( this );
		this.view = structureView; 
		return (AJDTStructureViewNode)view.getRootNode();
	}
	
    /**
     * Returns this page's tree viewer.
     *
     * @return this page's tree viewer, or <code>null</code> if 
     *   <code>createControl</code> has not been called yet
     */
    protected TreeViewer getTreeViewer() {
        return viewer;
    }
	
	public void setActiveNode(IStructureViewNode activeNode,int lineOffset) {
	   	 // This is used in order to seek into nodes that do not appear
	   	 // in the structure view (e.g. call sites, exception handlers)
	   	 // AMC to fill it in :)
	   	 setActiveNode(activeNode); // Andys quick hack... is this at all right?
	}

	/**
	 * @see StructureViewRenderer#setActiveNode(StructureViewNode)
	 */
	public void setActiveNode( IStructureViewNode node ) {
		TreeViewer viewer = getTreeViewer();
		viewer.setInput(node );		
	}
	
	/**
	 * @see StructureViewRenderer#updateView(StructureView)
	 */
	public void updateView(StructureView view) {
		AJDTEventTrace.generalEvent("outline updateview called (file:"+input.getName()+"): "+view.toString());
		final StructureView fView = view;
		AspectJUIPlugin.getDefault( ).getDisplay().asyncExec( new Runnable( ) {
			public void run( ) {
				try {
					update( fView );
				} catch (Exception e) {
				}
			}	
		} );		
	}
	
	/**
	 * Forces the page to update its contents.
	 *
	 * @see AspectJEditor#doSave(IProgressMonitor)
	 */
	public void update() {
		AJDTEventTrace.generalEvent("Editor Update called: "+input.getName());
		getControl().setRedraw(false);
				IAdaptable outline = getContentOutline(input,true);
		getTreeViewer().setInput(outline);
		expandTreeView( );
		getControl().setRedraw(true);
	}
	
	/**
	 * Overloaded version of update that takes a given structure
	 * view rather than going and geting one.
	 */
	public void update( StructureView view) {
		if (view!=null && this.view!=null && !view.toString().equals(this.view.toString())) {
			// I think the 'view' has to be the same object as 'this.view' - ok the contents of the view can
			// be different, but it must be the same high level object at the top.  Thats because views have
			// instances of the outline page set up as their renderers.  If we come in through this update
			// method and have a different view (different than the one this outline page instance 
			// has registered as being the rendered for) then we'll get unexpected results.  Ideally in this
			// case (if it can happen) we would want to unregister as the renderer for the old view and
			// register as the renderer for the new view.  We cant do that in this function as it would alter
			// the set of registered renderers whilst the AspectJ code is calling back renderers with updates.
			// YUCK.  Maybe it can't happen, but I've put this kind of 'almost' assertion in here for now ...
			AJDTEventTrace.generalEvent("Assumption Not True: Old view object:"+view.toString()+
										"  New view object:"+view.toString());
		}
		// add this extra text to stop us geting empty view structure updates
		// caused by builds from other projects.
		if ( input.getProject().equals( AJBuilder.getLastBuildTarget() )  ) {
			AJDTEventTrace.modelUpdated( input );

			this.view = view;

			getControl().setRedraw(false);
			AJDTStructureViewNode toDisplay = (AJDTStructureViewNode)view.getRootNode();
			getTreeViewer( ).setInput( toDisplay );
			expandTreeView( );
			getControl().setRedraw(true);
		}
	}
	
    /* (non-Javadoc)
     * Method declared on IPage (and Page).
     */
    public Control getControl() {
        if (viewer == null)
            return null;
        return viewer.getControl();
    }
	
	/**
	 * Set the expansion levels on the tree branches to their default settings
	 */
	private void expandTreeView( ) {
		TreeViewer viewer = getTreeViewer();
		viewer.collapseAll(); // starting point!
		Tree t = viewer.getTree();
		TreeItem[] tItems = t.getItems();
		for ( int i = 0; i < tItems.length; i++ ) {
			TreeItem item = tItems[i];
			AJDTStructureViewNode node = (AJDTStructureViewNode) item.getData();
			if ( node.getStructureNode() instanceof IProgramElement ) {
				viewer.expandToLevel( node, 1 );
			}
		}
	}
}
