/**********************************************************************
 Copyright (c) 2002, 2004 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 ...
 **********************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.ajde.Ajde;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.internal.ui.editor.actions.AJOrganizeImportsAction;
import org.eclipse.ajdt.internal.ui.editor.quickfix.JavaCorrectionAssistant;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.IWorkingCopyManagerExtension;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * AspectJ Editor extends internal JDT editor in order to use our TextTools.
 * Better would be a clean interface in JDT to achieve the same effect. We also
 * override getAdapter( ) to plug-in the AspectJ-aware outline view.
 */
public class AspectJEditor extends CompilationUnitEditor {
	

	public static final String ASPECTJ_EDITOR_ID = "org.eclipse.ajdt.internal.ui.editor.CompilationUnitEditor";

	private AnnotationAccessWrapper annotationAccessWrapper;

	private static Set activeEditorList = new HashSet();	

	private AspectJEditorTitleImageUpdater aspectJEditorErrorTickUpdater;

	/**
	 * Constructor for AspectJEditor
	 */
	public AspectJEditor() {
		
		super();	
		setRulerContextMenuId("#AJCompilationUnitRulerContext"); //$NON-NLS-1$	
		// Bug 78182
		aspectJEditorErrorTickUpdater= new AspectJEditorTitleImageUpdater(this);
	}
	
	// Existing in this map means the modification has occurred
	static Set modifiedAspectToClass = new HashSet();

	private AJSourceViewerConfiguration fAJSourceViewerConfiguration;

	private boolean isEditingAjFile = false;

	private AJCompilationUnitAnnotationModel.GlobalAnnotationModelListener fGlobalAnnotationModelListener;

	private IAnnotationModel annotationModel;

	private class AJTextOperationTarget implements ITextOperationTarget {
		private ITextOperationTarget parent;

		private JavaCorrectionAssistant fCorrectionAssistant;

		private IInformationPresenter fOutlinePresenter;
		
		public AJTextOperationTarget(ITextOperationTarget parent) {
			this.parent = parent;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.text.ITextOperationTarget#canDoOperation(int)
		 */
		public boolean canDoOperation(int operation) {
			return parent.canDoOperation(operation);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.text.ITextOperationTarget#doOperation(int)
		 */
		public void doOperation(int operation) {
			if (operation == CORRECTIONASSIST_PROPOSALS) {
				// use our own correction assistant
				if (fCorrectionAssistant == null) {
					fCorrectionAssistant = new JavaCorrectionAssistant(
							AspectJEditor.this);
					fCorrectionAssistant.install(getSourceViewer());
				}
				String msg = fCorrectionAssistant.showPossibleCompletions();
				setStatusLineErrorMessage(msg);
			} else if (operation == JavaSourceViewer.SHOW_OUTLINE) {
				// use our own outline presenter
				// not needed if/when eclipse bug 79489 is fixed
				if (fOutlinePresenter == null) {
					fOutlinePresenter = fAJSourceViewerConfiguration.getOutlinePresenter(getSourceViewer(),false);
					fOutlinePresenter.install(getSourceViewer());
				}
				fOutlinePresenter.showInformation();
			} else {
				parent.doOperation(operation);
			}
		}

	}

	public IDocumentProvider getDocumentProvider() {
		return super.getDocumentProvider();
	}
	
	/**
	 * Get an adapter - if they want an outliner, give them ours! Other adapters
	 * we could consider providing:
	 * <ol>
	 * <li>ITextOperationTarget</li>
	 * <li>IFindReplaceTarget</li>
	 * </ol>
	 */
	public Object getAdapter(Class key) {
		if (key.equals(ITextOperationTarget.class)) {
			// use our own wrapper around the one returned by the superclass
			return new AJTextOperationTarget((ITextOperationTarget) super
					.getAdapter(key));
		}
		if (key.equals(IAnnotationAccess.class)) {
			Object o = super.getAdapter(key);
			if (o instanceof IAnnotationAccessExtension) {
				if (annotationAccessWrapper == null) {
					annotationAccessWrapper = new AnnotationAccessWrapper();
				}
				annotationAccessWrapper
						.setWrapped((IAnnotationAccessExtension) o);
				return annotationAccessWrapper;
			}
			return o;
		}
		return super.getAdapter(key);
	}

	//Wrapper for IAnnotationAccessExtension. Purpose: this wrapper returns
	//an increased layer for overrideIndicator markers. This does not affect
	//the layer it gets displayed on, but it fixes the context menu so we
	//are still able to choose "open super implementation" when right-clicking
	//the vertical ruler (Matt/Luzius)
	class AnnotationAccessWrapper implements IAnnotationAccessExtension {

		private IAnnotationAccessExtension wrapped;

		public void setWrapped(IAnnotationAccessExtension w) {
			wrapped = w;
		}

		public String getTypeLabel(Annotation annotation) {
			return wrapped.getTypeLabel(annotation);
		}

		public int getLayer(Annotation annotation) {
			int x = wrapped.getLayer(annotation);
			if ("org.eclipse.jdt.ui.overrideIndicator".equals(annotation
					.getType())) {
				x += 2;
			}
			return x;
		}

		public void paint(Annotation annotation, GC gc, Canvas canvas,
				Rectangle bounds) {
			wrapped.paint(annotation, gc, canvas, bounds);
		}

		public boolean isPaintable(Annotation annotation) {
			return wrapped.isPaintable(annotation);
		}

		public boolean isSubtype(Object annotationType,
				Object potentialSupertype) {
			return wrapped.isSubtype(annotationType, potentialSupertype);
		}

		public Object[] getSupertypes(Object annotationType) {
			return wrapped.getSupertypes(annotationType);
		}
	}

	/**
	 * Override of doSave to comment-out call to getStatusLineManager - always
	 * returns null (why?) in our environment. Also ask the contentOutlinePage
	 * to update - not strictly required at the moment, but will be in the
	 * future.
	 */
	public void doSave(IProgressMonitor progressMonitor) {

		IDocumentProvider p = getDocumentProvider();
		if (p == null)
			return;

		if (p.isDeleted(getEditorInput())) {

			if (isSaveAsAllowed()) {

				/*
				 * 1GEUSSR: ITPUI:ALL - User should never loose changes made in
				 * the editors. Changed Behavior to make sure that if called
				 * inside a regular save (because of deletion of input element)
				 * there is a way to report back to the caller.
				 */
				performSaveAs(progressMonitor);

			} else {

				/*
				 * 1GF5YOX: ITPJUI:ALL - Save of delete file claims it's still
				 * there Missing resources.
				 */
				Shell shell = getSite().getShell();
				MessageDialog
						.openError(
								shell,
								"CompilationUnitEditor.error.saving.title1", "CompilationUnitEditor.error.saving.message1"); //$NON-NLS-1$ //$NON-NLS-2$
			}

		} else {

			IWorkingCopyManager manager = JavaPlugin.getDefault()
					.getWorkingCopyManager();
			ICompilationUnit unit = manager.getWorkingCopy(getEditorInput());

			if (unit != null) {
				synchronized (unit) {
					performSave(false, progressMonitor);
				}
			} else
				performSave(false, progressMonitor);
		}
	}
	
	/**
	 * Override to replace some of the java editor actions
	 * @see org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor#createActions()
	 */
	// Fix for bug 88474
	protected void createActions() {
		super.createActions();
		IAction organizeImports= new AJOrganizeImportsAction(this);
		organizeImports.setActionDefinitionId(IJavaEditorActionDefinitionIds.ORGANIZE_IMPORTS);
		setAction("OrganizeImports", organizeImports); //$NON-NLS-1$
	}


	//override this function to prevent others from setting the
	// SourceViewConfiguration
	//to a non-AspectJ one once it was set to AJSourceViewConfiguration
	protected void setSourceViewerConfiguration(
			SourceViewerConfiguration configuration) {
		Assert.isNotNull(configuration);
		SourceViewerConfiguration myConf = this.getSourceViewerConfiguration();
		if ((myConf != null) && (myConf instanceof AJSourceViewerConfiguration)
				&& !(configuration instanceof AJSourceViewerConfiguration))
			return;
		super.setSourceViewerConfiguration(configuration);
	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		if(annotationModel != null) {
			if(annotationModel instanceof CompilationUnitAnnotationModelWrapper) {
				((CompilationUnitAnnotationModelWrapper)annotationModel).setDelegate(getSourceViewer().getAnnotationModel());
			}
			if(fGlobalAnnotationModelListener == null) {
				fGlobalAnnotationModelListener = new AJCompilationUnitAnnotationModel.GlobalAnnotationModelListener();
				fGlobalAnnotationModelListener.addListener(JavaPlugin.getDefault().getProblemMarkerManager());
			}
			annotationModel.addAnnotationModelListener(fGlobalAnnotationModelListener);			
			IDocument document = getDocumentProvider().getDocument(getEditorInput());
			ISourceViewer sourceViewer= getSourceViewer();		
			sourceViewer.setDocument(document, annotationModel);
			IAnnotationModel model = getDocumentProvider().getAnnotationModel(getEditorInput());
			if(model != null) { // this is null in a linked source folder due to an eclipse bug..
				model.connect(document);
			}
		} 
	}
	
	public void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fInput = (IFileEditorInput) input;
			ICompilationUnit unit = null;
			//in case it is a .aj file, we need to register it in the
			// WorkingCopyManager
			if (CoreUtils.ASPECTJ_SOURCE_ONLY_FILTER.accept(fInput
					.getFile().getName())) {
				unit = AJCompilationUnitManager.INSTANCE
					.getAJCompilationUnitFromCache(fInput.getFile());
		
				if (unit != null){
					isEditingAjFile = true;
	
					annotationModel = new AJCompilationUnitAnnotationModel(unit.getResource());
					((AJCompilationUnitAnnotationModel)annotationModel).setCompilationUnit(unit);
					
				}
			} else if (CoreUtils.ASPECTJ_SOURCE_FILTER.accept(fInput
					.getFile().getName())){
				unit = JavaCore.createCompilationUnitFrom(fInput.getFile());
				annotationModel = new CompilationUnitAnnotationModelWrapper(unit);
			}
			
						
			if(annotationModel != null) {
				if(unit instanceof CompilationUnit) {
					JavaModelManager.getJavaModelManager().discardPerWorkingCopyInfo((CompilationUnit)unit);
				}
				unit.becomeWorkingCopy((IProblemRequestor)annotationModel, null);
				((IWorkingCopyManagerExtension) JavaPlugin.getDefault()
						.getWorkingCopyManager()).setWorkingCopy(input, unit);
			}

			AJLog.log("Editor opened on " + fInput.getFile().getName());
			// Ensure any advice markers are created since they are not
			// persisted.
			updateActiveConfig(fInput);
			synchronized(activeEditorList) {
				activeEditorList.add(this);
			}
			IDocument document = getDocumentProvider().getDocument(fInput);
			AspectJTextTools textTools = AspectJUIPlugin.getDefault()
					.getAspectJTextTools();

			textTools.setupJavaDocumentPartitioner(document,
					EclipseEditorIsolation.JAVA_PARTITIONING);

			if ("aj".equals(fInput.getFile().getFileExtension())) {
				JavaPlugin.getDefault().getWorkingCopyManager().connect(input);
			}
			
//			 Part of the fix for 89793 - editor icon is not always correct
			aspectJEditorErrorTickUpdater.updateEditorImage(getInputJavaElement());
		}
	}

	
	/**
	 * Update active config in AJDE.  Added as part of the fix for bug 70658.
	 */
	private void updateActiveConfig(IFileEditorInput fInput ) {
		IProject project = fInput.getFile().getProject();
		String configFile = AspectJPlugin.getBuildConfigurationFile(project);
		if ( !configFile.equals( Ajde.getDefault().getConfigurationManager().getActiveConfigFile()) ) {
			AJLog.log("Configuration file " + configFile + " selected for " + project.getName());
			Ajde.getDefault().getConfigurationManager().setActiveConfigFile( configFile );
		}				
	}


	public void dispose() {
		AJLog.log("Disposing editor for:" + getTitle());
		IEditorInput input = getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fInput = (IFileEditorInput) input;			
			// Fix for bug 79633 - editor buffer is not refreshed
			JavaPlugin.getDefault().getWorkingCopyManager().disconnect(input);
			
			AJLog.log("Editor closed - " + fInput.getFile().getName());
			synchronized(activeEditorList) {
				activeEditorList.remove(this);
			}

			try {
				ICompilationUnit unit = AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(fInput.getFile());
				if (unit != null) {
					unit.discardWorkingCopy();					
				}
			} catch (JavaModelException e) {
			}

		}
		if (aspectJEditorErrorTickUpdater != null) {
			aspectJEditorErrorTickUpdater.dispose();
			aspectJEditorErrorTickUpdater = null;
		}
		super.dispose();
	}
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		// AMC -added this method to catch user switching between multiple open
		// files
		// in the editor - used to trigger a structured selection event, but now
		// triggers a TextSelection event. TextSelection event does not give AJP
		// enouhg info to determine project, so have to do from here instead.
		IEditorInput input = getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fInput = (IFileEditorInput) input;
			AspectJPlugin.getDefault().setCurrentProject(
					fInput.getFile().getProject());
			// Ensure any advice markers are created since they are not
			// persisted - but
			// in this case (unlike when opening a new editor) they are likely
			// to
			// be correct already.
		}
		super.setFocus();
		
		// Sian: Added the code below to fix bug 77479 - link with editor does not work for .aj files 
		if(isEditingAjFile) {
			IViewPart view = getEditorSite().getPage().findView( PackageExplorerPart.VIEW_ID);
			if(view != null) {
				PackageExplorerPart packageExplorer = (PackageExplorerPart)view;
				try {
					Method isLinkingEnabledMethod = PackageExplorerPart.class.getDeclaredMethod("isLinkingEnabled", new Class[]{});
					isLinkingEnabledMethod.setAccessible(true);
					boolean linkingEnabled = ((Boolean)isLinkingEnabledMethod.invoke(packageExplorer, new Object[]{})).booleanValue();
					if(linkingEnabled) {
						IFileEditorInput fInput = (IFileEditorInput) input;
						AJCompilationUnit ajc = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit(fInput.getFile());
						packageExplorer.selectReveal(new StructuredSelection(ajc));
					}					
				} catch (Exception e) {
				}
			}
		}
	}

	public void gotoMarker(IMarker marker) {
		super.gotoMarker(marker);
	}
	
	protected void initializeEditor() {
		super.initializeEditor();
		IPreferenceStore store = this.getPreferenceStore();
		AspectJTextTools textTools = new AspectJTextTools(store);
		fAJSourceViewerConfiguration = new AJSourceViewerConfiguration(
				textTools, this);
		setSourceViewerConfiguration(fAJSourceViewerConfiguration);
	}


	/**
	 * Removes unsupported menu options. This is obviously not a nice way to do
	 * that. It would be much better do never add them in the first place. But
	 * this cannot easily be done since we cannot control specific actions - we
	 * can only have all or none by calling or not calling our super method.
	 * 
	 * @author Luzius
	 */
	public void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		if (isEditingAjFile) {
			menu.remove("org.eclipse.jdt.ui.refactoring.menu");

			//remove open type & call hierarchy
			IContributionItem[] items = menu.getItems();
			for (int i = 0; i < items.length; i++) {
				IContributionItem item = items[i];
				if ("group.open".equals(item.getId())) {
					menu.remove(items[i + 2]);
					menu.remove(items[i + 3]);
					break;
				}
			}
		}

	}
	
	/**
	 * @return Returns the activeEditorList.
	 */
	public static Set getActiveEditorList() {
		synchronized(activeEditorList) {
			return activeEditorList;
		}
	}

	/**
	 * Update the title image
	 */
	// Part of the fix for 89793 - editor icon is not always correct
	public void resetTitleImage() {
		aspectJEditorErrorTickUpdater.updateEditorImage(getInputJavaElement());
	}
	

}