/**********************************************************************
 Copyright (c) 2002 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 Julie Waterhouse - added code to popup AJDTPrefConfigWizard - August 3, 2003
 Julie Waterhouse - removed method calls for new aspect and AspectJ project.  
 This functionality has moved to the plugin.xml. - August 13, 2003.
 ...
 **********************************************************************/
package org.eclipse.ajdt.internal.core;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.internal.ui.AJDTConfigSettings;
import org.eclipse.ajdt.internal.ui.ajde.BuildOptionsAdapter;
import org.eclipse.ajdt.internal.ui.dialogs.MessageDialogWithToggle;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.javamodel.AJCompilationUnitManager;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.PDE;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.ui.IPDEUIConstants;
import org.eclipse.pde.internal.ui.editor.plugin.DependenciesPage;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * A utility class to capture all those little functions that keep cropping up.
 * Converting IPaths to fully-qualified native filenames being one of the most
 * common!!
 */
public class AJDTUtils {

	// icon sizes
	private static final Point SMALL_SIZE = new Point(16, 16);

	private static final Point BIG_SIZE = new Point(22, 16);

	private static final int SMALL_ICONS_MASK = 0x020;

	private static Hashtable imageDescriptorCache = new Hashtable();

	/**
	 * Return the fully-qualifed native OS path of the workspace. e.g.
	 * D:\eclipse\workspace
	 */
	public static String getWorkspacePath() {
		return AspectJUIPlugin.getWorkspace().getRoot().getLocation()
				.toOSString();
	}

	/**
	 * Return the fully-qualified name of the root directory for a project.
	 */
	public static String getProjectRootDirectory(IProject project) {
		return project.getLocation().toOSString();
	}

	/**
	 * Return the fully-qualified native OS path of a project resource
	 */
	public static String getResourcePath(IResource resource) {
		return resource.getLocation().toOSString();
	}

	/**
	 * decorate an icon given a set of adornment flags. Constants for flag
	 * values are defined in JavaElementImageDescriptor
	 */
	public static ImageDescriptor decorate(ImageDescriptor base, int decorations) {

		Point size = useSmallSize(decorations) ? SMALL_SIZE : BIG_SIZE;
		// Check the image descriptor cache
		String key = new String(base.toString() + ":::" + decorations + ":::"
				+ size.toString());
		// Example key is
		// "URLImageDescriptor(platform:/plugin/org.aspectj.ajde_1.1.0/icons/structure/file-lst.gif):::0:::Point
		// {22, 16}"
		// or
		// "URLImageDescriptor(platform:/plugin/org.eclipse.ui_2.0.2/icons/full/obj16/fldr_obj.gif):::0:::Point
		// {22, 16}"
		if (imageDescriptorCache.get(key) != null) {
			//System.err.println("IDCache hit for "+key);
			return (ImageDescriptor) imageDescriptorCache.get(key);
		} else {
			ImageDescriptor imageDescriptor = new JavaElementImageDescriptor(
					base, decorations, size);
			imageDescriptorCache.put(key, imageDescriptor);
			//System.err.println("IDCache mis for "+key);
			return imageDescriptor;
		}
	}

	/**
	 * determine which icon size to use for a given set of decorations
	 */
	private static boolean useSmallSize(int flags) {
		return (flags & SMALL_ICONS_MASK) != 0;
	}

	/**
	 * Attempt to update the project's build classpath with the AspectJ runtime
	 * library.
	 * 
	 * @param project
	 */
	public static void addAjrtToBuildPath(IProject project) {
		// Locate the aspectjrt.jar file.
		// String ajrtPath =
		//	AspectJPlugin.
		//	getDefault().
		//	getAjdtProjectProperties().
		//	getAspectjrtClasspath();

		//		if (ajrtPath != null)
		//		{
		IJavaProject javaProject = JavaCore.create(project);
		try {
			IClasspathEntry[] originalCP = javaProject.getRawClasspath();
			IClasspathEntry ajrtLIB = JavaCore.newVariableEntry(new Path(
					"ASPECTJRT_LIB"), // library location
					null, // no source
					null // no source
					);
			// Update the raw classpath with the new ajrtCP entry.
			int originalCPLength = originalCP.length;
			IClasspathEntry[] newCP = new IClasspathEntry[originalCPLength + 1];
			System.arraycopy(originalCP, 0, newCP, 0, originalCPLength);
			newCP[originalCPLength] = ajrtLIB;
			javaProject.setRawClasspath(newCP, new NullProgressMonitor());
		} catch (JavaModelException e) {
		}
		//		}
	}

	/**
	 * Adds the AspectJ Nature to the project
	 * 
	 * @param project
	 * @throws CoreException
	 */
	public static void addAspectJNature(IProject project) throws CoreException {
		// add the AspectJ Nature
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 1, prevNatures.length);
		newNatures[0] = AspectJUIPlugin.ID_NATURE;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
		IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(project.getName()+AspectJPreferences.HAS_SET_AJPLUGIN_DEPENDENCY, false);
		
		// Bugzilla 62625
		if (project.hasNature(PDE.PLUGIN_NATURE)) {
			// Dealing with a plugin project. In that case the
			// aspectjrt.jar should be added to the classpath container
			// that lists jars imported from dependent plugins. In order
			// to do this, should add a dependency on the plugin
			// org.aspectj.ajde to the current plugin project.
			
			// Bugzilla 72007
			// Checks if the plugin already has the plugin dependency
			// before adding it, this avoids duplication
			if(!hasAJPluginDependency(project)) {
				getAndPrepareToChangePDEModel(project);
				addAJPluginDependency(project);
				store.setValue(project.getName()+AspectJPreferences.HAS_SET_AJPLUGIN_DEPENDENCY, true);
			}
		} else {
			// A Java project that is not a plugin project. Just add
			// the aspectjrt.jar to the build path.
			addAjrtToBuildPath(project);
			store.setValue(project.getName()+AspectJPreferences.HAS_SET_AJPLUGIN_DEPENDENCY, false);
		}

		// PD: current thinking is not to change project dependencies to class folder ones
		// therefore, have commented out the following call.
		// changeProjectDependencies(project);

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				verifyWorkbenchConfiguration();
			}
		});

		//Luzius: set up build configuration
		BuildConfigurator.getBuildConfigurator().setup(project);
		
		//crete compilation units for .aj files
		AJCompilationUnitManager.INSTANCE.initCompilationUnits(project);
		
		checkMyEclipseNature(project);
		
		refreshPackageExplorer();
	}
	

	/**
	 * (Bug 71540) Detect if user is working with MyEclipse plugin and if yes,
	 * pop up a message box that tells to add aspectjrt.jar to the classpath of
	 * application server where the project gets deployed to.
	 * 
	 * @param The
	 *            project to be checked.
	 */
	public static void checkMyEclipseNature(IProject project){
		try {
			// check project nature
			if (project.hasNature("com.genuitec.eclipse.j2eedt.core.webnature") //$NON-NLS-1$
					|| project.hasNature("com.genuitec.eclipse.j2eedt.core.ejbnature")){ //$NON-NLS-1$
				//display message only once per eclipse startup
				if (!myEclipseMessageDisplayed){
					myEclipseMessageDisplayed = true;
					
					IWorkbenchWindow window = AspectJUIPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow();
					MessageDialog.openInformation(window.getShell(),
							AspectJUIPlugin
							.getResourceString("myEclipse.natureDetected.title"), //$NON-NLS-1$
							AspectJUIPlugin
							.getResourceString("myEclipse.natureDetected.message")); //$NON-NLS-1$
				}
			}
		} catch (CoreException e) {
		}
	}
	
	//flag used by public static void checkMyEclipseNature(IProject project)
	private static boolean myEclipseMessageDisplayed = false;

	/**
	 * Change any project dependencies on the give project to class folder
	 * dependencies. Also, take note of any exported entries on the classpath
	 * and carry those over to depending projects.
	 * 
	 * If a java project has a dependency on an AspectJ project, then this
	 * dependency must be a class folder dependency (and not a project
	 * dependency) otherwise an error marker occurs from the java builder for
	 * all dependent projects saying that they can't be built until the AspectJ
	 * project has been built. Rebuilding doesn't help.
	 * 
	 * @param IProject project
	 */
	public static void changeProjectDependencies(IProject project) {
		List outputLocationPaths = getOutputLocationPaths(project);
		if (outputLocationPaths.size() == 0)
			return;

		IProject[] referencingProjects = project.getReferencingProjects();
		// if there are noone depends on this project then we return
		if (referencingProjects.length == 0) return;
		
		IClasspathEntry[] exportedEntries = getExportedEntries(project);

		for (int i = 0; i < referencingProjects.length; i++) {
			IProject referencingProject = referencingProjects[i];
			try {
				if (!referencingProject.hasNature(JavaCore.NATURE_ID))
					continue;
			} catch (CoreException e1) {
			}

			IJavaProject javaProject = JavaCore.create(referencingProject);
			if (javaProject == null)
				continue;

			try {
				IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
				List newEntries = new ArrayList();
				for (int j = 0; j < cpEntry.length; j++) {
					IClasspathEntry entry = cpEntry[j];
					int entryKind = entry.getEntryKind();
					IPath entryPath = entry.getPath();
					if (entryKind == IClasspathEntry.CPE_PROJECT 
							&& entryPath.equals(project.getFullPath())) {
						if (!projectHasNoSrc(project)) {
							// the following loop has only been tested when
							// outputLocationPaths has
							// size = 1 (because of getOutputLocationPaths())
							for (Iterator iter = outputLocationPaths.iterator(); iter.hasNext();) {
								IPath outputLocationPath = (IPath) iter.next();
								IClasspathEntry classFolderEntry = JavaCore
										.newLibraryEntry(outputLocationPath, null,
												null);
								newEntries.add(classFolderEntry);
							}
						}		
					} else {
						newEntries.add(entry);
					}
				}
				if (exportedEntries.length > 0) {
					for (int j = 0; j < exportedEntries.length; j++) {
						newEntries.add(exportedEntries[j]);
					}
				}
				IClasspathEntry[] newCP = (IClasspathEntry[]) newEntries
						.toArray(new IClasspathEntry[newEntries.size()]);
				javaProject.setRawClasspath(newCP, new NullProgressMonitor());
			} catch (JavaModelException e) {
				continue;
			}
		}
	}

	public static void checkAndChangeDependencies(IProject project) {
		changeProjectDependencies(project);
		try {
			if (project.getPersistentProperty(BuildOptionsAdapter.OUTPUTJAR) != null &&
					!(project.getPersistentProperty(BuildOptionsAdapter.OUTPUTJAR).equals(""))) { 
				IPath pathToOutjar = new Path(project.getPersistentProperty(BuildOptionsAdapter.OUTPUTJAR));
				String outJar2 = AspectJUIPlugin.getDefault().getAjdtProjectProperties().getOutJar();
				IPath pathToOutjar2 = new Path(outJar2);
				IProject[] classFolderReferences = (IProject[]) getDependingProjects(project).get(0);;
				List javaElements= new ArrayList(5);
				List outputLocationPaths = getOutputLocationPaths(project);
				for (int i = 0; i < classFolderReferences.length; i++) {
					IJavaProject javaProject = JavaCore.create(classFolderReferences[i]);
					if (javaProject == null) continue;
					try {
						IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
						List cpEntries = new ArrayList();
						for (int j = 0; j < cpEntry.length; j++) {
							IClasspathEntry entry = cpEntry[j];
							if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {							
								for (Iterator iter = outputLocationPaths.iterator(); iter.hasNext();) {
									IPath path = (IPath) iter.next();
									if (entry.getPath().equals(path)) {
										IClasspathEntry outjarEntry = JavaCore
											.newLibraryEntry(pathToOutjar2, null,null);	
										cpEntries.add(outjarEntry);
									} else {
										cpEntries.add(entry);
									}
								}
							} else {
								cpEntries.add(entry);
							}
						}
						IClasspathEntry[] newCP = (IClasspathEntry[]) cpEntries.toArray(new IClasspathEntry[cpEntries.size()]);
						javaProject.setRawClasspath(newCP, new NullProgressMonitor());
						javaElements.add(javaProject);
					} catch (JavaModelException e) {
					}
				}
				// Forcing a build here if there is an outjar - otherwise have to build the project
				// manually twice.
				System.out.println("about to call build because there was an outjar: project " + project.getName());
				project.build(IncrementalProjectBuilder.FULL_BUILD,"org.eclipse.ajdt.ui.ajbuilder", null, null);
				AspectJUIPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE,null);
			}
		} catch (CoreException e) {
		}		
	}
	
	private static IClasspathEntry[] getExportedEntries(IProject project) {
		List exportedEntries = new ArrayList();

		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return new IClasspathEntry[0];
		}

		try {
			IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
			for (int j = 0; j < cpEntry.length; j++) {
				IClasspathEntry entry = cpEntry[j];
				if (entry.isExported()) {
					// we don't want to export it in the new classpath.
					if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						IClasspathEntry nonExportedEntry = JavaCore
							.newLibraryEntry(entry.getPath(),null,null);
						exportedEntries.add(nonExportedEntry);
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return (IClasspathEntry[]) exportedEntries.toArray(new IClasspathEntry[exportedEntries.size()]);
	}
	
	private static boolean projectHasNoSrc(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return false;
		}

		boolean foundSrc = false;
		try {
			IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
			for (int j = 0; j < cpEntry.length; j++) {
				IClasspathEntry entry = cpEntry[j];
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					foundSrc = true;
					break;
				}
			} 
			if (!foundSrc) return true;
		} catch (JavaModelException e) {
		}
		return false;	
	}
	
	/**
	 * Get the output locations for the project
	 * 
	 * @param project
	 * @return list of IPath objects
	 */
	public static List getOutputLocationPaths(IProject project) {
		List outputLocations = new ArrayList();
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null)
			return outputLocations;

		try {
			// Have been unable to create a user scenario where the following for
			// loop adds something to outputLocations, therefore always
			// fall through to the following if loop. However, if a project has
			// more than one output folder, then this for loop should pick them
			// up.
			// Needs testing.......
			IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
			for (int j = 0; j < cpEntry.length; j++) {
				IClasspathEntry entry = cpEntry[j];
				//int entryKind = entry.getEntryKind();
				int contentKind = entry.getContentKind();
				//if (entryKind == IClasspathEntry.CPE_SOURCE) {
				if (contentKind == ClasspathEntry.K_OUTPUT) {
					if (entry.getOutputLocation() != null) {
						outputLocations.add(entry.getOutputLocation());
					}
				}
			}
			// If we haven't added anything from reading the .classpath
			// file, then use the default output location
			if (outputLocations.size() == 0) {
				outputLocations.add(javaProject.getOutputLocation());
			}
		} catch (JavaModelException e) {
		}
		return outputLocations;
	}

	/**
	 * Change any class folder dependencies on this project to project
	 * dependencies. Also check whether the current project exports anything
	 * and if so, check whether the entry is also on the depending project's
	 * class path. Again, if this is the case, we remove it from
	 * the depending project's classpath as it was added when AJ nature
	 * was added to the project. This is used when AspectJ nature is removed 
	 * from a project
	 */
	private static void changeClassFolderDependencies(IProject project) {
		List outputLocationPaths = getOutputLocationPaths(project);
		if (outputLocationPaths.size() == 0)
			return;
		
		List dependingProjects = getDependingProjects(project);
		IProject[] cfDependingProjects = (IProject[]) dependingProjects.get(0);
		IProject[] elDependingProjects = (IProject[]) dependingProjects.get(1);
		
		if (cfDependingProjects.length == 0 && elDependingProjects.length == 0) return;
		
		JavaProject jp = (JavaProject) JavaCore.create(project);
		List exportedEntries = new ArrayList();
		try {
			IClasspathEntry[] cpe = jp.getRawClasspath();
			for (int i = 0; i < cpe.length; i++) {
				IClasspathEntry entry = cpe[i];
				if (entry.isExported()) {
					exportedEntries.add(entry);
				}
			}
		} catch (JavaModelException e1) {
		}

		if (cfDependingProjects.length == 0 && exportedEntries.size() > 0 
				&& elDependingProjects.length > 0) {
			for (int i = 0; i < elDependingProjects.length; i++) {
				IProject proj = elDependingProjects[i];
				JavaProject javaProject = (JavaProject) JavaCore.create(proj);
				if (javaProject == null)
					continue;
				try {
					IClasspathEntry[] classpathEntries = javaProject.getRawClasspath();
					List originalEntries = new ArrayList();
					for (Iterator iter = exportedEntries.iterator(); iter.hasNext();) {
						IClasspathEntry exportedCPEntry = (IClasspathEntry) iter.next();
						for (int j = 0; j < classpathEntries.length; j++) {
							if (classpathEntries[j].getEntryKind() == IClasspathEntry.CPE_LIBRARY 
									&& classpathEntries[j].getPath().equals(exportedCPEntry.getPath())) {
								originalEntries.add(JavaCore.newProjectEntry(project.getFullPath()));								
							} else {
								originalEntries.add(classpathEntries[j]);
							}
						}
						
					}
					IClasspathEntry[] origCP = (IClasspathEntry[]) originalEntries.toArray(new IClasspathEntry[originalEntries.size()]);
					javaProject.setRawClasspath(origCP,new NullProgressMonitor());
				} catch (JavaModelException e) {
					continue;
				}
			}
		}
		
		for (int i = 0; i < cfDependingProjects.length; i++) {
			IProject proj = cfDependingProjects[i];
			JavaProject javaProject = (JavaProject) JavaCore.create(proj);
			if (javaProject == null)
				continue;
			for (Iterator iterator = outputLocationPaths.iterator(); iterator.hasNext();) {
				IPath path = (IPath) iterator.next();
				try {
					IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
					List newEntries = new ArrayList();
					cpEntries: for (int j = 0; j < cpEntry.length; j++) {
						IClasspathEntry entry = cpEntry[j];
						int entryKind = entry.getEntryKind();
						IPath entryPath = entry.getPath();
						if (entryKind == IClasspathEntry.CPE_LIBRARY) {
							if (entryPath.equals(path)) {
								IClasspathEntry projectEntry = JavaCore.newProjectEntry(project.getFullPath());
								newEntries.add(projectEntry);								
							}
						} else {
							newEntries.add(entry);
						}
					}
					IClasspathEntry[] newCP = (IClasspathEntry[]) newEntries
							.toArray(new IClasspathEntry[newEntries.size()]);
					javaProject.setRawClasspath(newCP,
							new NullProgressMonitor());
				} catch (JavaModelException e) {
					continue;
				}
			}
		}
	}

	/**
	 * Get all projects within the workspace who have a dependency
	 * on the given project - this can either be a class folder dependency
	 * or on a library which the project exports.
	 * 
	 * @param IProject project
	 * @return List of two IProject[] where the first is all the 
	 *         class folder depending projects, and the second is all
	 *         the exported library dependent projects 
	 */
	public static List getDependingProjects(IProject project) {
		List projects = new ArrayList();
		
		IProject[] projectsInWorkspace = AspectJUIPlugin.getWorkspace().getRoot().getProjects();
		List outputLocationPaths = getOutputLocationPaths(project);
		IClasspathEntry[] exportedEntries = getExportedEntries(project);
		List classFolderDependingProjects = new ArrayList();
		List exportedLibraryDependingProjects = new ArrayList();
		
		workThroughProjects: for (int i = 0; i < projectsInWorkspace.length; i++) {
			if (projectsInWorkspace[i].equals(project) || !(projectsInWorkspace[i].isOpen()))
				continue workThroughProjects;
			try {
				if (projectsInWorkspace[i].hasNature(JavaCore.NATURE_ID)) {
					JavaProject javaProject = (JavaProject) JavaCore.create(projectsInWorkspace[i]);
					if (javaProject == null)
						continue workThroughProjects;

					try {
						IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
						for (int j = 0; j < cpEntry.length; j++) {
							IClasspathEntry entry = cpEntry[j];
							if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
								for (Iterator iter = outputLocationPaths.iterator(); iter.hasNext();) {
									IPath path = (IPath) iter.next();
									if (entry.getPath().equals(path)) {
										classFolderDependingProjects.add(projectsInWorkspace[i]);
										continue workThroughProjects;
									}
								}
								for (int k = 0; k < exportedEntries.length; k++) {
									if (entry.getPath().equals(exportedEntries[k].getPath())) {
										exportedLibraryDependingProjects.add(projectsInWorkspace[i]);
									}
								}
							}
						}
					} catch (JavaModelException e) {
						continue workThroughProjects;
					}
				}
			} catch (CoreException e) {
			}
		}
		projects.add(0,(IProject[]) classFolderDependingProjects.toArray(new IProject[] {}));
		projects.add(1,(IProject[]) exportedLibraryDependingProjects.toArray(new IProject[] {}));
		return projects;
	}

	
	/**
	 * Get all projects within the workspace on which the given project has a
	 * class folder dependency
	 * 
	 * @param IProject
	 *            project
	 * @return IProject[]
	 */
	public static IProject[] getRequiredClassFolderProjects(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null)
			return new IProject[0];

		List requiredProjects = new ArrayList();
		IProject[] projectsInWorkspace = AspectJUIPlugin.getWorkspace().getRoot()
				.getProjects();

		iterateOverProjects: for (int i = 0; i < projectsInWorkspace.length; i++) {
			try {
				if (!(projectsInWorkspace[i].isOpen())
						|| !projectsInWorkspace[i]
								.hasNature("org.eclipse.jdt.core.javanature"))
					continue iterateOverProjects;
			} catch (CoreException e1) {
				// nature could not be checked, suppose non-java
				continue iterateOverProjects;
			}
			IProject proj = projectsInWorkspace[i];
			List outputLocationPaths = getOutputLocationPaths(proj);
			for (Iterator iterator = outputLocationPaths.iterator(); iterator
					.hasNext();) {
				IPath path = (IPath) iterator.next();
				try {
					IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
					//List newEntries = new ArrayList();
					for (int j = 0; j < cpEntry.length; j++) {
						IClasspathEntry entry = cpEntry[j];
						int entryKind = entry.getEntryKind();
						IPath entryPath = entry.getPath();
						if (entryKind == IClasspathEntry.CPE_LIBRARY
								&& entryPath.equals(path)) {
							requiredProjects.add(projectsInWorkspace[i]);
							continue iterateOverProjects;
						}
					}
				} catch (JavaModelException e) {
					continue iterateOverProjects;
				}
			}
		}
		IProject[] projects = (IProject[]) requiredProjects
				.toArray(new IProject[] {});
		return projects;
	}

	/**
	 * When AspectJ nature is removed, need to remove markers on referencing
	 * projects which say that the current project needs to be built
	 */
	private static void removeMarkerOnReferencingProjects(IProject project) {
		try {
			String errorMessage = "The project cannot be built until its prerequisite "
					+ project.getName()
					+ " is rebuilt. Cleaning and rebuilding all projects is recommended";

			IProject[] refProjects = project.getReferencingProjects();
			// only get the class folder depending projects here
			IProject[] classFolderReferences = (IProject[]) getDependingProjects(project).get(0);
			IProject[] referencingProjects = new IProject[refProjects.length
					+ classFolderReferences.length];
			for (int i = 0; i < refProjects.length; i++) {
				referencingProjects[i] = refProjects[i];
			}
			for (int i = 0; i < classFolderReferences.length; i++) {
				referencingProjects[i + refProjects.length] = classFolderReferences[i];
			}

			for (int i = 0; i < referencingProjects.length; i++) {
				IProject referencingProject = referencingProjects[i];
				IMarker[] problemMarkers = referencingProject.findMarkers(
						IMarker.PROBLEM, false, IResource.DEPTH_INFINITE);
				if (problemMarkers.length > 0) {
					for (int j = 0; j < problemMarkers.length; j++) {
						IMarker marker = problemMarkers[j];
						int markerSeverity = marker.getAttribute(
								IMarker.SEVERITY, -1);
						String markerMessage = marker.getAttribute(
								IMarker.MESSAGE, "no message");

						if (markerSeverity == IMarker.SEVERITY_ERROR
								&& markerMessage.equals(errorMessage)) {
							marker.delete();
						}
					}
				}
			}
		} catch (CoreException e) {
		}
	}

	/**
	 * @param project
	 */
	private static void addAJPluginDependency(IProject project) {
		IWorkbenchWindow window = AspectJUIPlugin.getDefault().getWorkbench()
				.getActiveWorkbenchWindow();

		boolean autoImport = false;
		if ((AspectJPreferences.askPDEAutoImport() && confirmPDEAutoImport(window))
				|| (AspectJPreferences.doPDEAutoImport())) {
			autoImport = true;
		}

		if (autoImport) {
			importRuntimePlugin(project);
		} else {
			MessageDialog
					.openWarning(
							window.getShell(),
							AspectJUIPlugin
									.getResourceString("NoAutoPluginImportDialog.title"),
							AspectJUIPlugin
									.getResourceString("NoAutoPluginImportDialog.message"));
		}
	}

	/**
	 * @param project
	 */
	private static void importRuntimePlugin(IProject project) {
		ManifestEditor manEd = getAndPrepareToChangePDEModel(project);
		if (manEd != null) {
			IPluginModel model = (IPluginModel) manEd.getAggregateModel();
			try {
				addImportToPDEModel(model, AspectJUIPlugin.RUNTIME_PLUGIN_ID);
				manEd.doSave(new NullProgressMonitor());

				// Forced build necessary here. When the project has the new
				// nature given to it a build occurs and - in the scenario
				// where the user is contemplating the "automatically add
				// dependency for you ?" dialog - a build error will occur
				// because the runtime jar cannot be located. If they agree
				// to the automatic dependency import then this build should
				// remove that compile error from their problems view.
				// The above scenario will not occur in the future if the
				// user tells the dialog not to ask again.
				project.build(IncrementalProjectBuilder.FULL_BUILD,
						"org.eclipse.ajdt.ui.ajbuilder", null, null);

			} catch (CoreException e) {
			}
		}// end if we got a reference to the manifest editor
		else {
			MessageDialog
					.openError(
							AspectJUIPlugin.getDefault().getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							AspectJUIPlugin
									.getResourceString("AutoPluginImportDialog.noEditor.title"),
							AspectJUIPlugin
									.getResourceString("AutoPluginImportDialog.noEditor.message"));
		}
	}

	/**
	 * @param model
	 * @param importId
	 * @throws CoreException
	 */
	private static void addImportToPDEModel(IPluginModel model, String importId)
			throws CoreException {
		IPluginImport importNode = model.getPluginFactory().createImport();
		importNode.setId(importId);
		model.getPluginBase().add(importNode);
		IFile manifestFile = (IFile) model.getUnderlyingResource();
		manifestFile.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
	}

	/**
	 * Returns the manifest editor if it is open in the workspace. Note: You
	 * should switch to the PDE dependency management page before changing the
	 * dependencies to avoid update inconsistencies across the pages. To do this
	 * use the AJDTUtils.prepareToChangePDEModel(IProject) method.
	 * 
	 * @param project
	 * @return
	 */
	public static ManifestEditor getPDEManifestEditor(IProject project) {
		// Must have already been validated as a PDE project
		// to get to this method. Now get the id of the plugin
		// being developed in current project.
		String pluginId = PDECore.getDefault().getWorkspaceModelManager()
				.getWorkspacePluginModel(project).getPluginBase().getId();
		
		// Attempt to get hold of the open manifest editor
		// for the current project.
		ManifestEditor manEd = null;

		IEditorReference[] eRefs = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage()
				.getEditorReferences();
		for (int i = 0; i < eRefs.length; i++) {
			IEditorReference er = eRefs[i];
			if (er.getId().equals(IPDEUIConstants.MANIFEST_EDITOR_ID)
					&& er.getPartName().equals(pluginId)) {
				IEditorReference manEdRef = er;
				manEd = (ManifestEditor) manEdRef.getPart(true);
				break;
			}
		}// end for

		return manEd;
	}
	
	
	/**
	 * It is necessary to call this method before updating the pde model
	 * otherwise the changes may not be consistant across the pages.
	 */	
	public static ManifestEditor getAndPrepareToChangePDEModel(IProject project) {
		// Must have already been validated as a PDE project
		// to get to this method. Now get the id of the plugin
		// being developed in current project.

		String pluginId = PDECore.getDefault().getWorkspaceModelManager()
		.getWorkspacePluginModel(project).getPluginBase().getId();

		// Open the manifest editor if it is not already open.
		ManifestEditor.openPluginEditor(pluginId);
		ManifestEditor manEd = getPDEManifestEditor(project);
		
		// IMPORTANT
		// Necessary to force the active page to be the dependency management
		// page. If this is not done then there is a chance that the model
		// will not be updated consistently across the pages.
		if (manEd != null) {
			manEd.setActivePage(DependenciesPage.PAGE_ID);
		}
		return manEd;
	}

	public static void verifyWorkbenchConfiguration() {
		/*
		 * JWP: new code to popup the AJDT Preferences Configuration Wizard
		 * Since the finish() method of the AspectProjectWizard calls this
		 * method, this code covers both the case where an existing project is
		 * converted to an AspectJ project, and when the AspectProjectWizard's
		 * finish is invoked
		 */
		// First check to see whether we should popup the wizard
		boolean showWizard = true;
		// 1. Has the wizard been popped up before, and the user said
		// "don't ask me again"?
		if (AspectJPreferences.isAJDTPrefConfigDone()) {
			showWizard = false;
		}
		// 2. Have all of the settings already been set appropriately?
		if (AJDTConfigSettings.isAnalyzeAnnotationsDisabled()
				&& AJDTConfigSettings.isAspectJEditorDefault()
		/* && AJDTConfigSettings.isUnusedImportsDisabled() */) {
			showWizard = false;
		}

		// override: always show wizard if this is the first time running
		// on AJDT 1.2.0 (because the editor has changed id)
		if (!AspectJPreferences.isRunAJDT120()) {
			showWizard = true;
		}
		
		if (showWizard && !AspectJPreferences.isAJDTPrefConfigShowing()) {
			AspectJPreferences.setAJDTPrefConfigShowing(true);
			// make sure we don't run the wizard twice
			AspectJPreferences.setRunAJDT120(true);
			// Create and initialize the AJDT Preferences Configuration Wizard
			org.eclipse.ajdt.internal.ui.wizards.AJDTPrefConfigWizard wizard = new org.eclipse.ajdt.internal.ui.wizards.AJDTPrefConfigWizard();
			wizard.init();
			// Create the wizard dialog
			org.eclipse.jface.wizard.WizardDialog dialog = new org.eclipse.jface.wizard.WizardDialog(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getShell(), wizard);
			// Open the wizard dialog
			dialog.open();
		}
	}

	/**
	 * Removes the AspectJ Nature from an existing AspectJ project.
	 * 
	 * @param project
	 * @throws CoreException
	 */
	public static void removeAspectJNature(IProject project)
			throws CoreException {

		//remove compilation units for .aj files
		//(the way it is currently implemented, this must happen before nature gets removed)
		AJCompilationUnitManager.INSTANCE.removeCUsfromJavaModel(project);
		
		/* Clear any warnings and errors from the Tasks window BUG-FIX#40344 */
		AspectJUIPlugin ajPlugin = AspectJUIPlugin.getDefault();
		ajPlugin.setCurrentProject(project);
		ajPlugin.getAjdtProjectProperties().clearMarkers(true);

		// remove the AspectJ Nature
		IProjectDescription description = project.getDescription();
		String[] prevNatures = description.getNatureIds();
		String[] newNatures = new String[prevNatures.length - 1];
		int newPosition = 0;
		for (int i = 0; i < prevNatures.length; i++) {
			if (!prevNatures[i].equals(AspectJUIPlugin.ID_NATURE)) {
				// guard against array out of bounds which will occur if we
				// get to here in a project that DOES NOT have the aj nature
				// (should never happen).
				if (newPosition < newNatures.length) {
					newNatures[newPosition++] = prevNatures[i];
				} else {
					// exception... atempt to remove ajnature from a project
					// that
					// doesn't have it. Leave the project natures unchanged.
					newNatures = prevNatures;
					break;
				}// end else
			}// end if
		}// end for
		description.setNatureIds(newNatures);
		project.setDescription(description, null);

		// Bugzilla 62625
		if (project.hasNature(PDE.PLUGIN_NATURE)) {
			// Bugzilla 72007
			// Checks if it was ajdt that added the ajde dependancy and removes
			// it if it was
			IPreferenceStore store = AspectJUIPlugin.getDefault().getPreferenceStore();
			store.setDefault(project.getName()+AspectJPreferences.HAS_SET_AJPLUGIN_DEPENDENCY, false);
			boolean AJPluginDependancySetByAddAJNature = store.getBoolean(project.getName()+AspectJPreferences.HAS_SET_AJPLUGIN_DEPENDENCY);
			if(hasAJPluginDependency(project) && AJPluginDependancySetByAddAJNature) {
				getAndPrepareToChangePDEModel(project);
				removeAJPluginDependency(project);
				store.setValue(project.getName()+AspectJPreferences.HAS_SET_AJPLUGIN_DEPENDENCY, false);
			}
		} else {
			// Update the build classpath to try and remove the aspectjrt.jar
			removeAjrtFromBuildPath(project);
		}

		// PD: current thinking is not to change project dependencies to class folder ones
		// therefore, no need to change classfolder dependencies back to project ones.
		// changeClassFolderDependencies(project);
		removeMarkerOnReferencingProjects(project);
		
		//Luzius: tell build configurator aj nature has been removed
		BuildConfigurator.getBuildConfigurator().restoreJDTState(project);
		
		// Bugzilla 77179: remove resource listener
		AspectJUIPlugin.getDefault().disableBuildConfiguratorResourceChangeListener();
		
		//Ensures the project icon refreshes
		AJDTUtils.refreshPackageExplorer();
	}

	// Bugzilla 72007
	// This method checks wether the project already requires
	// org.aspectj.ajde to be imported. Returns true if it does.
	private static boolean hasAJPluginDependency(IProject project) {
		
 		ManifestEditor manEd = getPDEManifestEditor(project);
		IPluginModel model = null;
		IPluginImport[] imports = null;
		
		if (manEd != null) {
			model = (IPluginModel) manEd.getAggregateModel();
			imports = model.getPluginBase().getImports();
		}
		else {
			try {
				//checks the classpath for plugin dependencies
				IPackageFragmentRoot[] dependencies = JavaCore.create(project).getPackageFragmentRoots();
				for(int i = 0; i< dependencies.length; i++) {
					if(dependencies[i].getElementName().equals(AspectJPreferences.AJDE_JAR))
						return true;
				}
			} catch(JavaModelException e) {
			}
			return false;	
		}

		IPluginImport doomed = null;
		for (int i = 0; i < imports.length; i++) {
			IPluginImport importObj = imports[i];
			if (importObj.getId().equals(AspectJUIPlugin.RUNTIME_PLUGIN_ID)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param project
	 */
	private static void removeAJPluginDependency(IProject project) {
		// Attempt to get hold of the open manifest editor
		// for the current project.
		ManifestEditor manEd = getPDEManifestEditor(project);

		if (manEd != null) {
			IPluginModel model = (IPluginModel) manEd.getAggregateModel();
			try {
				removeImportFromPDEModel(model, AspectJUIPlugin.RUNTIME_PLUGIN_ID);
				manEd.doSave(new NullProgressMonitor());
			} catch (CoreException e) {
				ErrorDialog
						.openError(
								AspectJUIPlugin.getDefault()
										.getActiveWorkbenchWindow().getShell(),
								AspectJUIPlugin
										.getResourceString("AutoPluginRemoveErrorDialog.title"),
								AspectJUIPlugin
										.getResourceString("AutoPluginRemoveErrorDialog.message"),
								e.getStatus());
			}
		}// end if we got a reference to the manifest editor
		else {
			MessageDialog
					.openError(
							AspectJUIPlugin.getDefault().getWorkbench()
									.getActiveWorkbenchWindow().getShell(),
							AspectJUIPlugin
									.getResourceString("AutoPluginRemoveDialog.noEditor.title"),
							AspectJUIPlugin
									.getResourceString("AutoPluginRemoveDialog.noEditor.message"));
		}
	}

	/**
	 * @param model
	 * @param importId
	 * @throws CoreException
	 */
	private static void removeImportFromPDEModel(IPluginModel model,
			String importId) throws CoreException {
		IPluginImport[] imports = model.getPluginBase().getImports();
		IPluginImport doomed = null;

		for (int i = 0; i < imports.length; i++) {
			IPluginImport importObj = imports[i];
			if (importObj.getId().equals(importId)) {
				doomed = importObj;
				break;
			}
		}// end for

		if (doomed != null) {
			model.getPluginBase().remove(doomed);
		}

		IFile manifestFile = (IFile) model.getUnderlyingResource();
		manifestFile.refreshLocal(IResource.DEPTH_INFINITE,
				new NullProgressMonitor());
	}

	/**
	 * Attempt to update the project's build classpath by removing any occurance
	 * of the AspectJ runtime library.
	 * 
	 * @param project
	 */
	public static void removeAjrtFromBuildPath(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		try {
			IClasspathEntry[] originalCP = javaProject.getRawClasspath();
			ArrayList tempCP = new ArrayList();

			// Go through each current classpath entry one at a time. If it
			// is not a reference to the aspectjrt.jar then do not add it
			// to the collection of new classpath entries.
			for (int i = 0; i < originalCP.length; i++) {
				IPath path = originalCP[i].getPath();
				if (!path.toOSString().endsWith("ASPECTJRT_LIB")
						&& !path.toOSString().endsWith("aspectjrt.jar")) {
					tempCP.add(originalCP[i]);
				}
			}// end for

			// Set the classpath with only those elements that survived the
			// above filtration process.
			if (originalCP.length != tempCP.size()) {
				IClasspathEntry[] newCP = (IClasspathEntry[]) tempCP
						.toArray(new IClasspathEntry[tempCP.size()]);
				javaProject.setRawClasspath(newCP, new NullProgressMonitor());
			}// end if at least one classpath element removed
		} catch (JavaModelException e) {
		}
	}

	/**
	 * @param current
	 */
	public static void verifyAjrtVersion(IProject current) {
		IJavaProject javaProject = JavaCore.create(current);
		String ajrtPath = AspectJUIPlugin.getDefault().getAjdtProjectProperties()
				.getAspectjrtClasspath();
		try {
			IClasspathEntry[] originalCP = javaProject.getRawClasspath();
			ArrayList tempCP = new ArrayList();

			boolean changed = false;

			// Go through each current classpath entry one at a time. If it is a
			// reference to aspectjrt.jar
			// replace it - I could look through each reference to check if it
			// is now invalid - but I don't ...
			for (int i = 0; i < originalCP.length; i++) {
				IPath path = originalCP[i].getPath();
				if (path.toOSString().endsWith("aspectjrt.jar")) {
					IClasspathEntry ajrtCP = JavaCore.newLibraryEntry(new Path(
							ajrtPath), // library location
							null, // no source
							null // no source
							);
					tempCP.add(ajrtCP);
					changed = true;
					AJDTEventTrace.generalEvent("In project "
							+ current.getName() + " - replacing "
							+ originalCP[i].getPath() + " with "
							+ ajrtCP.getPath());
				} else {
					tempCP.add(originalCP[i]);
				}

			}

			// Set the classpath with only those elements that survived the
			// above filtration process.
			if (changed) {
				IClasspathEntry[] newCP = (IClasspathEntry[]) tempCP
						.toArray(new IClasspathEntry[tempCP.size()]);
				javaProject.setRawClasspath(newCP, new NullProgressMonitor());
			}
		} catch (JavaModelException e) {
			// Thrown if attempted to add a duplicate classpath entry.
		}
	}

	/**
	 * Prompts the user for whether to auto import aspectj runtime plugin when
	 * giving aspectj nature to PDE project.
	 * 
	 * @return <code>true</code> if it's OK to import, <code>false</code>
	 *         otherwise
	 */
	private static boolean confirmPDEAutoImport(IWorkbenchWindow window) {

		MessageDialogWithToggle dialog = MessageDialogWithToggle
				.openQuestion(
						window.getShell(),
						AspectJUIPlugin
								.getResourceString("PluginImportDialog.importConfirmTitle"),
						AspectJUIPlugin
								.getResourceString("PluginImportDialog.importConfirmMsg"),
						AspectJUIPlugin
								.getResourceString("PluginImportDialog.importConfirmToggleMsg"),
						false); // toggle is initially unchecked

		int result = dialog.getReturnCode();

		if (result >= 0 && dialog.getToggleState()) {
			if (result == 0) {
				// User chose Yes/Don't ask again, so always switch
				AspectJPreferences.setDoPDEAutoImport(true);
				AspectJPreferences.setAskPDEAutoImport(false);
			} else {
				// User chose No/Don't ask again, so never switch
				AspectJPreferences.setDoPDEAutoImport(false);
				AspectJPreferences.setAskPDEAutoImport(false);
			}
		}// end if
		return result == 0;
	}

	/**
	 * Decorate icon based on modifiers, errors etc.
	 * Possible decorations are: abstract, final, synchronized, static,
	 * runnable, warning, error, overrides, implements
	 */
	public static ImageDescriptor decorate( ImageDescriptor base, IProgramElement pNode ) {
		int flags = 0;
		if (pNode != null) {
			List modifiers = pNode.getModifiers();
			if ( modifiers != null ) { 
				if (modifiers.contains(IProgramElement.Modifiers.ABSTRACT)) {
					flags = flags | JavaElementImageDescriptor.ABSTRACT;
				}  
				if (modifiers.contains(IProgramElement.Modifiers.FINAL)) {
					flags = flags | JavaElementImageDescriptor.FINAL;
				} 
				if (modifiers.contains(IProgramElement.Modifiers.SYNCHRONIZED)) {
					flags = flags | JavaElementImageDescriptor.SYNCHRONIZED;
				} 
				if (modifiers.contains(IProgramElement.Modifiers.STATIC)) {
					flags = flags | JavaElementImageDescriptor.STATIC;
				}	
			}	
			if ( pNode.getKind() == IProgramElement.Kind.CONSTRUCTOR ||
				 pNode.getKind() == IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR) {
				flags = flags | JavaElementImageDescriptor.CONSTRUCTOR;	
			}
			if ( pNode.isRunnable( ) ) {
				flags = flags | JavaElementImageDescriptor.RUNNABLE;
			}
			if ( pNode.isOverrider( ) ) {
				flags = flags | JavaElementImageDescriptor.OVERRIDES;
			}
			if ( pNode.isImplementor( ) ) {
				flags = flags | JavaElementImageDescriptor.IMPLEMENTS;
			}
			IMessage sMessage = pNode.getMessage();
			if ( sMessage != null ) {
				if ( sMessage.getKind() == IMessage.ERROR ) {
					flags = flags | JavaElementImageDescriptor.ERROR;
				} else if ( sMessage.getKind() == IMessage.WARNING ) {
					flags = flags | JavaElementImageDescriptor.WARNING;
				}
			}
		}
		return decorate( base, flags );
	}
	
	public static void refreshPackageExplorer(){
		//refresh package explorer (needs to be done by UI thread)
		Runnable r = new Runnable(){
			public void run(){
				PackageExplorerPart pep = PackageExplorerPart.getFromActivePerspective();
				if (pep != null)
					pep.getTreeViewer().refresh();
			}
		};
		AspectJUIPlugin.getDefault().getDisplay().asyncExec(r);
	}
}