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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.ui.ajde.ProjectProperties;
import org.eclipse.ajdt.internal.ui.editor.quickfix.JavaCorrectionAssistant;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.javamodel.AJCompilationUnitManager;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.visualiser.NodeHolder;
import org.eclipse.ajdt.ui.visualiser.StructureModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.IWorkingCopyManagerExtension;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * AspectJ Editor extends internal JDT editor in order to use our TextTools.
 * Better would be a clean interface in JDT to achieve the same effect. We also
 * override getAdapter( ) to plug-in the AspectJ-aware outline view.
 */
public class AspectJEditor extends CompilationUnitEditor {

	public static final String ASPECTJ_EDITOR_ID = "org.eclipse.ajdt.internal.ui.editor.CompilationUnitEditor";

	private AspectJContentOutlinePage contentOutlinePage;

	private AnnotationAccessWrapper annotationAccessWrapper;

	private boolean markersNeedUpdating = true;

	private static Set activeEditorList = new HashSet();

	private IFileEditorInput currentFileInput;

	/**
	 * Constructor for AspectJEditor
	 */
	public AspectJEditor() {
		super();	
		
		// bug 77917 - use our own document provider so that we still get an
		// annotation model for .aj files.
		setDocumentProvider(AspectJUIPlugin.getDefault().getAJCompilationUnitDocumentProvider());
		
		//		activeEditorList.add(this);
		//		//this.setSourceViewerConfiguration()
		//		AspectJTextTools textTools =
		//			AspectJPlugin.getDefault().getAspectJTextTools();
		//		setSourceViewerConfiguration(
		//			new JavaSourceViewerConfiguration(textTools, this));
		//((PartSite)getSite()).getConfigurationElement()

	}

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		//re-initialize the ruler context menu so we can add our
		// ContextMenuManipulator
		//(code copied from a super implementation of this method)
		if (getRulerContextMenuId() != null) {
			String id = getRulerContextMenuId() != null ? getRulerContextMenuId()
					: DEFAULT_RULER_CONTEXT_MENU_ID;
			MenuManager manager = new MenuManager(id, id);
			manager.setRemoveAllWhenShown(true);
			manager.addMenuListener(getContextMenuListener());

			Control rulerControl = getVerticalRuler().getControl();
			Menu fRulerContextMenu = manager.createContextMenu(rulerControl);
			rulerControl.setMenu(fRulerContextMenu);
			//rulerControl.addMouseListener(getRulerMouseListener());
			getSite().registerContextMenu(getRulerContextMenuId(), manager,
					getSelectionProvider());
			manager.addMenuListener(new ContextMenuManipulator());
		}
	}

	// Existing in this map means the modification has occurred
	static Set modifiedAspectToClass = new HashSet();

	private AJSourceViewerConfiguration fAJSourceViewerConfiguration;

	private boolean isEditingAjFile = false;

	private class AJTextOperationTarget implements ITextOperationTarget {
		private ITextOperationTarget parent;

		private JavaCorrectionAssistant fCorrectionAssistant;

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
			} else {
				parent.doOperation(operation);
			}
		}

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
		//System.err.println( "Asked for adapter: " + key.getName() );
		if (key.equals(ITextOperationTarget.class)) {
			// use our own wrapper around the one returned by the superclass
			return new AJTextOperationTarget((ITextOperationTarget) super
					.getAdapter(key));
		}
		if (key.equals(IContentOutlinePage.class)) {
			return getContentOutlinePage(key);
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
			} else {
				return o;
			}
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

			//getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$

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
		if (contentOutlinePage == null) {
			Object outlinePage = getContentOutlinePage(IContentOutlinePage.class);
			if (outlinePage instanceof AspectJContentOutlinePage)
				contentOutlinePage = (AspectJContentOutlinePage) outlinePage;
		}

		// AMC - commented this out - it sometimes causes "Save Failed 'Null'"
		// problems and the update will never do anything since we don't have
		// eager parsing and incremental compilation yet.

		//if (contentOutlinePage != null)
		// contentOutlinePage.update( );
	}

	/**
	 * Get the content outline page - either the JDT one or our own depending on
	 * the preference setting.
	 */
	private Object getContentOutlinePage(Class key) {
		Object outlinePage = null;

		if (AspectJPreferences.isAspectJOutlineEnabled()) {
			IEditorInput input = getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile f = ((IFileEditorInput) input).getFile();
				IProject p = f.getProject();
				try {
					if (p.hasNature(AspectJUIPlugin.ID_NATURE)) {
						contentOutlinePage = new AspectJContentOutlinePage(
								this, f);
						outlinePage = contentOutlinePage;
					} else {
						outlinePage = super.getAdapter(key);
					}
				} catch (CoreException cEx) {
					outlinePage = super.getAdapter(key);
				}
			} else {
				outlinePage = super.getAdapter(key);
			}
		} else {
			outlinePage = super.getAdapter(key);
		}

		return outlinePage;
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

	public void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);

		if (input instanceof IFileEditorInput) {
			IFileEditorInput fInput = (IFileEditorInput) input;

			//in case it is a .aj file, we need to register it in the
			// WorkingCopyManager
			if (ProjectProperties.ASPECTJ_SOURCE_ONLY_FILTER.accept(fInput
					.getFile().getName())) {

				AJCompilationUnit unit = AJCompilationUnitManager.INSTANCE
						.getAJCompilationUnitFromCache(fInput.getFile());
				if (unit != null){
				isEditingAjFile = true;

				//TODO: if we want to get errors/warnings markers from the
				//parser, pass the appropriate ProblemRequestor instead of null
				unit.becomeWorkingCopy(null, null);

				//CompilationUnitAnnotationModel model = new
				// CompilationUnitAnnotationModel(fInput.getFile());
				//model.setIsActive(true);
				//IAnnotationModel model =
				// this.getDocumentProvider().getAnnotationModel(unit);
				((IWorkingCopyManagerExtension) JavaPlugin.getDefault()
						.getWorkingCopyManager()).setWorkingCopy(input, unit);
				}
			}

			if (currentFileInput != null) {
				removeAJDTMarkers(currentFileInput);
			}
			currentFileInput = fInput;
			AJDTEventTrace.editorOpened(fInput.getFile());
			// Ensure any advice markers are created since they are not
			// persisted.
			updateActiveConfig(fInput);
			updateAdviceMarkers(fInput);
			activeEditorList.add(this);
			IDocument document = getDocumentProvider().getDocument(fInput);
			AspectJTextTools textTools = AspectJUIPlugin.getDefault()
					.getAspectJTextTools();

			textTools.setupJavaDocumentPartitioner(document,
					IJavaPartitions.JAVA_PARTITIONING);

			// Fix to bug 61679 - update the input to the outline view
			if (contentOutlinePage != null) {
				contentOutlinePage.setInput(fInput.getFile());
				contentOutlinePage.update();
			}

			if ("aj".equals(fInput.getFile().getFileExtension())) {
				JavaPlugin.getDefault().getWorkingCopyManager().connect(input);
			}
		}
	}

	/**
	 * This method forces an update of the markers (i.e. they will all be
	 * deleted and then readded) - this is called from the build updating code
	 * in AspectJContentOutlinePage - to ensure that every editor has the right
	 * markers after a compilation
	 */
	public void forceUpdateOfAdviceMarkers() {
		markersNeedUpdating = true;
		updateAdviceMarkers((IFileEditorInput) getEditorInput());
	}

	/**
	 * Adds the advice markers for a file to the left hand gutter. It kicks off
	 * a thread that does a delete then adds all the new markers.
	 */
	public void updateAdviceMarkers(final IFileEditorInput fInput) {

		if (!markersNeedUpdating)
			return;

		if (fInput == null) {
			AJDTEventTrace
					.generalEvent("AspectJEditor: FileEditorInput is null for editor with title ("
							+ getTitle() + "): Cannot update markers on it");
			return;
		}

		if (fInput.getFile() == null) {
			AJDTEventTrace
					.generalEvent("AspectJEditor: fileeditorinput.getFile() is null: see bugzilla #43662");
			return;
		}

		removeAJDTMarkers(fInput);
		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				addNewMarkers(fInput);
			}
		});
	}
	
	/**
	 * Update active config in AJDE.  Added as part of the fix for bug 70658.
	 */
	private void updateActiveConfig(IFileEditorInput fInput ) {
		IProject project = fInput.getFile().getProject();
		String configFile = AspectJUIPlugin.getBuildConfigurationFile(project);
		if ( !configFile.equals( Ajde.getDefault().getConfigurationManager().getActiveConfigFile()) ) {
			AJDTEventTrace.buildConfigSelected( configFile, project );
			Ajde.getDefault().getConfigurationManager().setActiveConfigFile( configFile );
		}				
	}

	/**
	 * Remove all the AJDT markers from the given file input.
	 * 
	 * @param fInput
	 */
	private void removeAJDTMarkers(final IFileEditorInput fInput) {
		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					// Wipe all the current advice markers
					fInput.getFile().deleteMarkers(IAJModelMarker.ADVICE_MARKER,
							true, IResource.DEPTH_INFINITE);
					fInput.getFile().deleteMarkers(
							IAJModelMarker.DECLARATION_MARKER, true,
							IResource.DEPTH_INFINITE);
				} catch (CoreException ce) {
					//if file has been deleted, don't throw exception
					if (fInput.getFile().exists())
						AspectJUIPlugin.getDefault().getErrorHandler()
								.handleError("Advice marker delete failed", ce);
				}
			}
		});
	}

	/**
	 * Adds advice markers to mark each line in the source that is affected by
	 * advice from an aspect. It uses the StructureModelUtil code that the
	 * visualizer also uses - to determine what aspects are in effect on a
	 * specific source file.
	 * 
	 * @param fInput
	 *            The file editor input resource against which the markers will
	 *            be added.
	 */
	private void addNewMarkers(final IFileEditorInput fInput) {
		IProject project = fInput.getFile().getProject();

		// Don't add markers to resources in non AspectJ projects !
		try {
			if (project == null || !project.isOpen()
					|| !project.hasNature(AspectJUIPlugin.ID_NATURE))
				return;
		} catch (CoreException e) {
		}

		String path = fInput.getFile().getRawLocation().toOSString(); // Copes
																	  // with
																	  // linked
																	  // src
																	  // folders.
		// retrieve a map of line numbers to Vectors containing StructureNode
		// objects
		// Ask for the detailed version of the map (by specifying 'true') which
		// maps
		// line numbers to nodes representing advice (rather than just nodes
		// representing
		// aspects).
		Map m = StructureModelUtil.getLinesToAspectMap(path, true);

		if (m != null) {
			// iterate through the line numbers in the map
			Set keys = m.keySet();
			Iterator i = keys.iterator();
			while (i.hasNext()) {
				Object o = i.next();
				final Integer linenumberInt = (Integer) o;

				// for that line, go through all the advice in effect
				final Vector v = (Vector) m.get(o);
				// One runnable per line advised adds the appropriate marker
				IWorkspaceRunnable r = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) {
						try {
							boolean sameType = true;
							boolean runtimeTst = false;

							// Apples or Oranges?
							NodeHolder nh = (NodeHolder) v.get(0);
							//							if
							// (nh.node.getKind()!=IProgramElement.Kind.ADVICE)
							// {
							//								// probably an intertype decl - SIAN....
							//								System.err.println(">ITD:"+nh.node.toString());
							//								
							//							} else {
							// advice nodes
							if (v.size() > 1) {
								NodeHolder first = (NodeHolder) v.get(0);
								String adviceType = first.node.getExtraInfo() == null ? null
										: first.node.getExtraInfo()
												.getExtraAdviceInformation();
								for (Iterator iter = v.iterator(); iter
										.hasNext();) {
									NodeHolder element = (NodeHolder) iter
											.next();
									runtimeTst = runtimeTst
											|| element.runtimeTest;
									if (adviceType != null) {
										if (element.node.getExtraInfo() == null) {
											sameType = false;
										} else {
											sameType = sameType
													&& adviceType
															.equals(element.node
																	.getExtraInfo()
																	.getExtraAdviceInformation());
										}
									} else {
										sameType = sameType
												&& element.node.getExtraInfo() == null;
									}
								}
							} else if (v.size() == 1) {
								runtimeTst = ((NodeHolder) v.get(0)).runtimeTest;
							}
							final boolean runtimeTest = runtimeTst;
							final boolean useDefaultAdviceMarker = !sameType;
							for (int j = 0; j < v.size(); j++) {
								// sn will represent the advice in affect at the
								// given line.
								final NodeHolder noddyHolder = (NodeHolder) v
										.get(j);
								final IProgramElement sn = noddyHolder.node;
								final IResource ir = (IResource) fInput
										.getFile();
								// Thread required to ensure marker created and
								// set atomically
								// (and so reflected correctly in the ruler).

								ISourceLocation sl_sn = sn.getSourceLocation();
								String label = sn.toLinkLabelString();
								// SIAN: RUNTIMETEST local var gives you whether
								// to put the ? on
								// SIAN:
								// sn.getAdviceInfo().getExtraAdviceInformation()
								// will
								//       tell you if its
								// before/after/afterreturning/afterthrowing/around
								// advice

								String adviceType = sn.getName();
								IMarker marker = createMarker(linenumberInt,
										runtimeTest, ir, sn,
										useDefaultAdviceMarker,
										noddyHolder.runtimeTest);

								// Crude format is "FFFF:::NNNN:::NNNN:::NNNN"
								// Filename:::StartLine:::EndLine:::ColumnNumber

								// Grab the location of the pointcut
								ISourceLocation sLoc2 = sn.getSourceLocation();
								// was asn
								marker.setAttribute(IMarker.PRIORITY,
										IMarker.PRIORITY_HIGH);
								marker
										.setAttribute(
												AspectJUIPlugin.SOURCE_LOCATION_ATTRIBUTE,
												sLoc2.getSourceFile()
														.getAbsolutePath()
														+ ":::"
														+ sLoc2.getLine()
														+ ":::"
														+ sLoc2.getEndLine()
														+ ":::"
														+ sLoc2.getColumn());

								//									System.err.println(
								//									"Creating advicemarker at line="+
								// linenumberInt.intValue() +
								//									" advice="+ sn.getName() +
								//									" sourcefilepath=" + sLoc2.getSourceFile() +
								//								    " line="+ sLoc2.getLine());

							}
							//							}
						} catch (CoreException ce) {
							AspectJUIPlugin.getDefault().getErrorHandler()
									.handleError(
											"Exception creating advice marker",
											ce);
						}
					}
				};

				// Kick off the thread to add the marker...
				try {
					AspectJUIPlugin.getWorkspace().run(r, null);
				} catch (CoreException cEx) {
					AspectJUIPlugin.getDefault().getErrorHandler().handleError(
							"AJDT Error adding advice markers", cEx);
				}
			}
		}
		// Keep note that we are now up to date
		markersNeedUpdating = false;
	}

	public void dispose() {
		AJDTEventTrace.generalEvent("Disposing editor for:" + getTitle());
		IEditorInput input = getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fInput = (IFileEditorInput) input;
			AJDTEventTrace.editorClosed(fInput.getFile());
			activeEditorList.remove(this);

			try {
				ICompilationUnit unit = AJCompilationUnitManager.INSTANCE.getAJCompilationUnitFromCache(fInput.getFile());
				if (unit != null)
					unit.discardWorkingCopy();
			} catch (JavaModelException e) {
			}

		}
		removeAJDTMarkers(currentFileInput);
		super.dispose();
	}

	/**
	 * Used by the builder. This method forces content outlines to update. I've
	 * added this method because we now have a way to do a build without
	 * creating the ASM. If we don't create the ASM then the outlines don't get
	 * refreshed (there is no callback to update them) - so from the builder we
	 * call this function. It means we keep track of active editors, something
	 * we have not done previously.
	 * 
	 * We usually only update editors related to the specified project.
	 * 
	 * IF YOU PASS NULL, WE WILL UPDATE ALL THE EDITORS FOR ALL PROJECTS
	 */
	public static void forceEditorUpdates(final IProject project) {
		final Iterator editorIter = activeEditorList.iterator();
		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					while (editorIter.hasNext()) {
						AspectJEditor ajed = (AspectJEditor) editorIter.next();
						IEditorInput iei = ajed.getEditorInput();
						boolean updateThisEditor = true;
						if (project != null
								&& (iei instanceof IFileEditorInput)) {
							IFileEditorInput ifei = (IFileEditorInput) iei;
							if (!(ifei.getFile().getProject().getName()
									.equals(project.getName())))
								updateThisEditor = false;
						}
						if (updateThisEditor) {
							AJDTEventTrace
									.generalEvent("Forcing update of outline page for editor: "
											+ ajed.getEditorInput().getName());
							try {
								ajed.contentOutlinePage.update();
							} catch (Exception e) {
								AJDTEventTrace
										.generalEvent("Unexpected exception updating editor outline "
												+ e.toString());
							}
						}
					}
				} catch (Exception e) {
				}
			}
		});
	}

	/**
	 * Sian - added as part of the fix for bug 70658
	 * Force marker updates for any editors open on files in the project,
	 * or on all editors if project is null.
	 * @param project
	 */
	public static void forceMarkerUpdates(final IProject project) {
		final Iterator editorIter = activeEditorList.iterator();
		AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
			public void run() {
				try {
					while (editorIter.hasNext()) {
						AspectJEditor ajed = (AspectJEditor) editorIter.next();
						IEditorInput iei = ajed.getEditorInput();
						boolean updateThisEditor = true;
						if (project != null
								&& (iei instanceof IFileEditorInput)) {
							IFileEditorInput ifei = (IFileEditorInput) iei;
							if (!(ifei.getFile().getProject().getName()
									.equals(project.getName())))
								updateThisEditor = false;
						}
						if (updateThisEditor) {
							ajed.forceUpdateOfAdviceMarkers();							
						}
					}
				} catch (Exception e) {
				}
			}
		});
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
		//System.out.println( "Focus given to: " + getEditorInput( ).getName()
		// );
		IEditorInput input = getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fInput = (IFileEditorInput) input;
			AspectJUIPlugin.getDefault().setCurrentProject(
					fInput.getFile().getProject());
			// Ensure any advice markers are created since they are not
			// persisted - but
			// in this case (unlike when opening a new editor) they are likely
			// to
			// be correct already.
			updateAdviceMarkers(fInput);
		}
		super.setFocus();
	}

	public void gotoMarker(IMarker marker) {
		super.gotoMarker(marker);
	}

	protected void initializeEditor() {
		super.initializeEditor();
		IPreferenceStore store = this.getPreferenceStore();
		AspectJTextTools textTools = new AspectJTextTools(store);
		fAJSourceViewerConfiguration = new AJSourceViewerConfiguration(
				textTools, this, IJavaPartitions.JAVA_PARTITIONING);
		setSourceViewerConfiguration(fAJSourceViewerConfiguration);
	}

	/**
	 * @param linenumberInt
	 * @param runtimeTest
	 * @param ir
	 * @param programElement
	 * @param useDefaultAdviceMarker
	 * @return the IMarker created
	 * @throws CoreException
	 */
	private IMarker createMarker(final Integer linenumberInt,
			final boolean runtimeTest, final IResource ir,
			IProgramElement programElement, boolean useDefaultAdviceMarker,
			boolean nodeRuntimeTest) throws CoreException {
		String label = programElement.toLinkLabelString();
		String adviceType = "";
		if (programElement.getExtraInfo() != null) {
			adviceType = programElement.getExtraInfo()
					.getExtraAdviceInformation();
		}
		IMarker marker;
		if (useDefaultAdviceMarker) {
			if (runtimeTest) {
				marker = ir
						.createMarker(IAJModelMarker.DYNAMIC_ADVICE_MARKER);
			} else {
				if (adviceType == "") {
					marker = ir.createMarker(IAJModelMarker.DECLARATION_MARKER);
				} else {
					marker = ir.createMarker(IAJModelMarker.ADVICE_MARKER);
				}
			}
		} else if (adviceType.equals("before")) {
			if (runtimeTest) {
				marker = ir
						.createMarker(IAJModelMarker.DYNAMIC_BEFORE_ADVICE_MARKER);
			} else {
				marker = ir
						.createMarker(IAJModelMarker.BEFORE_ADVICE_MARKER);
			}
		} else if (adviceType.equals("around")) {
			if (runtimeTest) {
				marker = ir
						.createMarker(IAJModelMarker.DYNAMIC_AROUND_ADVICE_MARKER);
			} else {
				marker = ir
						.createMarker(IAJModelMarker.AROUND_ADVICE_MARKER);
			}
		} else if (adviceType.startsWith("after")) {
			if (runtimeTest) {
				marker = ir
						.createMarker(IAJModelMarker.DYNAMIC_AFTER_ADVICE_MARKER);
			} else {
				marker = ir
						.createMarker(IAJModelMarker.AFTER_ADVICE_MARKER);
			}
		} else {
			// It's an Intertype Declaration
			marker = ir.createMarker(IAJModelMarker.ITD_MARKER);
		}
		marker.setAttribute(IMarker.LINE_NUMBER, linenumberInt.intValue());
		if (nodeRuntimeTest) {
			label = label
					+ " "
					+ AspectJUIPlugin
							.getResourceString("AspectJEditor.runtimetest");
		}
		marker.setAttribute(IMarker.MESSAGE, label);
		return marker;
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

			//		//remove refactoring submenu
			//		IMenuManager sourceMM =
			// menu.findMenuUsingPath("org.eclipse.jdt.ui.source.menu");
			//		
			//		//remove all actions between import and codegroup in source
			// submenu
			//		IContributionItem[] items = sourceMM.getItems();
			//		boolean shouldberemoved = false;
			//		for (int i = 0; i < items.length; i++) {
			//			IContributionItem item = items[i];
			//			if(shouldberemoved){
			//				if ("codeGroup".equals(item.getId()))
			//					shouldberemoved = false;
			//				else
			//					sourceMM.remove(item);
			//			} else {
			//				shouldberemoved = "importGroup".equals(item.getId());
			//			}
			//		}

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
}