/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.internal.builder;

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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Aspect responsible for advice and declaration markers.  
 * Updates the markers for a given project when it is built.
 */
public aspect MarkerUpdating {

	private pointcut buildPerformed(Builder builder): 
		execution(* Builder.build(..)) && target(builder);
	
	before(Builder builder): buildPerformed(builder) {
		IProject project = builder.getProject();
		deleteAllMarkers(project);		
	}
	
	after(Builder builder) returning: buildPerformed(builder) {
		IProject project = builder.getProject();
		addNewMarkers(project);
	}

	/**
	 * Delete the advice markers for a project
	 */
	private void deleteAllMarkers(final IProject project) {	
		try {
			AspectJUIPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					// Delete all the existing markers
					try {
						project.deleteMarkers(IAJModelMarker.ADVICE_MARKER,
								true, IResource.DEPTH_INFINITE);
						project.deleteMarkers(
								IAJModelMarker.DECLARATION_MARKER, true,
								IResource.DEPTH_INFINITE);
					} catch (CoreException cEx) {}					
				}
			}, null);
		} catch (CoreException coreEx) {}
	}	
	
	/**
	 * Add new advice markers to a project
	 */
	private void addNewMarkers(final IProject project) {	
		try {
			AspectJUIPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					try {
						project.accept(new IResourceVisitor(){
							public boolean visit(IResource resource) {
								if(resource instanceof IFolder) {
									return true;
								} else if (resource instanceof IFile) {
									addMarkersToFile((IFile)resource);
									return false;
								} else {
									return true;
								}
							}
						});
					} catch (CoreException coreEx) {}
				}
			}, null);
		} catch (CoreException coreEx) {}
	}

	
	/**
	 * Add markers to a file
	 */
	private void addMarkersToFile(final IFile file) {	

		IProject project = file.getProject();

		// Don't add markers to resources in non AspectJ projects !
		try {
			if (project == null || !project.isOpen()
					|| !project.hasNature(AspectJUIPlugin.ID_NATURE))
				return;
		} catch (CoreException e) {
		}

		String path = file.getRawLocation().toOSString(); // Copes
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
				try {
					boolean sameType = true;
					boolean runtimeTst = false;

					NodeHolder nh = (NodeHolder) v.get(0);
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
						// Thread required to ensure marker created and
						// set atomically
						// (and so reflected correctly in the ruler).

						ISourceLocation sl_sn = sn.getSourceLocation();
						String label = sn.toLinkLabelString();
						String adviceType = sn.getName();
						IMarker marker = createMarker(linenumberInt,
								runtimeTest, file, sn,
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

					}
				} catch (CoreException ce) {
					AspectJUIPlugin.getDefault().getErrorHandler()
							.handleError(
									"Exception creating advice marker",
									ce);
				}		
			}
		}
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
