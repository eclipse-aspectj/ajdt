/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *     Matt Chapman - moved getAspectjrtClasspath here from ui plugin (84967)
 *******************************************************************************/
package org.eclipse.ajdt.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaProject;
import org.osgi.framework.Bundle;

/**
 * 
 * @author mchapman
 */
public class CoreUtils {

    public static final String PLUGIN_ID = "org.eclipse.ajdt.ui"; //$NON-NLS-1$
    public static final String ID_NATURE = PLUGIN_ID + ".ajnature"; //$NON-NLS-1$

    private static final String CLASSES = "classes";
    private static final String SOURCE = "source";
    /**
	 * Computed classpath to aspectjrt.jar
	 */
	private static String aspectjrtPath = null;
	
    private static String aspectjrtSourcePath = null;
    
    private static boolean sourceCheckDone = false;
    private static boolean rtCheckDone = false;

	/**
	 * Return the fully-qualified name of the root directory for a project.
	 */
	public static String getProjectRootDirectory(IProject project) {
		return project.getLocation().toOSString();
	}

	public static interface FilenameFilter {
		public boolean accept(String name);
	}

	public static final FilenameFilter ASPECTJ_SOURCE_ONLY_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return (name.endsWith(".aj")); //$NON-NLS-1$
		}
	};
	
	public static final FilenameFilter ASPECTJ_SOURCE_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return (name.endsWith(".java") || name.endsWith(".aj"));  //$NON-NLS-1$ //$NON-NLS-2$
		}
	};

	public static final FilenameFilter RESOURCE_FILTER = new FilenameFilter() {
		public boolean accept(String name) {
			return !(name.endsWith(".java") || name.endsWith(".aj") || name  //$NON-NLS-1$ //$NON-NLS-2$
					.endsWith(".class")); //$NON-NLS-1$
		}
	};

	/**
	 * Get the aspectjrt.jar classpath entry. This is usually in
	 * plugins/org.aspectj.ajde_ <VERSION>/aspectjrt.jar
	 */
	public synchronized static String getAspectjrtClasspath() {
		if (aspectjrtPath == null && !rtCheckDone) {
		    rtCheckDone = true;
		    aspectjrtPath = internalGetPath(AspectJPlugin.RUNTIME_PLUGIN_ID, false);
		    if (aspectjrtPath == null) {
    		    AspectJPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, AspectJPlugin.PLUGIN_ID, 
    		            "Could not find AspectJ runtime."));
		    }
		}
		return aspectjrtPath;
	}
	
	private static String internalGetPath(String bundleId, boolean useSource) {
        Bundle bundle = Platform
                .getBundle(bundleId);
    
        if (bundle != null) {
            URL installLoc = bundle.getEntry("/"); //$NON-NLS-1$
            URL resolved = null;
            try {
                resolved = FileLocator.resolve(installLoc);
                String fullPath = resolved.getFile();
                // !/ indicates a location inside of a jar
                if (fullPath.endsWith("!/")) {
                    fullPath = fullPath.substring(0, fullPath.length()-2);
                }
                if (fullPath.startsWith("file:")) {
                    fullPath = new URL(fullPath).getFile();
                }
                File ajrt = new File(fullPath);
                if (ajrt.exists()) {
                    if (ajrt.isDirectory()) {
                        // in a runtime workbench
                        ajrt = new File(ajrt, useSource ? SOURCE : CLASSES);
                    }
                    return ajrt.getCanonicalPath();
                }
            } catch (IOException e) {
            }
        }
        return null;
    }

    public synchronized static String getAspectjrtSourcePath() throws IOException {
        if (aspectjrtSourcePath == null && !sourceCheckDone) {
            sourceCheckDone = true;
            String aspectjrtClasspath = getAspectjrtClasspath();
            if (aspectjrtClasspath != null) {
                if (aspectjrtClasspath.endsWith(".jar")) {
                    int ajrtIndex = aspectjrtClasspath.lastIndexOf(AspectJPlugin.RUNTIME_PLUGIN_ID) + AspectJPlugin.RUNTIME_PLUGIN_ID.length();
                    aspectjrtSourcePath = aspectjrtClasspath.substring(0, ajrtIndex) + "." + SOURCE + aspectjrtClasspath.substring(ajrtIndex);
                } else if (aspectjrtClasspath.endsWith(CLASSES)) {
                    // runtime workbench
                    aspectjrtSourcePath = aspectjrtClasspath.substring(0, aspectjrtClasspath.length() - CLASSES.length()) + SOURCE;
                }
            }
        }
        return aspectjrtSourcePath;
    }

	/**
	 * Get all projects within the workspace who have a dependency on the given
	 * project - this can either be a class folder dependency or on a library
	 * which the project exports.
	 * 
	 * @param IProject
	 *            project
	 * @return List of two IProject[] where the first is all the class folder
	 *         depending projects, and the second is all the exported library
	 *         dependent projects
	 */
	public static List<IProject[]> getDependingProjects(IProject project) {
		List<IProject[]> projects = new ArrayList<IProject[]>();

		IProject[] projectsInWorkspace = AspectJPlugin.getWorkspace()
				.getRoot().getProjects();
		List<IPath> outputLocationPaths = getOutputLocationPaths(project);
		IClasspathEntry[] exportedEntries = getExportedEntries(project);
		List<IProject> classFolderDependingProjects = new ArrayList<IProject>();
		List<IProject> exportedLibraryDependingProjects = new ArrayList<IProject>();

		workThroughProjects: for (int i = 0; i < projectsInWorkspace.length; i++) {
			if (projectsInWorkspace[i].equals(project)
					|| !(projectsInWorkspace[i].isOpen()))
				continue workThroughProjects;
			try {
				if (projectsInWorkspace[i].hasNature(JavaCore.NATURE_ID)) {
					JavaProject javaProject = (JavaProject) JavaCore
							.create(projectsInWorkspace[i]);
					if (javaProject == null)
						continue workThroughProjects;

					try {
						IClasspathEntry[] cpEntry = javaProject
								.getRawClasspath();
						for (int j = 0; j < cpEntry.length; j++) {
							IClasspathEntry entry = cpEntry[j];
							if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
								for (Iterator<IPath> iter = outputLocationPaths
										.iterator(); iter.hasNext();) {
									IPath path = iter.next();
									if (entry.getPath().equals(path)) {
										classFolderDependingProjects
												.add(projectsInWorkspace[i]);
										continue workThroughProjects;
									}
								}
								for (int k = 0; k < exportedEntries.length; k++) {
									if (entry.getPath().equals(
											exportedEntries[k].getPath())) {
										exportedLibraryDependingProjects
												.add(projectsInWorkspace[i]);
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
		projects.add(0, classFolderDependingProjects
				.toArray(new IProject[] {}));
		projects.add(1, exportedLibraryDependingProjects
				.toArray(new IProject[] {}));
		return projects;
	}

	private static IClasspathEntry[] getExportedEntries(IProject project) {
		List<IClasspathEntry> exportedEntries = new ArrayList<IClasspathEntry>();

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
								.newLibraryEntry(entry.getPath(), null, null);
						exportedEntries.add(nonExportedEntry);
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return (IClasspathEntry[]) exportedEntries
				.toArray(new IClasspathEntry[exportedEntries.size()]);
	}

	/**
	 * Get the output locations for the project
	 * 
	 * @param project
	 * @return list of IPath objects
	 */
	public static List<IPath> getOutputLocationPaths(IProject project) {
		List<IPath> outputLocations = new ArrayList<IPath>();
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null)
			return outputLocations;

		try {
			// Have been unable to create a user scenario where the following
			// for
			// loop adds something to outputLocations, therefore always
			// fall through to the following if loop. However, if a project has
			// more than one output folder, then this for loop should pick them
			// up.
			// Needs testing.......
			IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
			for (int j = 0; j < cpEntry.length; j++) {
				IClasspathEntry entry = cpEntry[j];
				int contentKind = entry.getContentKind();
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
	
	public static IPath[] getOutputFolders(IJavaProject project) throws CoreException {
		List<IPath> paths = new ArrayList<IPath>();
		paths.add(project.getOutputLocation());
		IClasspathEntry[] cpe = project.getRawClasspath();
		for (int i = 0; i < cpe.length; i++) {
			if (cpe[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				IPath output = cpe[i].getOutputLocation();
				if (output != null) {
					paths.add(output);
				}
			}
		}
		return (IPath[])paths.toArray(new IPath[paths.size()]);
	}
	
	   public static boolean isAJProject(IProject project) {
	        if((project!=null) && project.isOpen()) {           
	            try {
	                if (project.hasNature(ID_NATURE)) { 
	                    return true;
	                }
	            } catch (CoreException e) {
	            }
	        }
	        return false;
	    }

    /**
     * Converts an AspectJ style signature in chars to 
     * a Java style signature as an array of String
     * 
     * Replace the 'P' for parameterized to 'L' for resolved
     */
    public static String[] listAJSigToJavaSig(List<char[]> chars) {
        if (chars != null) {
            String[] result = new String[chars.size()];
            int index = 0;
            for (Iterator<char[]> charsIter = chars.iterator(); charsIter.hasNext(); index++) {
                char[] c = charsIter.next();
                if (c == null) {
                    result[index] = "";
                    continue;
                }
                boolean wasLessThan = true;
                for (int i = 0; i < c.length; i++) {
                    if (wasLessThan) {
                        if (c[i] == 'P') { // Java spec does not use 'P' for parameterized types, AspectJ does
                            c[i] = 'L';
                        }
                        wasLessThan = false;
                    } else {
                        switch (c[i]) {
                            case '<':
                                wasLessThan = true;
                                break;
                                
                            case '/':
                                c[i] = '.';
    	                        wasLessThan = false;
                                break;
                        }
                    }
                }
                
                result[index] = new String(c);
            }
            return result;
        }
        return new String[0];
    }

    public static char[][] listStringsToCharArrays(List<String> strings) {
        if (strings != null) {
    	    char[][] result = new char[strings.size()][];
    	    int index = 0;
    	    for (Iterator<String> stringIter = strings.iterator(); stringIter.hasNext(); index++) {
                String string = stringIter.next();
                result[index] = string != null ? string.toCharArray() : "".toCharArray();
            }
    	    return result;
        }
        return new char[0][];
    }

    public static char[][] listCharsToCharArrays(List<char[]> strings) {
        if (strings != null) {
            char[][] result = new char[strings.size()][];
            int index = 0;
            for (Iterator<char[]> stringIter = strings.iterator(); stringIter.hasNext(); index++) {
                char[] string = stringIter.next();
                result[index] = string != null ? string : "".toCharArray();
            }
            return result;
        }
        return new char[0][];
    }
}
