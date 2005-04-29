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
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AJInjarElement;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
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

	// perRelMaps[0] = rels with runtime test
	// perRelMaps[1] = rels without runtime test
	private Map[] perRelMaps = new Map[] { new HashMap(), new HashMap() };

	// map asm kind strings to AJRelationships
	private Map kindMap = new HashMap();

	private Map extraChildren = new HashMap();

	// map IJavaElement to Integer line number
	private Map lineNumbers = new HashMap();
	
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
		kindMap.put("annotates", AJRelationshipManager.ANNOTATES);
		kindMap.put("annotated by", AJRelationshipManager.ANNOTATED_BY);
	}

	public IJavaElement getCorrespondingJavaElement(IProgramElement ipe) {
		return (IJavaElement) ipeToije.get(ipe);
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
		List advisedBy = getRelatedElements(
				AJRelationshipManager.ADVISED_BY, je);
		if ((advisedBy != null) && (advisedBy.size() > 0)) {
			return true;
		}
		// check for advised code elements
		List extras = getExtraChildren(je);
		if (extras != null) {
			for (Iterator iter = extras.iterator(); iter.hasNext();) {
				IJavaElement element = (IJavaElement) iter.next();
                advisedBy = getRelatedElements(AJRelationshipManager.ADVISED_BY,element);
                if ((advisedBy != null) && (advisedBy.size() > 0)) {
                	return true;
                }
            }
        }			
		return false;
	}
	
	public int getJavaElementLineNumber(IJavaElement je) {
		Integer i = (Integer)lineNumbers.get(je);
		if (i!=null) {
			return i.intValue();
		}
		return -1;
	}
	
	public String getJavaElementLinkName(IJavaElement je) {
		return (String) jeLinkNames.get(je);
	}

	public List getExtraChildren(IJavaElement je) {
		return (List)extraChildren.get(je);
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
		//dumpModel();
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
//											System.err.println("asmRelMap entry: "
//													+ ipe.toLinkLabelString() + ", relationship: "
//													+ rel.getName() + ", target: "
//													+ link.toLinkLabelString());
						IJavaElement sourceEl = (IJavaElement) ipeToije
								.get(ipe);
						IJavaElement targetEl = (IJavaElement) ipeToije
								.get(link);
											
						if (targetEl==null) {
							// There is no java element corresponding to the target program
							// element in this project - it is either in a different project
							// or in a jar, or outside the workspace. Create a placeholder
							// element. In future we could look for the resource in the
							// workspace, and locate the real java element for it
							if (link.getParent()==null) {
								// if the problem element has no parent, then we have a binary/injar
								// aspect, otherwise we don't know what it is, so we skip it
								targetEl = new AJInjarElement("injar aspect: "+link.getName());
							}
						}

						AJRelationshipType ajRel = (AJRelationshipType) kindMap.get(rel
								.getName());
						if (ajRel != null) {
//														System.out.println("Rel: " + rel.getName()
//																+ " source: " + sourceEl + " hashcode: " 
//																+ sourceEl.hashCode() + ", target: "
//																+ targetEl 
//																+ " hashcode: " + targetEl.hashCode());
							if ((sourceEl != null) && (targetEl != null)) {
								if(sourceEl instanceof AdviceElement) {
									((AdviceElement)sourceEl).setHasRuntimeTest(rel.hasRuntimeTest());
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
			//System.out.println("key="+key);
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				//System.out.println("node="+node.toLinkLabelString()+"("+node.hashCode()+")");
				ISourceLocation sl = node.getSourceLocation();
				//				Integer os = (Integer) lineToOffset.get(new Integer(sl
				//						.getLine()));
				//System.out.println("os="+os);
				//				int offset = os.intValue() + sl.getColumn() + 12;
				//System.out.println("guessed offset="+offset);

				// the offset we get for declare statement marks the start of the word
				// "declare", but the IJavaElement seems to start at the word after
				// "declare", so we need to adjust the offset in this case. 
				int fff = 0;
				String declare = "declare ";
				if (node.toLinkLabelString().indexOf(declare) != -1) {
					fff = declare.length();
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
						//System.out.println("extra child for "+parent+" is"+el+" hash="+el.hashCode());
						List l = (List) extraChildren.get(parent);
						if (l == null) {
							l = new ArrayList();
							extraChildren.put(parent, l);
						}
						l.add(el);
					}
					if (el != null) {
//						System.out.println("el=" + el + " (" +
//						 el.getClass() + ") "+el.hashCode()+")");
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
		System.out.println("AJDT model for project: " + project.getName());
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
							System.out.println("    "
									+ getJavaElementLinkName(je) + " --" + kind
									+ "-> " + getJavaElementLinkName(el));
							System.out.println("    " + je.hashCode() + " --"
									+ kind + "-> " + el.hashCode());
							System.out.println("    "
									+ je.getHandleIdentifier() + " --" + kind
									+ "-> " + el.getHandleIdentifier());
						}
					}
				}
			}
		}
		System.out.println("End of model");
	}

}