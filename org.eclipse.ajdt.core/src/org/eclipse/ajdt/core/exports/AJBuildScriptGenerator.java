/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/
package org.eclipse.ajdt.core.exports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.build.AssembleScriptGenerator;
import org.eclipse.pde.internal.build.AssemblyInformation;
import org.eclipse.pde.internal.build.BuildScriptGenerator;
import org.eclipse.pde.internal.build.Config;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.build.IXMLConstants;
import org.eclipse.pde.internal.build.Messages;
import org.eclipse.pde.internal.build.SourceFeatureInformation;
import org.eclipse.pde.internal.build.Utils;
import org.eclipse.pde.internal.build.builder.DevClassPathHelper;
import org.eclipse.pde.internal.build.builder.FeatureBuildScriptGenerator;
import org.eclipse.pde.internal.build.packager.PackageScriptGenerator;
import org.eclipse.pde.internal.build.site.BuildTimeSiteFactory;

/**
 * Mostly copied from BuildScriptGenerator.
 * Enables AspectJ plugins to be correctly exported. Changes marked with // AspectJ Change
 */
public class AJBuildScriptGenerator extends BuildScriptGenerator {

	// AspectJ Change Begin - aspectpath and inpath support
	protected List aspectpath;
	protected List inpath;
	// AspectJ Change end
	
	/**
	 * Indicates whether the assemble script should contain the archive
	 * generation statement.
	 */
	protected boolean generateArchive = true;
	/**
	 * Indicates whether scripts for a feature's children should be generated.
	 */
	protected boolean children = true;

	/**
	 * Source elements for script generation.
	 */
	protected String[] elements;

	/**
	 * Additional dev entries for the compile classpath.
	 */
	protected DevClassPathHelper devEntries;

	protected boolean recursiveGeneration = true;
	protected boolean generateBuildScript = true;
	protected boolean includePlatformIndependent = true;
	protected boolean signJars = false;
	protected boolean generateJnlp = false;
	private String product;
	//Map configuration with the expected output format: key: Config, value: string
	private HashMap archivesFormat;

	/**
	 * flag indicating if the assemble script should be generated
	 */
	private boolean generateAssembleScript = true;

	/** flag indicating if missing properties file should be logged */
	private boolean ignoreMissingPropertiesFile = true;

	private static final String PROPERTY_ARCHIVESFORMAT = "archivesFormat"; //$NON-NLS-1$

	/**
	 * 
	 * @throws CoreException
	 */
	public void generate() throws CoreException {
		List plugins = new ArrayList(5);
		List features = new ArrayList(5);
		sortElements(features, plugins);

		// It is not required to filter in the two first generateModels, since
		// it is only for the building of a single plugin
		generateModels(plugins);
		generateFeatures(features);
		flushState();
	}

	/**
	 * Separate elements by kind.
	 */
	protected void sortElements(List features, List plugins) {
		for (int i = 0; i < elements.length; i++) {
			int index = elements[i].indexOf('@');
			String type = elements[i].substring(0, index);
			String element = elements[i].substring(index + 1);
			if (type.equals("plugin") || type.equals("fragment")) //$NON-NLS-1$ //$NON-NLS-2$
				plugins.add(element);
			else if (type.equals("feature")) //$NON-NLS-1$
				features.add(element);
		}
	}

	/**
	 * 
	 * @param models
	 * @throws CoreException
	 */
	protected void generateModels(List models) throws CoreException {
		AJModelBuildScriptGenerator generator = null;
		try {
			for (Iterator iterator = models.iterator(); iterator.hasNext();) {
				// AspectJ Change begin
				generator = new AJModelBuildScriptGenerator();
				generator.setAspectpath(aspectpath);
				generator.setInpath(inpath);
				// AspectJ Change end
				generator.setReportResolutionErrors(reportResolutionErrors);
				generator.setIgnoreMissingPropertiesFile(ignoreMissingPropertiesFile);
				//Filtering is not required here, since we are only generating the
				// build for a plugin or a fragment
				String model = (String) iterator.next();
				generator.setModelId(model);
				generator.setSignJars(signJars);
				generator.generate();
			}
		} finally {
			if (generator != null)
				generator.getSite(false).getRegistry().cleanupOriginalState();
		}
	}

	private String[] getNameAndVersion(String id) {
		int versionPosition = id.indexOf(":"); //$NON-NLS-1$
		String[] result = new String[2];
		if (versionPosition != -1) {
			result[1] = id.substring(versionPosition + 1);
			result[0] = id.substring(0, versionPosition);
		} else
			result[0] = id;
		return result;
	}

	protected void generateFeatures(List features) throws CoreException {
		AssemblyInformation assemblageInformation = null;
		assemblageInformation = new AssemblyInformation();

		FeatureBuildScriptGenerator generator = null;
		try {
			for (Iterator i = features.iterator(); i.hasNext();) {
				String[] featureInfo = getNameAndVersion((String) i.next());
//				 AspectJ Change
				generator = new AJFeatureBuildScriptGenerator(featureInfo[0], featureInfo[1], assemblageInformation);
				generator.setGenerateIncludedFeatures(this.recursiveGeneration);
				generator.setAnalyseChildren(this.children);
				generator.setSourceFeatureGeneration(false);
				generator.setBinaryFeatureGeneration(true);
				generator.setScriptGeneration(generateBuildScript);
				generator.setPluginPath(pluginPath);
				generator.setBuildSiteFactory(null);
				generator.setDevEntries(devEntries);
				generator.setSourceToGather(new SourceFeatureInformation());//
				generator.setCompiledElements(generator.getCompiledElements());
				generator.setBuildingOSGi(isBuildingOSGi());
				generator.includePlatformIndependent(includePlatformIndependent);
				generator.setReportResolutionErrors(reportResolutionErrors);
				generator.setIgnoreMissingPropertiesFile(ignoreMissingPropertiesFile);
				generator.setSignJars(signJars);
				generator.setGenerateJnlp(generateJnlp);
				generator.generate();
			}

			if (generator != null && generateAssembleScript == true) {
				String[] featureInfo = null;
				if (features.size() == 1)
					featureInfo = getNameAndVersion((String) features.get(0));
				else
					featureInfo = new String[] {"all"}; //$NON-NLS-1$

//				 AspectJ Change
				generateAssembleScripts(assemblageInformation, featureInfo,  ((AJFeatureBuildScriptGenerator)generator).getSiteFactory());

				if (features.size() == 1)
					featureInfo = getNameAndVersion((String) features.get(0));
				else
					featureInfo = new String[] {""}; //$NON-NLS-1$

//				 AspectJ Change
				generatePackageScripts(assemblageInformation, featureInfo,  ((AJFeatureBuildScriptGenerator)generator).getSiteFactory());
			}
		} finally {
			if (generator != null)
				generator.getSite(false).getRegistry().cleanupOriginalState();
		}
	}

	protected void generatePackageScripts(AssemblyInformation assemblageInformation, String[] featureInfo, BuildTimeSiteFactory factory) throws CoreException {
		PackageScriptGenerator assembler = null;
		assembler = new PackageScriptGenerator(workingDirectory, assemblageInformation, featureInfo[0]);
		assembler.setSignJars(signJars);
		assembler.setGenerateJnlp(generateJnlp);
		assembler.setArchivesFormat(getArchivesFormat());
		assembler.setProduct(product);
		assembler.setBuildSiteFactory(factory);
		assembler.generate();
	}

	private void generateAssembleScripts(AssemblyInformation assemblageInformation, String[] featureInfo, BuildTimeSiteFactory factory) throws CoreException {
		AssembleScriptGenerator assembler = new AssembleScriptGenerator(workingDirectory, assemblageInformation, featureInfo[0]);
		assembler.setSignJars(signJars);
		assembler.setGenerateJnlp(generateJnlp);
		assembler.setArchivesFormat(getArchivesFormat());
		assembler.setProduct(product);
		assembler.setBuildSiteFactory(factory);
		assembler.generate();
	}

	public void setGenerateArchive(boolean generateArchive) {
		this.generateArchive = generateArchive;
//		 AspectJ Change
		super.setGenerateArchive(generateArchive);
	}

	/**
	 * 
	 * @param children
	 */
	public void setChildren(boolean children) {
		this.children = children;
//		 AspectJ Change
		super.setChildren(children);
	}

	/**
	 * 
	 * @param devEntries
	 */
	public void setDevEntries(String devEntries) {
		if (devEntries != null)
			this.devEntries = new DevClassPathHelper(devEntries);
//		 AspectJ Change
		super.setDevEntries(devEntries);
	}

	/**
	 * 
	 * @param elements
	 */
	public void setElements(String[] elements) {
		this.elements = elements;
//		 AspectJ Change
		super.setElements(elements);
	}

	public void setPluginPath(String[] pluginPath) {
		this.pluginPath = pluginPath;
//		 AspectJ Change
		super.setPluginPath(pluginPath);
	}

	/**
	 * Sets the recursiveGeneration.
	 * 
	 * @param recursiveGeneration
	 *            The recursiveGeneration to set
	 */
	public void setRecursiveGeneration(boolean recursiveGeneration) {
		this.recursiveGeneration = recursiveGeneration;
		// AspectJ Change
		super.setRecursiveGeneration(recursiveGeneration);
	}

	/**
	 * @param generateAssembleScript
	 *            The generateAssembleScript to set.
	 */
	public void setGenerateAssembleScript(boolean generateAssembleScript) {
		this.generateAssembleScript = generateAssembleScript;
//		 AspectJ Change
		super.setGenerateAssembleScript(generateAssembleScript);
	}

	/**
	 * @param value The reportResolutionErrors to set.
	 */
	public void setReportResolutionErrors(boolean value) {
		this.reportResolutionErrors = value;
//		 AspectJ Change
		super.setReportResolutionErrors(value);
	}

	/**
	 * @param value The ignoreMissingPropertiesFile to set.
	 */
	public void setIgnoreMissingPropertiesFile(boolean value) {
		ignoreMissingPropertiesFile = value;
//		 AspectJ Change
		super.setIgnoreMissingPropertiesFile(value);
	}

	public void setProduct(String value) {
		product = value;
//		 AspectJ Change
		super.setProduct(value);
	}

	public void setSignJars(boolean value) {
		signJars = value;
//		 AspectJ Change
		super.setSignJars(value);
	}

	public void setGenerateJnlp(boolean value) {
		generateJnlp = value;
//		 AspectJ Change
		super.setGenerateJnlp(value);
	}

	private class ArchiveTable extends HashMap {
		private static final long serialVersionUID = -3063402400461435816L;
		public ArchiveTable(int size) {
			super(size);
		}
		public  Object get(Object arg0) {
			Object result = super.get(arg0);
			if (result == null)
				result = IXMLConstants.FORMAT_ANTZIP;
			return result;
		}
	}
	public void setArchivesFormat(String archivesFormatAsString) throws CoreException {
		if (Utils.getPropertyFormat(PROPERTY_ARCHIVESFORMAT).equalsIgnoreCase(archivesFormatAsString)) {
			archivesFormat = new ArchiveTable(0);
			return;
		}

		archivesFormat = new ArchiveTable(getConfigInfos().size());
		String[] configs = Utils.getArrayFromStringWithBlank(archivesFormatAsString, "&"); //$NON-NLS-1$
		for (int i = 0; i < configs.length; i++) {
			String[] configElements = Utils.getArrayFromStringWithBlank(configs[i], ","); //$NON-NLS-1$
			if (configElements.length != 3) {
				IStatus error = new Status(IStatus.ERROR, IPDEBuildConstants.PI_PDEBUILD, IPDEBuildConstants.EXCEPTION_CONFIG_FORMAT, NLS.bind(Messages.error_configWrongFormat, configs[i]), null);
				throw new CoreException(error);
			}
			String[] archAndFormat = Utils.getArrayFromStringWithBlank(configElements[2], "-"); //$NON-NLS-1$

			Config aConfig = new Config(configElements[0], configElements[1], archAndFormat[0]);
			if (getConfigInfos().contains(aConfig)) {
				archivesFormat.put(aConfig, archAndFormat[1]);
			}
		}
	}

	protected HashMap getArchivesFormat() {
		if (archivesFormat == null) {
			try {
				//If not set, pass in the empty property to trigger the default value to be loaded
				setArchivesFormat(Utils.getPropertyFormat(PROPERTY_ARCHIVESFORMAT));
			} catch (CoreException e) {
				//ignore
			}
		}
		return archivesFormat;
	}

	public void includePlatformIndependent(boolean b) {
		includePlatformIndependent = b;
	}
	
	// AspectJ Change Begin - aspectpath and inpath support
	public void setAspectpath(List aspectpath) {
		this.aspectpath = aspectpath;
	}
	public void setInpath(List inpath) {
		this.inpath = inpath;
	}
	// AspectJ Change End
}
