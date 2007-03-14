/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 *               Matt Chapman - add source of advice markers
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.markers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.TimerLogEvent;
import org.eclipse.ajdt.core.javaelements.AJInjarElement;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJProjectModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.core.model.ModelComparison;
import org.eclipse.ajdt.internal.ui.diff.ChangesView;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;

/**
 * Class responsible for advice and declaration markers. Updates the markers for
 * a given project when it is built.
 */
public class MarkerUpdating {

	private static final String CHANGED_ADVICE_ANNOTATION_TYPE = "org.eclipse.ajdt.changedAdvice"; //$NON-NLS-1$
	
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
						project.deleteMarkers(
								IAJModelMarker.SOURCE_ADVICE_MARKER, true,
								IResource.DEPTH_INFINITE);
						project.deleteMarkers(
								IAJModelMarker.DECLARATION_MARKER, true,
								IResource.DEPTH_INFINITE);
						project.deleteMarkers(IAJModelMarker.CUSTOM_MARKER,
								true, IResource.DEPTH_INFINITE);
						project.deleteMarkers(
								IAJModelMarker.CHANGED_ADVICE_MARKER, true,
								IResource.DEPTH_INFINITE);
					} catch (CoreException cEx) {
					}
				}
			}, null);
		} catch (CoreException coreEx) {
		}
	}

	/**
	 * Add new advice markers to a project
	 */
	public static void addNewMarkers(final IProject project) {
		AJLog.logStart(TimerLogEvent.ADD_MARKERS);
		int numMarkers = 0;
		AJProjectModel ajProjectModel = AJModel.getInstance()
				.getModelForProject(project);
		if (ajProjectModel == null) {
			AJLog.logEnd(AJLog.BUILDER, TimerLogEvent.ADD_MARKERS,
					"0 markers (null project model)"); //$NON-NLS-1$
			return;
		}
		// Get all the relationships and sort by compilation unit..
		List allRelationships = ajProjectModel
				.getAllRelationships(new AJRelationshipType[] {
						AJRelationshipManager.ADVISED_BY,
						AJRelationshipManager.ADVISES,
						AJRelationshipManager.ANNOTATED_BY,
						AJRelationshipManager.ANNOTATES,
						AJRelationshipManager.DECLARED_ON,
						AJRelationshipManager.ASPECT_DECLARATIONS,
						AJRelationshipManager.SOFTENS,
						AJRelationshipManager.SOFTENED_BY });
		List allOtherRels = ajProjectModel
				.getOtherProjectAllRelationships(new AJRelationshipType[] {
						AJRelationshipManager.ADVISED_BY,
						AJRelationshipManager.ADVISES,
						AJRelationshipManager.ANNOTATED_BY,
						AJRelationshipManager.ANNOTATES,
						AJRelationshipManager.DECLARED_ON,
						AJRelationshipManager.ASPECT_DECLARATIONS,
						AJRelationshipManager.SOFTENS,
						AJRelationshipManager.SOFTENED_BY });
		if (allOtherRels != null) {
			allRelationships.addAll(allOtherRels);
		}

		// get list of elements that have been added/removed in this build
		AJProjectModel prev = null;
		AJProjectModel curr = AJModel.getInstance().getModelForProject(project);
		if (isChangedAdviceAnnotationActive()) {
			String ref = ChangesView.getReferencePoint(project);
			if (ref.equals(ChangesView.REF_LAST_FULL)) {
				prev = AJModel.getInstance().getPreviousFullBuildModel(project);
			} else if (ref.equals(ChangesView.REF_LAST_INC)) {
				prev = AJModel.getInstance().getPreviousModel(project);
			} else { // look for a map file
				IPath mapFile = project.getFile(ref).getLocation();
				prev = new AJProjectModel(project);
				boolean success = prev.loadModel(mapFile);
				if (!success) {
					prev = null;
				}
			}
		}

		Set changedEls = new HashSet();
		if (prev != null) {
			List diff[] = new ModelComparison(false)
					.compareProjects(prev, curr);
			if (diff != null) {
				for (Iterator iterator = diff[0].iterator(); iterator.hasNext();) {
					AJRelationship rel = (AJRelationship) iterator.next();
					changedEls.add(rel.getSource());
					changedEls.add(rel.getTarget());
				}
				for (Iterator iterator = diff[1].iterator(); iterator.hasNext();) {
					AJRelationship rel = (AJRelationship) iterator.next();
					changedEls.add(rel.getSource());
					changedEls.add(rel.getTarget());
				}
			}
		}

		Map cUsToListsOfRelationships = new HashMap();
		for (Iterator iter = allRelationships.iterator(); iter.hasNext();) {
			AJRelationship relationship = (AJRelationship) iter.next();
			IJavaElement source = relationship.getSource();
			ICompilationUnit parentCU = (ICompilationUnit) source
					.getAncestor(IJavaElement.COMPILATION_UNIT);
			if (parentCU != null) {
				if (cUsToListsOfRelationships.get(parentCU) instanceof List) {
					((List) cUsToListsOfRelationships.get(parentCU))
							.add(relationship);
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
			ICompilationUnit cu = (ICompilationUnit) iter.next();
			if (cu.getResource() != null && cu.getResource().exists()) {
				List relationships = (List) cUsToListsOfRelationships.get(cu);
				Map lineNumberToRelationships = new HashMap();
				for (Iterator iterator = relationships.iterator(); iterator
						.hasNext();) {
					AJRelationship relationship = (AJRelationship) iterator
							.next();
					IJavaElement source = relationship.getSource();
					Integer lineNumber = new Integer(ajProjectModel
							.getJavaElementLineNumber(source));
					if (lineNumberToRelationships.get(lineNumber) instanceof List) {
						((List) lineNumberToRelationships.get(lineNumber))
								.add(relationship);
					} else {
						List relationshipsForLine = new ArrayList();
						relationshipsForLine.add(relationship);
						lineNumberToRelationships.put(lineNumber,
								relationshipsForLine);
					}
				}
				Set lineNumbers = lineNumberToRelationships.keySet();
				// Create one marker for each affected line
				for (Iterator iterator = lineNumbers.iterator(); iterator
						.hasNext();) {
					numMarkers++;
					Integer lineNum = (Integer) iterator.next();
					List relationshipsForLine = (List) lineNumberToRelationships
							.get(lineNum);
					createMarker(lineNum.intValue(), cu.getResource(),
							relationshipsForLine, changedEls);
				}
			}
		}

		AJLog.logEnd(AJLog.BUILDER, TimerLogEvent.ADD_MARKERS, numMarkers
				+ " markers"); //$NON-NLS-1$
	}

	public static boolean isChangedAdviceAnnotationActive() {
		AnnotationPreferenceLookup lookup = EditorsPlugin.getDefault()
				.getAnnotationPreferenceLookup();
		AnnotationPreference preference = lookup
				.getAnnotationPreference(CHANGED_ADVICE_ANNOTATION_TYPE);
		IPreferenceStore store = EditorsPlugin.getDefault()
				.getPreferenceStore();
		String key = preference.getVerticalRulerPreferenceKey();
		if (key != null && store.getBoolean(key)) {
			return true;
		}
		return false;
	}

	/**
	 * Create AspectJ markers representing all the given relationships
	 * 
	 * @param lineNumber
	 * @param resource
	 * @param relationships
	 */
	private static void createMarker(int lineNumber, IResource resource,
			List relationships, Set addedEls) {
		String markerType = null;
		boolean runtimeTest = false;
		// Work out whether we need a runtime test marker or not
		boolean newlyadded = false;
		if (addedEls.size() > 0) {
			for (Iterator iter = relationships.iterator(); iter.hasNext();) {
				AJRelationship relationship = (AJRelationship) iter.next();
				runtimeTest = runtimeTest || relationship.hasRuntimeTest();
				newlyadded = newlyadded
						|| addedEls.contains(relationship.getSource());
			}
		}
		// Work out what marker type to use (all need to be the same due to
		// overlapping problems unless some are custom markers)
		for (Iterator iter = relationships.iterator(); iter.hasNext();) {
			AJRelationship relationship = (AJRelationship) iter.next();
			String savedMarkerType = getSavedMarkerType(relationship);
			if (savedMarkerType != null) {
				// Create a marker of the saved type or don't create one..
				if (savedMarkerType.equals(AJMarkersDialog.NO_MARKERS)) {
					continue;
				} else {
					try {
						IMarker marker = resource
								.createMarker(IAJModelMarker.CUSTOM_MARKER);
						marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
						String label;
						label = getMarkerLabel((AJRelationship) relationships
								.get(0));
						marker.setAttribute(IMarker.MESSAGE, label);
						marker.setAttribute(IMarker.PRIORITY,
								IMarker.PRIORITY_HIGH);
						marker
								.setAttribute(
										CustomMarkerImageProvider.IMAGE_LOCATION_ATTRIBUTE,
										savedMarkerType);
					} catch (CoreException e) {
					}
					continue;
				}
			} else {
				String markerTypeForRelationship = getMarkerTypeForRelationship(
						relationship, runtimeTest);
				if (markerType == null) {
					markerType = markerTypeForRelationship;
				} else if (!markerType.equals(markerTypeForRelationship)) {
					markerType = getCombinedMarkerType(markerType,
							markerTypeForRelationship, runtimeTest);
				}
			}
		}
		// Create the marker
		if (markerType != null) {
			try {
				IMarker marker = resource.createMarker(markerType);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				String label;
				if (relationships.size() == 1) {
					label = getMarkerLabel((AJRelationship) relationships
							.get(0));
				} else {
					label = getMultipleMarkersLabel(relationships.size());
				}
				marker.setAttribute(IMarker.MESSAGE, label);
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);

				if (newlyadded) {
					// create highlight markers for advice which is new in this
					// build
					IMarker marker2 = resource
							.createMarker(IAJModelMarker.CHANGED_ADVICE_MARKER);
					marker2.setAttribute(IMarker.LINE_NUMBER, lineNumber);
					marker2.setAttribute(IMarker.MESSAGE,
							UIMessages.CrosscuttingChangedMarkerText);
				}

			} catch (CoreException e) {
			}
		}
	}

	private static String getMultipleMarkersLabel(int number) {
		return number + " " + UIMessages.AspectJMarkersAtLine; //$NON-NLS-1$	
	}

	/**
	 * Get a label for the given relationship
	 * 
	 * @param relationship
	 * @return
	 */
	private static String getMarkerLabel(AJRelationship relationship) {
		return relationship.getRelationship().getDisplayName()
				+ " " //$NON-NLS-1$
				+ AJModel.getInstance().getJavaElementLinkName(
						relationship.getTarget())
				+ (relationship.hasRuntimeTest() ? " " + UIMessages.AspectJEditor_runtimetest //$NON-NLS-1$
						: ""); //$NON-NLS-1$
	}

	/**
	 * Get the marker type that should be used for the given relationship
	 * 
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
			IProgramElement.ExtraInformation extraInfo = null;
			try {
				if (target instanceof AdviceElement) {
					extraInfo = ((AdviceElement) target)
							.getAJExtraInformation();
				} else if (target instanceof AJInjarElement) {
					extraInfo = ((AJInjarElement) target)
							.getAJExtraInformation();
				}
				if (extraInfo != null) {
					if (extraInfo.getExtraAdviceInformation() != null) {
						if (extraInfo.getExtraAdviceInformation().equals(
								"before")) { //$NON-NLS-1$
							if (runtimeTest) {
								return IAJModelMarker.DYNAMIC_BEFORE_ADVICE_MARKER;
							} else {
								return IAJModelMarker.BEFORE_ADVICE_MARKER;
							}
						} else if (extraInfo.getExtraAdviceInformation()
								.equals("around")) { //$NON-NLS-1$
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
				}
			} catch (JavaModelException jme) {
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
								"before")) { //$NON-NLS-1$
							if (runtimeTest) {
								return IAJModelMarker.SOURCE_DYNAMIC_BEFORE_ADVICE_MARKER;
							} else {
								return IAJModelMarker.SOURCE_BEFORE_ADVICE_MARKER;
							}
						} else if (extraInfo.getExtraAdviceInformation()
								.equals("around")) { //$NON-NLS-1$
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

	private static String getSavedMarkerType(AJRelationship relationship) {
		IJavaElement source;
		AJRelationshipType relationshipType = relationship.getRelationship();
		if (relationshipType.equals(AJRelationshipManager.ADVISES)
				|| relationshipType.equals(AJRelationshipManager.ANNOTATES)
				|| relationshipType.equals(AJRelationshipManager.DECLARED_ON)
				|| relationshipType.equals(AJRelationshipManager.MATCHED_BY)
				|| relationshipType.equals(AJRelationshipManager.SOFTENS)) {
			source = relationship.getSource();
		} else {
			source = relationship.getTarget();
		}
		IType typeElement = (IType) source.getAncestor(IJavaElement.TYPE);
		if (typeElement instanceof AspectElement) {
			return AspectJPreferences.getSavedIcon(typeElement.getJavaProject()
					.getProject(), AJMarkersDialog
					.getFullyQualifiedAspectName(typeElement));
		} else {
			return null;
		}
	}

	/**
	 * Two or more markers on the same line - get the most approriate marker
	 * type to display
	 * 
	 * @param firstMarkerType
	 * @param secondMarkerType
	 * @return
	 */
	private static String getCombinedMarkerType(String firstMarkerType,
			String secondMarkerType, boolean runtimeTest) {
		if (firstMarkerType.indexOf("source") != -1 && secondMarkerType.indexOf("source") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
			return runtimeTest ? IAJModelMarker.SOURCE_DYNAMIC_ADVICE_MARKER
					: IAJModelMarker.SOURCE_ADVICE_MARKER;
		} else if (firstMarkerType.indexOf("source") != -1 || secondMarkerType.indexOf("source") != -1) { //$NON-NLS-1$ //$NON-NLS-2$ 
			return runtimeTest ? IAJModelMarker.DYNAMIC_SOURCE_AND_TARGET_MARKER
					: IAJModelMarker.SOURCE_AND_TARGET_MARKER;
		} else {
			return runtimeTest ? IAJModelMarker.DYNAMIC_ADVICE_MARKER
					: IAJModelMarker.ADVICE_MARKER;
		}
	}

}
