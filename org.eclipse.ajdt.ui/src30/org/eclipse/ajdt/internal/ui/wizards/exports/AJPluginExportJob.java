/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.ui.wizards.exports;
 
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eclipse.ajdt.internal.exports.AJBuildScriptGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.pde.internal.build.BuildScriptGenerator;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.build.ClasspathHelper;
import org.eclipse.pde.internal.ui.wizards.exports.PluginExportJob;

/**
 * Mostly copied from PluginExportJob.  
 * Enables AspectJ plugins to be correctly exported
 */
public class AJPluginExportJob extends PluginExportJob {

	private String fDevProperties;
	
	/**
	 * @param exportType
	 * @param exportSource
	 * @param destination
	 * @param zipFileName
	 * @param items
	 */
	public AJPluginExportJob(int exportType, boolean exportSource,
			String destination, String zipFileName, Object[] items) {
		super(exportType, exportSource, destination, zipFileName, items);
	}

	protected void doExport(String featureID, String version, String featureLocation, String os, String ws, String arch, IProgressMonitor monitor)
	throws CoreException, InvocationTargetException {
		monitor.beginTask("", 5); //$NON-NLS-1$
		monitor.setTaskName(PDEPlugin.getResourceString("FeatureExportJob.taskName")); //$NON-NLS-1$
		try {
			HashMap properties = createBuildProperties(os, ws, arch);
			makeScript(featureID, version, os, ws, arch, featureLocation);
			monitor.worked(1);
			runScript(getBuildScriptName(featureLocation), getBuildExecutionTargets(),
					properties, new SubProgressMonitor(monitor, 2));
			runScript(getAssemblyScriptName(featureID, os, ws, arch, featureLocation), new String[]{"main"}, //$NON-NLS-1$
					properties, new SubProgressMonitor(monitor, 2));
		} finally {
			monitor.done();
		}
	}

	
	private String getBuildScriptName(String featureLocation) {
		return featureLocation + Path.SEPARATOR + "build.xml"; //$NON-NLS-1$
	}
	
	private String[] getBuildExecutionTargets() {
		if (fExportSource && fExportType != EXPORT_AS_UPDATE_JARS)
			return new String[] {"build.jars", "build.sources", "gather.logs"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return new String[] {"build.jars", "gather.logs"}; //$NON-NLS-1$ //$NON-NLS-2$
	}

	private String getDevProperties() {
		if (fDevProperties == null) {
			fDevProperties = ClasspathHelper.getDevEntriesProperties(fBuildTempLocation + "/dev.properties", false); //$NON-NLS-1$
		}
		return fDevProperties;
	}
	
	private void makeScript(String featureID, String versionId, String os, String ws, String arch, String featureLocation) throws CoreException {
		BuildScriptGenerator generator = new AJBuildScriptGenerator();
		generator.setBuildingOSGi(PDECore.getDefault().getModelManager().isOSGiRuntime());
		generator.setChildren(true);
		generator.setWorkingDirectory(featureLocation);
		generator.setDevEntries(getDevProperties());
		generator.setElements(new String[] {"feature@" + featureID + (versionId == null ? "" : ":" + versionId)}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		generator.setPluginPath(getPaths());
		String format;
		if (fExportType == EXPORT_AS_ZIP)
			format = Platform.getOS().equals("macosx") ? "tarGz" : "antZip"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		else
			format = "folder"; //$NON-NLS-1$
		BuildScriptGenerator.setOutputFormat(format);
		BuildScriptGenerator.setForceUpdateJar(fExportType == EXPORT_AS_UPDATE_JARS);
		BuildScriptGenerator.setEmbeddedSource(fExportSource && fExportType != EXPORT_AS_UPDATE_JARS);
		BuildScriptGenerator.setConfigInfo(os + "," + ws + "," + arch); //$NON-NLS-1$ //$NON-NLS-2$
		generator.generate();	
	}
	
}
