/**********************************************************************
Copyright (c) 2002, 2005 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
Ian McGrath - updated compiler option retrieving methods
Matt Chapman - reorganised for project properties (40446)
**********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;

public class BuildOptionsAdapter
	implements org.aspectj.ajde.BuildOptionsAdapter {

	/**
	 * Tries to get a project-specific nature options map if it exists.  
	 * If it is not found returns the JavaCore's map.
	 */
	public Map getJavaOptionsMap() {
		Map optionsMap = null;
			
		JavaProject project;
		try {
			project = (JavaProject)AspectJUIPlugin.getDefault().getCurrentProject().getNature(JavaCore.NATURE_ID);
			optionsMap = project.getOptions(true);
		} catch (CoreException e) {
		}
		
		if (optionsMap == null) {
			return JavaCore.getOptions();
		} else {
			return optionsMap;
		}
	}

	// Return formatted version of the current build options set
	public String toString() {
		StringBuffer formattedOptions = new StringBuffer();
		formattedOptions.append("Current Compiler options set:");
		formattedOptions.append(
			"[Incremental compilation=" + getIncrementalMode() + "]");
		formattedOptions.append(
			"[NonStandard options='" + getNonStandardOptions() + "']");
		return formattedOptions.toString();
	}

	/**
	 * @see BuildOptionsAdapter#getLenientSpecMode()
	 */
	public boolean getLenientSpecMode() {
		return false;
	}

	/**
	 * @see BuildOptionsAdapter#getNonStandardOptions()
	 */
	public String getNonStandardOptions() {
		IProject currentProject = AspectJUIPlugin.getDefault().getCurrentProject();
		String nonStandardOptions = AspectJPreferences.getCompilerOptions(currentProject);
		if (AspectJUIPlugin.DEBUG_BUILDER) {
			System.out.println(
				"BuildOptionsAdapter.getNonStandardOptions called, returning :"
					+ nonStandardOptions);
		}
		
		nonStandardOptions += AspectJPreferences.getLintOptions(currentProject);
		nonStandardOptions += AspectJPreferences.getAdvancedOptions(currentProject);
		if (AspectJUIPlugin.getDefault().getAjdtBuildOptionsAdapter().getShowWeaveMessages()) {
			nonStandardOptions += " -showWeaveInfo"; //$NON-NLS-1$
		}
		return nonStandardOptions;
	}

	/**
	 * @see BuildOptionsAdapter#getPortingMode()
	 */
	public boolean getPortingMode() {
		return false;
	}

	/**
	 * @see BuildOptionsAdapter#getPreprocessMode()
	 */
	public boolean getPreprocessMode() {
		return false;
	}

	/**
	 * @see BuildOptionsAdapter#getSourceOnePointFourMode()
	 */
	public boolean getSourceOnePointFourMode() {
		return false;
	}

	/**
	 * @see BuildOptionsAdapter#getStrictSpecMode()
	 */
	public boolean getStrictSpecMode() {
		return false;
	}

	/**
	 * @see BuildOptionsAdapter#getUseJavacMode()
	 */
	public boolean getIncrementalMode() {
		IProject currentProject = AspectJUIPlugin.getDefault().getCurrentProject();			
		boolean incrementalMode = AspectJPreferences.getIncrementalOption(currentProject);
		
		if (AspectJUIPlugin.DEBUG_BUILDER) {
			System.out.println(
				"BuildOptionsAdapter.getIncrementalMode called, returning :"
					+ new Boolean(incrementalMode));
		}

		return incrementalMode;
	}
	
	public boolean getBuildAsm() {
		IProject currentProject = AspectJUIPlugin.getDefault().getCurrentProject();		
		boolean buildAsm = AspectJPreferences.getBuildASMOption(currentProject);

		if (AspectJUIPlugin.DEBUG_BUILDER) {
			System.out.println("BuildOptionsAdapter.getBuildAsm called, returning :"
				+ new Boolean(buildAsm));
		}
		return buildAsm;
	}
	
	public boolean getShowWeaveMessages() {
		IProject currentProject = AspectJUIPlugin.getDefault().getCurrentProject();	
		boolean showweavemessages =  AspectJPreferences.getShowWeaveMessagesOption(currentProject);
		if (AspectJUIPlugin.DEBUG_BUILDER) {
			System.out.println("BuildOptionsAdapter.getShowWeaveMessages called, returning :"
				+ new Boolean(showweavemessages));
		}
		return showweavemessages;
	}

	/**
	 * @see BuildOptionsAdapter#getWorkingOutputPath()
	 */
	public String getWorkingOutputPath() {
		return ""; //$NON-NLS-1$
	}
	
	/*
	 * The next few options are not used, since they're all passed via getJavaOptionsMap()
	 */
	public String getComplianceLevel() {
		return null;
	}

	public Set getDebugLevel() {
		return null;
	}
	
	public boolean getNoImportError() {
		return false;		
	}

	public boolean getPreserveAllLocals() {
		return false;	
	}
	
	public String getSourceCompatibilityLevel() {
		return null;
	}
	
	public Set getWarnings() {
		return null;		
	}

	public boolean getUseJavacMode() {
		return false;
	}

	public String getCharacterEncoding() {
		return null;
	}

	/**
	 * Method getOutJar.
	 * @return String
	 */
	public String getOutJar() {
		IProject thisProject = AspectJUIPlugin.getDefault().getCurrentProject();
		String outputJar = AspectJPreferences.getProjectOutJar(thisProject);
		
		// If outputJar does not start with a slash, we might need to prepend the project
		// work directory.
		if (outputJar.trim().length()>0 && !(outputJar.startsWith("\\") || outputJar.startsWith("/"))) { //$NON-NLS-1$
			String trimmedName = outputJar.trim();
			boolean prependProject = true;
			
			// It might still be a fully qualified path if the 2nd char is a ':' (i.e. its
			// a windows absolute path with a drive letter in it !)
			if (trimmedName.length()>1) {
				if (trimmedName.charAt(1)==':') prependProject = false;
			}
			
			if (prependProject) {
			  // Its a relative path, it should be relative to the project.
			  String projectBaseDirectory = thisProject.getLocation().toOSString();
			  outputJar = new String(projectBaseDirectory+File.separator+outputJar.trim());
			}
		}
		if (AspectJUIPlugin.DEBUG_BUILDER) {
			System.out.println(
				"BuildOptionsAdapter.getOutJar called, returning :"
					+ outputJar);
		}

		return outputJar;
	}



	/**
	 * Method getInJars.
	 * @return Set
	 */
	public Set getInJars() {
		return null;
	}
    
    public Set getInPath() {
    	IProject thisProject = AspectJUIPlugin.getDefault().getCurrentProject();
		String[] v = AspectJPreferences.getProjectInPath(thisProject);

		// need to expand any variables on the path
		String inpath = expandVariables(v[0], v[2]);
		
        // Ensure that every entry in the list is a fully qualified one.
        inpath = fullyQualifyPathEntries(inpath);

		if (AspectJUIPlugin.DEBUG_BUILDER) {
			System.out
					.println("BuildOptionsAdapter.getInPath called, returning :"
							+ inpath);
		}
		if (inpath.length() == 0)
			return null;

		return mapStringToSet(inpath, false);
    }
    
    private String expandVariables(String path, String eKinds) {
		StringBuffer resultBuffer = new StringBuffer();
		StringTokenizer strTok = new StringTokenizer(path,
				File.pathSeparator);
		StringTokenizer strTok2 = new StringTokenizer(eKinds,
				File.pathSeparator);
		while (strTok.hasMoreTokens()) {
			String current = strTok.nextToken();
			int entryKind = Integer.parseInt(strTok2.nextToken());
			if (entryKind==IClasspathEntry.CPE_VARIABLE) {
				int slashPos = current.indexOf(
						AspectJUIPlugin.NON_OS_SPECIFIC_SEPARATOR, 0);
				if (slashPos != -1) {
					String exp = JavaCore.getClasspathVariable(current.substring(0,slashPos)).toOSString();
					resultBuffer.append(exp);
					resultBuffer.append(current.substring(slashPos));
				} else {
					String exp = JavaCore.getClasspathVariable(current).toOSString();
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
			//if (current.startsWith(AspectJUIPlugin.NON_OS_SPECIFIC_SEPARATOR)) {
				// Try to resolve path relative to the workspace. Need to
				// replace part of the path string with a fully qualified
				// equivalent.
				String projectName = null;
				int slashPos = current.indexOf(
						AspectJUIPlugin.NON_OS_SPECIFIC_SEPARATOR, 1);
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
								+ AspectJUIPlugin.NON_OS_SPECIFIC_SEPARATOR
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
                    AJDTEventTrace.generalEvent("AspectJ path entry " + current
							+ " does not exist. Ignoring.");
                    
					if (AspectJUIPlugin.DEBUG_BUILDER) {
						System.out
								.println("BuildOptionsAdapter.fullyQualifyPathEntries detected path entry "
										+ current + " does not exist");
					}
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

	public Set getAspectPath() {
		IProject thisProject = AspectJUIPlugin.getDefault().getCurrentProject();
        String[] v = AspectJPreferences.getProjectAspectPath(thisProject);

		// need to expand any variables on the path
		String aspectpath = expandVariables(v[0], v[2]);

        // Ensure that every entry in the list is a fully qualified one.
        aspectpath = fullyQualifyPathEntries(aspectpath);
        
        if (AspectJUIPlugin.DEBUG_BUILDER) {
            System.out.println(
                "BuildOptionsAdapter.getAspectPath called, returning :"
                    + aspectpath);
        }
        if (aspectpath.length()==0) return null; 
        
        return mapStringToSet(aspectpath,false);
    }
    
    
	/**
	 * Utility method for converting a semicolon separated list of
	 * files stored in a string into a Set of java.io.File objects.
	 * 
	 */
	private Set mapStringToSet(String input, boolean validateFiles) {
		if (input.length()==0) return null;
		String inputCopy = input;
		
		StringBuffer invalidEntries = new StringBuffer();
		
		// For relative paths (they don't start with a File.separator
		// or a drive letter on windows) - we prepend the projectBaseDirectory
		String projectBaseDirectory = 
		  AspectJUIPlugin.getDefault().getCurrentProject().
		  getLocation().toOSString();
		  
		if (AspectJUIPlugin.DEBUG_BUILDER)
			System.out.println("Converting ]"+input+"[");
		
		Set fileSet = new HashSet();
		while (inputCopy.indexOf(java.io.File.pathSeparator)!=-1) {  //ASCFIXME - Bit too platform specific!
		  int idx = inputCopy.indexOf(java.io.File.pathSeparator);
		  String path = inputCopy.substring(0,idx);
		  
		  java.io.File f = new java.io.File(path);
		  if (!f.isAbsolute())
		  	f = new File(projectBaseDirectory+java.io.File.separator+path);
		  	if (validateFiles && !f.exists()) {
		  		invalidEntries.append(f+"\n");
		  		if (AspectJUIPlugin.DEBUG_BUILDER)System.out.println("Skipping file ]"+f.toString()+"[");  
		  	} else {
		  	  fileSet.add(f);
		  	  if (AspectJUIPlugin.DEBUG_BUILDER) System.out.println("Adding file ]"+f.toString()+"[");  
		  	}
		  inputCopy = inputCopy.substring(idx+1);	
		  
		}
		// Process the final element
		if (inputCopy.length()!=0) {
		  java.io.File f = new java.io.File(inputCopy);
		  if (!f.isAbsolute())
		  	f = new File(projectBaseDirectory+java.io.File.separator+inputCopy);
		  	if (validateFiles && !f.exists()) {
		  		invalidEntries.append(f+"\n");
		  		if (AspectJUIPlugin.DEBUG_BUILDER) System.out.println("Skipping file ]"+f.toString()+"[");
		  	} else {
		  fileSet.add(f);
		  if (AspectJUIPlugin.DEBUG_BUILDER) System.out.println("Adding file ]"+f.toString()+"[");  
	}
	
		}
		
		//ASCFIXME - Need to NLSify this string...
		if (validateFiles && invalidEntries.length()!=0) {
		  AspectJUIPlugin.getDefault().getErrorHandler().handleWarning(
		    "The following jar files do not exist and are being ignored:\n"+invalidEntries.toString());
		}
		return fileSet;
	}
	

	/**
	 * Method getSourceRoots.
	 * @return Set
	 */
	public Set getSourceRoots() {
		return null;
	}
}