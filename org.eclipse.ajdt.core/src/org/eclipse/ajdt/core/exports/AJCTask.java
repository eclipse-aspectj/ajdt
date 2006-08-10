/**********************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.core.exports;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.internal.build.Utils;
import org.eclipse.pde.internal.build.ant.AntScript;
import org.eclipse.pde.internal.build.ant.JavacTask;
import org.osgi.framework.Bundle;


public class AJCTask extends JavacTask {
	private String baseLocation;

	private String buildConfig;

	private String toolsLocation;

	protected List aspectpath;
	protected List inpath;

	public AJCTask(String location, String config, String toolsLocation) {
		super();
		this.baseLocation = location;
		this.buildConfig = config;
		this.toolsLocation = toolsLocation;
	}
	
	public void print(AntScript script) {
		if(script instanceof AJAntScript) {
			AJAntScript ajScript = (AJAntScript)script;
			Bundle toolsBundle = Platform.getBundle(AspectJPlugin.TOOLS_PLUGIN_ID);
			Bundle weaverBundle = Platform.getBundle(AspectJPlugin.WEAVER_PLUGIN_ID);
			Bundle runtimeBundle = Platform.getBundle(AspectJPlugin.RUNTIME_PLUGIN_ID);
			try {
				URL resolved = FileLocator.resolve(toolsBundle.getEntry("/")); //$NON-NLS-1$
				IPath ajdeLocation = Utils.makeRelative(new Path(resolved
						.getFile()), new Path(baseLocation));
				resolved = FileLocator.resolve(weaverBundle.getEntry("/")); //$NON-NLS-1$
				IPath weaverLocation = Utils.makeRelative(new Path(resolved
						.getFile()), new Path(baseLocation));
				resolved = FileLocator.resolve(runtimeBundle.getEntry("/")); //$NON-NLS-1$
				IPath runtimeLocation = Utils.makeRelative(new Path(resolved
						.getFile()), new Path(baseLocation));
				// toolsLocation locates the eclipse classes required by the iajc task,
				// such as OperationCanceledException from org.eclipse.equinox.common
				IPath eqcomLocation = Utils.makeRelative(new Path(toolsLocation),
						new Path(baseLocation));
				ajScript.printProperty(
						"aspectj.plugin.home", ajdeLocation.toPortableString()); //$NON-NLS-1$
				ajScript.printProperty(
						"aspectj.weaver.home", weaverLocation.toPortableString()); //$NON-NLS-1$
				ajScript.printProperty(
						"aspectj.runtime.home", runtimeLocation.toPortableString()); //$NON-NLS-1$
				ajScript.printProperty(
						"eclipse.tools.home", eqcomLocation.toPortableString()); //$NON-NLS-1$
			} catch (IOException e) {
			}
			
			// 101041: use includes/excludes from a build config file if one has
			// been specified, otherwise use all source folders
			boolean useBuildConfig = (buildConfig != null)
					&& (buildConfig.length() > 0);
			
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
			
			if (useBuildConfig) {
				ajScript.printTab();
				ajScript.print("<property"); //$NON-NLS-1$
				ajScript.printAttribute("file", buildConfig, false); //$NON-NLS-1$ //$NON-NLS-2$
				ajScript.print("/>"); //$NON-NLS-1$
				ajScript.println();
			}
			
			ajScript.printTab();
			ajScript.print("<iajc"); //$NON-NLS-1$
			ajScript.printAttribute("destDir", destdir, false); //$NON-NLS-1$
			ajScript.printAttribute("failonerror", "true", false); //$NON-NLS-1$ //$NON-NLS-2$
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
	
			ajScript.println("<forkclasspath refid=\"" + classpathId + "\" />"); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.printStartTag("forkclasspath"); //$NON-NLS-1$
			ajScript.indent++;
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
			ajScript.printTab();
			ajScript.print("<pathelement"); //$NON-NLS-1$
			ajScript.printAttribute("path", "${aspectj.runtime.home}/aspectjrt.jar", true); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.print("/>"); //$NON-NLS-1$
			ajScript.println();
			ajScript.printTab();
			ajScript.print("<pathelement"); //$NON-NLS-1$
			ajScript.printAttribute("path", "${eclipse.tools.home}", true); //$NON-NLS-1$ //$NON-NLS-2$
			ajScript.print("/>"); //$NON-NLS-1$
			ajScript.println();
			
			ajScript.indent--;
			ajScript.printEndTag("forkclasspath"); //$NON-NLS-1$
	
			// Add aspectpath and inpath
			if(aspectpath != null) {
				ajScript.printStartTag("aspectpath"); //$NON-NLS-1$
				ajScript.indent++;
				for (Iterator iter = aspectpath.iterator(); iter.hasNext();) {
					String path = (String) iter.next();
					ajScript.printTab();
					ajScript.print("<pathelement"); //$NON-NLS-1$
					ajScript.printAttribute("path", path, false); //$NON-NLS-1$ //$NON-NLS-2$
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
					ajScript.printAttribute("path", path, false); //$NON-NLS-1$ //$NON-NLS-2$
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
