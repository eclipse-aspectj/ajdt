/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Project specific settings. These used to be in the UI plugin, so the UI node
 * names have been kept for compatibility.
 */
public class AspectJCorePreferences {
			
    public static final String OPTION_AutobuildSuppressed = "org.eclipse.ajdt.ui.builder.autobuildSuppressed"; //$NON-NLS-1$

    
	private static final String ASPECTPATH_ATTRIBUTE_NAME = "org.eclipse.ajdt.aspectpath"; //$NON-NLS-1$
	
	private static final String INPATH_ATTRIBUTE_NAME = "org.eclipse.ajdt.inpath"; //$NON-NLS-1$

	public static final IClasspathAttribute ASPECTPATH_ATTRIBUTE = JavaCore.newClasspathAttribute(
			ASPECTPATH_ATTRIBUTE_NAME, "true"); //$NON-NLS-1$

	public static final IClasspathAttribute INPATH_ATTRIBUTE = JavaCore.newClasspathAttribute(
			INPATH_ATTRIBUTE_NAME, "true"); //$NON-NLS-1$

    public static final String OUT_JAR = "org.eclipse.ajdt.ui.outJar"; //$NON-NLS-1$

    public static final String INPATH_OUT_FOLDER = "org.eclipse.ajdt.ui.inpathOutFolder"; //$NON-NLS-1$

	public static final String ASPECTPATH = "org.eclipse.ajdt.ui.aspectPath"; //$NON-NLS-1$

	public static final String ASPECTPATH_CON_KINDS = "org.eclipse.ajdt.ui.aspectPath.contentKind"; //$NON-NLS-1$

	public static final String ASPECTPATH_ENT_KINDS = "org.eclipse.ajdt.ui.aspectPath.entryKind"; //$NON-NLS-1$

	public static final String INPATH = "org.eclipse.ajdt.ui.inPath"; //$NON-NLS-1$

	public static final String INPATH_CON_KINDS = "org.eclipse.ajdt.ui.inPath.contentKind"; //$NON-NLS-1$

	public static final String INPATH_ENT_KINDS = "org.eclipse.ajdt.ui.inPath.entryKind"; //$NON-NLS-1$

    public static String getProjectOutJar(IProject project) {
        IScopeContext projectScope = new ProjectScope(project);
        IEclipsePreferences projectNode = projectScope
                .getNode(AspectJPlugin.UI_PLUGIN_ID);
        return projectNode.get(OUT_JAR, ""); //$NON-NLS-1$
    }

    public static String getProjectInpathOutFolder(IProject project) {
        IScopeContext projectScope = new ProjectScope(project);
        IEclipsePreferences projectNode = projectScope
                .getNode(AspectJPlugin.UI_PLUGIN_ID);
        return projectNode.get(INPATH_OUT_FOLDER, null);
    }

    public static void setProjectOutJar(IProject project, String value) {
        IScopeContext projectScope = new ProjectScope(project);
        IEclipsePreferences projectNode = projectScope
                .getNode(AspectJPlugin.UI_PLUGIN_ID);
        projectNode.put(OUT_JAR, value);
        if (value.length() == 0) {
            projectNode.remove(OUT_JAR);
        }
        try {
            projectNode.flush();
        } catch (BackingStoreException e) {
        }
    }

    public static void setProjectInpathOutFolder(IProject project, String value) {
        IScopeContext projectScope = new ProjectScope(project);
        IEclipsePreferences projectNode = projectScope
                .getNode(AspectJPlugin.UI_PLUGIN_ID);
        if (value == null || value.length() == 0) {
            projectNode.remove(INPATH_OUT_FOLDER);
        } else {
            projectNode.put(INPATH_OUT_FOLDER, value);
        }
        try {
            projectNode.flush();
        } catch (BackingStoreException e) {
        }
    }

	public static void setProjectAspectPath(IProject project, String path,
			String cKinds, String eKinds) {
		IJavaProject javaProject = JavaCore.create(project);		
		removeAttribute(javaProject, ASPECTPATH_ATTRIBUTE);
		
		StringTokenizer pathTok = new StringTokenizer(path, File.pathSeparator);
		StringTokenizer eKindsTok = new StringTokenizer(eKinds, File.pathSeparator);
		int index = 1;
		while (pathTok.hasMoreTokens() && eKindsTok.hasMoreTokens()) {
			String entry = pathTok.nextToken();
			int eKind = Integer.parseInt(eKindsTok.nextToken());
			addToAspectPath(project,entry, eKind);
			index++;
		}
	}

	
    public static String[] getRawProjectAspectPath(IProject project) {
        return internalGetProjectPath(project, ASPECTPATH_ATTRIBUTE, false);
    }
    
    public static String[] getResolvedProjectAspectPath(IProject project) {
        return internalGetProjectPath(project, ASPECTPATH_ATTRIBUTE, true);
    }
    

	public static void addToAspectPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		addAttribute(jp,entry,AspectJCorePreferences.ASPECTPATH_ATTRIBUTE);
	}

	public static void removeFromAspectPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		removeAttribute(jp,entry,AspectJCorePreferences.ASPECTPATH_ATTRIBUTE);
	}

	/**
	 * Firstly, add library to the Java build path if it's not there already,
	 * then mark the entry as being on the aspect path
	 * @param project
	 * @param path
	 */
	public static void addToAspectPath(IProject project, String jarPath, int eKind) {
		IJavaProject jp = JavaCore.create(project);
		
		try {
            IClasspathEntry[] cp = jp.getRawClasspath();
            int cpIndex = getIndexInBuildPathEntry(cp, jarPath);
            if (cpIndex >= 0) { // already on classpath
                // add attribute to classpath entry
                // if it doesn't already exist
                IClasspathEntry aspectPathAdd = cp[cpIndex];
                // only add attribute if this element is not already on the aspect path
                if (!isOnAspectpath(aspectPathAdd)) {  
                    IClasspathAttribute[] attributes = aspectPathAdd.getExtraAttributes();
                    IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length + 1];
                    System.arraycopy(attributes, 0, newattrib, 0, attributes.length);
                    newattrib[attributes.length] = AspectJCorePreferences.ASPECTPATH_ATTRIBUTE;
                    switch(aspectPathAdd.getEntryKind()) {
                        case IClasspathEntry.CPE_LIBRARY:
                            aspectPathAdd = JavaCore.newLibraryEntry(aspectPathAdd.getPath(),
                                    aspectPathAdd.getSourceAttachmentPath(), 
                                    aspectPathAdd.getSourceAttachmentRootPath(),
                                    aspectPathAdd.getAccessRules(), newattrib, 
                                    aspectPathAdd.isExported());
                            break;
                            
                        case IClasspathEntry.CPE_VARIABLE:
                            aspectPathAdd = JavaCore.newVariableEntry(aspectPathAdd.getPath(),
                                    aspectPathAdd.getSourceAttachmentPath(), 
                                    aspectPathAdd.getSourceAttachmentRootPath(),
                                    aspectPathAdd.getAccessRules(), newattrib, 
                                    aspectPathAdd.isExported());
                            break;

                        case IClasspathEntry.CPE_CONTAINER:
                            aspectPathAdd = JavaCore.newContainerEntry(aspectPathAdd.getPath(),
                                    aspectPathAdd.getAccessRules(), newattrib, 
                                    aspectPathAdd.isExported());
                            break;
                            
                        case IClasspathEntry.CPE_PROJECT:
                            aspectPathAdd = JavaCore.newProjectEntry(aspectPathAdd.getPath(), 
                                    aspectPathAdd.getAccessRules(), 
                                    true, newattrib, aspectPathAdd.isExported());
                            break;
                    }
                    
                    cp[cpIndex] = aspectPathAdd;
                    jp.setRawClasspath(cp, null);
                }
    		} else {
    			addEntryToJavaBuildPath(jp, ASPECTPATH_ATTRIBUTE, jarPath, eKind);
    		}
		} catch (JavaModelException e) {
		}
	}

	public static boolean isOnAspectpath(IClasspathEntry entry) {
		IClasspathAttribute[] attributes = entry.getExtraAttributes();
		for (int j = 0; j < attributes.length; j++) {
			if (attributes[j].equals(AspectJCorePreferences.ASPECTPATH_ATTRIBUTE)) {
				return true;								
			}
		}
		return false;
	}
	
	
	
	
    /**
     * Checks to see if an entry is already on the aspect path
     */
	public static boolean isOnAspectpath(IProject project, String jarPath) {
		IJavaProject jp = JavaCore.create(project);
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if ((cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_PROJECT)) {
					String entry = JavaCore.getResolvedClasspathEntry(cp[i])
							.getPath().toPortableString();
					if (entry.equals(jarPath)) {
						IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
						for (int j = 0; j < attributes.length; j++) {
							if (attributes[j].equals(AspectJCorePreferences.ASPECTPATH_ATTRIBUTE)) {
								return true;								
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return false;
	}

	private static String[] getOldProjectAspectPath(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		String pathString = ""; //$NON-NLS-1$
		int index = 1;
		String value = projectNode.get(ASPECTPATH + index, ""); //$NON-NLS-1$
		if (value.length() == 0) {
			return null;
		}
		while (value.length() > 0) {
			pathString += value;
			pathString += File.pathSeparator;
			index++;
			value = projectNode.get(ASPECTPATH + index, ""); //$NON-NLS-1$
		}
	
		String contentString = ""; //$NON-NLS-1$
		index = 1;
		value = projectNode.get(ASPECTPATH_CON_KINDS + index, ""); //$NON-NLS-1$
		while (value.length() > 0) {
			contentString += toContentKind(value.toUpperCase());
			contentString += File.pathSeparator;
			index++;
			value = projectNode.get(ASPECTPATH_CON_KINDS + index, ""); //$NON-NLS-1$
		}
	
		String entryString = ""; //$NON-NLS-1$
		index = 1;
		value = projectNode.get(ASPECTPATH_ENT_KINDS + index, ""); //$NON-NLS-1$
		while (value.length() > 0) {
			entryString += toEntryKind(value.toUpperCase());
			entryString += File.pathSeparator;
			index++;
			value = projectNode.get(ASPECTPATH_ENT_KINDS + index, ""); //$NON-NLS-1$
		}
		return new String[] { pathString, contentString, entryString };
	}

	private static void removeOldAspectPathSetting(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		int index = 1;
		while (projectNode.get(ASPECTPATH + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(ASPECTPATH + index);
			index++;
		}
		index = 1;
		while (projectNode.get(ASPECTPATH_CON_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(ASPECTPATH_CON_KINDS + index);
			index++;
		}
		index = 1;
		while (projectNode.get(ASPECTPATH_ENT_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(ASPECTPATH_ENT_KINDS + index);
			index++;
		}
	
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}
	
	

    public static String[] getRawProjectInpath(IProject project) {
        return internalGetProjectPath(project, INPATH_ATTRIBUTE, false);
    }
    
    public static String[] getResolvedProjectInpath(IProject project) {
        return internalGetProjectPath(project, INPATH_ATTRIBUTE, true);
    }
	
	private static String[] internalGetProjectPath(IProject project, IClasspathAttribute attribute, boolean useResolvedPath) {
	    if (attribute == ASPECTPATH_ATTRIBUTE) {
    		String[] old = getOldProjectAspectPath(project);
    		if (old != null) {
    			AJLog.log("Migrating aspect path settings for project "+project.getName()); //$NON-NLS-1$
    			setProjectAspectPath(project,old[0],old[1],old[2]);
    			removeOldAspectPathSetting(project);
    		}
	    } else { // INPATH_ATTRIBUTE
            String[] old = getOldProjectInPath(project);
            if (old != null) {
                AJLog.log("Migrating inpath settings for project "+project.getName()); //$NON-NLS-1$
                setProjectInPath(project,old[0],old[1],old[2]);
                removeOldInPathSetting(project);
            }	        
	    }
		String pathString = ""; //$NON-NLS-1$
		String contentString = ""; //$NON-NLS-1$
		String entryString = ""; //$NON-NLS-1$
	
		IJavaProject javaProject = JavaCore.create(project);
		try {
            IClasspathEntry[] cp = javaProject.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
				boolean attributeFound = false;
				for (int j = 0; j < attributes.length; j++) {
					if (attributes[j].equals(attribute)) {
					    attributeFound = true;
					    List actualEntries = new ArrayList();
					    
					    if (useResolvedPath) {
					        // this entry is on the path.  must resolve it
					        if (cp[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					            IClasspathContainer container = 
					                JavaCore.getClasspathContainer(cp[i].getPath(), javaProject);
					            if (container != null) {
    					            actualEntries.addAll(resolveClasspathContainer(container, project));
					            }
					        } else if (cp[i].getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					            IProject requiredProj = project.getWorkspace().getRoot().getProject(
					                    cp[i].getPath().makeRelative().toPortableString());
					            if (! requiredProj.getName().equals(project.getName())   
					                    && requiredProj.exists()) {
    					            actualEntries.addAll(resolveDependentProjectClasspath(requiredProj, cp[i]));
					            }
					        } else {
					            actualEntries.add(JavaCore.getResolvedClasspathEntry(cp[i]));
					        } // cp[i].getEntryKind()
					    } else {
					        actualEntries.add(cp[i]);
					    } // useResolvedEntry
					    
					    for (Iterator cpIter = actualEntries.iterator(); cpIter.hasNext(); ) {
					        IClasspathEntry actualEntry = (IClasspathEntry) cpIter.next();
	                        pathString += actualEntry.getPath().toPortableString() + File.pathSeparator;
	                        contentString += actualEntry.getContentKind() + File.pathSeparator;
	                        entryString += actualEntry.getEntryKind() + File.pathSeparator;
					    }
					}  // attributes[j].equals(attribute)
				}  // for (int j = 0; j < attributes.length; j++)
				
				// there is a special case that we must look inside the classpath container for entries with
				// attributes if we are returning the resolved path and the container itself isn't already
				// on the path.
				if (!attributeFound && useResolvedPath && cp[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
				    IClasspathContainer container = 
                        JavaCore.getClasspathContainer(cp[i].getPath(), javaProject);
                    if (container != null) {
                        List /* IClasspathEntry */ containerEntries = resolveClasspathContainer(container, project);
                        // iterate through each entry and if it has the attribute, then add it to the 
                        for (Iterator cpIter = containerEntries.iterator(); cpIter.hasNext(); ) {
                            IClasspathEntry containerEntry = (IClasspathEntry) cpIter.next();
                            IClasspathAttribute[] containerAttrs = containerEntry.getExtraAttributes();
                            for (int j = 0; j < containerAttrs.length; j++) {
                                if (containerAttrs[j].getName().equals(attribute.getName())) {
                            		pathString += containerEntry.getPath().toPortableString() + File.pathSeparator;
                                    contentString += containerEntry.getContentKind() + File.pathSeparator;
                                    entryString += containerEntry.getEntryKind() + File.pathSeparator;
                                }
                            }
                        }  // for (Iterator cpIter = containerEntries.iterator(); cpIter.hasNext(); ) 
                    }  // container != null
				}  // !attributeFound && useResolvedPath && cp[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER
			}  // for (int i = 0; i < cp.length; i++)
		} catch (JavaModelException e) {
		}
		return new String[] { pathString, contentString, entryString };
	}

    public static List resolveDependentProjectClasspath(IProject requiredProj, IClasspathEntry projEntry) {
        // add all output locations and exported classpath entities
        // AspectJ compiler doesn't understand the concept of a java project
        List /*IClasspathEntry*/ actualEntries = new ArrayList();
        

        try {
            IJavaProject requiredJavaProj = JavaCore.create(requiredProj);
            IClasspathEntry[] requiredEntries = requiredJavaProj.getResolvedClasspath(true);
            for (int i = 0; i < requiredEntries.length; i++) {
                IClasspathEntry requiredEntry = requiredEntries[i];
                if (requiredEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {

                    // always add source entries even if not explicitly exported

                    // don't add the source folder itself, but instead add the outfolder
                    IPath outputLocation = requiredEntry.getOutputLocation();
                    if (outputLocation != null) {
	                    // XXX Not sure what I should be doing with these
    	                // Should the values override the existing values on the
        	            // entries, or should they be combined?
            	        IAccessRule[] rules = projEntry.getAccessRules();
                	    IClasspathAttribute[] attributes = projEntry.getExtraAttributes();

                	    // only add the out folder if it already exists
                	    if (requiredProj.getFolder(outputLocation.removeFirstSegments(1)).exists()) {
                            IClasspathEntry outFolder = JavaCore.newLibraryEntry(outputLocation,
                                    requiredEntry.getPath(),
                                    requiredProj.getFullPath(), rules, attributes, projEntry.isExported());
                            actualEntries.add(outFolder);
                	    }
                    }
                } else if (requiredEntry.isExported()) {
                    // must recur through this entry and add entries that it contains
                    switch(requiredEntry.getEntryKind()) {
                        case IClasspathEntry.CPE_CONTAINER:
                            IClasspathContainer container = 
                                JavaCore.getClasspathContainer(requiredEntry.getPath(), requiredJavaProj);
                            if (container != null) {
                                actualEntries.addAll(resolveClasspathContainer(container, requiredProj));
                            }
                            break;
                            
                        case IClasspathEntry.CPE_LIBRARY:
                            actualEntries.add(requiredEntry);
                            break;
                            
                        case IClasspathEntry.CPE_PROJECT:
                            IProject containedProj = requiredProj.getWorkspace().getRoot().getProject(
                                    requiredEntry.getPath().makeRelative().toPortableString());
                            if (! containedProj.getName().equals(requiredProj.getName())   
                                    && containedProj.exists()) {
                                actualEntries.addAll(resolveDependentProjectClasspath(containedProj, requiredEntry));
                            }
                            break;
                                
                        case IClasspathEntry.CPE_VARIABLE:
                            actualEntries.add(JavaCore.getResolvedClasspathEntry(requiredEntry));
                    }
                    
                }
            } // for (int i = 0; i < requiredEntries.length; i++)
            
            IPath outputLocation = requiredJavaProj.getOutputLocation();
            // Output location may not exist.  Do not put output location of required project
            // on path unless it exists
            boolean exists = false;
            // bug 244330 check to see if the project folder is also the output folder
            if (outputLocation.segmentCount() == 1) {
            	exists = true;
            } else {
            	if (requiredProj.getWorkspace().getRoot().getFolder(outputLocation).exists()) {
            		exists = true;
            	}
            }
            
            if (exists) {
                IClasspathEntry outFolder = JavaCore.newLibraryEntry(outputLocation,
                        null,
                        requiredProj.getFullPath());					                
                actualEntries.add(outFolder);
            }
        } catch (JavaModelException e) {
        }
        return actualEntries;
    }

    public static List resolveClasspathContainer(IClasspathContainer container, IProject thisProject) {
        List actualEntries = new ArrayList();
        IClasspathEntry[] containerEntries = 
            container.getClasspathEntries();
        for (int i = 0; i < containerEntries.length; i++) {
            // projects must be resolved specially since the AspectJ doesn't understand the 
            // concept of project
            if (containerEntries[i].getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                IProject requiredProj = thisProject.getWorkspace().getRoot().getProject(
                        containerEntries[i].getPath().makeRelative().toPortableString());
                if (! requiredProj.getName().equals(thisProject.getName())   
                        && requiredProj.exists()) {
                    actualEntries.addAll(resolveDependentProjectClasspath(requiredProj, containerEntries[i]));
                }
            } else {
                actualEntries.add(
                        JavaCore.getResolvedClasspathEntry(
                                containerEntries[i]));
            }
        }
        return actualEntries;
    }

	public static void setProjectInPath(IProject project, String path,
			String cKinds, String eKinds) {
		IJavaProject javaProject = JavaCore.create(project);
		removeAttribute(javaProject, INPATH_ATTRIBUTE);
	
		StringTokenizer pathTok = new StringTokenizer(path, File.pathSeparator);
		StringTokenizer eKindsTok = new StringTokenizer(eKinds, File.pathSeparator);
		int index = 1;
		while (pathTok.hasMoreTokens() && eKindsTok.hasMoreTokens()) {
			String entry = pathTok.nextToken();
			int eKind = Integer.parseInt(eKindsTok.nextToken());
			addToInPath(project,entry,eKind);
			index++;
		}
	}

	private static void addAttribute(IJavaProject jp, IClasspathEntry entry, IClasspathAttribute attr) {
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if (cp[i].equals(entry)) {
					IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
					IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length + 1];
					System.arraycopy(attributes, 0, newattrib, 0, attributes.length);
					newattrib[attributes.length] = attr;
					switch (cp[i].getEntryKind()) {
                        case IClasspathEntry.CPE_LIBRARY:
                            cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(),
                                    cp[i].getSourceAttachmentPath(), 
                                    cp[i].getSourceAttachmentRootPath(),
                                    cp[i].getAccessRules(), newattrib, 
                                    cp[i].isExported());
                            break;

                        case IClasspathEntry.CPE_VARIABLE:
                            cp[i] = JavaCore.newVariableEntry(cp[i].getPath(),
                                    cp[i].getSourceAttachmentPath(), 
                                    cp[i].getSourceAttachmentRootPath(),
                                    cp[i].getAccessRules(), newattrib, 
                                    cp[i].isExported());
                            break;

                        case IClasspathEntry.CPE_CONTAINER:
                            cp[i] = JavaCore.newContainerEntry(cp[i].getPath(),
                                    cp[i].getAccessRules(), newattrib, 
                                    cp[i].isExported());
                            break;

                        case IClasspathEntry.CPE_PROJECT:
                            cp[i] = JavaCore.newProjectEntry(cp[i].getPath(), 
                                    cp[i].getAccessRules(), true, newattrib,
                                    cp[i].isExported());
                            break;

                    }
				}
			}
			jp.setRawClasspath(cp, null);
		} catch (JavaModelException e) {
		}
	}
	
	private static void removeAttribute(IJavaProject jp, IClasspathEntry entry, IClasspathAttribute attr) {
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if (cp[i].equals(entry)) {
					IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
					IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length - 1];
					int count = 0;
					for (int j = 0; j < attributes.length; j++) {
						if (!attributes[j].equals(attr)) {
							newattrib[count++] = attributes[j];
						}
					}
                    switch(cp[i].getEntryKind()) {
                        case IClasspathEntry.CPE_LIBRARY:
                            cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(),
                                    cp[i].getSourceAttachmentPath(), cp[i]
                                            .getSourceAttachmentRootPath(),
                                    cp[i].getAccessRules(), newattrib, cp[i]
                                            .isExported());
                            break;
                            
                        case IClasspathEntry.CPE_VARIABLE:
                            cp[i] = JavaCore.newVariableEntry(cp[i].getPath(),
                                    cp[i].getSourceAttachmentPath(), cp[i]
                                            .getSourceAttachmentRootPath(),
                                    cp[i].getAccessRules(), newattrib, cp[i]
                                            .isExported());                     
                            break;

                        case IClasspathEntry.CPE_CONTAINER:
                            cp[i] = JavaCore.newContainerEntry(cp[i].getPath(),
                                    cp[i].getAccessRules(), newattrib, cp[i]
                                            .isExported());                     
                            break;
                            
                        case IClasspathEntry.CPE_PROJECT:
                            cp[i] = JavaCore.newProjectEntry(cp[i].getPath(), 
                                    cp[i].getAccessRules(), 
                                    true, newattrib, cp[i].isExported());
                            break;
                    }
				}
			}
			jp.setRawClasspath(cp, null);
		} catch (JavaModelException e) {
		}
	}
	
	public static void addToInPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		addAttribute(jp,entry,AspectJCorePreferences.INPATH_ATTRIBUTE);
	}
	
	public static void removeFromInPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		removeAttribute(jp,entry,AspectJCorePreferences.INPATH_ATTRIBUTE);
	}
	
	/**
	 * Firstly, add library to the Java build path if it's not there already,
	 * then mark the entry as being on the in path
	 * @param project
	 * @param path
	 */
	public static void addToInPath(IProject project, String jarPath, int eKind) {
		IJavaProject jp = JavaCore.create(project);
		try {
		    IClasspathEntry[] cp = jp.getRawClasspath();
    		int cpIndex = getIndexInBuildPathEntry(cp, jarPath);
    		if (cpIndex >= 0) { // already on classpath
    			// add attribute to classpath entry
    		    // if it doesn't already exist
			    IClasspathEntry inpathAdd = cp[cpIndex];
			    // only add attribute if this element is not already on the inpath
				if (!isOnInpath(inpathAdd)) {
				    IClasspathAttribute[] attributes = inpathAdd.getExtraAttributes();
    				IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length + 1];
    				System.arraycopy(attributes, 0, newattrib, 0, attributes.length);
    				newattrib[attributes.length] = AspectJCorePreferences.INPATH_ATTRIBUTE;
                    switch(inpathAdd.getEntryKind()) {
                        case IClasspathEntry.CPE_LIBRARY:
                            inpathAdd = JavaCore.newLibraryEntry(inpathAdd.getPath(),
                                    inpathAdd.getSourceAttachmentPath(), 
                                    inpathAdd.getSourceAttachmentRootPath(),
                                    inpathAdd.getAccessRules(), newattrib, 
                                    inpathAdd.isExported());
                            break;
                            
                        case IClasspathEntry.CPE_VARIABLE:
                            inpathAdd = JavaCore.newVariableEntry(inpathAdd.getPath(),
                                    inpathAdd.getSourceAttachmentPath(), 
                                    inpathAdd.getSourceAttachmentRootPath(),
                                    inpathAdd.getAccessRules(), newattrib, 
                                    inpathAdd.isExported());                     
                            break;

                        case IClasspathEntry.CPE_CONTAINER:
                            inpathAdd = JavaCore.newContainerEntry(inpathAdd.getPath(),
                                    inpathAdd.getAccessRules(), newattrib, 
                                    inpathAdd.isExported());                     
                            break;
                            
                        case IClasspathEntry.CPE_PROJECT:
                            inpathAdd = JavaCore.newProjectEntry(inpathAdd.getPath(), 
                                    inpathAdd.getAccessRules(), 
                                    true, newattrib, inpathAdd.isExported());
                            break;
                    }
    				cp[cpIndex] = inpathAdd;
    				jp.setRawClasspath(cp, null);
				}
    		} else {
    			addEntryToJavaBuildPath(jp, INPATH_ATTRIBUTE, jarPath, eKind); 
    		}
        } catch (JavaModelException e) {
        }
	}

	
    public static boolean isOnInpath(IClasspathEntry entry) {
		IClasspathAttribute[] attributes = entry.getExtraAttributes();
		for (int j = 0; j < attributes.length; j++) {
			if (attributes[j].equals(AspectJCorePreferences.INPATH_ATTRIBUTE)) {
				return true;								
			}
		}
		return false;
	}
	
    /**
     * Checks to see if an entry is already on the Inpath
     */
	public static boolean isOnInpath(IProject project, String jarPath) {
		IJavaProject jp = JavaCore.create(project);
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if ((cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE)
				        || (cp[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER)
				        || (cp[i].getEntryKind() == IClasspathEntry.CPE_PROJECT)) {
					String entry = JavaCore.getResolvedClasspathEntry(cp[i])
							.getPath().toPortableString();
					if (entry.equals(jarPath)) {
						IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
						for (int j = 0; j < attributes.length; j++) {
							if (attributes[j].equals(AspectJCorePreferences.INPATH_ATTRIBUTE)) {
								return true;								
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return false;
	}

	private static String[] getOldProjectInPath(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		String pathString = ""; //$NON-NLS-1$
		int index = 1;
		String value = projectNode.get(INPATH + index, ""); //$NON-NLS-1$
		if (value.length() == 0) {
			return null;
		}
		while (value.length() > 0) {
			pathString += value;
			pathString += File.pathSeparator;
			index++;
			value = projectNode.get(INPATH + index, ""); //$NON-NLS-1$
		}
	
		String contentString = ""; //$NON-NLS-1$
		index = 1;
		value = projectNode.get(INPATH_CON_KINDS + index, ""); //$NON-NLS-1$
		while (value.length() > 0) {
			contentString += toContentKind(value.toUpperCase());
			contentString += File.pathSeparator;
			index++;
			value = projectNode.get(INPATH_CON_KINDS + index, ""); //$NON-NLS-1$
		}
	
		String entryString = ""; //$NON-NLS-1$
		index = 1;
		value = projectNode.get(INPATH_ENT_KINDS + index, ""); //$NON-NLS-1$
		while (value.length() > 0) {
			entryString += toEntryKind(value.toUpperCase());
			entryString += File.pathSeparator;
			index++;
			value = projectNode.get(INPATH_ENT_KINDS + index, ""); //$NON-NLS-1$
		}
		return new String[] { pathString, contentString, entryString };
	}

	private static void removeOldInPathSetting(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		int index = 1;
		while (projectNode.get(INPATH + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(INPATH + index);
			index++;
		}
		index = 1;
		while (projectNode.get(INPATH_CON_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(INPATH_CON_KINDS + index);
			index++;
		}
		index = 1;
		while (projectNode.get(INPATH_ENT_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(INPATH_ENT_KINDS + index);
			index++;
		}

		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	public static void setInpathOutputFolder() {
	    
	}
	
	/**
	 * Remove all occurrences of an attribute
	 * @param javaProject
	 * @param attribute
	 */
	private static void removeAttribute(IJavaProject javaProject,
			IClasspathAttribute attribute) {
		try {
			IClasspathEntry[] cp = javaProject.getRawClasspath();
			boolean changed = false;
			for (int i = 0; i < cp.length; i++) {
				IClasspathAttribute[] attributes = cp[i]
						.getExtraAttributes();
				boolean found = false;
				for (int j = 0; !found && (j < attributes.length); j++) {
					if (attributes[j].equals(attribute)) {
						found = true;
					}
				}
				if (found) {
				    changed = true;
					IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length - 1];
					int count = 0;
					for (int j = 0; j < attributes.length; j++) {
						if (!attributes[j]
								.equals(attribute)) {
							newattrib[count++] = attributes[j];
						}
					}
                    switch(cp[i].getEntryKind()) {
                        case IClasspathEntry.CPE_LIBRARY:
                            cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(),
                                    cp[i].getSourceAttachmentPath(), 
                                    cp[i].getSourceAttachmentRootPath(),
                                    cp[i].getAccessRules(), newattrib, 
                                    cp[i].isExported());
                            break;
                            
                        case IClasspathEntry.CPE_VARIABLE:
                            cp[i] = JavaCore.newVariableEntry(cp[i].getPath(),
                                    cp[i].getSourceAttachmentPath(), 
                                    cp[i].getSourceAttachmentRootPath(),
                                    cp[i].getAccessRules(), newattrib, 
                                    cp[i].isExported());
                            break;

                        case IClasspathEntry.CPE_CONTAINER:
                            cp[i] = JavaCore.newContainerEntry(cp[i].getPath(),
                                    cp[i].getAccessRules(), newattrib, 
                                    cp[i].isExported());
                            break;
                            
                        case IClasspathEntry.CPE_PROJECT:
                            cp[i] = JavaCore.newProjectEntry(cp[i].getPath(), 
                                    cp[i].getAccessRules(), 
                                    true, newattrib, cp[i].isExported());
                            break;
                    }
				}
			}
			if (changed) {
				javaProject.setRawClasspath(cp, null);
			}
		} catch (JavaModelException e) {
		}
	}
	
	
	private static int getIndexInBuildPathEntry(IClasspathEntry[] cp, String jarPath) {
        for (int i = 0; i < cp.length; i++) {
            String entry = cp[i].getPath().toPortableString();
            if (entry.equals(jarPath)) {
                return i;
            }
        }
        return -1;
	}
	
	
	private static void addEntryToJavaBuildPath(IJavaProject jp,
			IClasspathAttribute attribute, String path, int eKind) {
		IClasspathAttribute[] attributes = new IClasspathAttribute[] { attribute };
		try {
			IClasspathEntry[] originalCP = jp.getRawClasspath();
			IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 1];
			IClasspathEntry cp = null;
			if (eKind == IClasspathEntry.CPE_LIBRARY) {
				cp = JavaCore.newLibraryEntry(
						new Path(path), null, null, new IAccessRule[0], attributes, false);
			} else if (eKind == IClasspathEntry.CPE_VARIABLE) {
				cp = JavaCore.newVariableEntry(
						new Path(path), null, null, new IAccessRule[0], attributes, false);
			} else if (eKind == IClasspathEntry.CPE_CONTAINER) {
			    cp = JavaCore.newContainerEntry(new Path(path), null, attributes, false);
			} else if (eKind == IClasspathEntry.CPE_PROJECT) {
			    cp = JavaCore.newProjectEntry(new Path(path), null, true, attributes, false);
			}
			
			// Update the raw classpath with the new entry.
			if (cp != null) {
				System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
				newCP[originalCP.length] = cp;
				jp.setRawClasspath(newCP, new NullProgressMonitor());
			}
		} catch (JavaModelException e) {
		} catch (NumberFormatException e) {
		}
	}

	private static String toContentKind(String contentStr) {
		int content = 0;
		if (contentStr.equals("SOURCE")) { //$NON-NLS-1$
			content = IPackageFragmentRoot.K_SOURCE;
		} else if (contentStr.equals("BINARY")) { //$NON-NLS-1$
			content = IPackageFragmentRoot.K_BINARY;
		}
		return new Integer(content).toString();
	}

	private static String toEntryKind(String entryStr) {
		int entry = 0;
		if (entryStr.equals("SOURCE")) { //$NON-NLS-1$
			entry = IClasspathEntry.CPE_SOURCE;
		} else if (entryStr.equals("LIBRARY")) { //$NON-NLS-1$
			entry = IClasspathEntry.CPE_LIBRARY;
		} else if (entryStr.equals("PROJECT")) { //$NON-NLS-1$
			entry = IClasspathEntry.CPE_PROJECT;
		} else if (entryStr.equals("VARIABLE")) { //$NON-NLS-1$
			entry = IClasspathEntry.CPE_VARIABLE;
		} else if (entryStr.equals("CONTAINER")) { //$NON-NLS-1$
			entry = IClasspathEntry.CPE_CONTAINER;
		}
		return new Integer(entry).toString();
	}
	
	

    public static boolean isAutobuildSuppressed() {
        Preferences store = AspectJPlugin.getDefault()
                .getPluginPreferences();
        return store.getBoolean(OPTION_AutobuildSuppressed);
    }

    static public void setAutobuildSuppressed(boolean done) {
        Preferences store = AspectJPlugin.getDefault()
                .getPluginPreferences();
        store.setValue(OPTION_AutobuildSuppressed, done);
    }

}
