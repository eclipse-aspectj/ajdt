
/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the IDE support for the AspectJ(tm)
 * programming language; see http://aspectj.org
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is AspectJ.
 *
 * The Initial Developer of the Original Code is Xerox Corporation. Portions
 * created by Xerox Corporation are Copyright (C) 1999-2002 Xerox Corporation.
 * All Rights Reserved.
 *
 * Contributor(s):
 */

package org.eclipse.ajdt.ui.visualiser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;
import org.aspectj.weaver.AsmRelationshipProvider;
import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Prototype functionality for package view clients.
 */  
public class StructureModelUtil {

	private static Object lastLoadedConfigFile;

	private static String activeConfigFile = "";
	 
	// Hashtable: maps sourcefile -> annotationmap
	// See comments in getLinesToAspectMap(String,boolean) below...
	private static Hashtable annotationsCache = new Hashtable();
    
	// Hashtable: maps SourceFile+T/F -> aspectmap
	// See comments in getLinesToAspectMap(String,boolean) below...
	private static Hashtable processedAnnotationsCache = new Hashtable();

	
	
	/**
	 * Just a pass through version of getLinesToAspectMap that allows for callers who
	 * relied on the old behaviour (i.e. the visualiser).
	 * 
	 * @param sourceFilePath
	 * @return Map is a map of line numbers to aspects
	 */
	public static Map getLinesToAspectMap(String sourceFilePath) {
		return getLinesToAspectMap(sourceFilePath,false);
	}
	
	
	/**
	 * Get all the files containing aspects that are included in the current
	 * build for a project, even those that do not advise any classes. Include
	 * inner aspects.
	 * 
	 * @param jp -
	 *            the project
	 * @param returnIResources -
	 *            if true IResources are returned, otherwise IProgramElements
	 *            are
	 * @return Set of all aspects in the project
	 */
	public static Set getAllAspects(IProject project, boolean returnIResources) {
		Set aspects = new HashSet();
		initialiseAJDE(project);

		List packages = StructureModelUtil.getPackagesInModel();

		Iterator iterator = packages.iterator();
		while (iterator.hasNext()) {
			Object[] progNodes = (Object[]) iterator.next();

			IProgramElement packageNode = (IProgramElement) progNodes[0];
			List files = StructureModelUtil.getFilesInPackage(packageNode);
			for (Iterator it = files.iterator(); it.hasNext();) {
				IProgramElement fileNode = (IProgramElement) it.next();
				List children = fileNode.getChildren();
				for (Iterator iter = children.iterator(); iter.hasNext();) {
					IProgramElement child = (IProgramElement) iter.next();
					if (child.getKind().equals(IProgramElement.Kind.ASPECT)) {
						aspects.add(fileNode);
						break;
					} else {
						// look for inner aspects
						List innerChildren = child.getChildren();
						for (Iterator innerIterator = innerChildren.iterator(); innerIterator
								.hasNext();) {
							IProgramElement element = (IProgramElement) innerIterator
									.next();
							if (element.getKind().equals(
									IProgramElement.Kind.ASPECT)) {
								aspects.add(fileNode);
								break;
							}
						}
					}
				}
			}
		}
		if (returnIResources) {
			Set resources = new HashSet();
			for (Iterator iter = aspects.iterator(); iter.hasNext();) {
				IProgramElement element = (IProgramElement) iter.next();
				String path = element.getSourceLocation().getSourceFile()
						.getAbsolutePath();
				IResource resource = AspectJUIPlugin.getDefault()
						.getAjdtProjectProperties().findResource(path, project);

				// Did we find it in this project? If not then look across the
				// workspace
				if (resource == null) {
					resource = AspectJUIPlugin.getDefault()
							.getAjdtProjectProperties().findResource(path);
				}
				resources.add(resource);
			}
			aspects = resources;
		}
		return aspects;
	}
	
    public static void wipeCache() {
    	// Bit crude... if you compile one project, you lose the
    	// annotations for all of them... 
    	AJDTEventTrace.generalEvent("Wiping StructureModelUtil caches");
    	annotationsCache.clear();
    	processedAnnotationsCache.clear();
		activeConfigFile = Ajde.getDefault().getConfigurationManager().getActiveConfigFile();
    }

	/**
	 * This method returns a map from affected source lines in a class to
	 * a List of aspects affecting that line.
	 * Based on method of same name by mik kirsten. To be replaced when StructureModelUtil
	 * corrects its implementation
	 * 
	 * AndyC: 2-Mar-03: Extended input parameter set with a boolean.  This determines
	 * if the map is filled with a map of lines->aspects or lines->advice_within_those_aspects
	 * The visualiser wants the first version of the map, the gutter annotations want the second
	 * version.  The gutter annotations needs to know more than just the aspect in affect, they
	 * need to know which advice within the aspect is in affect.
	 * 
	 * AndyC: 28-Aug-03
	 * To speed up this code I'm putting in two caches.  There are two performance
	 * bottlenecks in the code below:
	 * - Asking the ASM for the inline annotations is expensive
	 * - Converting the inline annotations into the map we need to return is expensive,
	 *   the map can have two kinds of contents depending on the setting of the boolean
	 *   parameter.
	 * 
	 * The first cache will cache requests made to the AsmManager:
	 * cachekey:   sourcefilename
	 * cacheentry: inlineannotations map for that file
	 * 
	 * The second cache will cache the results of the complex conversion from inline
	 * annotations to return map.
	 * cachekey:   sourcefilename suffixed with a 'T' or an 'F' depending on the value of
	 *             the boolean
	 * cacheentry: The map constructed from the inline annotations.
	 * 
	 * 
	 * 
	 * @param the full path of the source file to get a map for
	 * @param needIndividualNodes If true then the line numbers map to real advice markers within the aspects.
	 * 
	 * @return a Map from line numbers to a List of ProgramElementNodes.
	 */
	public static Map getLinesToAspectMap(String sourceFilePath,boolean needIndividualNodes) {
		//System.err.println("Cache sizes: annotationsCache.size="+annotationsCache.size()+
		//		 "  processedAnnotationsCache.size="+processedAnnotationsCache.size());
		if (activeConfigFile == null || 
		    !activeConfigFile.equals(Ajde.getDefault().getConfigurationManager().getActiveConfigFile()))
		  wipeCache();

		String cacheKey = sourceFilePath+(needIndividualNodes?"T":"F");
		
		// Query primary cache, have we seen this request recently?
		Map aspectMap = (Map)processedAnnotationsCache.get(cacheKey);
		if (aspectMap!=null) {
			//System.err.println("Found "+sourceFilePath+" in primary cache");
			return aspectMap;
		} 
		
		try {

			// Query secondary cache, have we asked for the annotations on this file
			// before?
			Map annotationsMap = null;
			annotationsMap = (Map)annotationsCache.get(sourceFilePath);
			if (annotationsMap==null) {
				annotationsMap = 
			    AsmManager.getDefault().getInlineAnnotations(
					sourceFilePath,true,true);
				if (sourceFilePath!=null && annotationsMap != null)
				  annotationsCache.put(sourceFilePath,annotationsMap);
			} 
//			else {
//				System.err.println("Found "+sourceFilePath+" in secondary cache");
//			}
	
			aspectMap = new HashMap();
	
			if (annotationsMap == null ) { return aspectMap; }
	
			Set keys = annotationsMap.keySet();
			for (Iterator it = keys.iterator(); it.hasNext();) {
				Object key = it.next();
				List annotations = (List) annotationsMap.get(key);
				for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
					IProgramElement node = (IProgramElement) it2.next();
					IRelationshipMap irm = AsmManager.getDefault().getRelationshipMap();
					Vector toStore = new Vector();
					
					// Advice with no runtime test
					IRelationship advises = irm.get(node,IRelationship.Kind.ADVICE,"advised by",false,false); 
					List toStoreNoRuntimeTest = processTargets(advises,needIndividualNodes);
					toStore.addAll(toStoreNoRuntimeTest);
					
					// Advice with a runtime test
					advises = irm.get(node,IRelationship.Kind.ADVICE,	"advised by",true,false); 
					List toStoreWithRuntimeTest = processTargets(advises,needIndividualNodes);
					toStore.addAll(toStoreWithRuntimeTest);
					
					// Lets have a mooch for other kinds of advice too !!

					// If needIndividualNodes is false - the visualiser is asking !
					// If needIndividualNodes is true  - the editor is asking for gutter annotations !
	//			    if (!needIndividualNodes) {
//				      IRelationship checker = irm.get(node,IRelationship.Kind.DECLARE,AsmRelationshipProvider.MATCHES_DECLARE,false,false);
//				      if (checker!=null) {
//				   	    System.err.println("Found a checker on this node: "+node.toLabelString());
//				   	    System.err.println("Num targets = "+checker.getTargets());
//				   	    System.err.println("First one is "+checker.getTargets().get(0).toString());
//				   	    List toStoreCheckers = processTargets(checker,needIndividualNodes);
//					    toStore.addAll(toStoreCheckers);
//				      }
				   
				      IRelationship intertypeDecls = irm.get(node,
				   		  IRelationship.Kind.DECLARE_INTER_TYPE,
				   		  AsmRelationshipProvider.INTER_TYPE_DECLARED_BY,false,false);
				      if (intertypeDecls!=null) {
				   	    List toStoreIntertypeDecls = processTargets(intertypeDecls,needIndividualNodes);
					    toStore.addAll(toStoreIntertypeDecls);
				      }
		//		   }

					if (toStore != null && toStore.size()!=0) aspectMap.put(key,toStore);
					
					
				}
			}

			processedAnnotationsCache.put(cacheKey,aspectMap); // Cache result
			return aspectMap;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Go through the targets of a relationship.
	 */
	private static List processTargets(IRelationship advises,boolean needIndividualNodes) {
		List aspectsAndAdvice = new Vector();
		if (advises != null) {
			List targets = advises.getTargets();

			for (Iterator it4 = targets.iterator(); it4.hasNext();) {
				String targetHandle = (String)it4.next();
				if (targetHandle != null) {
					IProgramElement pNode = AsmManager.getDefault().getHierarchy().findElementForHandle(targetHandle);

					if (pNode != null) {						
						if (pNode.getKind() == IProgramElement.Kind.ADVICE ||
							isIntertypeKind(pNode.getKind()) ||
							//if FILE_JAVA, guess it's an injar aspect
							(pNode.getKind() == IProgramElement.Kind.FILE_JAVA)) {
							IProgramElement theAspect = null;
							if (needIndividualNodes) {
								// Put the advice node and relevant info in the map
								NodeHolder noddyHolder = new NodeHolder(pNode,advises.hasRuntimeTest());
								aspectsAndAdvice.add(noddyHolder);
							} else {
								// Put the aspect node (parent of the advice node) in the map
								if ((pNode.getKind() == IProgramElement.Kind.FILE_JAVA))
									theAspect = pNode;
								else
									theAspect = pNode.getParent();
								aspectsAndAdvice.add(theAspect);
							}
							// We don't come into processTargets for deow if needIndividualNodes is true !
						} else if (pNode.getKind() == IProgramElement.Kind.DECLARE_ERROR) {
							aspectsAndAdvice.add(pNode.getParent());
						} else if (pNode.getKind() == IProgramElement.Kind.DECLARE_WARNING) {
							aspectsAndAdvice.add(pNode.getParent());							
						}						
					}
				}
			}
		}
		return aspectsAndAdvice;
	}
	
	private static boolean isIntertypeKind(IProgramElement.Kind kind) {
		if (kind == IProgramElement.Kind.INTER_TYPE_METHOD) return true;
		if (kind == IProgramElement.Kind.INTER_TYPE_FIELD) return true;
		if (kind == IProgramElement.Kind.INTER_TYPE_PARENT) return true;
		if (kind == IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR) return true;
		return false;
	}

	/**
	 * This method is copied from StructureModelUtil inoder for it to use the working
	 * version of getLineToAspectMap()
	 * 
	 * @return		the set of aspects with advice that affects the specified package
	 */
	public static Set getAspectsAffectingPackage(IProgramElement packageNode) {
		List files = StructureModelUtil.getFilesInPackage(packageNode);
		Set aspects = new HashSet();
		for (Iterator it = files.iterator(); it.hasNext();) {
			IProgramElement fileNode = (IProgramElement) it.next();
			Map adviceMap =
				getLinesToAspectMap(
					fileNode.getSourceLocation().getSourceFile().getAbsolutePath());
			Collection values = adviceMap.values();
			for (Iterator it2 = values.iterator(); it2.hasNext();) {
				aspects.add(it2.next());
			}
		}
		return aspects;
	}

	public static List getPackagesInModel() {
		List packages = new ArrayList();
		IHierarchy model =
			Ajde.getDefault().getStructureModelManager().getHierarchy();
		if (model.equals(IHierarchy.NO_STRUCTURE)) {
			return null;
		} else {
			// Fake the default package, add the root of the tree as the [default] package 
			Object[] o = new Object[2];
			o[0] = (IProgramElement)model.getRoot();
			o[1] = "<default>";
			packages.add(o);

			return getPackagesHelper(
				(IProgramElement) model.getRoot(),
				IProgramElement.Kind.PACKAGE,
				null,
				packages);
		}
	}

	private static List getPackagesHelper(
		IProgramElement node,
		IProgramElement.Kind kind,
		String prename,
		List matches) {

		if (kind == null || node.getKind().equals(kind)) {
			if (prename == null) {
				prename = new String(node.toString());
			} else {
				prename = new String(prename + "." + node);
			}
			Object[] o = new Object[2];
			o[0] = node;
			o[1] = prename;

			matches.add(o);
		}

		if (node.getChildren() != null) {
			for (Iterator it = node.getChildren().iterator(); it.hasNext();) {
				IProgramElement nextNode = (IProgramElement) it.next();
				getPackagesHelper(
					(IProgramElement) nextNode,
					kind,
					prename,
					matches);
			}
		}
		
		return matches;
	}

	/**
	 * This method sets the current project and initialises AJDE
	 */
	public static void initialiseAJDE(IProject withProject) {
	
		String configFile = AspectJUIPlugin.getBuildConfigurationFile(withProject);
		if (!configFile.equals(lastLoadedConfigFile)) {
			AJDTEventTrace.generalEvent("initialiseAJDE: switching configs - from:"+lastLoadedConfigFile+" to:"+configFile);
			Ajde.getDefault().getConfigurationManager().setActiveConfigFile(
				configFile);
			lastLoadedConfigFile = configFile;
		}
	}
	
	/**
	 * Helper function sorts a list of resources into alphabetical order
	 */
	private List sortElements(List oldElements) {
		Object[] temp = oldElements.toArray();
		SortingComparator comparator = new SortingComparator();

		Arrays.sort(temp, comparator);

		List newResources = Arrays.asList(temp);

		return newResources;
	}

	private static List sortArray(List oldElements) {
		Object[] temp = oldElements.toArray();
		SortArrayComparator comparator = new SortArrayComparator();

		Arrays.sort(temp, comparator);
		
		List newElements = Arrays.asList(temp);

		return newElements;
	}

	private class SortingComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			IProgramElement p1 = (IProgramElement) o1;
			IProgramElement p2 = (IProgramElement) o2;

			String name1 = p1.getName();
			String name2 = p2.getName();

			return name1.compareTo(name2);
		}
	}

	private static class SortArrayComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			Object[] array1 = (Object[]) o1;
			Object[] array2 = (Object[]) o2;

			IProgramElement p1 = (IProgramElement) array1[1];
			IProgramElement p2 = (IProgramElement) array2[1];

			String name1 = p1.getName();
			String name2 = p2.getName();

			return name1.compareTo(name2);
		}
	}

	/**
	 * @return		all of the AspectJ and Java source files in a package
	 */ 
	public static List getFilesInPackage(IProgramElement packageNode) {
		List packageContents;
		if (packageNode == null) {
			return null;
		} else {
			packageContents = packageNode.getChildren();	
		}
		List files = new ArrayList();
		if (packageContents != null) {
			for (Iterator it = packageContents.iterator(); it.hasNext(); ) {
				IProgramElement packageItem = (IProgramElement)it.next();
				if (packageItem.getKind() == IProgramElement.Kind.FILE_JAVA 
					|| packageItem.getKind() == IProgramElement.Kind.FILE_ASPECTJ) {
					files.add(packageItem);
				}
			} 
		}
		return files;
	}

}


