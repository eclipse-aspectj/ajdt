/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.ajdt.ui.visualiser.NodeHolder;
import org.eclipse.ajdt.ui.visualiser.StructureModelUtil;
import org.eclipse.ajdt.internal.builder.Builder;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

/**
 * Aspect responsible for advice and itd markers 
 */
public aspect PerEditorMarkerUpdating {

	private IFileEditorInput currentFileInput;
	
	private pointcut editorGivenNewInput():
		execution (* AspectJEditor.doSetInput(..));

	private pointcut editorGivenFocus(): 
		execution(* AspectJEditor.setFocus());
	
	private pointcut buildPerformed(): execution(* Builder.build(..));
	
	private pointcut editorClosed(): execution(* AspectJEditor.dispose());
		
	
	after(AspectJEditor editor) returning: (editorGivenFocus() || editorGivenNewInput()) && target(editor) {
		if(!(editor.getEditorInput().equals(currentFileInput))) {
			currentFileInput = (IFileEditorInput)editor.getEditorInput();
			updateAdviceMarkers(currentFileInput);
		}
	}
	
	after() returning: buildPerformed(){
		forceMarkerUpdates(AspectJUIPlugin.getDefault().getCurrentProject());
	}
	
	before(AspectJEditor editor): (editorClosed() || editorGivenNewInput()) && target(editor) {
		if (editor.getEditorInput() != null) {
			removeAJDTMarkers((IFileEditorInput)editor.getEditorInput());
		}
	}

	
	/**
	 * Sian - added as part of the fix for bug 70658
	 * Force marker updates for any editors open on files in the project,
	 * or on all editors if project is null.
	 * @param project
	 */
	private void forceMarkerUpdates(final IProject project) {
		Set activeEditorList = AspectJEditor.getActiveEditorList();
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
							updateAdviceMarkers((IFileEditorInput)ajed.getEditorInput());							
						}
					}
				} catch (Exception e) {
				}
			}
		});
	}
	
	
	/**
	 * Adds the advice markers for a file to the left hand gutter. It kicks off
	 * a thread that does a delete then adds all the new markers.
	 */
	private void updateAdviceMarkers(final IFileEditorInput fInput) {
		if (fInput == null) {
			AJDTEventTrace
					.generalEvent("AspectJEditor: FileEditorInput is null for editor with title ("
							+ fInput.getName() + "): Cannot update markers on it");
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

	
}
