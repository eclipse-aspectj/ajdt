/**********************************************************************
Copyright (c) 2002-2004 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer - initial version
Sian January - removed advice marker update methods as part of the fix 
	for bug 70658
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.FileStructureView;
import org.aspectj.ajde.ui.IStructureViewNode;
import org.aspectj.ajde.ui.StructureView;
import org.aspectj.ajde.ui.StructureViewManager;
import org.aspectj.ajde.ui.StructureViewProperties;
import org.aspectj.ajde.ui.StructureViewRenderer;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.builder.AJBuilder;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.core.AJDTStructureViewNode;
import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/**
 * Content outline page provides facility of Java content outline
 * page (in time!), plus support for aspects, pointcuts, advice,
 * introductions, declarations and navigation of creosscutting structure.
 */
public class AspectJContentOutlinePage extends ContentOutlinePage 
implements StructureViewRenderer {
	protected IFile input;

	// TO DO:
	// Have update method called after a build.

	private static final int RELATIONSHIP_LEVEL = 3;

	/**
	 * The editor that created this outline page
	 */
	private AbstractTextEditor editor;

	/**
	 * Properties controlling the tree view display
	 */
	private StructureViewProperties viewProperties;

	/**
	 * StructureViewManager that manages the views on our behalf
	 */
	private StructureViewManager  svManager;

	/**
	 * The view that we are displaying
	 */
	private StructureView view;

	private boolean outlinePageCreation = true;
	
	/**
	 * The sorter that sorts the contents alphabetically
	 */
	private ViewerSorter lexicalSorter;

	/**
	 * Creates a new AspectJContentOutlinePage.
	 */
	public AspectJContentOutlinePage( AbstractTextEditor editor, IFile input) {
		super();
		this.input = input;
		this.editor = editor;
		this.svManager = Ajde.getDefault().getStructureViewManager();
		this.viewProperties = svManager.getDefaultViewProperties();
		viewProperties.setGranularity(StructureViewProperties.Granularity.DECLARED_ELEMENTS);
		lexicalSorter = new LexicalSorter( );
	}

		
	/**  
	 * Creates the control and registers the popup menu for this page
	 * Menu id "org.eclipse.ajdt.outline"
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);

		configureHelp( );
		configureContextMenu( );
		registerToolbarActions();

		TreeViewer viewer = getTreeViewer();
//		TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		viewer.addSelectionChangedListener(this);
		
		viewer.setContentProvider(new WorkbenchContentProvider());
		viewer.setLabelProvider(new AspectJLabelProvider(parent));
		IAdaptable outline = getContentOutline(input,true);
		viewer.setInput(outline);
		expandTreeView( );

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
		IProject myProject = input.getProject( );
		IProject builtProject = AJBuilder.getLastBuildTarget();
		if ( input.getProject().equals( AJBuilder.getLastBuildTarget() )  ) {
			AJDTEventTrace.modelUpdated( input );

			this.view = view;

			getControl().setRedraw(false);
	//		getTreeViewer().setInput( (AJDTStructureViewNode) view.getRootNode());
			AJDTStructureViewNode toDisplay = (AJDTStructureViewNode)view.getRootNode();
			getTreeViewer( ).setInput( toDisplay );
			expandTreeView( );
			getControl().setRedraw(true);
		}
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
					//ErrorHandler.handleError("Document Outline update failed", e);	
				}
			}	
		} );		
	}

	/**
	 * @see StructureViewRenderer#setActiveNode(StructureViewNode)
	 */
	public void setActiveNode( IStructureViewNode node ) {
		TreeViewer viewer = getTreeViewer();
		viewer.setInput( (AJDTStructureViewNode)node );		
	}

	/**
	 * Update the input file
	 * @param input - the new file
	 */
	protected void setInput(IFile input) {
		this.input = input;
	}
	
	/**
	 * Outline is being closed
	 */
	public void dispose( ) {
		String action="Unable to unregister with structure view manager";
		if (this.view!=null) {
			boolean successfullyUnregistered = svManager.deleteView(this.view);
			action = "Unregistering with structure view manager.  Successful:"+successfullyUnregistered;
		}
		AJDTEventTrace.generalEvent( "Outline disposed for file "+editor.getTitle()+": "+action );	
	}

	/**
	 * Selection has changed in the tree view
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged( event );
		if ( outlinePageCreation ) {
			outlinePageCreation = false;
			return;			
		}
		ISelection selection = event.getSelection( );
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection sSel = (IStructuredSelection) selection;
			Object target = sSel.getFirstElement();
			if ( target instanceof AJDTStructureViewNode ) {
				AJDTStructureViewNode ajdtNode = (AJDTStructureViewNode) target;
				if(editor.getEditorInput() instanceof IFileEditorInput) {
					IMarker marker = ajdtNode.getMarker(((IFileEditorInput)editor.getEditorInput()).getFile().getProject() );
					//System.out.println( "marker = " + marker );
					if (ajdtNode.getStructureNode() != null) {
					AJDTEventTrace.nodeClicked( ajdtNode.getStructureNode().getName( ), marker );
						if ( marker != null) {
							try {
								IEditorPart ePart = IDE.openEditor(AspectJUIPlugin.getDefault( ).getActiveWorkbenchWindow().getActivePage( ), marker, false);	
								// second navigation to marker overrides default selection of
								// first element when the editor is opened!!
	//							ePart.gotoMarker( marker );
							} catch (PartInitException ex ) {
								System.err.println( "Doh!" + ex );
							}
						}	
					}
				} else {
					AJDTEventTrace.generalEvent("Problem in outline view: Editor input is not a file");
				}
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
			
	    // I believe the guard was in place to prevent problems of multiple callbacks to the outline 
	    // view.  I think multiple callbacks were due to not removing the view above.  With views
	    // being removed, the guard can be removed.
		//if ( registerForUpdates ) {
		structureView.setRenderer( this );
		//}
		this.view = structureView; 
		//return (AJDTStructureViewNode) view.getRootNode();				
//		return tempPatch( view.getRootNode( ) );
		return (AJDTStructureViewNode)view.getRootNode();
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


//	/**
//	 * Add a dummy parent node if required
//	 */
//	private AJDTStructureViewNode tempPatch( IStructureViewNode svn ) {
//		AJDTStructureViewNode ajdtNode = (AJDTStructureViewNode) svn;
//		AJDTStructureViewNode retVal = ajdtNode;			
//		if ( ajdtNode.getLabel().equalsIgnoreCase( "<build to view structure>" ) ) {
//			retVal = new AJDTStructureViewNode( ajdtNode );	
//		}
//		
//		return retVal;
//	}
	
	/**
	 * Do whatever is necessary to set up help for this outline
	 * For now, does nothing!
	 */
	private void configureHelp( ) {
//		WorkbenchHelp.setHelp(
//		getControl(),
//		new String[] { IReadmeConstants.CONTENT_OUTLINE_PAGE_CONTEXT });
	}


	/**
	 * Configure and register the context menu for our outline view
	 */
	private void configureContextMenu( ) {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end"));
		//$NON-NLS-1$

		TreeViewer viewer = getTreeViewer( );
		Menu menu = menuMgr.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);
		// Be sure to register it so that other plug-ins can add actions.
		getSite().registerContextMenu(
			"org.eclipse.ajdt.ui.outline",
			menuMgr,
			viewer);
		//$NON-NLS-1$
		
	}

	public void setActiveNode(IStructureViewNode activeNode,int lineOffset) {
   	 // This is used in order to seek into nodes that do not appear
   	 // in the structure view (e.g. call sites, exception handlers)
   	 // AMC to fill it in :)
   	 setActiveNode(activeNode); // Andys quick hack... is this at all right?
  	}

	/**
	 * Creates the necessary actions and registers them with the toolbar
	 */
	private void registerToolbarActions() {

		IToolBarManager toolBarManager = getSite().getActionBars().getToolBarManager( );
		if (toolBarManager != null) {

			Action sortAction = new LexicalSortingAction();
			toolBarManager.add(sortAction);
			
			Action fieldAction =
				new FilterAction(
					new CategoryFilter( AJDTStructureViewNode.Category.FIELD ),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideFields.label"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideFields.description.checked"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideFields.description.unchecked"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideFields.tooltip.checked"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideFields.tooltip.unchecked"),
					"HideFields.isChecked");
			JavaPluginImages.setLocalImageDescriptors(fieldAction, "fields_co.gif");
			toolBarManager.add(fieldAction);

			Action staticAction =
				new FilterAction(
					new StaticFilter( ),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideStaticMembers.label"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideStaticMembers.description.checked"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideStaticMembers.description.unchecked"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideStaticMembers.tooltip.checked"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideStaticMembers.tooltip.unchecked"),
					"HideStaticMembers.isChecked");
			JavaPluginImages.setLocalImageDescriptors(staticAction, "static_co.gif");
			toolBarManager.add(staticAction);

			Action publicAction =
				new FilterAction(
					new VisibilityFilter( ),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideNonePublicMembers.label"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideNonePublicMembers.description.checked"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideNonePublicMembers.description.unchecked"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideNonePublicMembers.tooltip.checked"),
					AspectJUIPlugin.getResourceString("JavaOutlinePage.HideNonePublicMembers.tooltip.unchecked"),
					"HideNonePublicMembers.isChecked");
			JavaPluginImages.setLocalImageDescriptors(publicAction, "public_co.gif");
			toolBarManager.add(publicAction);
			
			Action pointcutAction =
				new FilterAction(
					new CategoryFilter( AJDTStructureViewNode.Category.POINTCUT ),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HidePointcuts.label"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HidePointcuts.description.checked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HidePointcuts.description.unchecked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HidePointcuts.tooltip.checked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HidePointcuts.tooltip.unchecked"),
					"HidePointcuts.isChecked");
			pointcutAction.setImageDescriptor( AspectJImages.HIDE_POINTCUTS.getImageDescriptor( ) );
//			AbstractIcon pIcon = AspectJImages.registry( ).getIcon( IProgramElement.Kind.POINTCUT);
//			pointcutAction.setHoverImageDescriptor( ((AJDTIcon)pIcon).getImageDescriptor( ) );
//			pointcutAction.setDisabledImageDescriptor( AspectJImages.E_POINTCUT_DEF.getImageDescriptor( ) );
			toolBarManager.add(pointcutAction);
			
			Action adviceAction =
				new FilterAction(
					new CategoryFilter( AJDTStructureViewNode.Category.ADVICE ),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideAdvice.label"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideAdvice.description.checked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideAdvice.description.unchecked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideAdvice.tooltip.checked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideAdvice.tooltip.unchecked"),
					"HideAdvice.isChecked");
			adviceAction.setImageDescriptor( AspectJImages.HIDE_ADVICE.getImageDescriptor( ) );
//			AbstractIcon hIcon = AspectJImages.registry( ).getIcon( IProgramElement.Kind.ADVICE);
//			adviceAction.setHoverImageDescriptor( ((AJDTIcon)hIcon).getImageDescriptor( ) );
//			adviceAction.setDisabledImageDescriptor( AspectJImages.E_ADVICE.getImageDescriptor( ) );
			toolBarManager.add(adviceAction);
			
			Action introductionAction =
				new FilterAction(
					new CategoryFilter( AJDTStructureViewNode.Category.INTRODUCTION ),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideIntroductions.label"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideIntroductions.description.checked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideIntroductions.description.unchecked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideIntroductions.tooltip.checked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideIntroductions.tooltip.unchecked"),
					"HideIntroductions.isChecked");
			introductionAction.setImageDescriptor( AspectJImages.HIDE_ITDS.getImageDescriptor( ) );
//			AbstractIcon iIcon = AspectJImages.registry( ).getIcon( IProgramElement.Kind.INTER_TYPE_METHOD);
//			introductionAction.setHoverImageDescriptor( ((AJDTIcon)iIcon).getImageDescriptor( ) );
//			introductionAction.setDisabledImageDescriptor( AspectJImages.E_ITD_FIELD_DEF.getImageDescriptor( ) );
			toolBarManager.add(introductionAction);

			Action declarationAction =
				new FilterAction(
					new CategoryFilter( AJDTStructureViewNode.Category.DECLARATION ),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideDeclarations.label"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideDeclarations.description.checked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideDeclarations.description.unchecked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideDeclarations.tooltip.checked"),
					AspectJUIPlugin.getResourceString("AJavaOutlinePage.HideDeclarations.tooltip.unchecked"),
					"HideIntroductions.isChecked");
			declarationAction.setImageDescriptor( AspectJImages.HIDE_DECLARATIONS.getImageDescriptor( ) );
//			AbstractIcon dIcon = AspectJImages.registry( ).getIcon( IProgramElement.Kind.DECLARE_PARENTS);
//			declarationAction.setHoverImageDescriptor( ((AJDTIcon)dIcon).getImageDescriptor( ) );
//			declarationAction.setDisabledImageDescriptor( AspectJImages.E_DECLARE_PARENTS.getImageDescriptor( ) );
			toolBarManager.add(declarationAction);
			
		}
	}



	/**
	 * Enable or disable the sorting of the tree view
	 */
	private void setSorting( boolean on ) {
		if ( on ) {
			AJDTEventTrace.outlineViewAction( "Sorting " + "Alphabetical order", input );
			getTreeViewer( ).setSorter( lexicalSorter );
		} else {
			AJDTEventTrace.outlineViewAction( "Sorting " + "Declaration order", input );
			getTreeViewer( ).setSorter( null );
		}
	}

	/**
	 * Add a filter to the list of currently active filters
	 */
	private void addFilter( ViewerFilter filter ) {
		getTreeViewer( ).addFilter( filter );
		expandTreeView();
	}
	
	/**
	 * Remove a filter from the list of currently active filters
	 */
	private void removeFilter( ViewerFilter filter ) {
		getTreeViewer( ).removeFilter( filter );
		expandTreeView();
	}


	// Sorting and Filterig Inner Classes
	// -------------------------------------------------------------------------

	/**
	 * Inner class to handle sorting action
	 */
	class LexicalSortingAction extends Action {

		public LexicalSortingAction() {
			super();

			setText( AspectJUIPlugin.getResourceString("JavaOutlinePage.Sort.label"));
			//$NON-NLS-1$
			JavaPluginImages.setLocalImageDescriptors(this, "alphab_sort_co.gif");
			//$NON-NLS-1$

			boolean checked =
				AspectJUIPlugin.getDefault().getPreferenceStore().getBoolean(
					"LexicalSortingAction.isChecked");
			//$NON-NLS-1$
			valueChanged(checked, false);

		}

		public void run() {
			valueChanged(isChecked(), true);
		}

		private void valueChanged(boolean on, boolean store) {
			setChecked(on);
			setSorting( on );
				

			setToolTipText(
				on
					? AspectJUIPlugin.getResourceString("JavaOutlinePage.Sort.tooltip.checked")
					: AspectJUIPlugin.getResourceString("JavaOutlinePage.Sort.tooltip.unchecked"));
			setDescription(
				on
					? AspectJUIPlugin.getResourceString("JavaOutlinePage.Sort.description.checked")
					: AspectJUIPlugin.getResourceString("JavaOutlinePage.Sort.description.unchecked"));

			if (store)
				AspectJUIPlugin.getDefault().getPreferenceStore().setValue(
					"LexicalSortingAction.isChecked",
					on);
		}
	};
	
	/**
	 * Inner class to provide tree view sorting
	 */
	class LexicalSorter extends ViewerSorter {

		/**
		 * Return a category code for the element. This is used
		 * to sort alphabetically within categories. Categories are:
		 * pointcuts, advice, introductions, declarations, other. i.e.
		 * all pointcuts will be sorted together rather than interleaved
		 * with advice.
		 */
		public int category( Object element ) {
			if ( element instanceof AJDTStructureViewNode ) {
				return ( (AJDTStructureViewNode) element).category( );
			} else {
				return 0;
			}					
		}
	};
	
		// Filter classes ---------------------------------------------------
	
	/**
	 * Inner class to manage filter actions
	 */
	class FilterAction extends Action {

		private ViewerFilter fFilter;
		private String fCheckedDesc;
		private String fUncheckedDesc;
		private String fCheckedTooltip;
		private String fUncheckedTooltip;
		private String fPreferenceKey;

		public FilterAction(
			ViewerFilter filter,
			String label,
			String checkedDesc,
			String uncheckedDesc,
			String checkedTooltip,
			String uncheckedTooltip,
			String prefKey) {
			super();

			fFilter = filter;

			setText(label);
			fCheckedDesc = checkedDesc;
			fUncheckedDesc = uncheckedDesc;
			fCheckedTooltip = checkedTooltip;
			fUncheckedTooltip = uncheckedTooltip;
			fPreferenceKey = prefKey;

			boolean checked =
				AspectJUIPlugin.getDefault().getPreferenceStore().getBoolean(fPreferenceKey);
			valueChanged(checked, false);
		}

		public void run() {
			valueChanged(isChecked(), true);
		}

		private void valueChanged(boolean on, boolean store) {

			setChecked(on);

			if (on) {
				AJDTEventTrace.outlineViewAction( fUncheckedTooltip, input );
				addFilter(fFilter);
				setToolTipText(fCheckedTooltip);
				setDescription(fCheckedDesc);
			} else {
				AJDTEventTrace.outlineViewAction( fCheckedTooltip, input );
				removeFilter(fFilter);
				setToolTipText(fUncheckedTooltip);
				setDescription(fUncheckedDesc);
			}

			if (store)
				AspectJUIPlugin.getDefault().getPreferenceStore().setValue(fPreferenceKey, on);
		}
	};


	/**
	 * Inner class to filter based on element visibility modifier
	 * Hides non-public members
	 */
	class VisibilityFilter extends ViewerFilter {

		/**
		 * Returns whether the given element makes it through this filter.
		 *
		 * @param viewer the viewer
		 * @param parentElement the parent element
		 * @param element the element
		 * @return <code>true</code> if element is included in the
		 *   filtered set, and <code>false</code> if excluded
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			boolean isPublic = true;
			if ( element instanceof AJDTStructureViewNode ) {
				isPublic= ((AJDTStructureViewNode)element).isPublic();
			}
			return isPublic;
		}

	};


	/**
	 * Inner class to filter based on static / non-static
	 */
	class StaticFilter extends ViewerFilter {

		/**
		 * Returns whether the given element makes it through this filter.
		 *
		 * @param viewer the viewer
		 * @param parentElement the parent element
		 * @param element the element
		 * @return <code>true</code> if element is included in the
		 *   filtered set, and <code>false</code> if excluded
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			boolean isNonStatic = true;
			if ( element instanceof AJDTStructureViewNode ) {
				isNonStatic = !((AJDTStructureViewNode)element).getStructureNode().getModifiers().contains(
					IProgramElement.Modifiers.STATIC);
			}
			return isNonStatic;
		}

	};

	/**
	 * Inner class to filter elements based on category
	 */
	class CategoryFilter extends ViewerFilter {

		/** to filter out...*/
		private int category;
		
		public CategoryFilter( int category ) {
			this.category = category;
		}
		
		/**
		 * Returns whether the given element makes it through this filter.
		 *
		 * @param viewer the viewer
		 * @param parentElement the parent element
		 * @param element the element
		 * @return <code>true</code> if element is included in the
		 *   filtered set, and <code>false</code> if excluded
		 */
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			boolean isNotInCategory = true;
			if ( element instanceof AJDTStructureViewNode ) {
				int elementCat = ((AJDTStructureViewNode)element).category( );
				isNotInCategory = ( category != elementCat );
			}
			return isNotInCategory;
		}

	}



// =============================================================================
// archive - this section of the file contains the version fo filtering and
// sorting that uses the AJDE viewProperties APIs. Unfortunately this cannot
// be made to work reliably across multiple projtecs, so have reverted to
// TreeViewer filter model above.

	
//	/**
//	 * Enable or disable the sorting of the tree view
//	 */
//	private void setSorting( boolean on ) {
//		StructureViewProperties.Sorting sorting;
//		if ( on ) {
//			sorting = StructureViewProperties.Sorting.ALPHABETICAL;
//		} else {
//			sorting = StructureViewProperties.Sorting.DECLARATIONAL;
//		}
//		AJDTEventTrace.outlineViewAction( "Sorting " + sorting, input );
//		updateActiveConfig();
//		viewProperties.setSorting( sorting );
//		if ( view != null ) {
//			view.setViewProperties( viewProperties );
//			svManager.refreshView( view );
//		}
//	}


	// -------------------- Inner classes ----------------------------------------------
	
//	/**
//	 * Inner class to handle sorting action
//	 */
//	class LexicalSortingAction extends Action {
//
//		//		private JavaElementSorter fSorter= new JavaElementSorter();			
//
//		public LexicalSortingAction() {
//			super();
//
//			setText( AspectJPlugin.getResourceString("JavaOutlinePage.Sort.label"));
//			//$NON-NLS-1$
//			JavaPluginImages.setLocalImageDescriptors(this, "alphab_sort_co.gif");
//			//$NON-NLS-1$
//
//			boolean checked =
//				AspectJPlugin.getDefault().getPreferenceStore().getBoolean(
//					"LexicalSortingAction.isChecked");
//			//$NON-NLS-1$
//			valueChanged(checked, false);
//		}
//
//		public void run() {
//			valueChanged(isChecked(), true);
//		}
//
//		private void valueChanged(boolean on, boolean store) {
//			setChecked(on);
//			setSorting( on );
//				
//
//			setToolTipText(
//				on
//					? AspectJPlugin.getResourceString("JavaOutlinePage.Sort.tooltip.checked")
//					: AspectJPlugin.getResourceString("JavaOutlinePage.Sort.tooltip.unchecked"));
//			setDescription(
//				on
//					? AspectJPlugin.getResourceString("JavaOutlinePage.Sort.description.checked")
//					: AspectJPlugin.getResourceString("JavaOutlinePage.Sort.description.unchecked"));
//
//			if (store)
//				AspectJPlugin.getDefault().getPreferenceStore().setValue(
//					"LexicalSortingAction.isChecked",
//					on);
//		}
//	};
//	
//	
//	// Filter classes ---------------------------------------------------
//	
//	/**
//	 * Inner class to manage filter actions
//	 */
//	abstract class FilterAction extends Action {
//
//		private String fCheckedDesc;
//		private String fUncheckedDesc;
//		private String fCheckedTooltip;
//		private String fUncheckedTooltip;
//		private String fPreferenceKey;
//		protected StructureViewProperties viewProps;
//
//		public FilterAction(
//			StructureViewProperties viewProps,
//			String label,
//			String checkedDesc,
//			String uncheckedDesc,
//			String checkedTooltip,
//			String uncheckedTooltip,
//			String prefKey) {
//			super();
//
//
//			setText(label);
//			fCheckedDesc = checkedDesc;
//			fUncheckedDesc = uncheckedDesc;
//			fCheckedTooltip = checkedTooltip;
//			fUncheckedTooltip = uncheckedTooltip;
//			fPreferenceKey = prefKey;
//			this.viewProps = viewProps;
//
//			boolean checked =
//				AspectJPlugin.getDefault().getPreferenceStore().getBoolean(fPreferenceKey);
//			valueChanged(checked, false);
//		}
//
//		public void run() {
//			valueChanged(isChecked(), true);
//		}
//
//		private void valueChanged(boolean on, boolean store) {
//
//			setChecked(on);
//
//			if (on) {
//				AJDTEventTrace.outlineViewAction( fUncheckedTooltip, input );
//				applyFilter( );
//				setToolTipText(fCheckedTooltip);
//				setDescription(fCheckedDesc);
//			} else {
//				AJDTEventTrace.outlineViewAction( fCheckedTooltip, input );
//				removeFilter( );
//				setToolTipText(fUncheckedTooltip);
//				setDescription(fUncheckedDesc);
//			}
//
//			if ( view != null ) {
//				updateActiveConfig();
//				view.setViewProperties( viewProps );
//				svManager.refreshView( view );			
//			}
//
//			if (store)
//				AspectJPlugin.getDefault().getPreferenceStore().setValue(fPreferenceKey, on);
//		}
//		
//		protected abstract void applyFilter( );		
//		protected abstract void removeFilter( );
//	};
//
//
//	/**
//	 * Inner class to filter based on element visibility modifier
//	 * Hides non-public members
//	 */
//	class VisibilityFilterAction extends FilterAction {
//		
//		public VisibilityFilterAction(
//			StructureViewProperties viewProps,			
//			String label,
//			String checkedDesc,
//			String uncheckedDesc,
//			String checkedTooltip,
//			String uncheckedTooltip,
//			String prefKey) {
//				super( viewProps, label, checkedDesc, uncheckedDesc, checkedTooltip,
//						uncheckedTooltip, prefKey );
//		}
//		
//		
//		protected void applyFilter( ) {
//			viewProps.addFilteredMemberAccessibility( ProgramElementNode.Accessibility.PACKAGE );
//			viewProps.addFilteredMemberAccessibility( ProgramElementNode.Accessibility.PRIVATE );
//			viewProps.addFilteredMemberAccessibility( ProgramElementNode.Accessibility.PROTECTED );
//			viewProps.addFilteredMemberAccessibility( ProgramElementNode.Accessibility.PRIVILEGED );				
//		}
//		
//		protected void removeFilter( ) {
//			viewProps.removeFilteredMemberAccessibility( ProgramElementNode.Accessibility.PACKAGE );
//			viewProps.removeFilteredMemberAccessibility( ProgramElementNode.Accessibility.PRIVATE );
//			viewProps.removeFilteredMemberAccessibility( ProgramElementNode.Accessibility.PROTECTED );
//			viewProps.removeFilteredMemberAccessibility( ProgramElementNode.Accessibility.PRIVILEGED );							
//		}
//	};
//
//
//	/**
//	 * Inner class to filter based on static / non-static
//	 */
//	class ModifierFilterAction extends FilterAction {
//		
//		private ProgramElementNode.Modifiers modifier;
//		
//		public ModifierFilterAction(
//			StructureViewProperties viewProps,
//			ProgramElementNode.Modifiers modifier,
//			String label,
//			String checkedDesc,
//			String uncheckedDesc,
//			String checkedTooltip,
//			String uncheckedTooltip,
//			String prefKey) {
//				super( viewProps, label, checkedDesc, uncheckedDesc, checkedTooltip,
//						uncheckedTooltip, prefKey );
//				this.modifier = modifier;
//		}
//		
//		
//		protected void applyFilter( ) {
//			viewProps.addFilteredMemberModifiers( modifier );
//		}
//		
//		protected void removeFilter( ) {
//			viewProps.removeFilteredMemberModifiers( modifier );
//		}
//	};
//
//
//	/**
//	 * Inner class to filter out fields
//	 */
//	class KindFilterAction extends FilterAction {
//		
//		private ProgramElementNode.Kind kind;
//		
//		public KindFilterAction(
//			StructureViewProperties viewProps,
//			ProgramElementNode.Kind kind,
//			String label,
//			String checkedDesc,
//			String uncheckedDesc,
//			String checkedTooltip,
//			String uncheckedTooltip,
//			String prefKey) {
//				super( viewProps, label, checkedDesc, uncheckedDesc, checkedTooltip,
//						uncheckedTooltip, prefKey );
//				this.kind = kind;
//		}
//		
//		
//		protected void applyFilter( ) {
//			viewProps.addFilteredMemberKind( kind );
//		}
//		
//		protected void removeFilter( ) {
//			viewProps.removeFilteredMemberKind( kind );
//		}
//	};
} 
