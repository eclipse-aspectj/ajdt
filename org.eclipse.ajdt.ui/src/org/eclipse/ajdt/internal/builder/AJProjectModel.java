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
package org.eclipse.ajdt.internal.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.internal.ui.ajde.ProjectProperties;
import org.eclipse.ajdt.javamodel.AJCompilationUnitManager;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * 
 * @author mchapman
 */
public class AJProjectModel {
	IProject project;
	private Map ipeToije = new HashMap();
	private Map jeLinkNames = new HashMap();
	
	private Map perRelMap = new HashMap();
	
	// map asm kind strings to AJRelationships
	private Map kindMap = new HashMap();
		
	public AJProjectModel(IProject project) {
		this.project = project;
		
		// map ams kind strings to AJRelationships
		kindMap.put("advises",AJRelationshipManager.ADVISES);
		kindMap.put("advised by",AJRelationshipManager.ADVISED_BY);
		kindMap.put("declared on",AJRelationshipManager.DECLARED_ON);
		kindMap.put("aspect declarations",AJRelationshipManager.ASPECT_DECLARATIONS);
		kindMap.put("matched by",AJRelationshipManager.MATCHED_BY);
		kindMap.put("matches declare",AJRelationshipManager.MATCHES_DECLARE);
	}

	/*
	public List getAdvisesElements(IJavaElement je) {
		Map relMap = (Map)perRelMap.get(AJRelationshipManager.ADVISES);
		return (List)relMap.get(je);
	}

	public List getAdvisedByElements(IJavaElement je) {
		Map relMap = (Map)perRelMap.get(AJRelationshipManager.ADVISED_BY);
		return (List)relMap.get(je);
	}*/

	public List getRelatedElements(AJRelationship rel, IJavaElement je) {
		Map relMap = (Map)perRelMap.get(rel);
		if (relMap==null) {
			return null;
		}
		return (List)relMap.get(je);
	}

	public String getJavaElementLinkName(IJavaElement je) {
		return (String)jeLinkNames.get(je);
	}
	
	public void createProjectMap() {
		long start = System.currentTimeMillis();
		try {
			project.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					if (resource instanceof IFolder) {
						return true;
					} else if (resource instanceof IFile) {
						IFile f = (IFile) resource;
						if (ProjectProperties.ASPECTJ_SOURCE_FILTER.accept(f
								.getName())) {
							createMapForFile(f);
						}
						return false;
					} else {
						return true;
					}
				}
			});
		} catch (CoreException coreEx) {
			coreEx.printStackTrace();
		}
		long cp1 = System.currentTimeMillis();
		long elapsed = cp1 - start;
		System.out.println("processed files in " + elapsed);
		processRelationships();
		elapsed = System.currentTimeMillis() - cp1;
		System.out.println("processed rels in " + elapsed);
	}
	
	private void processRelationships() {
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
						//					System.err.println("asmRelMap entry: "
						//							+ ipe.toLinkLabelString() + ", relationship: "
						//							+ rel.getName() + ", target: "
						//							+ link.toLinkLabelString());
						IJavaElement sourceEl = (IJavaElement) ipeToije
								.get(ipe);
						IJavaElement targetEl = (IJavaElement) ipeToije
								.get(link);
						//					System.err.println("Eclipse entry: "
						//							+ sourceEl.getElementName() + ", relationship: "
						//							+ rel.getName() + ", target: "
						//							+ targetEl.getElementName());
						AJRelationship ajRel = (AJRelationship) kindMap.get(rel.getName());
						System.out.println("rel name="+rel.getName()+" ajRel=" + ajRel);
						if (ajRel != null) {
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
						}
						/*
						 * if (rel.getName().startsWith("advises")) {
						 * System.out.println("advises source="+sourceEl+"
						 * ("+sourceEl.hashCode()+")"); System.out.println("
						 * target="+targetEl+" ("+targetEl.hashCode()+")"); List
						 * l = (List) advisesMap.get(sourceEl); if (l == null) {
						 * l = new ArrayList(); advisesMap.put(sourceEl, l); }
						 * l.add(targetEl); } if
						 * (rel.getName().startsWith("advised by")) {
						 * System.out.println("advised by source="+sourceEl+"
						 * ("+sourceEl.hashCode()+")"); System.out.println("
						 * target="+targetEl+" ("+targetEl.hashCode()+")"); List
						 * l = (List) advisedByMap.get(sourceEl); if (l == null) {
						 * l = new ArrayList(); advisedByMap.put(sourceEl, l); }
						 * l.add(targetEl); }
						 */
					}
				}
			}
		}
	}

	private void createMapForFile(final IFile file) {
		IProject project = file.getProject();

		// Don't process files in non AspectJ projects
		try {
			if (project == null || !project.isOpen()
					|| !project.hasNature(AspectJUIPlugin.ID_NATURE))
				return;
		} catch (CoreException e) {
		}

		// Copes with linked src folders.
		String path = file.getRawLocation().toOSString();

		System.out.println("createMapForFile: " + path);

		Map annotationsMap = AsmManager.getDefault().getInlineAnnotations(path,
				true, true);
		if (annotationsMap == null) {
			return;
		}

		ICompilationUnit unit = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit(file);
		if (unit == null) {
			unit = JavaCore.createCompilationUnitFrom(file);
		}
		
		Set keys = annotationsMap.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			Object key = it.next();
			//System.out.println("key="+key);
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				System.out.println("node="+node.toLinkLabelString()+" ("+node.hashCode()+")");
				ISourceLocation sl = node.getSourceLocation();
//				Integer os = (Integer) lineToOffset.get(new Integer(sl
//						.getLine()));
				//System.out.println("os="+os);
//				int offset = os.intValue() + sl.getColumn() + 12;
				//System.out.println("guessed offset="+offset);
				boolean subElement = false;
				int offset = sl.getOffset();
				if (offset==0) {
					subElement = true;
					//System.out.println("0 offset, using parent");
					offset = node.getParent().getSourceLocation().getOffset();
				}
				//System.out.println("queried offset="+offset);
				if (unit != null) {
					try {
						IJavaElement el = unit.getElementAt(offset);
						if (subElement) {
							int start = offset;
							int end = offset+1;
							el = new AJCodeElement((JavaElement)el,sl.getLine(),node.toLabelString());
						}
						if (el != null) {
							System.out.println("el=" + el + " (" +
							 el.getClass() + ") "+el.hashCode()+")");
							System.out.println("ipeToije="+ipeToije.hashCode());
							ipeToije.put(node, el);
							jeLinkNames.put(el,node.toLinkLabelString());
						}
					} catch (JavaModelException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
			}
		}
	}

}