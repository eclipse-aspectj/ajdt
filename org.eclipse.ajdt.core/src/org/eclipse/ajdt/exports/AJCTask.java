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

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.pde.internal.build.ant.AntScript;
import org.eclipse.pde.internal.build.ant.JavacTask;
import org.osgi.framework.Bundle;


public class AJCTask extends JavacTask {

	public void print(AntScript script) {
		if(script instanceof AJAntScript) {
			AJAntScript ajScript = (AJAntScript)script;
			Bundle bundle = Platform.getBundle(AspectJPlugin.TOOLS_PLUGIN_ID);
			URL url = bundle.getEntry("/"); //$NON-NLS-1$
			URL resolved;
			try {
				resolved = Platform.resolve(url);
				String AJDELocation = resolved.toExternalForm();
				ajScript.printProperty("aspectj.plugin.home", AJDELocation); //$NON-NLS-1$
			} catch (IOException e) {
			}
						
			ajScript.printTab();
			ajScript.println("<taskdef resource=\"org/aspectj/tools/ant/taskdefs/aspectjTaskdefs.properties\">"); //$NON-NLS-1$
			ajScript.printStartTag("classpath"); //$NON-NLS-1$
			ajScript.print("<pathelement"); //$NON-NLS-1$
			ajScript.printAttribute("path", "${aspectj.plugin.home}/ajde.jar", true); //$NON-NLS-1$
			ajScript.println("/>"); //$NON-NLS-1$
			ajScript.printEndTag("classpath"); //$NON-NLS-1$
			ajScript.printEndTag("taskdef"); //$NON-NLS-1$
			
			ajScript.printTab();
			ajScript.print("<iajc"); //$NON-NLS-1$
			ajScript.printAttribute("destDir", destdir, false); //$NON-NLS-1$
			ajScript.printAttribute("failonerror", failonerror, false); //$NON-NLS-1$
			ajScript.printAttribute("verbose", verbose, false); //$NON-NLS-1$
			ajScript.printAttribute("fork", "true", false); //$NON-NLS-1$
			ajScript.printAttribute("debug", debug, false); //$NON-NLS-1$
			ajScript.printAttribute("bootclasspath", bootclasspath, false); //$NON-NLS-1$
			ajScript.printAttribute("source", source, false); //$NON-NLS-1$
			ajScript.printAttribute("target", target, false); //$NON-NLS-1$
			ajScript.println(">"); //$NON-NLS-1$
	
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
			// Add ajde.jar to this classpath too because we have forked
			ajScript.print("<pathelement"); //$NON-NLS-1$
			ajScript.printAttribute("path", "${aspectj.plugin.home}/ajde.jar", true); //$NON-NLS-1$
			ajScript.println("/>"); //$NON-NLS-1$
			
			ajScript.indent--;
			ajScript.printEndTag("forkclasspath"); //$NON-NLS-1$
	
			ajScript.printStartTag("srcdir"); 
			ajScript.indent++;
			for (int i = 0; i < srcdir.length; i++) {
				ajScript.printTab();
				ajScript.print("<pathelement"); //$NON-NLS-1$
				ajScript.printAttribute("path", srcdir[i], false);
				ajScript.println("/>"); //$NON-NLS-1$
			}
			ajScript.indent--;
			ajScript.printEndTag("srcdir"); //$NON-NLS-1$
			
			ajScript.printEndTag("iajc"); //$NON-NLS-1$
			ajScript.indent--;
		}
	}

	
}
