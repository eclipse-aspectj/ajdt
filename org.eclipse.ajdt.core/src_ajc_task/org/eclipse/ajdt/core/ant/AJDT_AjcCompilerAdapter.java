/********************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wes Isberg        initial implementation
 *     Andrew Eisenberg  Adapted for use with AJDT
 *******************************************************************/

package org.eclipse.ajdt.core.ant;

import java.io.File;
import java.lang.reflect.Method;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.compilers.CompilerAdapter;
import org.apache.tools.ant.util.SourceFileScanner;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.util.FileUtil;

/**
 * Adapt ajc to javac commands.
 * Note that the srcdirs set for javac are NOT passed on to ajc;
 * instead, the list of source files generated is passed to ajc.
 * <p>
 * Javac usually prunes the source file list based on the timestamps
 * of corresponding .class files, which is wrong for ajc which
 * requires all the files every time.  To work around this,
 * set the global property CLEAN ("build.compiler.clean") to delete
 * all .class files in the destination directory before compiling.
 * 
 * <p><u>Warnings</u>: 
 * <ol>
 * <li>cleaning will not work if no destination directory
 *     is specified in the javac task.
 *     (RFE: find and kill .class files in source dirs?)</li>
 * <li>cleaning will makes stepwise build processes fail
 * if they depend on the results of the prior compilation being
 * in the same directory, since this deletes <strong>all</strong>
 * .class files.</li>
 * <li>If no files are out of date, then the adapter is <b>never</b> called
 *     and thus cannot gain control to clean out the destination dir.
 *     </li>
 * <p>
 * 
 * @author Wes Isberg
 * @author Andrew Eisenberg
 * @since AspectJ 1.1, Ant 1.5.1, AJDT 2.1.0
 */
public class AJDT_AjcCompilerAdapter implements CompilerAdapter {
    
    
    /** 
     * Define this system/project property to signal that the 
     * destination directory should be cleaned 
     * and javac reinvoked
     * to get the complete list of files every time.
     */
    public static final String CLEAN = "build.compiler.clean";

    /** track whether we re-called <code>javac.execute()</code> */
    private static final ThreadLocal<Boolean> inSelfCall = new ThreadLocal<Boolean>() {
        public Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    Javac javac;

    public void setJavac(Javac javac) {
        this.javac = javac;
        javac.setTaskName(javac.getTaskName() + " - ajc");
    }

    public boolean execute() throws BuildException {
        javac.log("Note that you may see messages about skipping *.aj files above. " +
        		"These messages can be ignored as these files are handled directly by " +
        		"ajc.  Similarly, the messages below about skipping *.java files can be ignored.", Project.MSG_INFO);
        
        // javac task spits out messages that it 
        
        if (null == javac) {
            throw new IllegalStateException("null javac");
        }
        if (!((Boolean) inSelfCall.get()).booleanValue()
            && afterCleaningDirs()) {
            // if we are not re-calling ourself and we cleaned dirs,
            // then re-call javac to get the list of all source files.
            inSelfCall.set(Boolean.TRUE);
            javac.execute();
            // javac re-invokes us after recalculating file list
        } else {
            try {
                AjcTask ajc = new AjcTask();
                String err = ajc.setupAjc(javac);
                if (null != err) {
                    throw new BuildException(err, javac.getLocation());
                }
                addAJFiles(ajc);
                IMessageHolder handler = new MessageHandler();
                ajc.setMessageHolder(handler);

                String logFile = null;
                String[] args = javac.getCurrentCompilerArgs();
                for (int i = 0; i < args.length; i++) {
                    if (args[i].equals("-log") && args.length > i) {
                        logFile = args[i+1]; 
                        ajc.setLog(new File(logFile));
                        break;
                    }
                }

                ajc.execute();
                
                // if log file is used, this message handler will never show any errors
                IMessage[] messages = handler.getMessages(IMessage.ERROR, true);
                if (messages != IMessage.RA_IMessage && logFile != null) {
                    // log messages
                    String msg = "Compilation has errors or warnings. Log is available in " + logFile; 
                    javac.log(msg, Project.MSG_INFO);
                    return false;
                } else {
                    return ajc.wasCompilationSuccessful();
                }
                
            } finally {
                inSelfCall.set(Boolean.FALSE);
            }
        }
        return true;
    }

    /**
     * @param ajc
     */
    private void addAJFiles(AjcTask ajc) {
        ajc.addFiles(getAJFiles());
    }

    /**
     * If destDir exists and property CLEAN is set, 
     * this cleans out the dest dir of any .class files,
     * and returns true to signal a recursive call.
     * @return true if destDir was cleaned.
     */
    private boolean afterCleaningDirs() {
        String clean = javac.getProject().getProperty(CLEAN);
        if (null == clean) {
            return false;
        }
        File destDir = javac.getDestdir();
        if (null == destDir) {
            javac.log(
                CLEAN + " specified, but no dest dir to clean",
                Project.MSG_WARN);
            return false;
        }
        javac.log(
            CLEAN + " cleaning .class files from " + destDir,
            Project.MSG_VERBOSE);
        FileUtil.deleteContents(
            destDir,
            FileUtil.DIRS_AND_WRITABLE_CLASSES,
            true);
        return true;
    }

    
    
    
    protected File[] getAJFiles() {
        String[] list = javac.getSrcdir().list();
        File destDir = javac.getDestdir();
        File[] sourceFiles = new File[0]; 
        for (int i = 0; i < list.length; i++) {
            File srcDir = javac.getProject().resolveFile(list[i]);
            if (!srcDir.exists()) {
                throw new BuildException("srcdir \""
                                         + srcDir.getPath()
                                         + "\" does not exist!", javac.getLocation());
            }

            DirectoryScanner ds = getDirectoryScanner(srcDir);
            String[] files = ds.getIncludedFiles();

            AJFileNameMapper m = new AJFileNameMapper();
            SourceFileScanner sfs = new SourceFileScanner(javac);
            File[] moreFiles = sfs.restrictAsFiles(files, srcDir, destDir, m);
            if (moreFiles != null) {
                File[] origFiles = sourceFiles;
                sourceFiles = new File[origFiles.length + moreFiles.length];
                System.arraycopy(origFiles, 0, sourceFiles, 0, origFiles.length);
                System.arraycopy(moreFiles, 0, sourceFiles, origFiles.length, moreFiles.length);
            }
        }
        return sourceFiles;
    }

    /**
     * @param srcDir
     * @return
     */
    private DirectoryScanner getDirectoryScanner(File srcDir) {
        try {
            Method getDirectoryScannerMethod = MatchingTask.class.getDeclaredMethod("getDirectoryScanner", File.class);
            getDirectoryScannerMethod.setAccessible(true);
            return (DirectoryScanner) getDirectoryScannerMethod.invoke(javac, srcDir);
        } catch (Exception e) {
            throw new BuildException("Problem finding directory scanner for srcdir \""
                    + srcDir.getPath()
                    + "\"", e);
        }
    }

}
