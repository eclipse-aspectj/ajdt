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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.javamodel.AJCompilationUnitManager;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.visualiser.StructureModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaOutlinePage;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * 
 * @author mchapman
 */
public class AJModel {
	private static AJModel instance;

	// we need a IProgramElement to IJavaElement map per project
	private Map perProjectProgramElementMap = new HashMap();
    //map a IProgramElement to its corresponding IJavaElement
	//private Map ipeToije = new HashMap();

	// map a IJavaElement to a List of IJavaElements
	private Map advisesMap = new HashMap();

	private Map advisedByMap = new HashMap();

	// list of projects we know about
	private Set projectSet = new HashSet();

	// per project set of files we know about
	private Map perProjectFileSet = new HashMap();
	//private Set fileSet = new HashSet();
	
	// needs which project is being built, if any
	private IProject beingBuilt = null;
	
	private AJModel() {

	}

	public static AJModel getInstance() {
		if (instance == null) {
			instance = new AJModel();
		}
		return instance;
	}

	/* These commented out routines are the basis for a complete conversion layer
	 * on top of the underlying structure model. It would be good if we could perform
	 * this conversion after a build, and then discard the underlying structure model,
	 * using only this Eclipse-centric one throughout the rest of AJDT. We would then
	 * have to handle the life-cycle and persistence ourselves - serialising this
	 * structure instead of the .ajsym files used by the underlying structure model.
	 * To progress this we really need to be able to get character offset information
	 * from IProgramElement instead of line numbers, as otherwise we have to do a
	 * somewhat time consuming conversion. We also have to make sure our new data
	 * structure contains all the information we need throughout AJDT.
	 
	public void createMap(final IProject project) {
		System.out.println("creating map for project: " + project);
		projectSet.add(project);
		ipeToije.clear();
		advisesMap.clear();
		advisedByMap.clear();
		try {
			AspectJUIPlugin.getWorkspace().run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) {
					long start = System.currentTimeMillis();
					try {
						project.accept(new IResourceVisitor() {
							public boolean visit(IResource resource) {
								if (resource instanceof IFolder) {
									return true;
								} else if (resource instanceof IFile) {
									IFile f = (IFile) resource;
									if (ProjectProperties.ASPECTJ_SOURCE_FILTER
											.accept(f.getName())) {
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
					long cp1 = System.currentTimeMillis();
					long elapsed = cp1 - start;
					System.out.println("processed files in " + elapsed);
					processRelationships();
					elapsed = System.currentTimeMillis() - cp1;
					System.out.println("processed rels in " + elapsed);
				}
			}, null);
		} catch (CoreException coreEx) {
		}
	}
	
	private void init(IJavaElement je) {
		IProject project = je.getJavaProject().getProject();
		if (!projectSet.contains(project)) {
			System.out.println("map requested for project: " + project
					+ " project not known");
			String lst = AspectJUIPlugin.getBuildConfigurationFile(project);
			System.out.println("lst file=" + lst);
			long start = System.currentTimeMillis();
			AsmManager.getDefault().readStructureModel(lst);
			long elapsed = System.currentTimeMillis() - start;
			System.out.println("read structure model in " + elapsed + "ms");
			createMap(project);
		} else {
			System.out.println("map requested for project: " + project
					+ " project known");
		}
	}

	public List getAdvisesElements(IJavaElement je) {
		init(je);
		return (List) advisesMap.get(je);
	}

	public List getAdvisedByElements(IJavaElement je) {
		init(je);
		return (List) advisedByMap.get(je);
	}
	
	private void processRelationships() {
		IRelationshipMap relMap = AsmManager.getDefault().getRelationshipMap();
		for (Iterator iter = relMap.getEntries().iterator(); iter.hasNext();) {
			String sourceOfRelationship = (String) iter.next();
			IProgramElement ipe = AsmManager.getDefault().getHierarchy()
					.findElementForHandle(sourceOfRelationship);
			List relationships = relMap.get(ipe);
			for (Iterator iterator = relationships.iterator(); iterator
					.hasNext();) {
				Relationship rel = (Relationship) iterator.next();
				List targets = rel.getTargets();
				for (Iterator iterator2 = targets.iterator(); iterator2
						.hasNext();) {
					String t = (String) iterator2.next();
					IProgramElement link = AsmManager.getDefault()
							.getHierarchy().findElementForHandle(t);
					System.err.println("relMap entry:  "
							+ ipe.toLinkLabelString() + ", relationship: "
							+ rel.getName() + ", target: "
							+ link.toLinkLabelString());
					IJavaElement sourceEl = (IJavaElement) ipeToije.get(ipe);
					IJavaElement targetEl = (IJavaElement) ipeToije.get(link);
					System.err.println("Eclipse entry: "
							+ sourceEl.getElementName() + ", relationship: "
							+ rel.getName() + ", target: "
							+ targetEl.getElementName());
					if (rel.getName().startsWith("advises")) {
						System.out.println("advises");
						List l = (List) advisesMap.get(sourceEl);
						if (l == null) {
							l = new ArrayList();
							advisesMap.put(sourceEl, l);
						}
						l.add(targetEl);
					}
					if (rel.getName().startsWith("advised by")) {
						System.out.println("advised by");
						List l = (List) advisesMap.get(sourceEl);
						if (l == null) {
							l = new ArrayList();
							advisedByMap.put(sourceEl, l);
						}
						l.add(targetEl);
					}
				}
			}
		}
	}

*/
	
	private void initForProject(IProject project) {
		StructureModelUtil.initialiseAJDE(project);
//		if (!projectSet.contains(project)) {
//			System.out.println("map requested for project: " + project
//					+ " project not known");
//			String lst = AspectJUIPlugin.getBuildConfigurationFile(project);
//			long start = System.currentTimeMillis();
//			AsmManager.getDefault().readStructureModel(lst);
//			long elapsed = System.currentTimeMillis() - start;
//			System.out.println("read structure model in " + elapsed + "ms");
//			//projectSet.add(project);
//		}
	}
	
	private void initForFile(IFile file) {
		IProject project = file.getProject();
		initForProject(project);
		Set fileSet = (Set)perProjectFileSet.get(project);
		if (fileSet==null) {
			fileSet = new HashSet();
			perProjectFileSet.put(project,fileSet);
		}
		if (!fileSet.contains(file)) {
			createMapForFile(file);
			fileSet.add(file);
		}
	}

	public void aboutToBuild(IProject project) {
		beingBuilt = project;
	}
	
	public void clearAJModel(IProject project) {
		beingBuilt = null;
		Set fileSet = (Set)perProjectFileSet.get(project);
		if (fileSet!=null) {
			fileSet.clear();
		}
		Map ipeToije = (Map)perProjectProgramElementMap.get(project);
		if (ipeToije!=null) {
			ipeToije.clear();
		}
		//System.out.println("cleared maps for project "+project);
	}

	/**
	 * Maps the given IProgramElement to its corresponding IJavaElement
	 * @param ipe
	 * @return
	 */
	public IJavaElement getCorrespondingJavaElement(IProgramElement ipe) {
		IResource res = programElementToResource(ipe);
		if (res!=null && (res instanceof IFile)) {
			IFile file = (IFile)res;
			initForFile(file);
			Map ipeToije = (Map)perProjectProgramElementMap.get(file.getProject());
			return (IJavaElement)ipeToije.get(ipe);
		}
		return null;
	}
	
	private IResource programElementToResource(IProgramElement ipe) {
		try {
			String fileString = ipe.getSourceLocation().getSourceFile()
					.getCanonicalPath();
			//System.out.println("f=" + fileString);
			IProject[] projects = AspectJUIPlugin.getWorkspace().getRoot()
					.getProjects();
			for (int i = 0; i < projects.length; i++) {
				try {
					if (projects[i].isOpen()
							&& projects[i].hasNature(AspectJUIPlugin.ID_NATURE)) {
						String root = AJDTUtils
								.getProjectRootDirectory(projects[i]);
						//System.out.println("project="+projects[i]);
						//System.out.println("root="+root);
						if (fileString.startsWith(root)) {
							String path = fileString.substring(root.length());
							//System.out.println("path="+path);
							IPath ipath = new Path(path);
							//System.out.println("ipath="+ipath);
							IResource res = projects[i].findMember(ipath);
							//System.out.println("res="+res);
							return res;
						}
					}
				} catch (CoreException ce) {
				}
			}
		} catch (IOException e) {
		}
		return null;
	}
	
	/**
	 * Is this element advised by something (doesn't matter what). Doesn't
	 * trigger fullscale initialization, just the structure model
	 * deserialization plus the containing file mapping, not the entire project
	 * mapping.
	 * 
	 * @param je
	 * @return
	 */
	public boolean isAdvisedBy(IJavaElement je) {
		//System.out.println("isBuilding="+Builder.isBuilding+" isAdvisedBy: "+je);
		if (beingBuilt!=null) {
			return false;
		}
		try {
			IResource ir = je.getUnderlyingResource();
			if ((ir != null) && (ir instanceof IFile)) {
				IFile file = (IFile) ir;
				initForFile(file);
				IProject project = file.getProject();
				Map ipeToije = (Map)perProjectProgramElementMap.get(project);
				// look for je in map to find corresponding ipe
				IProgramElement pe = null;
				for (Iterator iter = ipeToije.entrySet().iterator(); (pe==null) && iter.hasNext();) {
					Map.Entry e = (Map.Entry)iter.next();
					if (je==e.getValue()) {
						pe = (IProgramElement)e.getKey();
					}
				}
				//System.out.println("pe="+pe);
				if (pe!=null) {
					IRelationshipMap irm = AsmManager.getDefault()
						.getRelationshipMap();
					IRelationship advisedBy = irm.get(pe,
							IRelationship.Kind.ADVICE, "advised by", false, false);
					IRelationship advisedByR = irm.get(pe,
							IRelationship.Kind.ADVICE, "advised by", true, false);
					if ((advisedBy!=null) || (advisedByR!=null)) {
						return true;
					}
					return false;
				}
			}
		} catch (JavaModelException e) {
		}
		return false;
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

		//System.out.println("createMapForFile: " + path);

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

		Map lineToOffset = new HashMap();
		fillLineToOffsetMap(lineToOffset, unit);

		Map ipeToije = (Map)perProjectProgramElementMap.get(project);
		if (ipeToije==null) {
			ipeToije = new HashMap();
			perProjectProgramElementMap.put(project,ipeToije);
		}
		
		Set keys = annotationsMap.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			Object key = it.next();
			//System.out.println("key="+key);
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				IProgramElement node = (IProgramElement) it2.next();
				//System.out.println("node="+node);
				ISourceLocation sl = node.getSourceLocation();
				Integer os = (Integer) lineToOffset.get(new Integer(sl
						.getLine()));
				//System.out.println("os="+os);
				int offset = os.intValue() + sl.getColumn() + 5;
				//System.out.println("offset="+offset);
				if (unit != null) {
					try {
						IJavaElement el = unit.getElementAt(offset);
						if (el != null) {
//							System.out.println("el=" + el + " (" +
//							 el.getClass() + ")");
							ipeToije.put(node, el);
						}
					} catch (JavaModelException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
			}
		}
	}

	private void fillLineToOffsetMap(Map map, ICompilationUnit unit) {
		String source;
		try {
			source = unit.getSource();
			int lines = 0;
			map.put(new Integer(1), new Integer(0));
			for (int i = 0; i < source.length(); i++) {
				if (source.charAt(i) == '\n') {
					lines++;
					//System.out.println("line="+(lines+1)+" offset="+i);
					map.put(new Integer(lines + 1), new Integer(i));
				}
			}
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Goes through all the open editors and updates the outline page for
	 * each (if they are using the standard Java outline page.
	 */
	public static void refreshOutlineViews() {
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench()
				.getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++) {
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int x = 0; x < pages.length; x++) {
				IEditorReference[] editors = pages[x].getEditorReferences();
				for (int z = 0; z < editors.length; z++) {
					IEditorPart editor = editors[z].getEditor(true);
					if (editor != null) {
						//IEditorInput input = editor.getEditorInput();
						//IFile editorFile = (IFile)
						// input.getAdapter(IFile.class);
						//System.out.println("file="+editorFile+" opened by
						// "+editor);
						Object out = editor
								.getAdapter(IContentOutlinePage.class);
						if (out instanceof JavaOutlinePage) {
							refreshOutline((JavaOutlinePage)out);
						}
					}
				}
			}
		}
	}
	
	private static void refreshOutline(JavaOutlinePage page) {
		try {
			// Here be dragons
			Class clazz = page.getClass();
			Field field = clazz
					.getDeclaredField("fOutlineViewer");
			field.setAccessible(true); // cough cough
			Class viewer = StructuredViewer.class;
			Method method = viewer.getMethod("refresh",
					new Class[] { boolean.class });
			Object outlineViewer = field.get(page);
			if (outlineViewer != null) {
			method.invoke(outlineViewer,
					new Object[] { Boolean.TRUE });
			//System.out.println("refreshed outline viewer");
			} 
			//else {
				//System.out.println("outline viewer was null");
			//}
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
	}
}