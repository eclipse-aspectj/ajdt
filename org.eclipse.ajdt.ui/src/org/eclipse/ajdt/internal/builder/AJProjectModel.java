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
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.asm.internal.Relationship;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.internal.core.CoreUtils;
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

	private Map extraChildren = new HashMap();

	public AJProjectModel(IProject project) {
		this.project = project;

		// map ams kind strings to AJRelationships
		kindMap.put("advises", AJRelationshipManager.ADVISES);
		kindMap.put("advised by", AJRelationshipManager.ADVISED_BY);
		kindMap.put("declared on", AJRelationshipManager.DECLARED_ON);
		kindMap.put("aspect declarations",
				AJRelationshipManager.ASPECT_DECLARATIONS);
		kindMap.put("matched by", AJRelationshipManager.MATCHED_BY);
		kindMap.put("matches declare", AJRelationshipManager.MATCHES_DECLARE);
	}

	public IJavaElement getCorrespondingJavaElement(IProgramElement ipe) {
		return (IJavaElement) ipeToije.get(ipe);
	}

	public List getRelatedElements(AJRelationship rel, IJavaElement je) {
		Map relMap = (Map) perRelMap.get(rel);
		if (relMap == null) {
			return null;
		}
		return (List) relMap.get(je);
	}

	/**
	 * Returns true if this element is advised, or if this element contains a
	 * sub-method element that is advised.
	 * 
	 * @param je
	 * @return
	 */
	public boolean isAdvised(IJavaElement je) {
		if (je.getElementType() == IJavaElement.METHOD) {
			List advisedBy = getRelatedElements(
					AJRelationshipManager.ADVISED_BY, je);
			if ((advisedBy != null) && (advisedBy.size() > 0)) {
				return true;
			}
			// check for advised code elements
			IJavaElement[] extras = getExtraChildren(je);
			if (extras != null) {
				for (int i = 0; i < extras.length; i++) {
					advisedBy = getRelatedElements(
							AJRelationshipManager.ADVISED_BY, extras[i]);
					if ((advisedBy != null) && (advisedBy.size() > 0)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public String getJavaElementLinkName(IJavaElement je) {
		return (String) jeLinkNames.get(je);
	}

	public IJavaElement[] getExtraChildren(IJavaElement je) {
		List l = (List) extraChildren.get(je);
		if (l == null) {
			return null;
		}
		return (IJavaElement[]) (l.toArray(new IJavaElement[] {}));
	}

	public void createProjectMap() {
		try {
			project.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					if (resource instanceof IFolder) {
						return true;
					} else if (resource instanceof IFile) {
						IFile f = (IFile) resource;
						if (CoreUtils.ASPECTJ_SOURCE_FILTER.accept(f
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
		}
		processRelationships();
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

						AJRelationship ajRel = (AJRelationship) kindMap.get(rel
								.getName());
						if (ajRel != null) {
							//							System.out.println("Rel: " + rel.getName()
							//									+ " source: " + sourceEl + " target: "
							//									+ targetEl);
							if ((sourceEl != null) && (targetEl != null)) {
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
								// extra filter due to AspectJ bug 80916
								if (!(l.contains(targetEl))) {
								    l.add(targetEl);
                                }
								
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

		//System.out.println("createMapForFile: " + path);

		Map annotationsMap = AsmManager.getDefault().getInlineAnnotations(path,
				true, true);
		if (annotationsMap == null) {
			return;
		}

		ICompilationUnit unit = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit(file);
		if (unit == null) {
			if (file.getName().endsWith(".java")) {
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

		Set keys = annotationsMap.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			Object key = it.next();
			//System.out.println("key="+key);
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				//System.out.println("node="+node.toLinkLabelString()+"
				// ("+node.hashCode()+")");
				ISourceLocation sl = node.getSourceLocation();
				//				Integer os = (Integer) lineToOffset.get(new Integer(sl
				//						.getLine()));
				//System.out.println("os="+os);
				//				int offset = os.intValue() + sl.getColumn() + 12;
				//System.out.println("guessed offset="+offset);
				int fff = 0;
				if (node.toLinkLabelString().indexOf("declare parents") >= 0) {
					//System.out.println("declare parents");
					fff = 10;
				}
				if (node.toLinkLabelString().indexOf("declare warning") >= 0) {
					//System.out.println("declare warning");
					fff = 10;
				}
				boolean subElement = false;
				int offset = sl.getOffset();
				if (offset == 0) {
					subElement = true;
					//System.out.println("0 offset, using parent");
					offset = node.getParent().getSourceLocation().getOffset();
				}
				//System.out.println("queried offset="+offset);
				try {
					IJavaElement el = unit.getElementAt(offset + fff);
					if (subElement) {
						IJavaElement parent = el;
						el = new AJCodeElement((JavaElement) parent, sl
								.getLine(), node.toLabelString());
						//System.out.println("extra child for "+parent+" is
						// "+el);
						List l = (List) extraChildren.get(parent);
						if (l == null) {
							l = new ArrayList();
							extraChildren.put(parent, l);
						}
						l.add(el);
					}
					if (el != null) {
						//System.out.println("el=" + el + " (" +
						// el.getClass() + ") "+el.hashCode()+")");
						ipeToije.put(node, el);
						jeLinkNames.put(el, node.toLinkLabelString());
					}
				} catch (JavaModelException e1) {
				}
			}
		}
	}

}