/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *     Ben Pryor - Bug 148288
 *     Andrew Eisenberg - adapted for AJDT 3.4
 *******************************************************************************/
package org.eclipse.ajdt.core.exports;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.util.NLS;
import org.eclipse.pde.internal.build.AbstractScriptGenerator;
import org.eclipse.pde.internal.build.AssembleScriptGenerator;
import org.eclipse.pde.internal.build.AssemblyInformation;
import org.eclipse.pde.internal.build.BuildScriptGenerator;
import org.eclipse.pde.internal.build.Config;
import org.eclipse.pde.internal.build.IPDEBuildConstants;
import org.eclipse.pde.internal.build.IXMLConstants;
import org.eclipse.pde.internal.build.Messages;
import org.eclipse.pde.internal.build.SourceFeatureInformation;
import org.eclipse.pde.internal.build.Utils;
import org.eclipse.pde.internal.build.builder.BuildDirector;
import org.eclipse.pde.internal.build.builder.CompilationScriptGenerator;
import org.eclipse.pde.internal.build.builder.DevClassPathHelper;
import org.eclipse.pde.internal.build.packager.PackageScriptGenerator;
import org.eclipse.pde.internal.build.site.BuildTimeFeature;
import org.eclipse.pde.internal.build.site.BuildTimeSiteFactory;
import org.eclipse.pde.internal.build.site.compatibility.Feature;
import org.osgi.framework.Version;

/*
 * Copied from BuildScriptGenerator.
 * Enables AspectJ plugins to be correctly exported. Changes marked with // AspectJ Change
 */
public class AJBuildScriptGenerator extends BuildScriptGenerator { // AspectJ Change
	/**
	 * Indicates whether the assemble script should contain the archive
	 * generation statement.
	 */
//	protected boolean generateArchive = true; // AspectJ Change
	/**
	 * Indicates whether scripts for a feature's children should be generated.
	 */
//	protected boolean children = true; // AspectJ Change

	/**
	 * Indicates whether the resulting archive will contain a group of all the configurations
	 */
//	protected boolean groupConfigs = false; // AspectJ Change

	/**
	 * Source elements for script generation.
	 */
//	protected String[] elements; // AspectJ Change

	/**
	 * Additional dev entries for the compile classpath.
	 */

	// AspectJ Change
	/*
  	protected DevClassPathHelper devEntries;
 

	protected boolean recursiveGeneration = true;
	protected boolean generateBuildScript = true;
	protected boolean includePlatformIndependent = true;
	protected boolean signJars = false;
	protected boolean generateJnlp = false;
	protected boolean generateFeatureVersionSuffix = false;
	protected String product;
	
	*/
	
	//Map configuration with the expected output format: key: Config, value: string
	private HashMap archivesFormat;

	private String archivesFormatAsString;

	/**
	 * flag indicating if the assemble script should be generated
	 */
	private boolean generateAssembleScript = true;

	/** flag indicating if missing properties file should be logged */
	private boolean ignoreMissingPropertiesFile = true;

	/** flag indicating if we should generate the plugin & feature versions lists */
	protected boolean generateVersionsList = false;

	private Properties antProperties = null;
	private BundleDescription[] bundlesToBuild;

	private static final String PROPERTY_ARCHIVESFORMAT = "archivesFormat"; //$NON-NLS-1$

	// AspectJ Change Begin - aspectpath and inpath support
	protected List aspectpath;
	protected List inpath;
	// AspectJ Change end

	/**
	 * 
	 * @throws CoreException
	 */
	public void generate() throws CoreException {
		if (archivesFormatAsString != null) {
			realSetArchivesFormat(archivesFormatAsString);
			archivesFormatAsString = null;
		}

		List plugins = new ArrayList(5);
		List features = new ArrayList(5);
		try {
			AbstractScriptGenerator.setStaticAntProperties(antProperties);

			sortElements(features, plugins);
			pluginsForFilterRoots = plugins;
			featuresForFilterRoots = features;
			getSite(true); //This forces the creation of the siteFactory which contains all the parameters necessary to initialize.
			//TODO To avoid this. What would be necessary is use the BuildTimeSiteFactory to store the values that are stored in the AbstractScriptGenerator and to pass the parameters to a new BuildTimeSiteFacotry when created.
			//More over this would allow us to remove some of the setters when creating a new featurebuildscriptgenerator.

			// It is not required to filter in the two first generateModels, since
			// it is only for the building of a single plugin
			generateModels(plugins);
			generateFeatures(features);
			flushState();
		} finally {
			AbstractScriptGenerator.setStaticAntProperties(null);
		}
	}

	/**
	 * Separate elements by kind.
	 */
	protected void sortElements(List features, List plugins) {
		if (elements == null)
			return;
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
		AJModelBuildScriptGenerator generator = null; // AspectJ Change
		try {
			for (Iterator iterator = models.iterator(); iterator.hasNext();) {
				generator = new AJModelBuildScriptGenerator(); // AspectJ Change
				generator.setAspectpath(aspectpath); // AspectJ Change
				generator.setInpath(inpath); // AspectJ Change
				generator.setReportResolutionErrors(reportResolutionErrors);
				generator.setIgnoreMissingPropertiesFile(ignoreMissingPropertiesFile);
				//Filtering is not required here, since we are only generating the build for a plugin or a fragment
				String[] modelInfo = getNameAndVersion((String) iterator.next());
				generator.setBuildSiteFactory(siteFactory);
				generator.setModelId(modelInfo[0], modelInfo[1]);

				generator.setPluginPath(pluginPath);
				generator.setDevEntries(devEntries);
				generator.setCompiledElements(generator.getCompiledElements());
				generator.setBuildingOSGi(isBuildingOSGi());
				generator.setSignJars(signJars);
				generator.generate();
			}
			if (bundlesToBuild != null)
				for (int i = 0; i < bundlesToBuild.length; i++) {
					generator = new AJModelBuildScriptGenerator(); // AspectJ Change
					generator.setAspectpath(aspectpath); // AspectJ Change
					generator.setInpath(inpath); // AspectJ Change
					generator.setReportResolutionErrors(reportResolutionErrors);
					generator.setIgnoreMissingPropertiesFile(ignoreMissingPropertiesFile);
					//Filtering is not required here, since we are only generating the build for a plugin or a fragment
					generator.setBuildSiteFactory(siteFactory);
					generator.setModel(bundlesToBuild[i]);

					generator.setPluginPath(pluginPath);
					generator.setDevEntries(devEntries);
					generator.setCompiledElements(generator.getCompiledElements());
					generator.setBuildingOSGi(isBuildingOSGi());
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
		BuildDirector generator = null;

		if (features.size() > 0) {
			assemblageInformation = new AssemblyInformation();

			generator = new AJBuildDirector(assemblageInformation);
			generator.setGenerateIncludedFeatures(this.recursiveGeneration);
			generator.setAnalyseChildren(this.children);
			generator.setBinaryFeatureGeneration(true);
			generator.setScriptGeneration(generateBuildScript);
			generator.setPluginPath(pluginPath);
			generator.setBuildSiteFactory(siteFactory);
			generator.setDevEntries(devEntries);
			generator.setSourceToGather(new SourceFeatureInformation());//
			generator.setCompiledElements(generator.getCompiledElements());
			generator.setBuildingOSGi(isBuildingOSGi());
			generator.includePlatformIndependent(includePlatformIndependent);
			generator.setReportResolutionErrors(reportResolutionErrors);
			generator.setIgnoreMissingPropertiesFile(ignoreMissingPropertiesFile);
			generator.setSignJars(signJars);
			generator.setGenerateJnlp(generateJnlp);
			generator.setGenerateVersionSuffix(generateFeatureVersionSuffix);
			generator.setProduct(product);
		}

		if (generator != null) {
			try {
                String[] featureInfo = null;
				for (Iterator i = features.iterator(); i.hasNext();) {
					featureInfo = getNameAndVersion((String) i.next());
					BuildTimeFeature feature = getSite(false).findFeature(featureInfo[0], featureInfo[1], true);
					generator.generate(feature);
				}
				
                if (shouldFlatten())
                    generateCompileScript(assemblageInformation, featureInfo);


				if (generateAssembleScript == true) {
					featureInfo = null;
					if (features.size() == 1)
						featureInfo = getNameAndVersion((String) features.get(0));
					else
						featureInfo = new String[] {"all"}; //$NON-NLS-1$

					generateAssembleScripts(assemblageInformation, featureInfo, ((AJBuildDirector) generator).getSiteFactory()); // AspectJ Change

					if (features.size() == 1)
						featureInfo = getNameAndVersion((String) features.get(0));
					else
						featureInfo = new String[] {""}; //$NON-NLS-1$

					generatePackageScripts(assemblageInformation, featureInfo, ((AJBuildDirector) generator).getSiteFactory()); // AspectJ Change
				}
				if (generateVersionsList)
					generateVersionsLists(assemblageInformation);
			} finally {
				getSite(false).getRegistry().cleanupOriginalState();
			}
		}
	}

	private boolean shouldFlatten() {
	    try {
            Field flattenField = BuildScriptGenerator.class.getDeclaredField("flatten");
            flattenField.setAccessible(true);
            return flattenField.getBoolean(this);
        } catch (Exception e) {
            return false;
        }
    }

    protected void generateVersionsLists(AssemblyInformation assemblageInformation) throws CoreException {
		if (assemblageInformation == null)
			return;
		List configs = getConfigInfos();
		Set features = new HashSet();
		Set plugins = new HashSet();
		Properties versions = new Properties();

		//For each configuration, save the version of all the features in a file 
		//and save the version of all the plug-ins in another file
		for (Iterator iter = configs.iterator(); iter.hasNext();) {
			Config config = (Config) iter.next();
			String configString = config.toStringReplacingAny("_", ANY_STRING); //$NON-NLS-1$

			//Features
			Collection list = assemblageInformation.getFeatures(config);
			versions.clear();
			features.addAll(list);
			String featureFile = DEFAULT_FEATURE_VERSION_FILENAME_PREFIX + '.' + configString + PROPERTIES_FILE_SUFFIX;
			readVersions(versions, featureFile);
			for (Iterator i = list.iterator(); i.hasNext();) {
				Feature feature = (Feature) i.next();
				recordVersion(feature.getId(), new Version(feature.getVersion()), versions);
			}
			saveVersions(versions, featureFile);

			//Plugins
			list = assemblageInformation.getPlugins(config);
			versions.clear();
			plugins.addAll(list);
			String pluginFile = DEFAULT_PLUGIN_VERSION_FILENAME_PREFIX + '.' + configString + PROPERTIES_FILE_SUFFIX;
			readVersions(versions, pluginFile);
			for (Iterator i = list.iterator(); i.hasNext();) {
				BundleDescription bundle = (BundleDescription) i.next();
				recordVersion(bundle.getSymbolicName(), bundle.getVersion(), versions);
			}
			saveVersions(versions, pluginFile);
		}

		//Create a file containing all the feature versions  
		versions.clear();
		String featureFile = DEFAULT_FEATURE_VERSION_FILENAME_PREFIX + PROPERTIES_FILE_SUFFIX;
		readVersions(versions, featureFile);
		for (Iterator i = features.iterator(); i.hasNext();) {
			Feature feature = (Feature) i.next();
			recordVersion(feature.getId(), new Version(feature.getVersion()), versions);
		}
		saveVersions(versions, featureFile);

		//Create a file containing all the plugin versions
		versions.clear();
		String pluginVersion = DEFAULT_PLUGIN_VERSION_FILENAME_PREFIX + PROPERTIES_FILE_SUFFIX;
		readVersions(versions, pluginVersion);
		for (Iterator i = plugins.iterator(); i.hasNext();) {
			BundleDescription bundle = (BundleDescription) i.next();
			recordVersion(bundle.getSymbolicName(), bundle.getVersion(), versions);
		}
		saveVersions(versions, pluginVersion);
	}

	protected void recordVersion(String name, Version version, Properties properties) {
		String versionString = version.toString();
		if (properties.containsKey(name)) {
			Version existing = new Version((String) properties.get(name));
			if (version.compareTo(existing) >= 0) {
				properties.put(name, versionString);
			}
		} else {
			properties.put(name, versionString);
		}
		String suffix = '_' + String.valueOf(version.getMajor()) + '.' + String.valueOf(version.getMinor()) + '.' + String.valueOf(version.getMicro());
		properties.put(name + suffix, versionString);
	}

	private String getFilePath(String fileName) {
		return workingDirectory + '/' + fileName;
	}

	protected void readVersions(Properties properties, String fileName) {
		String location = getFilePath(fileName);
		File f = new File(location);
		// check to prevent a failure during testing
		if (f.exists()) {
			try {
				InputStream is = new BufferedInputStream(new FileInputStream(f));
				try {
					properties.load(is);
				} finally {
					is.close();
				}
			} catch (IOException e) {
				//Ignore
			}
		}
	}

	protected void saveVersions(Properties properties, String fileName) throws CoreException {
		String location = getFilePath(fileName);
		try {
			OutputStream os = new BufferedOutputStream(new FileOutputStream(location));
			try {
				properties.store(os, null);
			} finally {
				os.close();
			}
		} catch (IOException e) {
			String message = NLS.bind(Messages.exception_writingFile, location);
			throw new CoreException(new Status(IStatus.ERROR, PI_PDEBUILD, EXCEPTION_WRITING_FILE, message, null));
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
		assembler.setGroupConfigs(groupConfigs);
		assembler.setVersionsList(generateVersionsList);
		assembler.generate();
	}

    private void generateAssembleScripts(AssemblyInformation assemblageInformation, String[] featureInfo, BuildTimeSiteFactory factory) throws CoreException {
        AssembleScriptGenerator assembler = new AssembleScriptGenerator(workingDirectory, assemblageInformation, featureInfo[0]);
        assembler.setSignJars(signJars);
        assembler.setGenerateJnlp(generateJnlp);
        assembler.setArchivesFormat(getArchivesFormat());
        assembler.setProduct(product);
        assembler.setProductQualifier(productQualifier);
        assembler.setBuildSiteFactory(factory);
        assembler.setGroupConfigs(groupConfigs);
        assembler.setVersionsList(generateVersionsList);
        assembler.setContextMetadataRepositories(contextMetadata);
        assembler.generate();
    }
	
    private void generateCompileScript(
            AssemblyInformation assemblageInformation, String[] featureInfo)
            throws CoreException {
        CompilationScriptGenerator generator = new CompilationScriptGenerator();
        generator.setBuildSiteFactory(siteFactory);
        generator.setWorkingDirectory(workingDirectory);
        generator.setAssemblyData(assemblageInformation);
        generator.setFeatureId(featureInfo[0]);
        generator.setParallel(parallel);
        generator.setThreadCount(threadCount);
        generator.setThreadsPerProcessor(threadsPerProcessor);
        generator.generate();
    }

	public void setGenerateArchive(boolean generateArchive) {
		this.generateArchive = generateArchive;
	}

	/**
	 * 
	 * @param children
	 */
	public void setChildren(boolean children) {
		this.children = children;
	}

	/**
	 * 
	 * @param devEntries
	 */
	public void setDevEntries(String devEntries) {
		if (devEntries != null)
			this.devEntries = new DevClassPathHelper(devEntries);
	}

	/**
	 * 
	 * @param elements
	 */
	public void setElements(String[] elements) {
		this.elements = elements;
	}

	/**
	 * Sets the recursiveGeneration.
	 * 
	 * @param recursiveGeneration
	 *            The recursiveGeneration to set
	 */
	public void setRecursiveGeneration(boolean recursiveGeneration) {
		this.recursiveGeneration = recursiveGeneration;
	}

	/**
	 * @param generateAssembleScript
	 *            The generateAssembleScript to set.
	 */
	public void setGenerateAssembleScript(boolean generateAssembleScript) {
		this.generateAssembleScript = generateAssembleScript;
	}

	/**
	 * Whether or not to generate plugin & feature versions lists
	 * @param generateVersionsList
	 */
	public void setGenerateVersionsList(boolean generateVersionsList) {
		this.generateVersionsList = generateVersionsList;
	}

	/**
	 * @param value The reportResolutionErrors to set.
	 */
	public void setReportResolutionErrors(boolean value) {
		this.reportResolutionErrors = value;
	}

	/**
	 * @param value The ignoreMissingPropertiesFile to set.
	 */
	public void setIgnoreMissingPropertiesFile(boolean value) {
		ignoreMissingPropertiesFile = value;
	}

	public void setProduct(String value) {
		product = value;
	}

	public void setSignJars(boolean value) {
		signJars = value;
	}

	public void setGenerateJnlp(boolean value) {
		generateJnlp = value;
	}

	public void setGenerateFeatureVersionSuffix(boolean value) {
		generateFeatureVersionSuffix = value;
	}

	private static class ArchiveTable extends HashMap {
		private static final long serialVersionUID = -3063402400461435816L;

		public ArchiveTable(int size) {
			super(size);
		}

		public Object get(Object arg0) {
			Object result = super.get(arg0);
			if (result == null)
				result = IXMLConstants.FORMAT_ANTZIP;
			return result;
		}
	}

	public void setArchivesFormat(String archivesFormatAsString) {
		this.archivesFormatAsString = archivesFormatAsString;
	}

	public void realSetArchivesFormat(String formatString) throws CoreException {
		if (Utils.getPropertyFormat(PROPERTY_ARCHIVESFORMAT).equalsIgnoreCase(formatString)) {
			archivesFormat = new ArchiveTable(0);
			return;
		}

		archivesFormat = new ArchiveTable(getConfigInfos().size() + 1);
		String[] configs = Utils.getArrayFromStringWithBlank(formatString, "&"); //$NON-NLS-1$
		for (int i = 0; i < configs.length; i++) {
			String[] configElements = Utils.getArrayFromStringWithBlank(configs[i], ","); //$NON-NLS-1$
			if (configElements.length != 3) {
				IStatus error = new Status(IStatus.ERROR, IPDEBuildConstants.PI_PDEBUILD, IPDEBuildConstants.EXCEPTION_CONFIG_FORMAT, NLS.bind(Messages.error_configWrongFormat, configs[i]), null);
				throw new CoreException(error);
			}
			String[] archAndFormat = Utils.getArrayFromStringWithBlank(configElements[2], "-"); //$NON-NLS-1$
			if (archAndFormat.length != 2) {
				String message = NLS.bind(Messages.invalid_archivesFormat, formatString);
				IStatus status = new Status(IStatus.ERROR, IPDEBuildConstants.PI_PDEBUILD, message);
				throw new CoreException(status);
			}

			Config aConfig = new Config(configElements[0], configElements[1], archAndFormat[0]);
			if (getConfigInfos().contains(aConfig) || (groupConfigs == true && configElements[0].equals("group"))) { //$NON-NLS-1$
				archivesFormat.put(aConfig, archAndFormat[1]);
			}
		}
	}

	protected HashMap getArchivesFormat() {
		if (archivesFormat == null) {
			try {
				//If not set, pass in the empty property to trigger the default value to be loaded
				realSetArchivesFormat(Utils.getPropertyFormat(PROPERTY_ARCHIVESFORMAT));
			} catch (CoreException e) {
				//ignore
			}
		}
		return archivesFormat;
	}

	public void includePlatformIndependent(boolean b) {
		includePlatformIndependent = b;
	}

	public void setGroupConfigs(boolean value) {
		groupConfigs = value;
	}

	public void setImmutableAntProperties(Properties properties) {
		antProperties = properties;
	}

	public void setBundles(BundleDescription[] bundles) {
		bundlesToBuild = bundles;
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
