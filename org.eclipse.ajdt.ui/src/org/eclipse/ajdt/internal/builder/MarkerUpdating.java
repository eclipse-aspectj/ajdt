/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 *               Matt Chapman - add source of advice markers
 ******************************************************************************/
package org.eclipse.ajdt.internal.builder;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.IProgramElement.Kind;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.core.CoreUtils;
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
 * Class responsible for advice and declaration markers.  
 * Updates the markers for a given project when it is built.
 */
public class MarkerUpdating {

	/**
	 * Delete the advice markers for a project
	 */
	public static void deleteAllMarkers(final IProject project) {	
		try {
			AspectJPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					// Delete all the existing markers
					try {
						project.deleteMarkers(IAJModelMarker.ADVICE_MARKER,
								true, IResource.DEPTH_INFINITE);
						project.deleteMarkers(IAJModelMarker.SOURCE_ADVICE_MARKER,
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
	public static void addNewMarkers(final IProject project) {	
		try {
			AspectJPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					try {
						project.accept(new IResourceVisitor(){
							public boolean visit(IResource resource) {
								if(resource instanceof IFolder) {
									return true;
								} else if (resource instanceof IFile) {
									if (CoreUtils.ASPECTJ_SOURCE_FILTER.accept(resource.getName())) {
										addMarkersToFile((IFile)resource);
									}
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
	private static void addMarkersToFile(final IFile file) {	
		IProject project = file.getProject();

		// Don't add markers to resources in non AspectJ projects !
		if (project == null || !project.isOpen()
				|| !AspectJPlugin.isAJProject(project)) {
			return;
		}
			
		// Copes with linked src folders
		String path = file.getRawLocation().toOSString();
		
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
						createMarker(linenumberInt.intValue(),
								runtimeTest, file, sn,
								useDefaultAdviceMarker,
								noddyHolder.runtimeTest,
								noddyHolder.adviceType);
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
	private static IMarker createMarker(int linenumberInt,
			final boolean runtimeTest, final IResource ir,
			IProgramElement programElement, boolean useDefaultAdviceMarker,
			boolean nodeRuntimeTest, String adviceType) throws CoreException {

		String label = programElement.toLinkLabelString();
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
		} else if (adviceType.startsWith("advises")) {
			String subType = adviceType.substring("advises".length());
			if (subType.startsWith("before")) {
				marker = ir
					.createMarker(IAJModelMarker.SOURCE_BEFORE_ADVICE_MARKER);
			} else if (subType.startsWith("after")) {
				marker = ir
					.createMarker(IAJModelMarker.SOURCE_AFTER_ADVICE_MARKER);
			} else if (subType.startsWith("around")) {
				marker = ir
				.createMarker(IAJModelMarker.SOURCE_AROUND_ADVICE_MARKER);
			} else {
				marker = ir
					.createMarker(IAJModelMarker.SOURCE_ADVICE_MARKER);
			}
			// Store the accessibility and kind in an attribute, so we can use
			// appropriate icons when we later populate the marker context menu
			// TODO: query this information when needed instead
			Kind kind = programElement.getKind();
			Accessibility acc = programElement.getAccessibility();
			char markerKind='?';
			if (kind == Kind.CODE) {
				markerKind='C';
			} else if ((kind == Kind.METHOD) || (kind == Kind.CONSTRUCTOR)) {
				markerKind='M';
			} else if (kind == Kind.FIELD) {
				markerKind='F';
			} else if (kind == Kind.ASPECT) {
				markerKind='A';
			}
			char markerAcc='?';
			if (acc == Accessibility.PUBLIC) {
				markerAcc='G';
			} else if (acc == Accessibility.PROTECTED) {
				markerAcc='Y';
			} else if (acc == Accessibility.PACKAGE) {
				markerAcc='B';
			} else if (acc == Accessibility.PRIVATE) {
				markerAcc='R';
			}
			marker.setAttribute(AspectJUIPlugin.ACCKIND_ATTRIBUTE,""+markerKind+markerAcc);
		} else {
			// It's an Intertype Declaration
			marker = ir.createMarker(IAJModelMarker.ITD_MARKER);
		}
		marker.setAttribute(IMarker.LINE_NUMBER, linenumberInt);
		if (nodeRuntimeTest) {
			label = label
					+ " "
					+ AspectJUIPlugin
							.getResourceString("AspectJEditor.runtimetest");
		}
		marker.setAttribute(IMarker.MESSAGE, label);
		marker.setAttribute(IMarker.PRIORITY,
				IMarker.PRIORITY_HIGH);
		ISourceLocation sLoc2 = programElement.getSourceLocation();
		
		// Crude format is "FFFF:::NNNN:::NNNN:::NNNN"
		// Filename:::StartLine:::EndLine:::ColumnNumber
		marker.setAttribute(
						AspectJUIPlugin.SOURCE_LOCATION_ATTRIBUTE,
						sLoc2.getSourceFile()
								.getAbsolutePath()
								+ ":::"
								+ sLoc2.getLine()
								+ ":::"
								+ sLoc2.getEndLine()
								+ ":::"
								+ sLoc2.getColumn());
		return marker;
	}
	
}
