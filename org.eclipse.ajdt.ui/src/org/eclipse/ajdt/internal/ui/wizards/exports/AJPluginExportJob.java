/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.ui.wizards.exports;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.ajdt.exports.AJBuildScriptGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.build.BuildScriptGenerator;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.TargetPlatform;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.build.FeatureExportInfo;
import org.eclipse.pde.internal.ui.build.FeatureExportJob;

/**
 * Mostly copied from PluginExportJob and FeatureExportJob.  
 * Enables AspectJ plugins to be correctly exported
 */
public class AJPluginExportJob extends FeatureExportJob {

	private String fFeatureLocation;
	
	private FeatureExportInfo info;
	
	/**
	 * 
	 * @param info
	 */
	public AJPluginExportJob(FeatureExportInfo info) {
		super(info);
		this.info = info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.wizards.exports.FeatureExportJob#doExports(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doExports(IProgressMonitor monitor)
			throws InvocationTargetException, CoreException {
		try {
            monitor.beginTask("", 10); //$NON-NLS-1$
			// create a feature to contain all plug-ins
			String featureID = "org.eclipse.pde.container.feature"; //$NON-NLS-1$
			fFeatureLocation = fBuildTempLocation + File.separator + featureID;
			String[] config = new String[] {TargetPlatform.getOS(), TargetPlatform.getWS(), TargetPlatform.getOSArch(), TargetPlatform.getNL() };
			createFeature(featureID, fFeatureLocation, config, false);
			createBuildPropertiesFile(fFeatureLocation);
			if (info.useJarFormat)
				createPostProcessingFile(new File(fFeatureLocation, PLUGIN_POST_PROCESSING));
			doExport(featureID, null, fFeatureLocation, TargetPlatform.getOS(), TargetPlatform.getWS(), TargetPlatform.getOSArch(), 
                    new SubProgressMonitor(monitor, 7));
		} catch (IOException e) {
		} finally {
			for (int i = 0; i < info.items.length; i++) {
				if (info.items[i] instanceof IPluginModelBase)
					deleteBuildFiles(info.items[i]);
			}
			cleanup(null, new SubProgressMonitor(monitor, 3));
			monitor.done();
		}
	}
	
// AspectJ Change Begin - override from FeatureExportJob	
	protected void doExport(String featureID, String version, String featureLocation, String os, String ws, String arch, IProgressMonitor monitor) throws CoreException, InvocationTargetException {
//		fHasErrors = false;
		monitor.beginTask("", 9); //$NON-NLS-1$
		monitor.setTaskName(PDEUIMessages.FeatureExportJob_taskName); //$NON-NLS-1$
		try {
			HashMap properties = createAntBuildProperties(os, ws, arch);
			BuildScriptGenerator generator = new AJBuildScriptGenerator();
			setupGenerator(generator, featureID, version, os, ws, arch, featureLocation);
			generator.generate();
			monitor.worked(1);
			runScript(getBuildScriptName(featureLocation), getBuildExecutionTargets(), properties, new SubProgressMonitor(monitor, 2));
			runScript(getAssemblyScriptName(featureID, os, ws, arch, featureLocation), new String[] {"main"}, //$NON-NLS-1$
					properties, new SubProgressMonitor(monitor, 2));
			runScript(getPackagerScriptName(featureID, os, ws, arch, featureLocation), null, properties, new SubProgressMonitor(monitor, 2));
			properties.put("destination.temp.folder", fBuildTempLocation + "/pde.logs"); //$NON-NLS-1$ //$NON-NLS-2$
			runScript(getBuildScriptName(featureLocation), new String[] {"gather.logs"}, properties, new SubProgressMonitor(monitor, 2)); //$NON-NLS-1$
		} finally {
			monitor.done();
		}
	}
//	 AspectJ Change End
	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.wizards.exports.FeatureExportJob#getPaths()
	 */
	protected String[] getPaths() {
		String[] paths =  super.getPaths();
		String[] all = new String[paths.length + 1];
		all[0] = fFeatureLocation + File.separator + "feature.xml"; //$NON-NLS-1$
		System.arraycopy(paths, 0, all, 1, paths.length);
		return all;
	}
	
	private void createBuildPropertiesFile(String featureLocation) {
		File file = new File(featureLocation);
		if (!file.exists() || !file.isDirectory())
			file.mkdirs();
		Properties prop = new Properties();
		prop.put("pde", "marker"); //$NON-NLS-1$ //$NON-NLS-2$
		save(new File(file, "build.properties"),prop, "Marker File");  //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void save(File file, Properties properties, String header) {
		try {
			FileOutputStream stream = new FileOutputStream(file);
			properties.store(stream, header); 
			stream.flush();
			stream.close();
		} catch (IOException e) {
			PDECore.logException(e);
		}
	}
// AspectJ Change Begin - copied from FeatureExportJob
	private String getBuildScriptName(String featureLocation) {
		return featureLocation + IPath.SEPARATOR + "build.xml"; //$NON-NLS-1$
	}

	private String[] getBuildExecutionTargets() {
		if (info.exportSource)
			return new String[] {"build.jars", "build.sources"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return new String[] {"build.jars"}; //$NON-NLS-1$ //$NON-NLS-2$
	}
// AspectJ Change End
	
}
