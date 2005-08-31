/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 *               Matt Chapman - add source of advice markers
 ******************************************************************************/
package org.eclipse.ajdt.internal.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.TimerLogEvent;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;


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
		AJLog.logStart(TimerLogEvent.ADD_MARKERS);
		int numMarkers = 0;
		AJModel ajModel = AJModel.getInstance();
		// Get all the relationships and sort by compilation unit..
		List allRelationships = ajModel.getAllRelationships(project, new AJRelationshipType[] {
				AJRelationshipManager.ADVISED_BY, 
				AJRelationshipManager.ADVISES, 
				AJRelationshipManager.ANNOTATED_BY, 
				AJRelationshipManager.ANNOTATES, 
				AJRelationshipManager.DECLARED_ON, 
				AJRelationshipManager.ASPECT_DECLARATIONS,
				AJRelationshipManager.SOFTENS,
				AJRelationshipManager.SOFTENED_BY});
		Map cUsToListsOfRelationships = new HashMap();
		for (Iterator iter = allRelationships.iterator(); iter.hasNext();) {
			AJRelationship relationship = (AJRelationship) iter.next();
			IJavaElement source = relationship.getSource();
			ICompilationUnit parentCU = (ICompilationUnit)source.getAncestor(IJavaElement.COMPILATION_UNIT);
			if(parentCU != null) {
				if(cUsToListsOfRelationships.get(parentCU) instanceof List) {
					((List)cUsToListsOfRelationships.get(parentCU)).add(relationship);
				} else {
					List relationshipsForCU = new ArrayList();
					relationshipsForCU.add(relationship);
					cUsToListsOfRelationships.put(parentCU, relationshipsForCU);
				}
			}
		}
		// For each compilation unit sort the relationships by line number..
		Set affectedCompilationUnits = cUsToListsOfRelationships.keySet();
		for (Iterator iter = affectedCompilationUnits.iterator(); iter
				.hasNext();) {
			ICompilationUnit cu = (ICompilationUnit)iter.next();
			if(cu.getResource().exists()) {
				List relationships = (List) cUsToListsOfRelationships.get(cu);
				Map lineNumberToRelationships = new HashMap();
				for (Iterator iterator = relationships.iterator(); iterator
						.hasNext();) {
					AJRelationship relationship = (AJRelationship) iterator.next();
					IJavaElement source = relationship.getSource();
					Integer lineNumber = new Integer(ajModel.getJavaElementLineNumber(source));
					if(lineNumberToRelationships.get(lineNumber) instanceof List) {
						((List)lineNumberToRelationships.get(lineNumber)).add(relationship);
					} else {
						List relationshipsForLine = new ArrayList();
						relationshipsForLine.add(relationship);
						lineNumberToRelationships.put(lineNumber, relationshipsForLine);
					}				
				}
				Set lineNumbers = lineNumberToRelationships.keySet();
				// Create one marker for each affected line
				for (Iterator iterator = lineNumbers.iterator(); iterator.hasNext();) {
					numMarkers++;
					Integer lineNum = (Integer) iterator.next();
					List relationshipsForLine = (List) lineNumberToRelationships.get(lineNum);
					createMarker(lineNum.intValue(), cu.getResource(), relationshipsForLine);
				}
			}
		}
		AJLog.logEnd(TimerLogEvent.ADD_MARKERS,numMarkers + " markers");
	}
		


	/**
	 * Create AspectJ markers representing all the given relationships
	 * @param lineNumber
	 * @param resource
	 * @param relationships
	 */
	private static void createMarker(int lineNumber, IResource resource, List relationships) {
		String markerType = null;
		boolean runtimeTest = false;
		// Work out whether we need a runtime test marker or not
		for (Iterator iter = relationships.iterator(); iter.hasNext();) {
			AJRelationship relationship = (AJRelationship) iter.next();
			runtimeTest = runtimeTest || relationship.hasRuntimeTest();
		}
		// Work out what marker type to use (all need to be the same due to overlapping problems)
		for (Iterator iter = relationships.iterator(); iter.hasNext();) {
			AJRelationship relationship = (AJRelationship) iter.next();
			String markerTypeForRelationship = getMarkerTypeForRelationship(relationship, runtimeTest);
			if(markerType == null) {
				markerType = markerTypeForRelationship;
			} else if(!markerType.equals(markerTypeForRelationship)){
				markerType = getCombinedMarkerType(markerType, markerTypeForRelationship, runtimeTest);
			}
		}
		// Create the marker
		try {
			IMarker marker = resource.createMarker(markerType);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			String label;
			if(relationships.size() == 1) {
				label = getMarkerLabel((AJRelationship)relationships.get(0));
			} else {
				label = getMultipleMarkersLabel(relationships.size());
			}
			marker.setAttribute(IMarker.MESSAGE, label);
			marker.setAttribute(IMarker.PRIORITY,
					IMarker.PRIORITY_HIGH);
		} catch (CoreException e) {
		}		
	}

	private static String getMultipleMarkersLabel(int number) {
		return number + " " + UIMessages.AspectJMarkersAtLine; //$NON-NLS-1$	
	}

	/**
	 * Get a label for the given relationship
	 * @param relationship
	 * @return
	 */
	private static String getMarkerLabel(AJRelationship relationship) {		
		return relationship.getRelationship().getDisplayName()
				+ " "
				+ AJModel.getInstance().getJavaElementLinkName(
						relationship.getTarget()) 
						+ (relationship.hasRuntimeTest() 
								? " " + UIMessages.AspectJEditor_runtimetest 
										: "") ;
	}

	/**
	 * Get the marker type that should be used for the given relationship
	 * @param relationship
	 * @param runtimeTest
	 * @return
	 */
	private static String getMarkerTypeForRelationship(
			AJRelationship relationship, boolean runtimeTest) {
		IJavaElement source = relationship.getSource();
		IJavaElement target = relationship.getTarget();
		AJRelationshipType type = relationship.getRelationship();
		if (type.equals(AJRelationshipManager.ADVISED_BY)) {
			if (target instanceof AdviceElement) {
				try {
					IProgramElement.ExtraInformation extraInfo = ((AdviceElement) target)
							.getAJExtraInformation();
					if (extraInfo.getExtraAdviceInformation() != null) {
						if (extraInfo.getExtraAdviceInformation().equals(
								"before")) {
							if (runtimeTest) {
								return IAJModelMarker.DYNAMIC_BEFORE_ADVICE_MARKER;
							} else {
								return IAJModelMarker.BEFORE_ADVICE_MARKER;
							}
						} else if (extraInfo.getExtraAdviceInformation()
								.equals("around")) {
							if (runtimeTest) {
								return IAJModelMarker.DYNAMIC_AROUND_ADVICE_MARKER;
							} else {
								return IAJModelMarker.AROUND_ADVICE_MARKER;
							}
						} else {
							if (runtimeTest) {
								return IAJModelMarker.DYNAMIC_AFTER_ADVICE_MARKER;
							} else {
								return IAJModelMarker.AFTER_ADVICE_MARKER;
							}
						}
					}
				} catch (JavaModelException jme) {
				}
			}
			if (runtimeTest) {
				return IAJModelMarker.DYNAMIC_ADVICE_MARKER;
			} else {
				return IAJModelMarker.ADVICE_MARKER;
			}
		} else if (type.equals(AJRelationshipManager.ADVISES)) {
			if (source instanceof AdviceElement) {
				try {
					IProgramElement.ExtraInformation extraInfo = ((AdviceElement) source)
							.getAJExtraInformation();
					if (extraInfo.getExtraAdviceInformation() != null) {
						if (extraInfo.getExtraAdviceInformation().equals(
								"before")) {
							if (runtimeTest) {
								return IAJModelMarker.SOURCE_DYNAMIC_BEFORE_ADVICE_MARKER;
							} else {
								return IAJModelMarker.SOURCE_BEFORE_ADVICE_MARKER;
							}
						} else if (extraInfo.getExtraAdviceInformation()
								.equals("around")) {
							if (runtimeTest) {
								return IAJModelMarker.SOURCE_DYNAMIC_AROUND_ADVICE_MARKER;
							} else {
								return IAJModelMarker.SOURCE_AROUND_ADVICE_MARKER;
							}
						} else {
							if (runtimeTest) {
								return IAJModelMarker.SOURCE_DYNAMIC_AFTER_ADVICE_MARKER;
							} else {
								return IAJModelMarker.SOURCE_AFTER_ADVICE_MARKER;
							}
						}
					}
				} catch (JavaModelException jme) {
				}
			}
			if (runtimeTest) {
				return IAJModelMarker.SOURCE_DYNAMIC_ADVICE_MARKER;
			} else {
				return IAJModelMarker.SOURCE_ADVICE_MARKER;
			}
		} else if (type.equals(AJRelationshipManager.ASPECT_DECLARATIONS)
				|| type.equals(AJRelationshipManager.ANNOTATED_BY)
				|| type.equals(AJRelationshipManager.SOFTENED_BY)) {
			return IAJModelMarker.ITD_MARKER;
		} else if (type.equals(AJRelationshipManager.DECLARED_ON)
				|| type.equals(AJRelationshipManager.ANNOTATES)
				|| type.equals(AJRelationshipManager.SOFTENS)) {
			return IAJModelMarker.SOURCE_ITD_MARKER;
		}
		return IAJModelMarker.ADVICE_MARKER;
	}

	/**
	 * Two or more markers on the same line - get the most approriate marker type to display
	 * @param firstMarkerType
	 * @param secondMarkerType
	 * @return
	 */
	private static String getCombinedMarkerType(String firstMarkerType, String secondMarkerType, boolean runtimeTest) {
		if (firstMarkerType.indexOf("source") != -1 && secondMarkerType.indexOf("source") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
			return runtimeTest ? IAJModelMarker.SOURCE_DYNAMIC_ADVICE_MARKER : IAJModelMarker.SOURCE_ADVICE_MARKER;
		} else if (firstMarkerType.indexOf("source") != -1 || secondMarkerType.indexOf("source") != -1) { //$NON-NLS-1$ //$NON-NLS-2$ 
			return runtimeTest ? IAJModelMarker.DYNAMIC_SOURCE_AND_TARGET_MARKER : IAJModelMarker.SOURCE_AND_TARGET_MARKER;
		} else {
			return runtimeTest ? IAJModelMarker.DYNAMIC_ADVICE_MARKER : IAJModelMarker.ADVICE_MARKER;
		}
	}

	
}
