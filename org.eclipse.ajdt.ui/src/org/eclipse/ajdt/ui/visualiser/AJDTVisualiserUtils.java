/*********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html 
 * Contributors: 
 * 
 * Sian Whiting -  initial version.
 **********************************************************************/
package org.eclipse.ajdt.ui.visualiser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.contribution.visualiser.interfaces.IMember;
import org.eclipse.contribution.visualiser.jdtimpl.JDTMember;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Utility method used by the providers for the new Visualiser
 */
public class AJDTVisualiserUtils {
	
	private static IProject project;
	private static Object lastLoadedConfigFile;


	/**
	 * This method returns a list of Maps 
	 * from Integers to a List of Strings representing 
	 * the names of the aspects affecting the line
	 * @param showWarnings
	 * @param showErrors
	 */
	public static List getMarkupInfo(IMember member, IProject project, boolean showErrors, boolean showWarnings) {
		AJDTVisualiserUtils.project = project;

		List returningList = new LinkedList();
		List packages = StructureModelUtil.getPackagesInModel();

        String parent = member.getContainingGroup().getFullname();
        boolean defaultPackage = parent.startsWith("(default");

		Iterator it = packages.iterator();
		while (it.hasNext()) {
			Object[] o = (Object[]) it.next();

			if (parent.equals(o[1]) || defaultPackage && o[1].equals("<default>")) {

				IProgramElement packageNode = (IProgramElement) o[0];
				List files = StructureModelUtil.getFilesInPackage(packageNode);

				Iterator it2 = files.iterator();
				while (it2.hasNext()) {

					IProgramElement file = (IProgramElement) it2.next();
					
					org.aspectj.bridge.ISourceLocation isl =
						file.getSourceLocation();

					String testpath = isl.getSourceFile().getAbsolutePath();
					if(testpath.endsWith(".java")) {
						testpath = testpath.substring(0, testpath.length() - 5);
					} else if(testpath.endsWith(".aj")) {
						testpath = testpath.substring(0, testpath.length() - 3);
					}

					if (testpath.endsWith(File.separator + member.getName())) {
						try {
							String fullpath =
								isl.getSourceFile().getAbsolutePath();

							Map lineAdviceMap =
								StructureModelUtil.getLinesToAspectMap(
									fullpath);

							Map map = changeMap(lineAdviceMap, 0);
							map = addErrorsAndWarnings(map, member, showErrors, showWarnings);
							returningList.add(map);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}

					}
				}
			}
		}
		return returningList;
	}

	
	/**
	 * @param map
	 * @param showErrors
	 * @param showWarnings
	 * @return
	 */
	private static Map addErrorsAndWarnings(Map map, IMember member, boolean showErrors, boolean showWarnings) {
		if(showErrors || showWarnings) {
			if(member instanceof JDTMember) {
				IJavaElement jEl = ((JDTMember)member).getResource();
				if(jEl != null) {
					try {
						IResource res = jEl.getUnderlyingResource();
						IMarker[] ajdtProblems = res.findMarkers("org.eclipse.ajdt.ui.problemmarker", true, IResource.DEPTH_ONE);
						for (int i = 0; i < ajdtProblems.length; i++) {
							// Get the severity so we know whether it's a warnign or an error.
							Integer severity = (Integer)ajdtProblems[i].getAttribute(IMarker.SEVERITY);						
							if(severity != null && severity.intValue() == IMarker.SEVERITY_ERROR && showErrors) {
								// Get the name of the declaring aspect from the related locations attribute
								String aspectName = getDeclaringAspect(ajdtProblems[i]);
								Integer lineNumber = (Integer)ajdtProblems[i].getAttribute(IMarker.LINE_NUMBER);
								List kinds = (List)map.get(lineNumber);
								if(kinds != null) {
									kinds.add(AJDTMarkupProvider.aspectJErrorKind + ":::" + aspectName);
								} else {
									kinds = new ArrayList();
									kinds.add(AJDTMarkupProvider.aspectJErrorKind + ":::" + aspectName);
									map.put(lineNumber, kinds);
								}
							} else if(severity != null && severity.intValue() == IMarker.SEVERITY_WARNING && showWarnings) {
								// Get the name of the declaring aspect from the related locations attribute
								String aspectName = getDeclaringAspect(ajdtProblems[i]);
								Integer lineNumber = (Integer)ajdtProblems[i].getAttribute(IMarker.LINE_NUMBER);
								List kinds = (List)map.get(lineNumber);
								if(kinds != null) {
									kinds.add(AJDTMarkupProvider.aspectJWarningKind + ":::" + aspectName);
								} else {
									kinds = new ArrayList();
									kinds.add(AJDTMarkupProvider.aspectJWarningKind + ":::" + aspectName);
									map.put(lineNumber, kinds);
								}
							}
						}
					} catch (CoreException cEx) {
						AJDTEventTrace.generalEvent("Exception finding errors and warnings: " + cEx.getMessage());
					}
				}
				
			}
		}
		return map;
	}


	/**
	 * @param ajdtProblems
	 * @param i
	 * @return
	 * @throws CoreException
	 */
	private static String getDeclaringAspect(IMarker marker) throws CoreException {
		String aspectName = "";
		String relatedLoc = (String)marker.getAttribute(
		        AspectJUIPlugin.RELATED_LOCATIONS_ATTRIBUTE_PREFIX + "0");
		if (relatedLoc != null){
			String[] parts = relatedLoc.split(":::");
			if (parts.length > 0) {
				aspectName = parts[0];
				if(aspectName.endsWith(".java")) {
					aspectName = aspectName.substring(0, aspectName.length() - 5);
				} else if (aspectName.endsWith(".aj")) {
					aspectName = aspectName.substring(0, aspectName.length() - 3);
				}
				int lastSeparator = aspectName.lastIndexOf(File.separator);
				if(lastSeparator != -1) {
					aspectName = aspectName.substring(lastSeparator + File.separator.length(), aspectName.length());
				}
			}
		}
		return aspectName;
	}


	/**
	 * This method changes a Map from line numbers to a List of ProgramElementNodes
	 * into a Map from line numbers to a List of Strings representing the names of
	 * the aspects affecting that line number
	 * 
	 * @return a Map from Integers to a List of Strings
	 */
	private static Map changeMap(Map oldmap, int startline) {
		HashMap newmap = new HashMap();

		Set keys = oldmap.keySet();

		Iterator it = keys.iterator();
		while (it.hasNext()) {
			Integer line = (Integer) it.next();

			Object o = oldmap.get(line);

			List oldaspects = (List)o;

			List newaspects = new LinkedList();

			Iterator iterator2 = oldaspects.iterator();
			while (iterator2.hasNext()) {

				IProgramElement theaspect =
					(IProgramElement) iterator2.next();

				String fullpath = theaspect.getSourceLocation().getSourceFile().getAbsolutePath();

				Path path = new Path(fullpath);
				String aspectname = path.removeFileExtension().lastSegment();

				newaspects.add(aspectname);
			}

			if (!newaspects.isEmpty()) {
				line = new Integer(line.intValue() + startline);

				newmap.put(line, newaspects);
			}
		}

		return newmap;
	}


	/**
	 * Iterates through all the packages in a project and returns a Set containing
	 * all the Aspects in a project that are currently active.
	 * 
	 * @param JP the project
	 * @param stringRepresentation if true the set returned is a set of strings
	 * otherwise it is a set of IResources
	 */
	public static Set getAllAspects(IJavaProject JP, boolean stringRepresentation) {
	
		project = JP.getProject();
	
		initialiseAJDE();
	
		List packages = StructureModelUtil.getPackagesInModel();
	
		Set aspects = new HashSet();
	
		Iterator it = packages.iterator();
		while (it.hasNext()) {
			Object[] progNodes = (Object[]) it.next();
			
			IProgramElement packageNode = (IProgramElement) progNodes[0];//it.next();
	
			Set temp =
				StructureModelUtil.getAspectsAffectingPackage(packageNode);
	
			aspects.addAll(temp);
		}
	
		return changeSet(aspects,stringRepresentation);
	}


	/**
	 * This method sets the current project and initialises AJDE
	 */
	private static void initialiseAJDE() {
	
		String configFile = AspectJUIPlugin.getBuildConfigurationFile(project);
		if (!configFile.equals(lastLoadedConfigFile)) {
			AJDTEventTrace.generalEvent("initialiseAJDE: switching configs - from:"+lastLoadedConfigFile+" to:"+configFile);
			Ajde.getDefault().getConfigurationManager().setActiveConfigFile(
				configFile);
			lastLoadedConfigFile = configFile;
		}
	}


	/**
	 * This method returns a list of all the classes in an IPackageFragment
	 * as a list of three element arrays.
	 * 
	 * @return Object[] of length 3
	 * 			Object[0] is an IResource corresponding to a class
	 * 			Object[1] is the number of lines in the class
	 * 			Object[2] is a map from Integers to a List of Strings representing 
	 * 						the names of the aspects affecting the line
	 */
	public static List getAllClasses(IPackageFragment pf) {
		IJavaProject JP = pf.getJavaProject();
		project = JP.getProject();
		LinkedList returningClasses = new LinkedList();	
		initialiseAJDE();
		
		try {
			ICompilationUnit[] packageFragmentChildren = pf.getCompilationUnits();
			
			List packages = StructureModelUtil.getPackagesInModel();
			
			String pf_string = pf.toString();
				
			boolean defaultPackage = false;
			
			String test = "";
			if (pf_string.startsWith("<default>")) {
				defaultPackage = true;
			} else {
				int end = pf_string.indexOf("[");
				test = pf.toString().substring(0, end - 1);
				if(test.indexOf("(not open)") != -1) {
					test = test.substring(0, test.length() - 10).trim();
				}
			}
		
			Iterator it = packages.iterator();
			while (it.hasNext()) {
				Object[] o = (Object[]) it.next();
		
				if ((defaultPackage && o[1].equals("<default>")) || test.equals(o[1])) {
		
					IProgramElement packageNode = (IProgramElement) o[0];
		
					List unsortedFiles =
						StructureModelUtil.getFilesInPackage(packageNode);
		
					List files = sortElements(unsortedFiles);
					
					for (int j = 0; j < files.size(); j++) {
						try {
							IProgramElement fileNode =
								(IProgramElement) files.get(j);
							
							/* Sian - added this test to cope with the case where a project has multiple source
							 * folders containing packages with the same name. Eclipse considers these to be two different
							 *  packages where as the structure model sees it as one.  We therefore need to check that each
							 *  file in the structure model actually is one of the children of the package we are being
							 *  asked about. 
							 */
							if(!containsFile(packageFragmentChildren, fileNode)) {
								continue;
							}
							
							org.aspectj.bridge.ISourceLocation isl =
								fileNode.getSourceLocation();
		
							String fullpath =
								isl.getSourceFile().getAbsolutePath();
		
							Map lineAdviceMap =
								StructureModelUtil.getLinesToAspectMap(fullpath);
		
							Object entry[] = new Object[3];
		
							IResource res =
								AspectJUIPlugin
									.getDefault()
									.getAjdtProjectProperties()
									.findResource(
									fullpath, project);
		
							int endLine = isl.getEndLine();
		
							entry[0] = res;
		
							entry[1] = new Integer(endLine);
		
							entry[2] = changeMap(lineAdviceMap, 0);
		
							returningClasses.add(entry);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}
					break;
				}	
			}
		} catch (JavaModelException jme) {
			AJDTEventTrace.generalEvent("Exception finding files in a package. " + jme.getMessage());
		}
	
		return returningClasses;
	}


	/**
	 * Tests whether the array contains a CompilationUnit with the same name as the ProgramElement 
	 * @param packageFragmentChildren - CompilationUnits
	 * @param fileNode - ProgramElement
	 * @return
	 */
	private static boolean containsFile(ICompilationUnit[] packageFragmentChildren, IProgramElement fileNode) {
		for (int i = 0; i < packageFragmentChildren.length; i++) {
			if (packageFragmentChildren[i].getElementName().equals(fileNode.getName())) {
				return true;
			}
		}
		return false;
	}


	/**
	 * This method returns a list of all the classes in an ICompilationUnit
	 * as a list of three element arrays.
	 * 
	 * @return Object[] of length 3
	 * 			Object[0] is an IResource corresponding to a class
	 * 			Object[1] is the number of lines in the class
	 * 			Object[2] is a map from Integers to a List of Strings representing 
	 * 						the names of the aspects affecting the line
	 */
	public static List getAllClasses(ICompilationUnit CU) {
	
		project = CU.getJavaProject().getProject();
	
		initialiseAJDE();
	
		LinkedList returningClasses = new LinkedList();
		List packages = StructureModelUtil.getPackagesInModel();
	
	    String cu_parent = CU.getParent().toString();
	    
		// If the CompilationUnit is in the default package then the cu_parent string will start:
		// [default] [in ......
	    // This differs from other cases when the cu is in a real package because it will then start:
	    // figures.gui [in ......
	    // So, this next piece of code, which relies on the position of that first '[' will break ....
	    
		int end;
		
		if (cu_parent.startsWith("<default>")) {
			// Jump over the first [ and find the index of the second one !
			end = cu_parent.substring(1).indexOf("[")+1;
		} else {
			end = cu_parent.indexOf("[");
		}
		
		String test = cu_parent.substring(0, end - 1);
		if(test.indexOf("(not open)") != -1) {
			test = test.substring(0, test.length() - 10).trim();
		}
		String path = CU.getPath().toString();
	
		Iterator it = packages.iterator();
		while (it.hasNext()) {
			Object[] o = (Object[]) it.next();
	
			if (test.equals(o[1])) {
	
				IProgramElement packageNode = (IProgramElement) o[0];
				List files = StructureModelUtil.getFilesInPackage(packageNode);
	
				Iterator it2 = files.iterator();
				while (it2.hasNext()) {
	
					IProgramElement file = (IProgramElement) it2.next();
					
					org.aspectj.bridge.ISourceLocation isl =
						file.getSourceLocation();
	
					String testpath = isl.getSourceFile().getAbsolutePath();
					testpath = testpath.replace('\\','/');
	
					if (testpath.endsWith(path)) {
						try {
							String fullpath =
								isl.getSourceFile().getAbsolutePath();
	
							Map lineAdviceMap =
								StructureModelUtil.getLinesToAspectMap(
									fullpath);
	
							Object entry[] = new Object[3];
	
							IResource res =
								AspectJUIPlugin
									.getDefault()
									.getAjdtProjectProperties()
									.findResource(
									fullpath, project);
	
							int endLine = isl.getEndLine();
	
							entry[0] = res;
							entry[1] = new Integer(endLine);
							entry[2] = changeMap(lineAdviceMap, 0);
	
							returningClasses.add(entry);
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
	
					}
				}
			}
		}
	
		return returningClasses;
	}


	/**
		 * This method changes a set of ProgramElementNodes into a Set of IResources or Strings.
		 * 
		 * @param oldset the input set of program element nodes
		 * @param stringRepresentation specifies if you want strings or IResources back
		 * @return a Set of IResources or Strings
		 */
		private static Set changeSet(Set oldset,boolean stringRepresentation) {
			Set newset = new HashSet();
	
			Iterator it = oldset.iterator();
	
			
			while (it.hasNext()) {
				Object obj = it.next();
				
				List aspects = (List)obj;//(List) it.next();
			
				Iterator iterator2 = aspects.iterator();
				
	
			while (iterator2.hasNext()) {
	
				Object progNodes = iterator2.next();
	
				IProgramElement progNode = (IProgramElement) progNodes;
	
				String path = progNode.getSourceLocation().getSourceFile().getAbsolutePath();
	
				if (stringRepresentation) {
					newset.add(path);
				} else {
				  IResource resource =
					AspectJUIPlugin
						.getDefault()
						.getAjdtProjectProperties()
						.findResource(
						path, project);

				  // Did we find it in this project?  If not then look across the workspace
				  if (resource == null) {
					resource = AspectJUIPlugin.getDefault().getAjdtProjectProperties().findResource(path);
				  }
				  newset.add(resource);
				}
			}
		}
	
		return newset;
	}
		
	/**
	 * Helper function sorts a list of resources into alphabetical order
	 */
	private static List sortElements(List oldElements) {

		Object[] temp = oldElements.toArray();
		SortingComparator comparator = new SortingComparator();

		Arrays.sort(temp, comparator);

		List newResources = Arrays.asList(temp);

		return newResources;
	}
	
	/**
	 * Compares two ProgramElementNodes by their String names.
	 */
	private static class SortingComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			IProgramElement p1 = (IProgramElement) o1;
			IProgramElement p2 = (IProgramElement) o2;
	
			String name1 = p1.getName();
			String name2 = p2.getName();
	
			return name1.compareTo(name2);
		}
	}
	
}
