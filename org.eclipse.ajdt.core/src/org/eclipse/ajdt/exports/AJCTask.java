/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.exports;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.internal.build.Utils;
import org.eclipse.pde.internal.build.ant.AntScript;
import org.eclipse.pde.internal.build.ant.JavacTask;
import org.osgi.framework.Bundle;


public class AJCTask extends JavacTask {
	private String baseLocation;
	
	public AJCTask(String location) {
		super();
		this.baseLocation = location;
	}
	
	public void print(AntScript script) {
		if(script instanceof AJAntScript) {
			AJAntScript ajScript = (AJAntScript)script;
			Bundle toolsBundle = Platform.getBundle(AspectJPlugin.TOOLS_PLUGIN_ID);
			Bundle weaverBundle = Platform.getBundle(AspectJPlugin.WEAVER_PLUGIN_ID);
			try {
				URL resolved = Platform.resolve(toolsBundle.getEntry("/")); //$NON-NLS-1$
				String ajdeLoc = new File(Platform.asLocalURL(resolved)
						.getFile()).getAbsolutePath();
				IPath ajdeLocation = Utils.makeRelative(new Path(ajdeLoc),
						new Path(baseLocation));
				resolved = Platform.resolve(weaverBundle.getEntry("/")); //$NON-NLS-1$
				String weaverLoc = new File(Platform.asLocalURL(resolved)
						.getFile()).getAbsolutePath();
				IPath weaverLocation = Utils.makeRelative(new Path(weaverLoc),
						new Path(baseLocation));
				ajScript.printProperty(
						"aspectj.plugin.home", ajdeLocation.toString()); //$NON-NLS-1$
				ajScript.printProperty(
						"aspectj.weaver.home", weaverLocation.toString()); //$NON-NLS-1$
			} catch (IOException e) {
			}
						
			ajScript.println("<taskdef resource=\"org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties\">"); //$NON-NLS-1$
			ajScript.indent++;
			ajScript.printStartTag("classpath"); //$NON-NLS-1$
			ajScript.indent++;
			ajScript.printTab();
			ajScript.print("<pathelement"); //$NON-NLS-1$
			ajScript.printAttribute("path", "${aspectj.plugin.home}/ajde.jar", true); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.print("/>"); //$NON-NLS-1$
			ajScript.println();
			ajScript.printTab();
			ajScript.print("<pathelement"); //$NON-NLS-1$
			ajScript.printAttribute("path", "${aspectj.weaver.home}/aspectjweaver.jar", true); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.print("/>"); //$NON-NLS-1$
			ajScript.println();
			ajScript.indent--;
			ajScript.printEndTag("classpath"); //$NON-NLS-1$
			ajScript.indent--;
			ajScript.printEndTag("taskdef"); //$NON-NLS-1$
			
			ajScript.printTab();
			ajScript.print("<iajc"); //$NON-NLS-1$
			ajScript.printAttribute("destDir", destdir, false); //$NON-NLS-1$
			ajScript.printAttribute("failonerror", failonerror, false); //$NON-NLS-1$
			ajScript.printAttribute("verbose", verbose, false); //$NON-NLS-1$
			ajScript.printAttribute("fork", "true", false); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.printAttribute("debug", debug, false); //$NON-NLS-1$
			ajScript.printAttribute("bootclasspath", bootclasspath, false); //$NON-NLS-1$
			ajScript.printAttribute("source", source, false); //$NON-NLS-1$
			ajScript.printAttribute("target", target, false); //$NON-NLS-1$
			ajScript.print(">"); //$NON-NLS-1$
			ajScript.println();
			
			ajScript.indent++;
	
			ajScript.printStartTag("forkclasspath"); //$NON-NLS-1$
			ajScript.indent++;
			for (Iterator iter = classpath.iterator(); iter.hasNext();) {
				String path = (String) iter.next();
				ajScript.printTab();
				ajScript.print("<pathelement"); //$NON-NLS-1$
				ajScript.printAttribute("path", path, false); //$NON-NLS-1$
				ajScript.print("/>"); //$NON-NLS-1$
				ajScript.println();
			}
			// Add ajde.jar and aspectjweaver.jar to this classpath too because we have forked
			ajScript.printTab();
			ajScript.print("<pathelement"); //$NON-NLS-1$
			ajScript.printAttribute("path", "${aspectj.plugin.home}/ajde.jar", true); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.print("/>"); //$NON-NLS-1$
			ajScript.println();
			ajScript.printTab();
			ajScript.print("<pathelement"); //$NON-NLS-1$
			ajScript.printAttribute("path", "${aspectj.weaver.home}/aspectjweaver.jar", true); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.print("/>"); //$NON-NLS-1$
			ajScript.println();
			
			ajScript.indent--;
			ajScript.printEndTag("forkclasspath"); //$NON-NLS-1$
	
			ajScript.printStartTag("srcdir"); //$NON-NLS-1$
			ajScript.indent++;
			for (int i = 0; i < srcdir.length; i++) {
				ajScript.printTab();
				ajScript.print("<pathelement"); //$NON-NLS-1$
				ajScript.printAttribute("path", srcdir[i], false); //$NON-NLS-1$
				ajScript.print("/>"); //$NON-NLS-1$
				ajScript.println();
			}
			ajScript.indent--;
			ajScript.printEndTag("srcdir"); //$NON-NLS-1$
			
			ajScript.printEndTag("iajc"); //$NON-NLS-1$
			ajScript.indent--;
		}
	}

	
}
