/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.model;

import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.IRelationship;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;


/**
 * testing a class that is no longer being used.  keep but, can 
 * probably delete the commented out methods
 */
public class BinaryWeavingSupportTest extends AJDTCoreTestCase {

//	public void testFindProjectForPath() throws Exception {
//		IProject project = createPredefinedProject14("MyAspectLibrary"); //$NON-NLS-1$
//		IPath base = project.getLocation();
//		IPath loc = base.append("aspects.jar"); //$NON-NLS-1$
//
//		IProject foundProject = BinaryWeavingSupportDELETEME.findProjectForPath(loc);
//		assertNotNull("Didn't find project for path: " + loc, foundProject); //$NON-NLS-1$
//		assertEquals("Didn't find correct project for path: " + loc, project, //$NON-NLS-1$
//				foundProject);
//
//		loc = base.append("bin"); //$NON-NLS-1$
//
//		foundProject = BinaryWeavingSupportDELETEME.findProjectForPath(loc);
//		assertNotNull("Didn't find project for path: " + loc, foundProject); //$NON-NLS-1$
//		assertEquals("Didn't find correct project for path: " + loc, project, //$NON-NLS-1$
//				foundProject);
//	}
//
//	public void testFindSourceFolderResource() throws Exception {
//		IProject project = createPredefinedProject14("MyAspectLibrary"); //$NON-NLS-1$
//		IResource res = BinaryWeavingSupportDELETEME.findSourceFolderResource(project,
//				"bar", "MyBar.aj"); //$NON-NLS-1$ //$NON-NLS-2$
//		assertNotNull("Didn't find source folder resource", res); //$NON-NLS-1$
//		String path = res.getFullPath().toPortableString();
//		assertEquals("Incorrect path for MyBar.aj", //$NON-NLS-1$
//				"/MyAspectLibrary/src/bar/MyBar.aj", path); //$NON-NLS-1$
//	}
//
//	public void testFindSourceFolderResourceNoSrcFolder() throws Exception {
//		IProject project = createPredefinedProject("bug153682"); //$NON-NLS-1$
//		IResource res = BinaryWeavingSupportDELETEME.findSourceFolderResource(project,
//				"foo", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$
//		assertNotNull("Didn't find source folder resource: ", res); //$NON-NLS-1$
//		String path = res.getFullPath().toPortableString();
//		assertEquals("Incorrect path for Test.java", //$NON-NLS-1$
//				"/bug153682/foo/Test.java", path); //$NON-NLS-1$
//	}

//	public void testFindSourceFolderResourceMultipleSrcFolders()
//			throws Exception {
//		IProject project = createPredefinedProject("MultipleOutputFolders"); //$NON-NLS-1$
//		IResource res = BinaryWeavingSupportDELETEME.findSourceFolderResource(project,
//				"p1", "Class1.java"); //$NON-NLS-1$ //$NON-NLS-2$
//		assertNotNull("Didn't find source folder resource: ", res); //$NON-NLS-1$
//		String path = res.getFullPath().toPortableString();
//		assertEquals("Incorrect path for Class1.java", //$NON-NLS-1$
//				"/MultipleOutputFolders/src/p1/Class1.java", path); //$NON-NLS-1$
//
//		res = BinaryWeavingSupportDELETEME.findSourceFolderResource(project,
//				"p2", "GetInfo.aj"); //$NON-NLS-1$ //$NON-NLS-2$
//		assertNotNull("Didn't find source folder resource: ", res); //$NON-NLS-1$
//		path = res.getFullPath().toPortableString();
//		assertEquals("Incorrect path for GetInfo.aj", //$NON-NLS-1$
//				"/MultipleOutputFolders/src2/p2/GetInfo.aj", path); //$NON-NLS-1$
//	}
//
//	public void testFindSourceFolderResourcePackages() throws Exception {
//		IProject project = createPredefinedProject("AJProject83082"); //$NON-NLS-1$
//		IResource res = BinaryWeavingSupportDELETEME.findSourceFolderResource(project,
//				"wpstest.aspectj", "A.aj"); //$NON-NLS-1$ //$NON-NLS-2$
//		assertNotNull("Didn't find source folder resource: ", res); //$NON-NLS-1$
//		String path = res.getFullPath().toPortableString();
//		assertEquals("Incorrect path for A.aj", //$NON-NLS-1$
//				"/AJProject83082/src/wpstest/aspectj/A.aj", path); //$NON-NLS-1$
//	}

//	public void testFindSourceFolderResourceDefaultPackage() throws Exception {
//		IProject project = createPredefinedProject("MyAspectLibraryDefaultPackage"); //$NON-NLS-1$
//		IResource res = BinaryWeavingSupportDELETEME.findSourceFolderResource(project,
//				"", "MyBar.aj"); //$NON-NLS-1$ //$NON-NLS-2$
//		assertNotNull("Didn't find source folder resource: ", res); //$NON-NLS-1$
//		String path = res.getFullPath().toPortableString();
//		assertEquals("Incorrect path for MyBar.aj", //$NON-NLS-1$
//				"/MyAspectLibraryDefaultPackage/src/MyBar.aj", path); //$NON-NLS-1$
//	}

//	public void testFindElementAtLine() throws Exception {
//		IProject project = createPredefinedProject14("MyAspectLibrary"); //$NON-NLS-1$
//		IResource aj = project.findMember("src/bar/MyBar.aj"); //$NON-NLS-1$
//		assertNotNull("Couldn't find MyBar.aj file", aj); //$NON-NLS-1$
//		AJCompilationUnit ajcu = AJCompilationUnitManager.INSTANCE
//				.getAJCompilationUnit((IFile) aj);
//		assertNotNull("Couldn't find AJCompilationUnit for file " //$NON-NLS-1$
//				+ aj, ajcu);
//		Class[] expected = new Class[] { null, null, null,
//				IPackageDeclaration.class, null, AspectElement.class,
//				AspectElement.class, AdviceElement.class, AdviceElement.class,
//				AspectElement.class, AspectElement.class, IMethod.class,
//				IMethod.class, AspectElement.class, null, null, null };
//		for (int i = 0; i < expected.length; i++) {
//			IJavaElement el = BinaryWeavingSupportDELETEME.findElementAtLine(ajcu, i);
//			if (expected[i] == null) {
//				assertNull("Expected null element at line " + i + " got: " //$NON-NLS-1$ //$NON-NLS-2$
//						+ el, el);
//			} else {
//				assertTrue("Expected instance of " + expected[i] + " got: " //$NON-NLS-1$ //$NON-NLS-2$
//						+ el.getClass(), expected[i].isInstance(el));
//			}
//		}
//	}

	public void testAspectPathDirWeaving() throws Exception {
		/*IProject libProject = */createPredefinedProject("MyAspectLibrary2"); //$NON-NLS-1$
//		AJProjectModelFacade libModel = AJProjectModelFactory.getInstance().getModelForProject(libProject);
        
		IProject weaveMeProject = createPredefinedProject("WeaveMe2"); //$NON-NLS-1$
        AJProjectModelFacade weaveMeModel = AJProjectModelFactory.getInstance().getModelForProject(weaveMeProject);
        
        AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
		List allRels = weaveMeModel.getRelationshipsForProject(rels);
		IJavaElement mainEl = null;
		for (Iterator iter = allRels.iterator(); (mainEl == null)
				&& iter.hasNext();) {
			IRelationship rel = (IRelationship) iter.next();
			IJavaElement source = weaveMeModel.programElementToJavaElement(rel.getSourceHandle());
			if (source.getElementName().equals("main")) { //$NON-NLS-1$
				mainEl = source;
			}
		}
		assertNotNull("Didn't find element for advised main method", mainEl); //$NON-NLS-1$
		List related = weaveMeModel.getRelationshipsForElement(
		        mainEl, AJRelationshipManager.ADVISED_BY);
		assertNotNull("getRelatedElements returned null", related); //$NON-NLS-1$
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		for (Iterator iter = related.iterator(); iter.hasNext();) {
			IJavaElement el = (IJavaElement) iter.next();
			String elName = el.getElementName();
			String resName = el.getResource().getName();
			if (elName.equals("before")) { //$NON-NLS-1$
				found1 = true;
				assertEquals(
						"Found before element in wrong file", "MyBar.aj", resName); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (elName.equals("afterReturning")) { //$NON-NLS-1$
				found2 = true;
				assertEquals(
						"Found before afterReturning in wrong file", "MyBar2.aj", resName); //$NON-NLS-1$ //$NON-NLS-2$
			} else if (elName.equals("around")) { //$NON-NLS-1$
				found3 = true;
				assertEquals(
						"Found before around in wrong file", "MyBar3.aj", resName); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		assertTrue("Didn't find advised by before() relationship", found1); //$NON-NLS-1$
		assertTrue(
				"Didn't find advised by afterReturning() relationship", found2); //$NON-NLS-1$
		assertTrue("Didn't find advised by around() relationship", found3); //$NON-NLS-1$

		// now look for added "advises" relationships in the aspect library
		// probject
		// XXX Not doing this any more
//		rels = new AJRelationshipType[] { AJRelationshipManager.ADVISES };
//		allRels = libModel.getRelationshipsForProject(rels);
//		found1 = false;
//		found2 = false;
//		found3 = false;
//		for (Iterator iter = allRels.iterator(); iter.hasNext();) {
//			IRelationship rel = (IRelationship) iter.next();
//			String sourceName = libModel.programElementToJavaElement(rel.getSourceHandle()).getElementName();
//			for (Iterator targetIter = rel.getTargets().iterator(); targetIter.hasNext(); ) {
//    			String targetName = libModel.programElementToJavaElement((String) targetIter.next()).getElementName();
//    			if (sourceName.equals("before")) { //$NON-NLS-1$
//    				found1 = true;
//    				assertEquals("Incorrect target name", "main", targetName); //$NON-NLS-1$ //$NON-NLS-2$
//    			} else if (sourceName.equals("afterReturning")) { //$NON-NLS-1$
//    				found2 = true;
//    				assertEquals("Incorrect target name", "main", targetName); //$NON-NLS-1$ //$NON-NLS-2$
//    			} else if (sourceName.equals("around")) { //$NON-NLS-1$
//    				found3 = true;
//    				assertEquals("Incorrect target name", "main", targetName); //$NON-NLS-1$ //$NON-NLS-2$
//    			}
//			}
//		}
//		assertTrue("Didn't find advises before() relationship", found1); //$NON-NLS-1$
//		assertTrue("Didn't find advises afterReturning() relationship", found2); //$NON-NLS-1$
//		assertTrue("Didn't find advises around() relationship", found3); //$NON-NLS-1$

		// there is no "getOtherProjectAllRelationships" any more
//		List otherProjectRels = model.getOtherProjectAllRelationships(rels);
//		found1 = false;
//		found2 = false;
//		found3 = false;
//		for (Iterator iter = otherProjectRels.iterator(); iter.hasNext();) {
//			AJRelationship rel = (AJRelationship) iter.next();
//			String sourceName = rel.getSource().getElementName();
//			String targetName = rel.getTarget().getElementName();
//			if (sourceName.equals("before")) { //$NON-NLS-1$
//				found1 = true;
//				assertEquals("Incorrect target name", "main", targetName); //$NON-NLS-1$ //$NON-NLS-2$
//			} else if (sourceName.equals("afterReturning")) { //$NON-NLS-1$
//				found2 = true;
//				assertEquals("Incorrect target name", "main", targetName); //$NON-NLS-1$ //$NON-NLS-2$
//			} else if (sourceName.equals("around")) { //$NON-NLS-1$
//				found3 = true;
//				assertEquals("Incorrect target name", "main", targetName); //$NON-NLS-1$ //$NON-NLS-2$
//			}
//		}
//		assertTrue("Didn't find advises before() relationship", found1); //$NON-NLS-1$
//		assertTrue("Didn't find advises afterReturning() relationship", found2); //$NON-NLS-1$
//		assertTrue("Didn't find advises around() relationship", found3); //$NON-NLS-1$
	}

	public void testBug160236() throws Exception {
		IProject project = createPredefinedProject("bug160236"); //$NON-NLS-1$
		AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(project);

		AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
		List allRels = model.getRelationshipsForProject(rels);
		boolean found1 = false;
		boolean found2 = false;
		boolean found3 = false;
		for (Iterator iter = allRels.iterator(); iter.hasNext();) {
			IRelationship rel = (IRelationship) iter.next();
			String sourceName = model.programElementToJavaElement(rel.getSourceHandle()).getElementName();
			if (sourceName.equals("Sample")) { //$NON-NLS-1$
			    for (Iterator targetIter = rel.getTargets().iterator(); targetIter.hasNext(); ) {
			        IJavaElement targetElt = model.programElementToJavaElement((String) targetIter.next());
    				if ((targetElt.getElementName().equals("afterReturning")) //$NON-NLS-1$
    						&& (targetElt.getParent().getElementName().equals("AbstractBeanConfigurerAspect"))) { //$NON-NLS-1$
    					found1 = true;
    				} else if ((targetElt.getElementName().equals("before")) //$NON-NLS-1$
                            && (targetElt.getParent().getElementName().equals("AbstractBeanConfigurerAspect"))) { //$NON-NLS-1$
    					found2 = true;
    				} else {
    					fail("Unexpected target found: " + targetElt.getHandleIdentifier()); //$NON-NLS-1$
    				}
			    }
			} else if (sourceName.equals("main")) { //$NON-NLS-1$
			    for (Iterator targetIter = rel.getTargets().iterator(); targetIter.hasNext(); ) {
			        IJavaElement targetElt = model.programElementToJavaElement((String) targetIter.next());
	                if ((targetElt.getElementName().equals("before")) //$NON-NLS-1$
	                        && (targetElt.getParent().getElementName()
								.equals("AnnotationBeanConfigurerAspect"))) { //$NON-NLS-1$
	                    found3 = true;
    				} else {
    					fail("Unexpected target found: " + targetElt.getHandleIdentifier()); //$NON-NLS-1$
    				}
			    }
			}
		}
		assertTrue(
				"Didn't find Sample advised by afterReturning() binary aspect relationship", found1); //$NON-NLS-1$
		assertTrue(
				"Didn't find Sample advised by before() binary aspect relationship", found2); //$NON-NLS-1$
		assertTrue(
				"Didn't find main advised by before() binary aspect relationship", found3); //$NON-NLS-1$

	}
}
