/**********************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.build.tasks;

import java.util.Iterator;
import java.util.List;

import org.eclipse.pde.internal.build.ant.AntScript;
import org.eclipse.pde.internal.build.ant.JavacTask;


public class AJCTask extends JavacTask {

	private String buildConfig;

	private String[] ajdeClasspath;

	protected List aspectpath;
	protected List inpath;

	public AJCTask(String config, String[] ajdeClasspath) {
		super();
		this.buildConfig = config;
		this.ajdeClasspath = ajdeClasspath;
	}
	
	public void print(AntScript script) {
		if(script instanceof AJAntScript) {
			AJAntScript ajScript = (AJAntScript)script;
			
			ajScript.println("<path id=\"ajde.classpath\">"); //$NON-NLS-1$
			ajScript.indent++;
			if (ajdeClasspath != null) { 
    			for (int i = 0; i < ajdeClasspath.length; i++) {		
    				ajScript.printTab();
    				ajScript.print("<pathelement"); //$NON-NLS-1$
    				ajScript.printAttribute("path", ajdeClasspath[i], true); //$NON-NLS-1$ 
    				ajScript.print("/>"); //$NON-NLS-1$
    				ajScript.println();
    			}
		    }
			ajScript.indent--;
			ajScript.printTab();
			ajScript.print("</path>"); //$NON-NLS-1$
			ajScript.println();
						
			// 101041: use includes/excludes from a build config file if one has
			// been specified, otherwise use all source folders
			boolean useBuildConfig = (buildConfig != null)
					&& (buildConfig.length() > 0);
			
			ajScript.println("<taskdef resource=\"org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties\">"); //$NON-NLS-1$
			ajScript.indent++;
			ajScript.println("<classpath refid=\"ajde.classpath\" />"); //$NON-NLS-1$ 
			ajScript.indent--;
			ajScript.printEndTag("taskdef"); //$NON-NLS-1$
			
			if (useBuildConfig) {
				ajScript.printTab();
				ajScript.print("<property"); //$NON-NLS-1$
				ajScript.printAttribute("file", buildConfig, false); //$NON-NLS-1$ 
				ajScript.print("/>"); //$NON-NLS-1$
				ajScript.println();
			}

			ajScript.printProperty("ajcArgFile",""); //$NON-NLS-1$ //$NON-NLS-2$

			ajScript.printTab();
			ajScript.print("<iajc"); //$NON-NLS-1$
			ajScript.printAttribute("destDir", destdir, false); //$NON-NLS-1$
			ajScript.printAttribute("failonerror", "true", false); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.printAttribute("argfiles", "${ajcArgFile}", false); //$NON-NLS-1$ //$NON-NLS-2$	
			ajScript.printAttribute("verbose", "true", false); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.printAttribute("fork", "true", false); //$NON-NLS-1$ //$NON-NLS-2$
			if (useBuildConfig) {
				ajScript.printAttribute("srcdir", ".", false); //$NON-NLS-1$ //$NON-NLS-2$
				ajScript.printAttribute("includes", "${src.includes}", false); //$NON-NLS-1$ //$NON-NLS-2$
				ajScript.printAttribute("excludes", "${src.excludes}", false); //$NON-NLS-1$ //$NON-NLS-2$
			}
			ajScript.printAttribute("maxmem", "512m", false); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.printAttribute("debug", debug, false); //$NON-NLS-1$
			ajScript.printAttribute("bootclasspath", bootclasspath, false); //$NON-NLS-1$
			ajScript.printAttribute("source", source, false); //$NON-NLS-1$
			ajScript.printAttribute("target", target, false); //$NON-NLS-1$
			ajScript.print(">"); //$NON-NLS-1$
			ajScript.println();
			
			ajScript.indent++;
	
			ajScript.println("<classpath refid=\"" + classpathId + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.println("<classpath refid=\"ajde.classpath\" />"); //$NON-NLS-1$ 
			ajScript.println("<forkclasspath refid=\"ajde.classpath\" />"); //$NON-NLS-1$ 
	
			// Add aspectpath and inpath
			if(aspectpath != null) {
				ajScript.printStartTag("aspectpath"); //$NON-NLS-1$
				ajScript.indent++;
				for (Iterator iter = aspectpath.iterator(); iter.hasNext();) {
					String path = (String) iter.next();
					ajScript.printTab();
					ajScript.print("<pathelement"); //$NON-NLS-1$
					ajScript.printAttribute("path", path, false); //$NON-NLS-1$ 
					ajScript.print("/>"); //$NON-NLS-1$
					ajScript.println();
				}
				ajScript.indent--;
				ajScript.printEndTag("aspectpath"); //$NON-NLS-1$				
			}
			if(inpath != null) {
				ajScript.printStartTag("inpath"); //$NON-NLS-1$
				ajScript.indent++;
				for (Iterator iter = inpath.iterator(); iter.hasNext();) {
					String path = (String) iter.next();
					ajScript.printTab();
					ajScript.print("<pathelement"); //$NON-NLS-1$
					ajScript.printAttribute("path", path, false); //$NON-NLS-1$ 
					ajScript.print("/>"); //$NON-NLS-1$
					ajScript.println();
				}
				ajScript.indent--;
				ajScript.printEndTag("inpath"); //$NON-NLS-1$
			}
			
			if (!useBuildConfig) {
				for (int i = 0; i < srcdir.length; i++) {
					ajScript.printTab();
					ajScript.print("<src path="); //$NON-NLS-1$
					ajScript.printQuotes(srcdir[i]);
					ajScript.println("/>"); //$NON-NLS-1$
				}
			}
			ajScript.indent--;
			ajScript.printEndTag("iajc"); //$NON-NLS-1$
		}
	}

	public void setAspectpath(List aspectpath) {
		this.aspectpath = aspectpath;
	}

	public void setInpath(List inpath) {
		this.inpath = inpath;
	}
	
}
