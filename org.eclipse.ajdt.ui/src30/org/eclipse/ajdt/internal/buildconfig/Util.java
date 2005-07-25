/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.buildconfig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Luzius Meisser
 *
 */
public class Util {
    
    private static String OS_INDEPENDENT_LINE_SEPARATOR = System
			.getProperty("line.separator"); //$NON-NLS-1$
    
	
    // LstSupport
	/**
     * Get the path of the file <code>file</code> relative to 
     * the location <code>fromDir</code>. 
	 * @param file 
	 * @param fromDir
	 * @return relative path in a <code>java.lang.String</code>
	 */
	public static String getRelativePathString(IPath file, IPath fromDir) {
		String result = null;

		if (file != null && fromDir != null) {
            // Check for identical inputs 
            if (file.equals(fromDir)) {
                result = "."; //$NON-NLS-1$
            }// end if identical
            else {
                List fromList;
                List fileList;
                fromList = getPathList(fromDir.toFile());
                fileList = getPathList(file.toFile());
                result = matchPathLists(fromList, fileList);
            }// end else different
		}// end if inputs are usable
		return result;
	}

    /**
     * Get the path of the file <code>file</code> relative to 
     * the location <code>fromDir</code>. 
     * @param file 
     * @param fromDir
     * @return relative path in a <code>java.lang.String</code>
     */
    public static String getRelativePathString(String file, String fromDir) {
		String result = null;

		if (file != null && fromDir != null) {
			return getRelativePathString(new Path(file), new Path(fromDir));
		}// end if inputs are usable
		return result;
	}
    
    /**
	 *  
	 */
	private static List getPathList(File f) {
		List l = new ArrayList();
		File r;
		try {
			r = f.getCanonicalFile();
			while (r != null) {
				l.add(r.getName());
				r = r.getParentFile();
			}
		} catch (IOException e) {
			l = null;
		}
		return l;
	}

	/**
     * 
	 */
	private static String matchPathLists(List fromList, List toList) {
		int i;
		int j;
		String result = ""; //$NON-NLS-1$
        
		i = fromList.size() - 1;
		j = toList.size() - 1;

		// First, get rid of the common root
		while ((i >= 0) && (j >= 0) &&
                (fromList.get(i).equals(toList.get(j)))) {
			i--;
			j--;
		}// end while still finding common components 

		// For each remaining level in the home path add
        // a double dot (..)
		for (; i >= 0; i--) {
			result += ".." + AspectJPlugin.NON_OS_SPECIFIC_SEPARATOR; //$NON-NLS-1$
		}// end for each remaining level 

		// for each level in the file path, add the path
		for (; j >= 1; j--) {
			result += toList.get(j) +
                AspectJPlugin.NON_OS_SPECIFIC_SEPARATOR;
		}

		// Return the file name
		result += toList.get(j);
		return result;
	}
    
    /**
     * If input file contains no links to other lst files then just returns
     * the IPath to the original file. Otherwise, creates a new file in the 
     * same directory as the original file with the extension ".inlined.lst"
     * and returns an IPath for the new file. The original .lst file is 
     * left remaining on disk with its original name.
     * @param lstFile
     * @return
     * @throws FileNotFoundException
     */
    public static IPath getInlinedLstFile(IPath lstFile)
			throws FileNotFoundException {
		IPath result = lstFile;

		if (lstFile != null) {
            // Quick check that we have a bona fide file to work with
			if (!lstFile.toFile().exists()) {
				throw new FileNotFoundException(lstFile.toOSString()
						+ " " + AspectJUIPlugin.getResourceString("buildConfig.notFound")); //$NON-NLS-1$ //$NON-NLS-2$
			}
            
			// Quick check to see if file has a line starting with the
			// link symbol "@". If not, just return the input IPath to
			// the caller.
			if (lstFileContainsLinks(lstFile)) {
				File resultFile = new File(getInlinedFileName(lstFile
						.toOSString()));
				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(
							resultFile));
					writeInlinedLstFileToBuffer(lstFile.toFile(), bw);
					bw.close();
					result = new Path(resultFile.getAbsolutePath());
				} catch (IOException e) {
				}
			}// end if a link found
		}// end if input is usable

		return result;
	}
    
    public static String getInlinedFileName(String originalName) {
		String result = originalName;
		if (originalName != null) {
			String tmp1 = originalName.substring(0, originalName
					.lastIndexOf('.'));
			String tmp2 = originalName.substring(originalName.lastIndexOf('.'));
			result = tmp1 + ".inlined" + tmp2;  //$NON-NLS-1$
		}// end if input is usable
		return result;
	}
    
    /**
	 * @param file
	 * @param bos
	 * @throws FileNotFoundException
	 */
	private static void writeInlinedLstFileToBuffer(File file,
			BufferedWriter bw) throws FileNotFoundException {
        // Open up the input file
        BufferedReader in = new BufferedReader(new FileReader(file));   
        String line = null;
        try {
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("@")) { //$NON-NLS-1$
                    bw.write(line);
                    bw.newLine();
                }// end if no lst link reference found on line
                else {
                    // Get the location of the linked file. If it 
                    // exists then recursively call it from here 
                    // (passing in the original BufferedWriter).
                    String linkedFilePath = line.substring(1);
                    File linkedFile = new File(linkedFilePath);
                    if (linkedFile.isAbsolute()) {
                    	if (linkedFile.exists()) {
                            writeInlinedLstFileToBuffer(linkedFile, bw);
                        }
                    }// end if file linking to is absolute
                    else {
                        // Don't panic. Need to obtain an absolute path
						// to the linked file.
						linkedFile = new File(file.getParentFile()
								.getAbsolutePath()
								+ AspectJPlugin.NON_OS_SPECIFIC_SEPARATOR
								+ linkedFilePath);
						if (linkedFile.exists()) {
							writeInlinedLstFileToBuffer(linkedFile, bw);
						}
                    }// end else linking using relative path
                }// else line contains a link reference
            }// end while more lines to read
        }
        catch (IOException e) {
        }
	}

	/**
	 * @param lstFile
	 * @throws FileNotFoundException
	 */
	private static boolean lstFileContainsLinks(IPath lstFile)
			throws FileNotFoundException {
		boolean result = false;
		String path = lstFile.toOSString();
		BufferedReader in = new BufferedReader(new FileReader(path));
		String line = null;
		try {
			while ((line = in.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("@")) { //$NON-NLS-1$
					result = true;
					break;
				}// end if lst link reference found
			}
		} catch (IOException e) {
		}
		return result;
	}

	/**
	 * @param file
	 * @param files
	 * @param options
	 * @param links
	 * @throws FileNotFoundException
	 */
    public static void getLstFileContents(
            IPath file,
            List files,
            List options,
			List links) throws FileNotFoundException {
    	// Check that we have been passed in the necessary
        // INOUT instances.
        if (file != null &&
                files != null &&
                options != null &&
                links != null) {
            String path = file.toOSString();
            BufferedReader in = new BufferedReader(new FileReader(path));
            String line = null;
            try {
				while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("@")) { //$NON-NLS-1$
                        links.add(line);
                    }// end if lst link reference found
                    else if (line.startsWith("-")) { //$NON-NLS-1$
                        // Option. Determine whether or not
						// the option requires an argument.
						if (AjcOptionsChecker.optionWithArg(line)) {
							// Grab the next line
							String nextLine = in.readLine();
							if (nextLine != null) {
								options.add(line
										+ OS_INDEPENDENT_LINE_SEPARATOR
										+ nextLine.trim());
							}// end if
							else {
								// Just add the option anyway
								options.add(line);
							}// end else arg expected but none found
						}// end if argument expected
                        else {
                            options.add(line);
                        }// end else no argument expected
                    }// end else if option found
                    else {
                        // Source file reference ?
                        // Check that the file actually exists
                        // before adding to the files list ??
                    	if (!line.equals("")) //$NON-NLS-1$
                    		files.add(line);
                    }// end else a possible source file reference found
				}// while more lines to read
			} catch (IOException e) {
			}
		}// end if valid inputs
	}
    
    /**
     * Helper class
     */
    static class AjcOptionsChecker {
        private static Set optionsWithArgs;
        
        static {
            initialiseOptionsWithArgs();
        }// end static initialisation block

        public static boolean optionWithArg(String option) {
            boolean result = false;
            if (optionsWithArgs.contains(option)) {
            	result = true;
            }
            return result;
        }
        
		/**
		 * 
		 */
		private static void initialiseOptionsWithArgs() {
			optionsWithArgs = new HashSet();
            optionsWithArgs.add("-inpath"); //$NON-NLS-1$
            optionsWithArgs.add("-injars"); //$NON-NLS-1$
            optionsWithArgs.add("-aspectpath"); //$NON-NLS-1$
            optionsWithArgs.add("-outjar"); //$NON-NLS-1$
            optionsWithArgs.add("-argfile"); //$NON-NLS-1$
            optionsWithArgs.add("-sourceroots"); //$NON-NLS-1$
            optionsWithArgs.add("-Xlintfile"); //$NON-NLS-1$
            optionsWithArgs.add("-cp"); //$NON-NLS-1$
            optionsWithArgs.add("-classpath"); //$NON-NLS-1$
            optionsWithArgs.add("-bootclasspath"); //$NON-NLS-1$
            optionsWithArgs.add("-d"); //$NON-NLS-1$
            optionsWithArgs.add("-encoding"); //$NON-NLS-1$
            optionsWithArgs.add("-source"); //$NON-NLS-1$
            optionsWithArgs.add("-target"); //$NON-NLS-1$
            optionsWithArgs.add("-log"); //$NON-NLS-1$
            optionsWithArgs.add("-repeat"); //$NON-NLS-1$
            optionsWithArgs.add("-inpath"); //$NON-NLS-1$
		}
    }// end class AjcOptionsChecker
    
    // End LstSupport
}
