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
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.ajdt.internal.exports.AJBuildScriptGenerator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.build.AbstractScriptGenerator;
import org.eclipse.pde.internal.build.BuildScriptGenerator;
import org.eclipse.pde.internal.build.IXMLConstants;
import org.eclipse.pde.internal.core.ClasspathHelper;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.TargetPlatform;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.wizards.exports.PluginExportJob;

/**
 * Mostly copied from PluginExportJob and FeatureExportJob.  
 * Enables AspectJ plugins to be correctly exported
 */
public class AJPluginExportJob extends PluginExportJob {

	private String fFeatureLocation;
	private String[] fSigningInfo;
	private String fDevProperties;
	private String[] fJnlpInfo = null;
	
	/**
	 * 
	 * @param toDirectory
	 * @param useJarFormat
	 * @param exportSource
	 * @param destination
	 * @param zipFileName
	 * @param items
	 */
	public AJPluginExportJob(
			boolean toDirectory,
			boolean useJarFormat,
			boolean exportSource,
			String destination,
			String zipFileName,
			Object[] items) {
			this(toDirectory, useJarFormat, exportSource, destination, zipFileName, items, null);
		}

	/**
	 * 
	 * @param toDirectory
	 * @param useJarFormat
	 * @param exportSource
	 * @param destination
	 * @param zipFileName
	 * @param items
	 * @param signingInfo
	 */
	public AJPluginExportJob(
			boolean toDirectory,
			boolean useJarFormat,
			boolean exportSource,
			String destination,
			String zipFileName,
			Object[] items,
			String[] signingInfo) {
			super(toDirectory, useJarFormat, exportSource, destination, zipFileName, items, signingInfo);
			this.fSigningInfo = signingInfo;
		}

	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.wizards.exports.FeatureExportJob#doExports(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doExports(IProgressMonitor monitor)
			throws InvocationTargetException, CoreException {
		try {
            monitor.beginTask("", 10);
			// create a feature to contain all plug-ins
			String featureID = "org.eclipse.pde.container.feature"; //$NON-NLS-1$
			fFeatureLocation = fBuildTempLocation + File.separator + featureID;
			createFeature(featureID, fFeatureLocation);
			createBuildPropertiesFile(fFeatureLocation);
			if (fUseJarFormat)
				createPostProcessingFile(new File(fFeatureLocation, PLUGIN_POST_PROCESSING));
			doExport(featureID, null, fFeatureLocation, TargetPlatform.getOS(), TargetPlatform.getWS(), TargetPlatform.getOSArch(), 
                    new SubProgressMonitor(monitor, 7));
		} catch (IOException e) {
		} finally {
			for (int i = 0; i < fItems.length; i++) {
				if (fItems[i] instanceof IPluginModelBase)
					deleteBuildFiles((IPluginModelBase)fItems[i]);
			}
			cleanup(new SubProgressMonitor(monitor, 3));
			monitor.done();
		}
	}

	
	private void makeScript(String featureID, String versionId, String os, String ws, String arch, String featureLocation) throws CoreException {
		BuildScriptGenerator generator = new AJBuildScriptGenerator();
		generator.setBuildingOSGi(PDECore.getDefault().getModelManager().isOSGiRuntime());
		generator.setChildren(true);
		generator.setWorkingDirectory(featureLocation);
		generator.setDevEntries(getDevProperties());
		generator.setElements(new String[] {"feature@" + featureID + (versionId == null ? "" : ":" + versionId)}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		generator.setPluginPath(getPaths());
		generator.setReportResolutionErrors(false);
		generator.setIgnoreMissingPropertiesFile(true);
		generator.setSignJars(fSigningInfo != null);
		generator.setGenerateJnlp(fJnlpInfo != null);
		String config = os + ',' + ws + ',' + arch;
		AbstractScriptGenerator.setConfigInfo(config);  //This needs to be set before we set the format
		String format;
		if (fExportToDirectory)
			format = config + '-' + IXMLConstants.FORMAT_FOLDER;
		else
			format = config + '-' + IXMLConstants.FORMAT_ANTZIP;
		generator.setArchivesFormat(format);
		AbstractScriptGenerator.setForceUpdateJar(false);
		AbstractScriptGenerator.setEmbeddedSource(fExportSource);
		generator.generate();
	}

	
	protected void doExport(String featureID, String version, String featureLocation, String os, String ws, String arch, IProgressMonitor monitor) throws CoreException, InvocationTargetException {
		monitor.beginTask("", 5); //$NON-NLS-1$
		monitor.setTaskName(PDEPlugin.getResourceString("FeatureExportJob.taskName")); //$NON-NLS-1$
		try {
			HashMap properties = createAntBuildProperties(os, ws, arch);
			makeScript(featureID, version, os, ws, arch, featureLocation);
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

	
	/* (non-Javadoc)
	 * @see org.eclipse.pde.internal.ui.wizards.exports.FeatureExportJob#getPaths()
	 */
	protected String[] getPaths() throws CoreException {
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

	private String getBuildScriptName(String featureLocation) {
		return fFeatureLocation + IPath.SEPARATOR + "build.xml"; //$NON-NLS-1$
	}

	private String[] getBuildExecutionTargets() {
		if (fExportSource)
			return new String[] {"build.jars", "build.sources"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return new String[] {"build.jars"}; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private String getDevProperties() {
		if (fDevProperties == null) {
			fDevProperties = ClasspathHelper.getDevEntriesProperties(fBuildTempLocation + "/dev.properties", false); //$NON-NLS-1$
		}
		return fDevProperties;
	}
}
