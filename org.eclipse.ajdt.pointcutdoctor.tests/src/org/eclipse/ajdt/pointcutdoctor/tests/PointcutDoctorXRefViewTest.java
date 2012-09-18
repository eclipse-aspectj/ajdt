/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.tests;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.pointcutdoctor.ui.AlmostMatchedJPSProvider;
import org.eclipse.ajdt.pointcutdoctor.ui.PointcutDoctorUIPlugin;
import org.eclipse.ajdt.pointcutdoctor.ui.ShadowNode;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.contribution.xref.core.IXReference;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;


public class PointcutDoctorXRefViewTest extends UITestCase {
	
	private static int testNum = 0;

	/**
	 * To represent expected crossref result
	 */
	private static class XRef {
		private String relName;
		private List<String> children;
		public XRef(String name, String[] children) {
			this.relName = name;
			this.children = new ArrayList<String>();
			this.children.addAll(Arrays.asList(children));
		}
	}
	
	private XRef ref(String name, String... children) {
		return new XRef(name, children);
	}
	
	public class TargetInfo {

		private ICompilationUnit cu;
		private int offset;
		private IJavaElement element;

		public TargetInfo(ICompilationUnit cu, int offset) throws JavaModelException {
			this.cu = cu;
			this.offset = offset;
			this.element = cu.getElementAt(offset);
			if (element==null) {
			    fail("Target element not found");
			}
		}
		
		@Override
		public String toString() {
			if (element!=null)
				return element.toString();
			else 
				return "TargetInfo("+cu.toString()+" @ "+offset+")";
		}
	}

	private static final String TARGET_MARKER = "/*XXX*/";

	private PointcutDoctorUIPlugin doctor;
	private IProject project;
	private final String projectName = "DefaultEmptyProject";
	private IJavaProject javaProject;
	private AlmostMatchedJPSProvider xrefProvider = new AlmostMatchedJPSProvider();
	
	private TargetInfo target = null;

	public void setUp() throws Exception {
		testNum++;
		setAutobuilding(false);
		project = createPredefinedProject(projectName);
		javaProject = JavaCore.create(project);
		doctor = PointcutDoctorUIPlugin.getDefault();
		if (testNum==1) { 
			// When we first start...
			assertFalse(doctor.isEnabled());
		}
		doctor.setEnabled(true);
	}
	
	public void testScaffolding() throws Exception {
		// When the first tests runs...
		assertTrue(project.exists());
		assertTrue(javaProject.exists());
		
		createSourceFile("my.aspects", "MyAspect.aj", 
				"package my.aspects;\n" +
				"public aspect MyAspect {\n" +
				"}");
		
		createSourceFile("my.classes", "MyClass.java", 
				"package my.classes;\n" +
				"public class MyClass {\n" +
				"}\n");
		
		assertTrue(getResource("DefaultEmptyProject/src/my/classes/MyClass.java").exists());
		assertTrue(getResource("DefaultEmptyProject/src/my/aspects/MyAspect.aj").exists());
		
		build();
	}
	
	public void testSimple() throws Exception {
		assertTrue(project.exists());
		assertTrue(javaProject.exists());
		
		createSourceFile("myaspects", "MyAspect.aj", 
				"package myaspects;\n" +
				"public aspect MyAspect {\n" +
				"    before() : execution(int m(*)) {/*XXX*/}\n" +
				"}");
		
		createSourceFile("myclasses", "MyClass.java", 
				"package myclasses;\n" +
				"public class MyClass {\n" +
				"    public void m() {}\n" +
				"    public int m(int x, int y) {return x;}\n" +
				"    public int m(int x) {return x;}\n" +
				"}\n");
		
		assertNotNull(target);
		assertNotNull(target.element);
		assertTrue(target.element instanceof AdviceElement);
		
		build();
		
		assertXRefs( 
				ref("almost advises",
						"int myclasses.MyClass.m(int, int)",
						"void myclasses.MyClass.m()"
						),
				ref("advise",
						"int myclasses.MyClass.m(int)"));
	}

	
	private void assertXRefs(XRef... _expectedRefs) {
		List<XRef> expectedRefs = new ArrayList<XRef>();
		expectedRefs.addAll(Arrays.asList(_expectedRefs));
		Collection<IXReference> refs = xrefProvider.getXReferences(target.element, null);
		for (IXReference ixReference : refs) {
			String actualName = ixReference.getName();
			List<String> expectedChildren = getExpectedChildren(expectedRefs, actualName);
			assertAssociates(actualName, expectedChildren, getActualRefs(actualName, refs));
		}
	}

	private void assertAssociates(String relName, List<String> expectedChildren, IXReference actualRef) {
		for (Iterator<IAdaptable> iter = actualRef.getAssociates(); iter.hasNext(); ) {
			String actualChild = ((ShadowNode) iter.next()).getLabel();
			assertTrue("Unexpected xRef "+relName+" >> "+actualChild, expectedChildren.contains(actualChild));
			expectedChildren.remove(actualChild);
		}
		assertTrue("Did not find "+relName+ " >> "+expectedChildren, expectedChildren.isEmpty());
	}

	private IXReference getActualRefs(String actualName, Collection<IXReference> refs) {
		for (IXReference ixReference : refs) {
			if (ixReference.getName().equals(actualName)) {
				return ixReference;
			}
		}
		fail("Did not find expected relationship: "+actualName);
		return null;
	}
	
	private List<String> getExpectedChildren(List<XRef> expectedRefs,
			String actualName) {
		for (XRef refNode : expectedRefs) {
			if (refNode.relName.equals(actualName)) {
				List<String> children = refNode.children;
				expectedRefs.remove(refNode);
				return children;
			}
		}
		fail("Unexpected relationship: "+actualName);
		return null; // unreachable
	}

	private void build() throws CoreException {
		buildProject(javaProject);
	}

	////////////////////// junk to make test writing a little easier ///////////////////////////////////////////
	
	private void createSourceFile(String pkgName, String cuName, String contents) throws UnsupportedEncodingException, CoreException {
		ICompilationUnit cu = createCompilationUnitAndPackage(pkgName, cuName, contents, javaProject);
		if (contents.contains(TARGET_MARKER)) {
			assertNull(target);
			target = new TargetInfo(cu, contents.indexOf(TARGET_MARKER));
		}
	}
	
	/**
	 * Get an IResource from a path String starting at the workspace root.
	 * <p>
	 * Different type of resource is returned based on the length of the path
	 * and whether or not it ends with a path separator.
	 * <p>
	 * For example
	 * 
	 * "" length = 0 => type of resource is IWorkspaceRoot "foo" length = 1 =>
	 * type of resource is IProject "foo/src/Foo.java" length > 1 and no
	 * trailing "/" => type is IFile
	 * "foo/src/          length > 1 and a trailing "/" => type is IFolder
	 */
	public static IResource getResource(String pathToFile) {
		return getResource(Path.ROOT.append(pathToFile));
	}

	/**
	 * Get an IResource indicated by a given path starting at the workspace
	 * root.
	 * <p>
	 * Different type of resource is returned based on the length of the path
	 * and whether or not it ends with a path separator.
	 */
	public static IResource getResource(IPath path) {
		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			if (path.segmentCount() == 0) {
				return root;
			}
			IProject project = root.getProject(path.segment(0));
			if (path.segmentCount() == 1) {
				return project;
			}
			if (path.hasTrailingSeparator()) {
				return root.getFolder(path);
			}
			else {
				return root.getFile(path);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
