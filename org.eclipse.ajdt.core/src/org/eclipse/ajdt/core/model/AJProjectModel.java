/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AsmRelationshipProvider;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.core.TimerLogEvent;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AJInjarElement;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.text.CoreMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.osgi.util.NLS;

/**
 * @author mchapman
 */
public class AJProjectModel {

	private static final int MODEL_VERSION = 101;
	private static final String MODEL_FILE = ".elementMap"; //$NON-NLS-1$

	IProject project;

	private Map ipeToije = new HashMap();

	private Map jeLinkNames = new HashMap();

	// perRelMaps[0] = rels with runtime test
	// perRelMaps[1] = rels without runtime test
	private Map[] perRelMaps = new Map[] { new HashMap(), new HashMap() };

	// map asm kind strings to AJRelationships
	private Map kindMap = new HashMap();

	private Map extraChildren = new HashMap();

	// remember those AdviceElements which are the source of relationships
	// with a runtime test. This information can be determined from the
	// relationship map, but it is used by the workbench image decorator so is
	// stored separately for performance
	private Set hasRuntime = new HashSet();
	
	// map IJavaElement to Integer line number
	private Map lineNumbers = new HashMap();

	private Persistence persistence;
	
	// only for information/diagnosis purposes
	private int relsCount;
	
	public AJProjectModel(IProject project) {
		this.project = project;

		// map asm kind strings to AJRelationships
		kindMap.put(AsmRelationshipProvider.ADVISES, AJRelationshipManager.ADVISES);
		kindMap.put(AsmRelationshipProvider.ADVISED_BY, AJRelationshipManager.ADVISED_BY);
		kindMap.put(AsmRelationshipProvider.INTER_TYPE_DECLARES, AJRelationshipManager.DECLARED_ON);
		kindMap.put(AsmRelationshipProvider.INTER_TYPE_DECLARED_BY,
				AJRelationshipManager.ASPECT_DECLARATIONS);
		kindMap.put(AsmRelationshipProvider.MATCHED_BY, AJRelationshipManager.MATCHED_BY);
		kindMap.put(AsmRelationshipProvider.MATCHES_DECLARE, AJRelationshipManager.MATCHES_DECLARE);
		kindMap.put(AsmRelationshipProvider.ANNOTATES, AJRelationshipManager.ANNOTATES);
		kindMap.put(AsmRelationshipProvider.ANNOTATED_BY, AJRelationshipManager.ANNOTATED_BY);
		kindMap.put(AsmRelationshipProvider.SOFTENS, AJRelationshipManager.SOFTENS);
		kindMap.put(AsmRelationshipProvider.SOFTENED_BY, AJRelationshipManager.SOFTENED_BY);
		
		// Enable these lines if we ever want these relationships in the model
		//kindMap.put("uses pointcut", AJRelationshipManager.USES_POINTCUT);
		//kindMap.put("pointcut used by", AJRelationshipManager.POINTCUT_USED_BY);			
	}

	public IJavaElement getCorrespondingJavaElement(IProgramElement ipe) {
		return (IJavaElement) ipeToije.get(ipe);
	}

	private Persistence getPersistence() {
		if (persistence == null) {
			persistence = new Persistence();
		}
		return persistence;
	}
	
	public void saveModel() {
		getPersistence().saveModel(null);
	}
	
	public void saveModel(IPath file) {
		getPersistence().saveModel(file);
	}

	public void loadModel() {
		if (!getPersistence().isPersisted()) {
			return;
		}
		AJLog.logStart(TimerLogEvent.LOAD_MODEL);
		boolean worked = getPersistence().loadModel(null);
		AJLog.logEnd(TimerLogEvent.LOAD_MODEL,relsCount + " rels in project: "+project.getName()); //$NON-NLS-1$
		if (!worked && getPersistence().isPersisted()) {
			AJLog.log("Loading model failed for project: "+project.getName()); //$NON-NLS-1$
		}
		return;
	}
	
	public void loadModel(IPath file) {
		AJLog.logStart(TimerLogEvent.LOAD_MODEL);
		boolean worked = getPersistence().loadModel(file);
		AJLog.logEnd(TimerLogEvent.LOAD_MODEL,relsCount + " rels in project: "+project.getName()); //$NON-NLS-1$
		if (!worked) {
			AJLog.log("Loading model failed for file: "+file); //$NON-NLS-1$
		}
	}
	
	public void deleteModelFile() {
		getPersistence().deleteModelFile();
	}
	
	public List getRelatedElements(AJRelationshipType rel, IJavaElement je) {
		// Get related elements for given relationship, both with and without
		// runtime test. Avoid creating a new List if at all possible.
		Map relMap1 = (Map) perRelMaps[0].get(rel);
		Map relMap2 = (Map) perRelMaps[1].get(rel);
		List l1 = null;
		List l2 = null;
		if (relMap1 != null) {
			l1 = (List) relMap1.get(je);
		}
		if (relMap2 != null) {
			l2 = (List) relMap2.get(je);
		}
		if ((l1 == null) && (l2 == null)) {
			return null;
		}
		if (l2 == null) {
			return l1;
		}
		if (l1 == null) {
			return l2;
		}
		List combined = new ArrayList(l1);
		combined.addAll(l2);
		return combined;
	}

	public List getAllRelationships(AJRelationshipType[] rels) {
		List allRels = new ArrayList();
		for (int i = 0; i < rels.length; i++) {
			for (int j = 0; j <= 1; j++) { // with and without runtime test
				Map relMap = (Map) perRelMaps[j].get(rels[i]);
				if (relMap != null) {
					for (Iterator iter = relMap.keySet().iterator(); iter
							.hasNext();) {
						IJavaElement source = (IJavaElement) iter.next();
						List targetList = (List) relMap.get(source);
						for (Iterator iter2 = targetList.iterator(); iter2
								.hasNext();) {
							IJavaElement target = (IJavaElement) iter2.next();
							allRels.add(new AJRelationship(source, rels[i],
									target, (j == 0)));
						}
					}
				}
			}
		}
		return allRels;
	}

	/**
	 * Returns true if this element is advised, or if this element contains a
	 * sub-method element that is advised.
	 * 
	 * @param je
	 * @return
	 */
	public boolean isAdvised(IJavaElement je) {
		List advisedBy = getRelatedElements(AJRelationshipManager.ADVISED_BY,
				je);
		if ((advisedBy != null) && (advisedBy.size() > 0)) {
			return true;
		}
		// check for advised code elements
		List extras = getExtraChildren(je);
		if (extras != null) {
			for (Iterator iter = extras.iterator(); iter.hasNext();) {
				IJavaElement element = (IJavaElement) iter.next();
				advisedBy = getRelatedElements(
						AJRelationshipManager.ADVISED_BY, element);
				if ((advisedBy != null) && (advisedBy.size() > 0)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if this element (such as an AdviceElement) is the source
	 * of a relationship
	 * @param je
	 * @return
	 */
	public boolean hasRuntimeTest(IJavaElement je) {
		return hasRuntime.contains(je);
	}
	
	public int getJavaElementLineNumber(IJavaElement je) {
		Integer i = (Integer) lineNumbers.get(je);
		if (i != null) {
			return i.intValue();
		}
		return -1;
	}

	public String getJavaElementLinkName(IJavaElement je) {
		return (String) jeLinkNames.get(je);
	}

	public List getExtraChildren(IJavaElement je) {
		return (List) extraChildren.get(je);
	}

	public void createProjectMap() {
		AJLog.logStart(TimerLogEvent.CREATE_MODEL);
		try {
			project.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					if (resource instanceof IFolder) {
						return true;
					} else if (resource instanceof IFile) {
						IFile f = (IFile) resource;
						if (CoreUtils.ASPECTJ_SOURCE_FILTER.accept(f.getName())) {
							createMapForFile(f);
						}
						return false;
					} else {
						return true;
					}
				}
			});
		} catch (CoreException coreEx) {
		}
		processRelationships();
		AJLog.logEnd(TimerLogEvent.CREATE_MODEL,relsCount + " rels in project: "+project.getName()); //$NON-NLS-1$

		//dumpModel();
		//dumpAJDEStructureModel();
	}

	private void processRelationships() {
		relsCount = 0;
		IRelationshipMap asmRelMap = AsmManager.getDefault()
				.getRelationshipMap();
		for (Iterator iter = asmRelMap.getEntries().iterator(); iter.hasNext();) {
			String sourceOfRelationship = (String) iter.next();
			IProgramElement ipe = AsmManager.getDefault().getHierarchy()
					.findElementForHandle(sourceOfRelationship);
			List relationships = asmRelMap.get(ipe);
			if (relationships != null) {
				for (Iterator iterator = relationships.iterator(); iterator
						.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					List targets = rel.getTargets();
					for (Iterator iterator2 = targets.iterator(); iterator2
							.hasNext();) {
						String t = (String) iterator2.next();
						IProgramElement link = AsmManager.getDefault()
								.getHierarchy().findElementForHandle(t);
						// System.err.println("asmRelMap entry: "
						// + ipe.toLinkLabelString() + ", relationship: "
						// + rel.getName() + ", target: "
						// + link.toLinkLabelString());
						IJavaElement sourceEl = (IJavaElement) ipeToije
								.get(ipe);
						IJavaElement targetEl = (IJavaElement) ipeToije
								.get(link);

						if (targetEl == null) {
							// There is no java element corresponding to the target program
							// element in this project - it is either in a different project
							// or in a jar, or outside the workspace. Create a placeholder
							// element. In future we could look for the resource in the
							// workspace, and locate the real java element for it
							if (link.getParent() == null) {
								// if the problem element has no parent, then we
								// have a binary/injar aspect, otherwise we
								// don't know what it is, so we skip it
								String name = NLS.bind(CoreMessages.injarElementLabel, link.getName());
								targetEl = new AJInjarElement(name);

								// store this elements, so that it gets saved
								jeLinkNames.put(targetEl, name);
								lineNumbers.put(targetEl, new Integer(0));
							}
						}

						AJRelationshipType ajRel = (AJRelationshipType) kindMap
								.get(rel.getName());
						if (ajRel != null) {
							// System.out.println("Rel: " + rel.getName()
							// + " source: " + sourceEl + " hashcode: "
							// + sourceEl.hashCode() + ", target: "
							// + targetEl
							// + " hashcode: " + targetEl.hashCode());
							if ((sourceEl != null) && (targetEl != null)) {
								if(sourceEl instanceof AdviceElement) {
									if (rel.hasRuntimeTest()) {
										hasRuntime.add(sourceEl);
									}
								}
								
								Map perRelMap = rel.hasRuntimeTest() ? perRelMaps[0]
										: perRelMaps[1];
								Map relMap = (Map) perRelMap.get(ajRel);
								if (relMap == null) {
									relMap = new HashMap();
									perRelMap.put(ajRel, relMap);
								}
								List l = (List) relMap.get(sourceEl);
								if (l == null) {
									l = new ArrayList();
									relMap.put(sourceEl, l);
								}
								l.add(targetEl);
								relsCount++;
							}
						}
					}
				}
			}
		}
	}

	private void createMapForFile(final IFile file) {
		IProject project = file.getProject();

		// Don't process files in non AspectJ projects
		if (project == null || !project.isOpen()
				|| !AspectJPlugin.isAJProject(project)) {
			return;
		}

		// Copes with linked src folders.
		String path = file.getRawLocation().toOSString();

				Map annotationsMap = AsmManager.getDefault().getInlineAnnotations(path,
				true, true);
		if (annotationsMap == null) {
			return;
		}

		ICompilationUnit unit = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit(file);
		if (unit == null) {
			if (file.getName().endsWith(".java")) { //$NON-NLS-1$
				// JavaCore can only cope with .java files. The
				// AJCompilationUnitManager
				// should have given us the unit for .aj files
				unit = JavaCore.createCompilationUnitFrom(file);
			}
		}
		if (unit == null) {
			// no point continuing if we still don't have a compilation unit
			return;
		}

		if (unit instanceof AJCompilationUnit) {
			try {
				// ensure structure of the AJCompilationUnit is fully
				// known - otherwise there is a timing window where
				// the model can be built before the reconciler has
				// updated the structure
				unit.reconcile(ICompilationUnit.NO_AST, false, null, null);
			} catch (JavaModelException e) {
			}
		}

		Set keys = annotationsMap.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			Object key = it.next();
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				ISourceLocation sl = node.getSourceLocation();
		
				// the offset we get for declare statement marks the start of the word
				// "declare", but the IJavaElement seems to start at the word after
				// "declare", so we need to adjust the offset in this case.
				int fff = 0;
				String declare = "declare "; //$NON-NLS-1$
				if (node.toLinkLabelString().indexOf(declare) != -1) {
					fff = declare.length();
				}
				int offset = sl.getOffset();
				// in some versions of ajde, code elements have an offset of
				// zero - in cases like this, we go with the offset of the
				// parent instead
				if (offset == 0) {
					offset = node.getParent().getSourceLocation().getOffset();
				}
				try {
					IJavaElement el = unit.getElementAt(offset + fff);
					if (node.getKind() == IProgramElement.Kind.CODE) {
						IJavaElement parent = el;
						el = new AJCodeElement((JavaElement) parent, sl
								.getLine(), node.toLabelString());
						List l = (List) extraChildren.get(parent);
						if (l == null) {
							l = new ArrayList();
							extraChildren.put(parent, l);
						}
						l.add(el);
					}
					if (el != null) {
						ipeToije.put(node, el);
						jeLinkNames.put(el, node.toLinkLabelString());
						lineNumbers.put(el, new Integer(sl.getLine()));
					}
				} catch (JavaModelException e1) {
				}
			}
		}
	}

	// for debugging...
	private void dumpModel() {
		System.out.println("AJDT model for project: " + project.getName()); //$NON-NLS-1$
		for (Iterator iter = kindMap.keySet().iterator(); iter.hasNext();) {
			String kind = (String) iter.next();
			AJRelationshipType rel = (AJRelationshipType) kindMap.get(kind);
			for (int i = 0; i <= 1; i++) { // with and without runtime test
				Map relMap = (Map) perRelMaps[i].get(rel);
				if (relMap != null) {
					for (Iterator iter2 = relMap.keySet().iterator(); iter2
							.hasNext();) {
						IJavaElement je = (IJavaElement) iter2.next();
						List related = (List) relMap.get(je);
						for (Iterator iter3 = related.iterator(); iter3
								.hasNext();) {
							IJavaElement el = (IJavaElement) iter3.next();
							System.out.println(""); //$NON-NLS-1$
							System.out.println("    " //$NON-NLS-1$
									+ getJavaElementLinkName(je) + " --" + kind //$NON-NLS-1$
									+ "-> " + getJavaElementLinkName(el)); //$NON-NLS-1$
							System.out.println("    " + je.hashCode() + " --" //$NON-NLS-1$ //$NON-NLS-2$
									+ kind + "-> " + el.hashCode()); //$NON-NLS-1$
							System.out.println("    " //$NON-NLS-1$
									+ je.getHandleIdentifier() + " --" + kind //$NON-NLS-1$
									+ "-> " + el.getHandleIdentifier()); //$NON-NLS-1$
						}
					}
				}
			}
		}
		System.out.println("End of model"); //$NON-NLS-1$
	}

	// for debugging...
	private void dumpAJDEStructureModel() {
		System.out.println("AJDE structure model for project: " + project.getName()); //$NON-NLS-1$
		
		IRelationshipMap asmRelMap = AsmManager.getDefault().getRelationshipMap();
		for (Iterator iter = asmRelMap.getEntries().iterator(); iter.hasNext();) {
			String sourceOfRelationship = (String) iter.next();
			IProgramElement ipe = AsmManager.getDefault().getHierarchy()
									.findElementForHandle(sourceOfRelationship);
			List relationships = asmRelMap.get(ipe);
			if (relationships != null) {
				for (Iterator iterator = relationships.iterator(); iterator.hasNext();) {
					Relationship rel = (Relationship) iterator.next();
					List targets = rel.getTargets();
					for (Iterator iterator2 = targets.iterator(); iterator2.hasNext();) {
						String t = (String) iterator2.next();
						IProgramElement link = AsmManager.getDefault().getHierarchy().findElementForHandle(t);
						System.out.println(""); //$NON-NLS-1$
						System.out.println("      sourceOfRelationship " + sourceOfRelationship); //$NON-NLS-1$
						System.out.println("          relationship " + rel.getName()); //$NON-NLS-1$
						System.out.println("              target " + link.getName()); //$NON-NLS-1$
					}
				}
				
			}
		}
		System.out.println("End of AJDE structure model"); //$NON-NLS-1$
	}
	
	private class Persistence {
	
		private static final int RUNTIME_OFFSET = 100;
	
		private Map idMap;
	
		private int idCount;
	
		private List elementList;
	
		private AJRelationshipType[] relTypes = AJRelationshipManager.allRelationshipTypes;
	
		private Map relIDs;
	
		public Persistence() {
			relIDs = new HashMap();
			for (int i = 0; i < relTypes.length; i++) {
				relIDs.put(relTypes[i], new Integer(i));
			}
		}
	
		public boolean isPersisted() {
			return getDefaultFile().toFile().exists();
		}
	
		private IPath getDefaultFile() {
			return AspectJPlugin.getDefault().getStateLocation().append(
					project.getName() + MODEL_FILE);
		}
	

		/**
		 * Save the current model to the given file path, or if null to the
		 * default location for the current project.
		 * @param path
		 */
		public void saveModel(IPath path) {
			if (path == null) {
				path = getDefaultFile();
			}
			//System.out.println("saving model file=" + path.makeAbsolute());
			try {
				FileOutputStream fos = new FileOutputStream(path.toFile());
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				saveVersion(oos);
				saveJavaElements(oos);
				saveRelationships(oos);
				saveExtraChildren(oos);
				oos.flush();
				fos.flush();
				oos.close();
				fos.close();
				//System.out.println("finishing saving model");
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
	
		}
	
		public boolean loadModel(IPath path) {
			if (path == null) {
				path = getDefaultFile();
			}
			if (!path.toFile().exists()) {
				return false;
			}
			try {
				FileInputStream fis = new FileInputStream(path.toFile());
				ObjectInputStream ois = new ObjectInputStream(fis);
				int version = loadVersion(ois);
				//System.out.println("loading model version: " + version);
				if (version == MODEL_VERSION) {
					loadJavaElements(ois);
					loadRelationships(ois);
					loadExtraChildren(ois);
				}
				ois.close();
				fis.close();
				return true;
			} catch (ClassNotFoundException e) {
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
			return false;
		}
		
		public void deleteModelFile() {
			IPath path = getDefaultFile();
			File modelFile = path.toFile();
			if (modelFile.exists()) {
				modelFile.delete();
			}
		}
	
		private int getID(IJavaElement el) {
			return ((Integer) idMap.get(el)).intValue();
		}
	
		void saveVersion(ObjectOutputStream oos) throws IOException {
			oos.writeInt(MODEL_VERSION);
		}
	
		int loadVersion(ObjectInputStream ois) throws IOException {
			return ois.readInt();
		}
	
		void saveJavaElements(ObjectOutputStream oos) throws IOException {
			// assumption is that every JavaElement in the relationship map
			// has a link name and line number - this is currently true
			// based on the way the map is built
	
			idMap = new HashMap();
			idCount = 0;
	
			int numElements = lineNumbers.keySet().size();
			oos.writeInt(numElements);
			for (Iterator iter = lineNumbers.keySet().iterator(); iter
					.hasNext();) {
				IJavaElement element = (IJavaElement) iter.next();
				// remember the id of each element so that we can refer to it
				// later
				idMap.put(element, new Integer(idCount++));
				String handleIdentifier = element.getHandleIdentifier().intern();
				oos.writeObject(handleIdentifier);
				String linkName = (String) jeLinkNames.get(element);
				oos.writeObject(linkName);
				Integer lineNum = (Integer) lineNumbers.get(element);
				oos.writeInt(lineNum.intValue());
			}
		}
	
		void loadJavaElements(ObjectInputStream ois) throws IOException,
				ClassNotFoundException {
			int numElements = ois.readInt();
			elementList = new ArrayList(numElements);
			for (int i = 0; i < numElements; i++) {
				String handleIdentifier = (String) ois.readObject();
				IJavaElement element = AspectJCore.create(handleIdentifier);
				// remember the element as it will be referred to by id later
				elementList.add(element);
				String linkName = (String) ois.readObject();
				jeLinkNames.put(element, linkName);
				Integer lineNum = new Integer(ois.readInt());
				lineNumbers.put(element, lineNum);
			}
		}
	
		private int encodeRelType(AJRelationshipType rel, boolean hasRuntimeTest) {
			int id = ((Integer) relIDs.get(rel)).intValue();
			if (hasRuntimeTest) {
				id += RUNTIME_OFFSET;
			}
			return id;
		}
	
		private AJRelationshipType decodeRelType(int id) {
			if (id >= RUNTIME_OFFSET) {
				id -= RUNTIME_OFFSET;
			}
			return relTypes[id];
		}
	
		private boolean hasRuntimeTest(int id) {
			return (id >= RUNTIME_OFFSET);
		}
	
		void saveRelationships(ObjectOutputStream oos) throws IOException {
			// first count total number of relationship types
			int numRelTypes = 0;
			for (Iterator iter = kindMap.values().iterator(); iter.hasNext();) {
				AJRelationshipType rel = (AJRelationshipType) iter.next();
				for (int i = 0; i <= 1; i++) { // with and without runtime test
					Map relMap = (Map) perRelMaps[i].get(rel);
					if (relMap != null) {
						numRelTypes++;
					}
				}
			}
			oos.writeInt(numRelTypes);
	
			for (Iterator iter = kindMap.values().iterator(); iter.hasNext();) {
				AJRelationshipType rel = (AJRelationshipType) iter.next();
				for (int i = 0; i <= 1; i++) { // with and without runtime test
					Map relMap = (Map) perRelMaps[i].get(rel);
					if (relMap != null) {
						oos.writeInt(encodeRelType(rel, (i == 0)));
						oos.writeInt(relMap.size());
						for (Iterator iter2 = relMap.keySet().iterator(); iter2
								.hasNext();) {
							IJavaElement source = (IJavaElement) iter2.next();
							oos.writeInt(getID(source));
							List targetList = (List) relMap.get(source);
							oos.writeInt(targetList.size());
							for (Iterator iter3 = targetList.iterator(); iter3
									.hasNext();) {
								IJavaElement target = (IJavaElement) iter3
										.next();
								oos.writeInt(getID(target));
							}
						}
					}
				}
			}
		}
	
		void loadRelationships(ObjectInputStream ois) throws IOException {
			relsCount = 0;
			int numRelTypes = ois.readInt();
	
			for (int i = 0; i < numRelTypes; i++) {
				int relType = ois.readInt();
				int numRels = ois.readInt();
				AJRelationshipType ajRel = decodeRelType(relType);
				Map perRelMap = hasRuntimeTest(relType) ? perRelMaps[0]
						: perRelMaps[1];
				Map relMap = (Map) perRelMap.get(ajRel);
				if (relMap == null) {
					relMap = new HashMap();
					perRelMap.put(ajRel, relMap);
				}
	
				for (int j = 0; j < numRels; j++) {
					int sourceID = ois.readInt();
					IJavaElement sourceEl = (IJavaElement) elementList
							.get(sourceID);
					if (hasRuntimeTest(relType)) {
						hasRuntime.add(sourceEl);
					}
					
					List l = (List) relMap.get(sourceEl);
					if (l == null) {
						l = new ArrayList();
						relMap.put(sourceEl, l);
					}
	
					int numTargets = ois.readInt();
					for (int k = 0; k < numTargets; k++) {
						int targetID = ois.readInt();
						IJavaElement targetEl = (IJavaElement) elementList
								.get(targetID);
						l.add(targetEl);
						relsCount++;
					}
				}
			}
		}
	
		void saveExtraChildren(ObjectOutputStream oos) throws IOException {
			int numParents = extraChildren.size();
			oos.writeInt(numParents);
			for (Iterator iter = extraChildren.keySet().iterator(); iter
					.hasNext();) {
				IJavaElement parent = (IJavaElement) iter.next();
				// the parents is probably not one of our elements, so write out the handle
				oos.writeObject(parent.getHandleIdentifier());
				List children = (List) extraChildren.get(parent);
				oos.writeInt(children.size());
				for (Iterator iterator = children.iterator(); iterator
						.hasNext();) {
					IJavaElement child = (IJavaElement) iterator.next();
					oos.writeInt(getID(child));
				}
			}
		}
	
		void loadExtraChildren(ObjectInputStream ois) throws IOException,
			ClassNotFoundException {
			int numParents = ois.readInt();
			for (int i = 0; i < numParents; i++) {
				String parentHandle = (String)ois.readObject();
				IJavaElement parent = AspectJCore.create(parentHandle);
				int numChildren = ois.readInt();
				List children = new ArrayList(numChildren);
				for (int j = 0; j < numChildren; j++) {
					int childID = ois.readInt();
					IJavaElement je = (IJavaElement) elementList.get(childID);
					children.add(je);
				}
				extraChildren.put(parent, children);
			}
		}
	}

}