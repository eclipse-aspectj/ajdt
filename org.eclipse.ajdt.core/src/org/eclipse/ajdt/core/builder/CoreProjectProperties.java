/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.ajde.Ajde;
import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.core.AJLog;
import org.eclipse.ajdt.internal.core.CoreUtils;
import org.eclipse.ajdt.internal.core.builder.BuildClasspathResolver;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Core version of project properties - subclassed by UI version
 *  
 */
public class CoreProjectProperties implements IProjectProperties {

	/**
	 * Created in getClasspath(), should be flushed at end of build
	 */
	private String cachedClasspath = null;

	private Map filesKnownMap = new HashMap();

	/**
	 * The name of the current project
	 */
	public String getProjectName() {
		return AspectJPlugin.getDefault().getCurrentProject().getName();
	}

	/*
	 * @see ProjectPropertiesAdapter#getClassToExecute()
	 */
	public String getClassToExecute() {
		System.err
				.println("*** AJ Plugin: ProjectProperties.getClassToExecute NOT IMPLEMENTED ***");
		return null;
	}

	/**
	 * The home directory of the current project
	 */
	public String getRootProjectDir() {
		IProject project = AspectJPlugin.getDefault().getCurrentProject();
		return project.getLocation().toOSString();
	}

	public List getBuildConfigFiles() {
		return null;
	}

	public String getDefaultBuildConfigFile() {
		String defaultLstFile = AspectJPlugin
				.getBuildConfigurationFile(AspectJPlugin.getDefault()
						.getCurrentProject());
		return defaultLstFile;
	}

	public String getLastActiveBuildConfigFile() {

		String currentLstFile = AspectJPlugin
				.getBuildConfigurationFile(AspectJPlugin.getDefault()
						.getCurrentProject());
		// System.err.println("AC_temp_debug:
		// ProjectProperties.getLastActiveBuildConfigFile(): Returning
		// "+currentLstFile);
		return currentLstFile;
	}

	public List getProjectSourceFiles() {
		// TODO Use build configs when available in core plugin
		// include all source files for now
		IProject project = AspectJPlugin.getDefault().getCurrentProject();
		List sourceFiles = new ArrayList();
		try {
			IJavaProject jp = JavaCore.create(project);
			IClasspathEntry[] cpes = jp.getRawClasspath();
			for (int i = 0; i < cpes.length; i++) {
				if (cpes[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath path = cpes[i].getPath();
					IResource res = project.findMember(path
							.removeFirstSegments(1));
					if ((res != null) && (res.getType() == IResource.FOLDER)) {
						List l = allFiles((IFolder) res);
						sourceFiles.addAll(l);
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return sourceFiles;
	}

	public void setProjectSourceFileListKnown(IProject project, boolean known) {
		filesKnownMap.put(project, new Boolean(known));
	}

	public boolean isProjectSourceFileListKnown(IProject project) {
		Boolean known = (Boolean) filesKnownMap.get(project);
		if (known == null) {
			return false;
		}
		return known.booleanValue();
	}

	//return a list of all file resources in the given folder, including all
	// sub-folders
	// copied from BuildProperties in UI plugin
	private List allFiles(IContainer folder) {
		final List contents = new ArrayList();
		try {
			folder.accept(new IResourceVisitor() {
				public boolean visit(IResource res) {
					if (res.getType() == IResource.FILE
							&& CoreUtils.ASPECTJ_SOURCE_FILTER.accept(res
									.getName())) {
						contents.add(res.getLocation().toFile());
					}
					return true;
				}
			});
		} catch (CoreException e) {
		}
		return contents;
	}

	/*
	 * @see ProjectPropertiesAdapter#getProjectSourcePath()
	 */
	public String getProjectSourcePath() {
		IProject p = AspectJPlugin.getDefault().getCurrentProject();
		// todo - how to tell if the root of the project source tree??
		// is it a getPersistentProperty( )??
		return p.getLocation().toOSString();
	}

	/**
	 * get the classpath to use for compiling the current project.
	 */
	public String getClasspath() {
		if (cachedClasspath != null)
			return cachedClasspath;
		IProject proj = AspectJPlugin.getDefault().getCurrentProject();
		IJavaProject jp = JavaCore.create(proj);
		// bug 73035: use this build classpath resolver which is a direct
		// copy from JDT, so the classpath environment is much closer between
		// AspectJ and Java projects.
		cachedClasspath = new BuildClasspathResolver().getClasspath(
				AspectJPlugin.getWorkspace().getRoot(), jp);
		return cachedClasspath;
	}

	public void flushClasspathCache() {
		cachedClasspath = null;
	}

	/**
	 * Called to determine where the resultant class files should go when AJC is
	 * compiling code. We grab the location from the current project and convert
	 * it to a string.
	 */
	public String getOutputPath() {
		IProject currProject = AspectJPlugin.getDefault().getCurrentProject();
		IJavaProject jProject = JavaCore.create(currProject);
		IPath workspaceRelativeOutputPath;
		try {
			workspaceRelativeOutputPath = jProject.getOutputLocation();
		} catch (JavaModelException e) {
			return currProject.getLocation().toOSString();
		}
		if (workspaceRelativeOutputPath.segmentCount() == 1) { // project
			// root
			return jProject.getResource().getLocation().toOSString();
		} else {
			IFolder out = ResourcesPlugin.getWorkspace().getRoot().getFolder(
					workspaceRelativeOutputPath);
			return out.getLocation().toOSString();
		}
	}

	/*
	 * @see ProjectPropertiesAdapter#getBootClasspath()
	 */
	public String getBootClasspath() {
		return null;
	}

	public String getExecutionArgs() {
		return "";
	}

	/*
	 * @see ProjectPropertiesAdapter#getVmArgs()
	 */
	public String getVmArgs() {
		return null;
	}

	public Set getInJars() {
		return null;
	}

	public Map getSourcePathResources() {
		return null;
	}

	/**
	 * Get the output jar file for the compilation results. Return null to leave
	 * classfiles unjar'd in output directory From -outjar
	 */
	public String getOutJar() {
		IProject thisProject = AspectJPlugin.getDefault().getCurrentProject();
		String outputJar = AspectJCorePreferences.getProjectOutJar(thisProject);

		// If outputJar does not start with a slash, we might need to prepend
		// the project work directory.
		if (outputJar.trim().length() > 0
				&& !(outputJar.startsWith("\\") || outputJar.startsWith("/"))) { //$NON-NLS-1$
			String trimmedName = outputJar.trim();
			boolean prependProject = true;

			// It might still be a fully qualified path if the 2nd char is a ':'
			// (i.e. its
			// a windows absolute path with a drive letter in it !)
			if (trimmedName.length() > 1) {
				if (trimmedName.charAt(1) == ':')
					prependProject = false;
			}

			if (prependProject) {
				// Its a relative path, it should be relative to the project.
				String projectBaseDirectory = thisProject.getLocation()
						.toOSString();
				outputJar = new String(projectBaseDirectory + File.separator
						+ outputJar.trim());
			}
		}

		return outputJar;
	}

	public Set getSourceRoots() {
		return null;
	}

	/**
	 * Get the set of aspect jar files to be used for the compilation. Returning
	 * null or an empty set disables this option. Set members should be of type
	 * java.io.File. From -aspectpath
	 */
	public Set getAspectPath() {
		IProject thisProject = AspectJPlugin.getDefault().getCurrentProject();
		String[] v = AspectJCorePreferences.getProjectAspectPath(thisProject);

		// need to expand any variables on the path
		String aspectpath = expandVariables(v[0], v[2]);

		// Ensure that every entry in the list is a fully qualified one.
		aspectpath = fullyQualifyPathEntries(aspectpath);

		if (aspectpath.length() == 0)
			return null;

		return mapStringToSet(aspectpath, false);
	}

	public Set getInpath() {
		IProject thisProject = AspectJPlugin.getDefault().getCurrentProject();
		String[] v = AspectJCorePreferences.getProjectInPath(thisProject);

		// need to expand any variables on the path
		String inpath = expandVariables(v[0], v[2]);

		// Ensure that every entry in the list is a fully qualified one.
		inpath = fullyQualifyPathEntries(inpath);

		if (inpath.length() == 0)
			return null;

		return mapStringToSet(inpath, false);
	}

	private String expandVariables(String path, String eKinds) {
		StringBuffer resultBuffer = new StringBuffer();
		StringTokenizer strTok = new StringTokenizer(path, File.pathSeparator);
		StringTokenizer strTok2 = new StringTokenizer(eKinds,
				File.pathSeparator);
		while (strTok.hasMoreTokens()) {
			String current = strTok.nextToken();
			int entryKind = Integer.parseInt(strTok2.nextToken());
			if (entryKind == IClasspathEntry.CPE_VARIABLE) {
				int slashPos = current.indexOf(
						AspectJPlugin.NON_OS_SPECIFIC_SEPARATOR, 0);
				if (slashPos != -1) {
					String exp = JavaCore.getClasspathVariable(
							current.substring(0, slashPos)).toOSString();
					resultBuffer.append(exp);
					resultBuffer.append(current.substring(slashPos));
				} else {
					String exp = JavaCore.getClasspathVariable(current)
							.toOSString();
					resultBuffer.append(exp);
				}
			} else {
				resultBuffer.append(current);
			}
			resultBuffer.append(File.pathSeparator);
		}
		return resultBuffer.toString();
	}

	/**
	 * @param inputPath
	 * @return
	 */
	private String fullyQualifyPathEntries(String inputPath) {
		StringBuffer resultBuffer = new StringBuffer();
		StringTokenizer strTok = new StringTokenizer(inputPath,
				File.pathSeparator);
		while (strTok.hasMoreTokens()) {
			String current = strTok.nextToken();
			File f = new File(current);
			if (f.exists() && f.isAbsolute()) {
				// entry not relative to workspace (it's fully qualifed)
				resultBuffer.append(current);
			} else {
				//if
				// (current.startsWith(AspectJUIPlugin.NON_OS_SPECIFIC_SEPARATOR))
				// {
				// Try to resolve path relative to the workspace. Need to
				// replace part of the path string with a fully qualified
				// equivalent.
				String projectName = null;
				int slashPos = current.indexOf(
						AspectJPlugin.NON_OS_SPECIFIC_SEPARATOR, 1);
				if (slashPos != -1) {
					projectName = current.substring(1, slashPos);
				} else {
					projectName = current.substring(1);
				}

				IProject project = AspectJPlugin.getWorkspace().getRoot()
						.getProject(projectName);

				if (project != null && project.getLocation() != null) {
					String projectPath = project.getLocation().toString();

					if (slashPos != -1) {
						resultBuffer.append(projectPath
								+ AspectJPlugin.NON_OS_SPECIFIC_SEPARATOR
								+ current.substring(slashPos + 1));
					} else {
						resultBuffer.append(projectPath);
					}
				}// end if named project found
				else {
					// Inform user that the supplied path contains an
					// entry that does not now exist.

					// TODO : Open a message dialog warning user that the
					// path entry does not exist. Tricky at the moment as
					// an AJ project build calls getInPath() (and hence this
					// method) more than once resulting in more than one
					// pop-ups.
					// AspectJPlugin.getDefault().getErrorHandler().handleWarning(
					//		AspectJPlugin.getFormattedResourceString(
					//				"Path.entryNotFound.warningMessage",
					//				current));
					AJLog.log("AspectJ path entry " + current
							+ " does not exist. Ignoring.");
				}// end else entry not found in workspace
			}// end if entry is relative to workspace
			resultBuffer.append(File.pathSeparator);
		}// end while more tokens to process

		String result = resultBuffer.toString();
		if (result.endsWith(File.pathSeparator)) {
			result = result.substring(0, result.length() - 1);
		}

		return result;
	}

	/**
	 * Utility method for converting a semicolon separated list of files stored
	 * in a string into a Set of java.io.File objects.
	 *  
	 */
	private Set mapStringToSet(String input, boolean validateFiles) {
		if (input.length() == 0)
			return null;
		String inputCopy = input;

		StringBuffer invalidEntries = new StringBuffer();

		// For relative paths (they don't start with a File.separator
		// or a drive letter on windows) - we prepend the projectBaseDirectory
		String projectBaseDirectory = AspectJPlugin.getDefault()
				.getCurrentProject().getLocation().toOSString();

		Set fileSet = new HashSet();
		while (inputCopy.indexOf(java.io.File.pathSeparator) != -1) { //ASCFIXME
																	  // - Bit
																	  // too
																	  // platform
																	  // specific!
			int idx = inputCopy.indexOf(java.io.File.pathSeparator);
			String path = inputCopy.substring(0, idx);

			java.io.File f = new java.io.File(path);
			if (!f.isAbsolute())
				f = new File(projectBaseDirectory + java.io.File.separator
						+ path);
			if (validateFiles && !f.exists()) {
				invalidEntries.append(f + "\n");
			} else {
				fileSet.add(f);
			}
			inputCopy = inputCopy.substring(idx + 1);

		}
		// Process the final element
		if (inputCopy.length() != 0) {
			java.io.File f = new java.io.File(inputCopy);
			if (!f.isAbsolute())
				f = new File(projectBaseDirectory + java.io.File.separator
						+ inputCopy);
			if (validateFiles && !f.exists()) {
				invalidEntries.append(f + "\n");
			} else {
				fileSet.add(f);
			}

		}

		//ASCFIXME - Need to NLSify this string...
		if (validateFiles && invalidEntries.length() != 0) {
			Ajde.getDefault().getErrorHandler().handleWarning(
					"The following jar files do not exist and are being ignored:\n"
							+ invalidEntries.toString());
		}
		return fileSet;
	}

}
