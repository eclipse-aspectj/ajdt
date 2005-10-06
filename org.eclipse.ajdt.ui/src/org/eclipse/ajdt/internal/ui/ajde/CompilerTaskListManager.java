/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.ajde.TaskListManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.builder.AJBuilder;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.IAJModelMarker;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 */
public class CompilerTaskListManager implements TaskListManager {

	private static CompilerTaskListManager instance;
	
	// singleton
	private CompilerTaskListManager() {
		
	}
	
	public static CompilerTaskListManager getInstance() {
		if (instance==null) {
			instance = new CompilerTaskListManager();
		}
		return instance;
	}
	
    /**
     * problems for task list
     */
    private static List problems = new ArrayList();

    /**
     * resources that were affected by the compilation.
     */
    private static Set affectedResources = new HashSet();

    /**
     * Markers created in projects other than the one under compilation, which
     * should be cleared next time the compiled project is rebuilt
     */
    private static Map otherProjectMarkers = new HashMap();
    

    /**
     * Add a problem to the tasks list for the given file and line number
     */
    public void addSourcelineTask(final String message,
            final ISourceLocation location, final IMessage.Kind kind) {

        // No one should be calling this method as it doesn't have all the
        // information.
        // It is missing extra source locations and info about whether the
        // message
        // has resulted from a declare statement.
        //TODO: Chuck this:
        //	throw new RuntimeException("Why the hell are we in here? We should
        // always be using the other addSourcelineTask() method");
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("CompilerMessage received ]" + message + "["); //$NON-NLS-1$ //$NON-NLS-2$

        problems.add(new ProblemTracker(message, location, kind));
 
        
        // When will showMessages() get called if we are not 'finishing off a
        // compilation' - the reason this
        // is important is that when AJDE is asked to build a model of a lst
        // file, it calls this routine
        // with any errors it finds whilst parsing the file... if no one calls
        // showMessages then the
        // messages will be stuck in the problems List and not pushed out until
        // later!
    }

    /**
     * Add a problem to the tasks list associated with the current project
     */
    public void addProjectTask(String message, IMessage.Kind kind) {
        problems.add(new ProblemTracker(message, null, kind));
    }

    protected void addAffectedResource(IResource res) {
    	affectedResources.add(res);
    }
    
    public static void clearOtherProjectMarkers(IProject p) {
		List l = (List) otherProjectMarkers.get(p.getName());
		if (l != null) {
			ListIterator li = l.listIterator();
			while (li.hasNext()) {
				IMarker m = (IMarker) li.next();
				try {
					m.delete();
				} catch (CoreException ce) {
					// can be ignored
				} // not the end of the world.
			}
			l.clear();
		}
	}

    /**
	 * Called from Ajde to clear all tasks in problem list
	 */
    public void clearTasks() {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("clearTasks() called"); //$NON-NLS-1$
        affectedResources.clear();
        problems.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.aspectj.ajde.TaskListManager#addSourcelineTask(org.aspectj.bridge.IMessage)
     */
    public void addSourcelineTask(IMessage msg) {
		if (msg.getSourceLocation() == null) {
			this.addProjectTask(msg.getMessage(), msg.getKind());
		} else {
			problems.add(new ProblemTracker(msg.getMessage(), msg
					.getSourceLocation(), msg.getKind(), msg.getDeclared(), msg
					.getExtraSourceLocations(), msg.getID(), msg
					.getSourceStart(), msg.getSourceEnd()));
		}
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.aspectj.ajde.TaskListManager#hasWarning()
     */
    public boolean hasWarning() {
        return false;
    }

    /**
     * Callable from anywhere in the plugin, will put any unreported problems
     * onto the task bar. This is currently used by the model builder for build
     * configuration files. Ajde builds the model and reports errors through
     * this class - BuildConfigurationEditor then asks this helper method to
     * report them. We need to move this error reporting stuff out of here if it
     * is going to be used by more than just the compiler.
     */
    public static void showOutstandingProblems() {
        if (problems.size() > 0 || affectedResources.size() > 0) {
            getInstance().showMessages();
        }
    }

    private void showMessages() {

        // THIS MUST STAY IN A SEPARATE THREAD - This is because we need
        // to create and setup the marker in an atomic operation. See
        // AMC or ASC.
        IWorkspaceRunnable r = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) {

                try {
                	
                    Iterator affectedResourceIterator = affectedResources
                            .iterator();
                    //boolean wipedProjectLevelMarkers = false;
                    AJLog.log("Types affected during build = "+affectedResources.size()); //$NON-NLS-1$
                    IResource ir = null;
                    while (affectedResourceIterator.hasNext()) {
                        ir = (IResource) affectedResourceIterator.next();
                        try {
                            if (ir.exists()) {
                                ir.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
                                        IResource.DEPTH_INFINITE);
                                ir.deleteMarkers(IAJModelMarker.AJDT_PROBLEM_MARKER, true,
                                        IResource.DEPTH_INFINITE);
                                ir.deleteMarkers(IMarker.TASK, true,
                                        IResource.DEPTH_INFINITE);
                            }
                        } catch (ResourceException re) {
                        	AJLog.log("Failed marker deletion: resource=" //$NON-NLS-1$
                                            + ir.getLocation());
                            throw re;
                        }
                    }

                    IProject  project = AJBuilder.getLastBuildTarget();
                    Iterator problemIterator = problems.iterator();
                    ProblemTracker p = null;
                    while (problemIterator.hasNext()) {
                        p = (ProblemTracker) problemIterator.next();
                        ir = null;
                        IMarker marker = null;

                        try {
                            //Bugfix 44155: Create marker of type TASK if
                            // problem is starts with todo, PROBLEM
                            // otherwise
//                            AJDTEventTrace.generalEvent("Creating Marker, " +
//                             "kind: '" + p.kind + "'; text: '" + p.message
//                             + "'");  
                        	
                            if (p.location != null) {
                                ir = locationToResource(p.location, project);
                                int prio = getTaskPriority(p);
                                if (prio != -1) {
                                    marker = ir.createMarker(IMarker.TASK);
                                    marker.setAttribute(IMarker.PRIORITY, prio);
                                } else {
                                    if (p.declaredErrorOrWarning) {
                                        marker = ir
                                                .createMarker(IAJModelMarker.AJDT_PROBLEM_MARKER);
                                    } else {
                                    	// create Java marker with problem id so
										// that quick fix is available
										marker = ir
												.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
										marker.setAttribute(
												IJavaModelMarker.ID, p.id);
										if ((p.start >= 0) && (p.end >= 0)) {
											marker.setAttribute(
													IMarker.CHAR_START,
													new Integer(p.start));
											marker.setAttribute(
													IMarker.CHAR_END,
													new Integer(p.end + 1));
										}
                                    }
                                }

                                if (!ir.getProject().equals(project)) {
                                    addOtherProjectMarker(project, marker);
                                }
                                if (p.location.getLine() > 0) {
                                    marker.setAttribute(IMarker.LINE_NUMBER,
                                            new Integer(p.location.getLine()));
                                }
                            } else {
                                marker = project.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
                            }
                            setSeverity(marker, p.kind);
                            
                            if ((p.extraLocs != null) && (p.extraLocs.size() > 0)) { // multiple part message
                                int relCount=0;
                                for (Iterator iter = p.extraLocs.iterator(); iter
                                		.hasNext();) {
                                    ISourceLocation sLoc = (ISourceLocation) iter
                                    .next();
                                    StringBuffer attrData = new StringBuffer();
                                    attrData.append(sLoc.getSourceFile().getAbsolutePath());
                                    attrData.append(":::"); //$NON-NLS-1$
                                    attrData.append(sLoc.getLine());
                                    attrData.append(":::"); //$NON-NLS-1$
                                    attrData.append(sLoc.getEndLine());
                                    attrData.append(":::"); //$NON-NLS-1$
                                    attrData.append(sLoc.getColumn());
                                    marker.setAttribute(AspectJUIPlugin.RELATED_LOCATIONS_ATTRIBUTE_PREFIX
                                          +(relCount++),attrData.toString());
                                }
                            }
                            
                            setMessage(marker, p.message);
                        } catch (ResourceException re) {
                        	AJLog.log("Failed marker creation: resource=" //$NON-NLS-1$
                                            + p.location.getSourceFile()
                                                    .getPath()
                                            + " line=" //$NON-NLS-1$
                                            + p.location.getLine()
                                            + " message=" + p.message); //$NON-NLS-1$
                            throw re;
                        }
                    }
                    clearTasks();
                } catch (CoreException e) {
                	ErrorHandler.handleAJDTError(
                            UIMessages.CompilerTaskListManager_Error_creating_marker, e);
                }                
            }
        };

        try {
            AspectJPlugin.getWorkspace().run(r, null);
        } catch (CoreException cEx) {
        	ErrorHandler.handleAJDTError(
                    UIMessages.CompilerTaskListManager_Error_adding_problem_markers, cEx);
        }
 		 // Part of the fix for bug 89793 - editor image is not updated
        Set activeEditorList = AspectJEditor.getActiveEditorList();
        synchronized(activeEditorList) {
	        for(Iterator iter = activeEditorList.iterator(); iter.hasNext();) {
	        	((AspectJEditor)iter.next()).resetTitleImage();
	        }
	    }
    }

    /**
     * returns -1 if problem is not a task and the tasks priority otherwise
     * takes case sensitivity into account though this does not seem to
     * supported by the current compiler (AJDT 1.1.10)
     */
    private int getTaskPriority(ProblemTracker p) {
        if (p == null)
            return -1;

        String message = p.message;

        Preferences pref = JavaCore.getPlugin().getPluginPreferences();
        String tags = pref.getString("org.eclipse.jdt.core.compiler.taskTags"); //$NON-NLS-1$
        String caseSens = pref
                .getString("org.eclipse.jdt.core.compiler.taskCaseSensitive"); //$NON-NLS-1$
        String priorities = pref
                .getString("org.eclipse.jdt.core.compiler.taskPriorities"); //$NON-NLS-1$

        boolean caseSensitive;
        if (caseSens.equals("disabled")) { //$NON-NLS-1$
            caseSensitive = false;
        } else {
            caseSensitive = true;
        }

        StringTokenizer tagTokens = new StringTokenizer(tags, ","); //$NON-NLS-1$
        StringTokenizer priorityTokens = new StringTokenizer(priorities, ","); //$NON-NLS-1$
        while (tagTokens.hasMoreTokens()) {
            String prio = priorityTokens.nextToken();
            String token = tagTokens.nextToken();
            if (caseSensitive) {
                if (message.startsWith(token))
                    return getPrioritiyFlag(prio);
            } else {
                if (token.length() <= message.length()) {
                    String temp = message.substring(0, token.length());
                    if (token.compareToIgnoreCase(temp) == 0)
                        return getPrioritiyFlag(prio);
                }
            }

        }
        return -1;
    }

    private int getPrioritiyFlag(String prio) {
        if (prio.equals("NORMAL")) //$NON-NLS-1$
            return IMarker.PRIORITY_NORMAL;
        if (prio.equals("HIGH")) //$NON-NLS-1$
            return IMarker.PRIORITY_HIGH;
        return IMarker.PRIORITY_LOW;
    }

    /**
     * Sets the given marker to have the appropriate message.
     * 
     * @param marker
     *            the marker to set the message for
     * @param message
     *            the raw message which may require manipulation
     * @param id
     *            the number of this message, which may be an element of a
     *            multipart message
     * @param count
     *            the number of parts to this message (most messages are single
     *            part)
     * @throws CoreException
     */
    private void setMessage(IMarker marker, String message)
            throws CoreException {
        // FIXME: Remove this horrid hack.
        // Hack the filename off the front and the line number
        // off the end
        if (message.indexOf("\":") != -1 && message.indexOf(", at line") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
            String hackedMessage = message
                    .substring(message.indexOf("\":") + 2); //$NON-NLS-1$
            message = hackedMessage.substring(0, hackedMessage
                    .indexOf(", at line")); //$NON-NLS-1$
        }
        marker.setAttribute(IMarker.MESSAGE, message);
    }

    /**
     * Sets the given marker to have hte appropriate severity, according to the
     * kind.
     * 
     * @param marker
     *            the marker to set the message for
     * @param kind
     *            used to determine the appropriate severity
     * @throws CoreException
     */
    private void setSeverity(IMarker marker, IMessage.Kind kind)
            throws CoreException {
        if (kind == IMessage.ERROR) {
            marker.setAttribute(IMarker.SEVERITY, new Integer(
                    IMarker.SEVERITY_ERROR));
        } else if (kind == IMessage.WARNING) {
            marker.setAttribute(IMarker.SEVERITY, new Integer(
                    IMarker.SEVERITY_WARNING));
        } else {
            marker.setAttribute(IMarker.SEVERITY, new Integer(
                    IMarker.SEVERITY_INFO));
        }

    }

    /**
     * Try to map a source location in a project to an IResource
     * 
     * @param isl
     *            the source location
     * @param project
     *            the project to look in first
     * @return the IResource if a match was found, null otherwise
     */
    private IResource locationToResource(ISourceLocation isl, IProject project) {
        IResource ir = null;

        String loc = isl.getSourceFile().getPath();

        // try this project
        ir = AspectJUIPlugin.getDefault().getAjdtProjectProperties()
                .findResource(loc, project);

        if (ir == null) {
            // try any project
            ir = AspectJUIPlugin.getDefault().getAjdtProjectProperties()
                    .findResource(loc);
            if (ir == null) {
                // fix for declare
                // warning/error bug which
                // returns only file name
                // (unqualified)
                ir = tryToFindResource(loc);
            }
            // At least warn that you are going to
            // blow up
            // with an event trace ...
            if (ir == null)
            	AJLog.log("Whilst adding post compilation markers to resources, cannot locate valid eclipse resource for file " //$NON-NLS-1$
                                + loc);
        }

        return ir;
    }

    private IResource tryToFindResource(String fileName) {
        IResource ret = null;
        String toFind = fileName.replace('\\', '/');
        IProject project = AspectJPlugin.getDefault().getCurrentProject();
        IJavaProject jProject = JavaCore.create(project);
        try {
            IClasspathEntry[] classpathEntries = jProject
                    .getResolvedClasspath(false);
            for (int i = 0; i < classpathEntries.length; i++) {
                IClasspathEntry cpEntry = classpathEntries[i];
                if (cpEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    IPath sourcePath = cpEntry.getPath();
                    // remove the first segment because the findMember call
                    // following
                    // always adds it back in under the covers (doh!) and we end
                    // up
                    // with two first segments otherwise!
                    sourcePath = sourcePath.removeFirstSegments(1);
                    IResource[] srcContainer = new IResource[] { project
                            .findMember(sourcePath) };
                    ret = findFile(srcContainer, toFind);
                    if (ret != null)
                        break;
                } else if (cpEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                    IPath projPath = cpEntry.getPath();
                    IResource projResource = AspectJPlugin.getWorkspace()
                            .getRoot().findMember(projPath);
                    ret = findFile(new IResource[] { projResource }, toFind);
                }
            }
        } catch (JavaModelException jmEx) {
        	ErrorHandler.handleAJDTError(
        			UIMessages.ajErrorDialogTitle,
        			UIMessages.jmCoreException, jmEx);
        }

        if (ret == null)
            ret = project;
        return ret;
    }

    private IResource findFile(IResource[] srcContainer, String name) {
        IResource ret = null;
        try {
            for (int i = 0; i < srcContainer.length; i++) {
                IResource ir = srcContainer[i];
                if (ir.getFullPath().toString().endsWith(name)) {
                    ret = ir;
                    break;
                }
                if (ir instanceof IContainer) {
                    ret = findFile(((IContainer) ir).members(), name);
                    if (ret != null)
                        break;
                }
            }
        } catch (Exception e) {
        }
        return ret;
    }

    private void addOtherProjectMarker(IProject p, IMarker m) {
        if (!otherProjectMarkers.containsKey(p.getName())) {
            otherProjectMarkers.put(p.getName(), new ArrayList());
        }
        List l = (List) otherProjectMarkers.get(p.getName());
        l.add(m);
    }

    /**
     * Inner class used to track problems found during compilation Values of -1
     * are used to indicate no line or column number available.
     */
    class ProblemTracker {

        public ISourceLocation location;

        public String message;

        public IMessage.Kind kind;

        public boolean declaredErrorOrWarning = false;

        public List/* ISourceLocation */extraLocs;

        public int id;
        public int start;
        public int end;
        
        public ProblemTracker(String m, ISourceLocation l, IMessage.Kind k) {
            this(m, l, k, false, null, -1, -1, -1);
        }

        public ProblemTracker(String m, ISourceLocation l, IMessage.Kind k,
                boolean deow, List/* ISourceLocation */extraLocs, int id,
				int start, int end) {
            location = l;
            message = m;
            kind = k;
            declaredErrorOrWarning = deow;
            this.extraLocs = extraLocs;
            this.id = id;
            this.start = start;
            this.end = end;
        }
    }

}
