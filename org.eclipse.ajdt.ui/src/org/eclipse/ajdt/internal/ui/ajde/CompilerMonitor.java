/**********************************************************************
 Copyright (c) 2002, 2004 IBM Corporation and others.
 All rights reserved. This program and the accompanying materials
 are made  available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html
 Contributors:
 Adrian Colyer, Andy Clement, Tracy Gardner - initial version
 Matt Chapman - add support for Go To Related Location entries
 ...
 **********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.StringTokenizer;

import org.aspectj.ajde.BuildProgressMonitor;
import org.aspectj.ajde.TaskListManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.WeaverMetrics;
import org.eclipse.ajdt.buildconfigurator.BuildConfigurator;
import org.eclipse.ajdt.buildconfigurator.ProjectBuildConfigurator;
import org.eclipse.ajdt.internal.builder.Builder;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.swt.widgets.Shell;

/**
 * The compiler monitor interface is used by the AspectJ builder. Just before
 * delegating the compilation of the source files to the 'external' ajc
 * compiler, the builder prepares a compiler monitor for this perspective. The
 * compiler monitor is then called back through the CompilerMessages and
 * CompileProgressMonitor interfaces. When the final message comes from the AJC
 * compiler (it should be finishProgress()) then the compiler monitor sets the
 * finished flag which is used by the builder to determine when it is safe to
 * exit the build function. This syncrhonization appears to be necessary because
 * the compilation is done asynchronously once the AJC compiler is started.
 */
public class CompilerMonitor implements TaskListManager, BuildProgressMonitor {

    private long compileStartTime;

    private boolean reportedCompiledMessages;

    private boolean reportedWovenMessages;

    /**
     * Which Eclipse IProgressMonitor should this CompilerMonitor keep updating?
     */
    private IProgressMonitor monitor = null;

    /**
     * Is this CompilerMonitor instance currently 'in use' ?
     */
    private boolean compilationInProgress = false;

    /**
     * resources that were affected by the compilation.
     */
    private static List affectedResources = new ArrayList();

    /**
     * problems for task list
     */
    private static List problems = new ArrayList();

    /**
     * Markers created in projects other than the one under compilation, which
     * should be cleared next time the compiled project is rebuilt
     */
    private static Map otherProjectMarkers = new HashMap();

    /**
     * Ratio of max value used by Ajde to MONITOR_MAX
     */
    private float ajdeMonitorMaxRatio = 1.0f;

    /**
     * Monitor progress against Ajde max
     */
    private int currentAjdeProgress;

    /**
     * Determine if a linked folder exists in the buildList
     */
    private boolean linked;

    /**
     * Called from the Builder to set up the compiler for a new build.
     */
    public void prepare(IProject project, List buildList,
            IProgressMonitor eclipseMonitor) {

        //check if the folder contains linked resources
        linked = false;
        IResource[] res = null;

        try {
            res = project.members();
        } catch (CoreException e) {
            //should not occur but for some reason one of the following it
            // true:
            //1. This resource does not exist.
            //2. includePhantoms is false and resource does not exist.
            //3. includePhantoms is false and this resource is a project that
            // is not open.
        }

        for (int i = 0; (linked == false) && (i < res.length); i++) {
            if (res[i].getType() == IResource.FOLDER) {
                linked = res[i].isLinked();
            }
        }

        if (AspectJUIPlugin.DEBUG_COMPILER) {
            System.out.println("CompilerMonitor.prepare called: IPM is "
                    + (eclipseMonitor == null ? "Null" : "Not-Null"));
        }

        monitor = eclipseMonitor;
        if (monitor != null) {
            monitor.beginTask(AspectJUIPlugin.getResourceString("ajCompilation"),
                    AspectJUIPlugin.PROGRESS_MONITOR_MAX);
        }

        compileStartTime = System.currentTimeMillis();
        reportedCompiledMessages = false;
        reportedWovenMessages = false;
        compilationInProgress = true;

    }

    /** ******* BuildProgressMonitor interface implementation ************* START */

    /*
     * Methods are called from Ajde in the following sequence: - start( ) -
     * clearTasks( ) - setProgressBarMax( max ) - followed by any number of
     * setProgressBarVal(), incrementProgressBarVal( ) and setProgressText( )
     * calls in any order - finish( )
     */

    /**
     * Ajde has started a compile
     */
    public void start(final String configFile) {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err
                    .println("AJDE Callback: CompileProgressMonitor.start() called");
        currentAjdeProgress = 0;
        if (monitor != null) {
            AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (monitor != null) {
                        if (Builder.isLocalBuild) {
                            ProjectBuildConfigurator pbc = BuildConfigurator
                                    .getBuildConfigurator()
                                    .getActiveProjectBuildConfigurator();
                            if (pbc != null) {
                                String configName = pbc
                                        .getActiveBuildConfiguration()
                                        .getName();
                                monitor.setTaskName("Building project with \'"
                                        + configName + "\' configuration");
                            }
                        }
                    }// end if
                }// end run method
            });
        }
    }

    /**
     * Ajde wishes to display information about the progress of the compilation.
     */
    public void setProgressText(String text) {

        if (text.startsWith("compiled: ") && !reportedCompiledMessages) {
            reportedCompiledMessages = true;
            AJDTEventTrace.generalEvent("Time to first 'compiled:' message: "
                    + (System.currentTimeMillis() - compileStartTime) + "ms");
        }
        if (text.startsWith("woven: ") && !reportedWovenMessages) {
            reportedWovenMessages = true;
            AJDTEventTrace.generalEvent("Time to first 'woven:' message: "
                    + (System.currentTimeMillis() - compileStartTime) + "ms");
        }

        if (text.startsWith("compiled: ")) {
            // If a project contains a 'srclink' and that link is to a directory
            // that isn't defined
            // in another eclipse project, then we may get resource paths here
            // that cannot be
            // found in eclipse. So the entry added to affectedResources will be
            // null. However, as
            // this code is only used to ensure we tidy up markers, that does
            // not matter - if it does
            // not exist, it cannot have outstanding markers.

            IPath resourcePath = new Path(text.substring(10));
            IWorkspaceRoot workspaceRoot = AspectJUIPlugin.getWorkspace()
                    .getRoot();

            if (linked) {
                IFile[] files = workspaceRoot
                        .findFilesForLocation(resourcePath);
                for (int i = 0; i < files.length; i++) {
                    affectedResources.add(files[i]);
                }
            } else {
                IFile file = workspaceRoot.getFileForLocation(resourcePath);
                if (file == null)
                    AJDTEventTrace
                            .generalEvent("Processing progress message: Can't find eclipse resource for file with path "
                                    + text);
                else
                    affectedResources.add(file);
            }
        }

        final String amendedText = removePrefix(text);
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("AJDE Callback: setProgressText(" + text + ")");
        if (monitor != null) {
            AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (monitor != null)
                        monitor.subTask(amendedText);
                }
            });
        }

    }

    /**
     * Ajde wishes to advance the progress bar to this absolute value
     */
    public void setProgressBarVal(int newVal) {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("AJDE Callback: setProgressBarVal(" + newVal
                    + ")");

        if (newVal >= currentAjdeProgress) {
            incrementProgressBarVal("setProgressBarVal() delegating to ");
        }
    }

    /**
     * Ajde informs us that the compiler is assuming a maximum progress bar
     * value of <code>maxVal</code>
     */
    public void setProgressBarMax(int maxVal) {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("AJDE Callback: setProgressBarMax(" + maxVal
                    + ")");
        ajdeMonitorMaxRatio = ((float) maxVal)
                / AspectJUIPlugin.PROGRESS_MONITOR_MAX;
    }

    public void incrementProgressBarVal() {
        incrementProgressBarVal("AJDE Callback:");
    }

    /**
     * Ajde asks us to increment the progress bar by one
     */
    public void incrementProgressBarVal(String caller) {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println(caller + " incrementProgressBarVal():"
                    + currentAjdeProgress);
        currentAjdeProgress++;
        // Bug 22258 - Reworked internals of run() method, it used to be
        // responsible for
        // increasing currentProgress but I've changed it since if anything
        // causes the execution
        // of the thread to hang then currentProgress doesn't increase and
        // setProgressBarVal()
        // just loops calling this routine.
        if (monitor != null) {
            AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (monitor != null)
                        monitor.worked(1);
                }
            });
        }
    }

    /**
     * Ajde asks our preference for the maximum value of the progress bar
     */
    public int getProgressBarMax() {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("AJDE Callback: getProgressBarMax()");
        return AspectJUIPlugin.PROGRESS_MONITOR_MAX;
    }

    /**
     * Ajde informs us that a compilation has finished. We may under some
     * circumstances get multiple calls to finish. This method is marked
     * synchronized to let one finish finish before another finish gets in!
     */
    public synchronized void finish() {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("AJDE Callback: finish()");
        // AMC - moved this next monitor var set outside of thread -
        // this status change must be instantly visible
        compilationInProgress = false;

        // Summarize what happened during weaving...
        AJDTEventTrace.generalEvent("Weaver stress level: ");
        int fastMatchOnTypeMaybe = (WeaverMetrics.fastMatchOnTypeAttempted
                - WeaverMetrics.fastMatchOnTypeTrue - WeaverMetrics.fastMatchOnTypeFalse);
        AJDTEventTrace.generalEvent("Fast fast matching (type level) of #"
                + WeaverMetrics.fastMatchOnTypeAttempted + " types "
                + "resulting in us dismissing "
                + WeaverMetrics.fastMatchOnTypeFalse);
        //System.err.println(" YES/NO/MAYBE =
        // "+Metrics.fastMatchOnTypeTrue+"/"+Metrics.fastMatchOnTypeFalse+"/"+Metrics.fastMatchOnTypeMaybe);
        //		int fastMatchMaybe = (WeaverMetrics.fastMatchOnShadowsAttempted
        //				- WeaverMetrics.fastMatchOnShadowsFalse -
        // WeaverMetrics.fastMatchOnShadowsTrue);
        AJDTEventTrace.generalEvent("Fast matching within the remaining #"
                + (WeaverMetrics.fastMatchOnTypeTrue + fastMatchOnTypeMaybe)
                + " types, " + "we fast matched on #"
                + WeaverMetrics.fastMatchOnShadowsAttempted
                + " shadows and dismissed #"
                + WeaverMetrics.fastMatchOnShadowsFalse);
        // System.err.println(" YES/NO/MAYBE =
        // "+Metrics.fastMatchTrue+"/"+Metrics.fastMatchFalse+"/"+fastMatchMaybe);
        AJDTEventTrace.generalEvent("Slow match then attempted on #"
                + WeaverMetrics.matchAttempted + " shadows of which "
                + WeaverMetrics.matchTrue + " successful");
        WeaverMetrics.reset();

        if (AspectJUIPlugin.getDefault().getDisplay().isDisposed())
            System.err.println("Not finishing with bpm, display is disposed!");
        else
            AspectJUIPlugin.getDefault().getDisplay().asyncExec(new Runnable() {
                public void run() {

                    if (monitor != null) {
                        // ask the project to perform a refresh to pick up the
                        // newly generated classfiles - bug 30462
                        // It think during the processing in refreshOutputDir -
                        // monitor can go null...
                        //                    if (monitor!=null) monitor.setTaskName("");
                        if (monitor != null)
                            monitor.worked(AspectJUIPlugin.PROGRESS_MONITOR_MAX);
                        if (monitor != null)
                            monitor.done();
                        monitor = null;
                    }
                    //monitoringInProgress = false;
                    // Attempting this here will lock the GUI up if there is
                    // another
                    // compile about to
                    // be done. So it has been moved to an equivalent call
                    // (showOutstandingProblems())
                    // in the Builder.java file.
                    //showMessages();

                    // this next piece should really be handled by an aspect
                    //				AspectVisualiserPlugin.getDefault().refreshView();

                }
            });
    }

    /**
     * Has the most recent compile finished?
     */
    public boolean finished() {
        return !compilationInProgress;
    }

    /** ******* CompileProgressMonitor interface implementation ************* END */

    /**
     * A compiler message has been emitted, which potentially includes the fully
     * qualifed path of a resource. Chop it down to just the project-relative
     * portion.
     */
    private String removePrefix(String msg) {
        String ret = msg;
        IProject p = AspectJUIPlugin.getDefault().getCurrentProject();
        String projectLocation = p.getLocation().toOSString() + "\\";
        if (msg.indexOf(projectLocation) != -1) {
            ret = msg.substring(0, msg.indexOf(projectLocation))
                    + msg.substring(msg.indexOf(projectLocation)
                            + projectLocation.length());
        } else {
            projectLocation = projectLocation.replace('\\', '/');
            if (msg.indexOf(projectLocation) != -1) {
                ret = msg.substring(0, msg.indexOf(projectLocation))
                        + msg.substring(msg.indexOf(projectLocation)
                                + projectLocation.length());
            }
        }

        // special cases...
        // this message is too long to be meaningful
        if (ret.startsWith("might need to weave")) {
            ret = "weaving ...";
        }
        // we always get this next one, and it seems to be nonsense
        if (ret.startsWith("directory classpath entry does not exist: null")) {
            ret = "";
        }

        // chop (from x\y\z\a\b\C.java) to just (C.java)
        if (ret.startsWith("woven") && ret.indexOf("(from") != -1) {
            int loc = ret.indexOf("(from");
            if (loc != -1) {
                String fromPiece = ret.substring(loc);
                int lastSlash = fromPiece.lastIndexOf("/");
                if (lastSlash == -1)
                    lastSlash = fromPiece.lastIndexOf("\\");
                if (lastSlash != -1) {
                    fromPiece = fromPiece.substring(lastSlash + 1);
                    ret = ret.substring(0, loc) + " (" + fromPiece;
                } else {
                    int space = fromPiece.indexOf(" ");
                    if (space != -1)
                        ret = ret.substring(0, loc) + " ("
                                + fromPiece.substring(space + 1);
                }
            }
        }
        return ret;
    }

    /** ******* TaskListManager interface implementation ************* START */

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
            System.err.println("CompilerMessage received ]" + message + "[");

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

    /**
     * Called from Ajde to clear all tasks in problem list
     */
    public void clearTasks() {
        if (AspectJUIPlugin.DEBUG_COMPILER)
            System.err.println("clearTasks() called");

        affectedResources = new ArrayList();
        problems = new ArrayList();
    }

    /** ******* TaskListManager interface implementation ************* END */

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
            new CompilerMonitor().showMessages();
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
                    IResource ir = null;
                    //boolean wipedProjectLevelMarkers = false;
                    while (affectedResourceIterator.hasNext()) {
                        ir = (IResource) affectedResourceIterator.next();
                        try {
                            if (ir.exists()) {
                                ir.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
                                        IResource.DEPTH_INFINITE);
                                ir.deleteMarkers(IMarker.TASK, true,
                                        IResource.DEPTH_INFINITE);
                            }
                        } catch (ResourceException re) {
                            AJDTEventTrace
                                    .generalEvent("Failed marker deletion: resource="
                                            + ir.getLocation());
                            throw re;
                        }
                    }

                    IProject project = org.eclipse.ajdt.internal.builder.Builder
                            .getLastBuildTarget();
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
                            //AJDTEventTrace.generalEvent("Creating Marker,
                            // kind: '" + p.kind + "'; text: '" + p.message
                            // + "'");                            
                            if (p.location != null) {
                                ir = locationToResource(p.location, project);
                                int prio = getTaskPriority(p);
                                if (prio != -1) {
                                    marker = ir.createMarker(IMarker.TASK);
                                    marker.setAttribute(IMarker.PRIORITY, prio);
                                } else {
                                    if (p.declaredErrorOrWarning) {
                                        marker = ir
                                                .createMarker("org.eclipse.ajdt.ui.problemmarker");
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
                                    marker.setAttribute(
                                            AspectJUIPlugin.RELATED_LOCATIONS_ATTRIBUTE_PREFIX
                                            +(relCount++),
										sLoc.getSourceFile().getAbsolutePath()
											+ ":::"
											+ sLoc.getLine()
											+ ":::"
											+ sLoc.getEndLine()
											+ ":::"
											+ sLoc.getColumn());
                                }
                            }
                            
                            setMessage(marker, p.message);
                        } catch (ResourceException re) {
                            AJDTEventTrace
                                    .generalEvent("Failed marker creation: resource="
                                            + p.location.getSourceFile()
                                                    .getPath()
                                            + " line="
                                            + p.location.getLine()
                                            + " message=" + p.message);
                            throw re;
                        }
                    }
                    clearTasks();
                } catch (Exception e) {
                    AspectJUIPlugin.getDefault().getErrorHandler().handleError(
                            "Error creating marker", e);
                }
            }
        };

        try {
            AspectJUIPlugin.getWorkspace().run(r, null);
        } catch (CoreException cEx) {
            AspectJUIPlugin.getDefault().getErrorHandler().handleError(
                    "AJDT Error adding problem markers", cEx);
        }
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
        if (message.indexOf("\":") != -1 && message.indexOf(", at line") != -1) {
            String hackedMessage = message
                    .substring(message.indexOf("\":") + 2);
            message = hackedMessage.substring(0, hackedMessage
                    .indexOf(", at line"));
        }
        marker.setAttribute(IMarker.MESSAGE, message);
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
        String tags = pref.getString("org.eclipse.jdt.core.compiler.taskTags");
        String caseSens = pref
                .getString("org.eclipse.jdt.core.compiler.taskCaseSensitive");
        String priorities = pref
                .getString("org.eclipse.jdt.core.compiler.taskPriorities");

        boolean caseSensitive;
        if (caseSens.equals("disabled")) {
            caseSensitive = false;
        } else {
            caseSensitive = true;
        }

        StringTokenizer tagTokens = new StringTokenizer(tags, ",");
        StringTokenizer priorityTokens = new StringTokenizer(priorities, ",");
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
        if (prio.equals("NORMAL"))
            return IMarker.PRIORITY_NORMAL;
        if (prio.equals("HIGH"))
            return IMarker.PRIORITY_HIGH;
        return IMarker.PRIORITY_LOW;
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
                //ir =
                // AspectJPlugin.getDefault().getCurrentProject();
            }
            // At least warn that you are going to
            // blow up
            // with an event trace ...
            if (ir == null)
                AJDTEventTrace
                        .generalEvent("Whilst adding post compilation markers to resources, cannot locate valid eclipse resource for file "
                                + loc);
        }

        return ir;
    }

    private IResource tryToFindResource(String fileName) {
        IResource ret = null;
        String toFind = fileName.replace('\\', '/');
        IProject project = AspectJUIPlugin.getDefault().getCurrentProject();
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
                    IResource projResource = AspectJUIPlugin.getWorkspace()
                            .getRoot().findMember(projPath);
                    ret = findFile(new IResource[] { projResource }, toFind);
                }
            }
        } catch (JavaModelException jmEx) {
            String message = AspectJUIPlugin.getResourceString("jmCoreException");
            Status status = new Status(Status.ERROR, AspectJUIPlugin.PLUGIN_ID,
                    Status.OK, message, jmEx);
            Shell shell = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow()
                    .getShell();
            org.eclipse.jface.dialogs.ErrorDialog.openError(shell,
                    AspectJUIPlugin.getResourceString("ajErrorDialogTitle"),
                    message, status);
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
     * Inner class used to ensure marker creation and attribute setting occur as
     * an atomic unit (otherwise tick marks do not appear correctly in editor
     * margin.
     */
    class ProblemAdder implements IWorkspaceRunnable {
        private Iterator problemIterator;

        public ProblemAdder(Iterator it) {
            this.problemIterator = it;
        }

        public void run(IProgressMonitor monitor) {
            System.out.println("Adding problem markers");
            while (problemIterator.hasNext()) {
                ProblemTracker p = (ProblemTracker) problemIterator.next();
                IResource ir = null;
                if (p.location != null) {
                    String loc = p.location.getSourceFile().getAbsolutePath();
                    ir = AspectJUIPlugin.getDefault().getAjdtProjectProperties()
                            .findResource(loc);
                    if (ir == null) {
                        ir = AspectJUIPlugin.getDefault().getCurrentProject();
                    }
                    try {
                        IMarker im = ir.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
                        if (p.location.getLine() > 0) {
                            im.setAttribute(IMarker.LINE_NUMBER, p.location
                                    .getLine());
                        }
                        if (p.location.getColumn() > 0) {
                            //im.setAttribute(IMarker.CHAR_START ,
                            // p.location.getColumnNumber());
                            //im.setAttribute(IMarker.CHAR_END,...);
                        }

                        if (p.kind == IMessage.ERROR) {
                            im.setAttribute(IMarker.SEVERITY,
                                    IMarker.SEVERITY_ERROR);
                        } else if (p.kind == IMessage.WARNING) {
                            im.setAttribute(IMarker.SEVERITY,
                                    IMarker.SEVERITY_WARNING);
                        } else {
                            im.setAttribute(IMarker.SEVERITY,
                                    IMarker.SEVERITY_INFO);
                        }

                        // FIXME: Remove this horrid hack.
                        // Hack the filename off the front and the line number
                        // off the end
                        if (p.message.indexOf("\":") != -1
                                && p.message.indexOf(", at line") != -1) {
                            String hackedMessage = p.message
                                    .substring(p.message.indexOf("\":") + 2);
                            hackedMessage = hackedMessage.substring(0,
                                    hackedMessage.indexOf(", at line"));
                            im.setAttribute(IMarker.MESSAGE, hackedMessage);
                        } else {
                            im.setAttribute(IMarker.MESSAGE, p.message);
                        }
                    } catch (Exception e) {
                        AspectJUIPlugin.getDefault().getErrorHandler()
                                .handleError("Error creating marker", e);
                    }
                }
            }
            clearTasks();
        } // end of run method

    }; // end of ProblemAdder class

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
    };
}