/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.core.tests.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.model.AJModel;
import org.eclipse.ajdt.core.model.AJRelationship;
import org.eclipse.ajdt.core.model.AJRelationshipManager;
import org.eclipse.ajdt.core.model.AJRelationshipType;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Tests the element map persistence funtionality in AJProjectModel
 */
public class AJModelPersistenceTest extends AJDTCoreTestCase {

	private static final String MODEL_FILE = ".elementMap";

	private String getFileName(IProject project) {
		return AspectJPlugin.getDefault().getStateLocation().append(
				project.getName() + MODEL_FILE).toOSString();
	}

	/**
	 * @throws Exception
	 */
	public void testReloadingModel() throws Exception {
		IProject project = createPredefinedProject("TJP Example");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] {
					AJRelationshipManager.ADVISED_BY,
					AJRelationshipManager.ADVISES };
			compareAfterReloadingModel(rels, project);
		} finally {
			deleteProject(project);
		}
	}

	public void testReloadingModel2() throws Exception {
		IProject project = createPredefinedProject("Bean Example");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] {
					AJRelationshipManager.ADVISED_BY,
					AJRelationshipManager.ADVISES,
					AJRelationshipManager.DECLARED_ON,
					AJRelationshipManager.ASPECT_DECLARATIONS };
			compareAfterReloadingModel(rels, project);
		} finally {
			deleteProject(project);
		}
	}

	public void testReloadingModel3() throws Exception {
		IProject project = createPredefinedProject("MarkersTest");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] {
					AJRelationshipManager.ADVISED_BY,
					AJRelationshipManager.ADVISES,
					AJRelationshipManager.DECLARED_ON,
					AJRelationshipManager.ASPECT_DECLARATIONS,
					AJRelationshipManager.MATCHED_BY,
					AJRelationshipManager.MATCHES_DECLARE };
			compareAfterReloadingModel(rels, project);
		} finally {
			deleteProject(project);
		}
	}

	public void testReloadingModel4() throws Exception {
		IProject project = createPredefinedProject("Spacewar Example");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] {
					AJRelationshipManager.ADVISED_BY,
					AJRelationshipManager.ADVISES,
					AJRelationshipManager.DECLARED_ON,
					AJRelationshipManager.ASPECT_DECLARATIONS,
					AJRelationshipManager.MATCHED_BY,
					AJRelationshipManager.MATCHES_DECLARE };
			compareAfterReloadingModel(rels, project);
		} finally {
			deleteProject(project);
		}
	}

	public void testReloadingModel5() throws Exception {
		IProject libProject = createPredefinedProject("MyAspectLibrary");
		IProject weaveMeProject = createPredefinedProject("WeaveMe");
		try {
			AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
			compareAfterReloadingModel(rels, weaveMeProject);
		} finally {
			deleteProject(weaveMeProject);
			deleteProject(libProject);
		}
	}

	// can be used to read the contents of an element map file
	public static void main(String[] args) {
		new AJModelPersistenceTest().readModelFile(new File(args[0]));
	}

	private void readModelFile(File f) {
		try {
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fis);
			int version = ois.readInt();
			System.out.println("====");
			System.out.println("loading model version: " + version);
			if (version == 101) {
				int numElements = ois.readInt();
				System.out.println("numElements: " + numElements);

				for (int i = 0; i < numElements; i++) {
					String handleIdentifier = (String) ois.readObject();
					System.out.println("handle: " + handleIdentifier);
					String linkName = (String) ois.readObject();
					System.out.println("linkName: " + linkName);
					Integer lineNum = new Integer(ois.readInt());
					System.out.println("lineNum:" + lineNum);
				}

				int numRelTypes = ois.readInt();
				
				System.out.println("====");
				System.out.println("num rel types=" + numRelTypes);

				for (int i = 0; i < numRelTypes; i++) {
					int relType = ois.readInt();
					int numRels = ois.readInt();
					System.out.println(numRels+" rels of type: "+relType);
					for (int j = 0; j < numRels; j++) {
						int sourceID = ois.readInt();
						System.out.print("source: "+sourceID+" targets: ");
						int numTargets = ois.readInt();
						for (int k = 0; k < numTargets; k++) {
							int targetID = ois.readInt();
							System.out.print(targetID+" ");
						}
						System.out.println();
					}
				}

				System.out.println("====");
				
				int numParents = ois.readInt();
				System.out.println("num parents: " + numParents);
				for (int i = 0; i < numParents; i++) {
					String parentHandle = (String)ois.readObject();
					System.out.println("parent: " + parentHandle);
					int numChildren = ois.readInt();
					System.out.print("extra children: ");
					for (int j = 0; j < numChildren; j++) {
						int childID = ois.readInt();
						System.out.print(childID+" ");
					}
					System.out.println();
				}
				
				System.out.println("====");
				System.out.println("end of model");
			} else {
				System.out.println("unknown model version: " + version);
			}
			ois.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void compareAfterReloadingModel(AJRelationshipType[] rels,
			IProject project) throws IOException {
		List allRels = AJModel.getInstance().getAllRelationships(project, rels);

		List sourceList = new ArrayList(allRels.size());
		List targetList = new ArrayList(allRels.size());
		List relationList = new ArrayList(allRels.size());

		for (Iterator iter = allRels.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			IJavaElement source = rel.getSource();
			// System.out.println("source: " + source.getHandleIdentifier());
			sourceList.add(source.getHandleIdentifier().intern());
			IJavaElement target = rel.getTarget();
			// System.out.println("target: " + target.getHandleIdentifier());
			targetList.add(target.getHandleIdentifier().intern());
			relationList.add(rel.getRelationship().getInternalName().intern());
		}
		AJModel.getInstance().saveModel(project);
		File modelFile = new File(getFileName(project));
		assertTrue(
				"Serialized project model file does not exist: " + modelFile,
				modelFile.exists());
		assertTrue("Serialized project model file should not be empty: "
				+ modelFile, modelFile.length() > 0);

		AJModel.getInstance().clearMap(project);

		// we have to move the saved file out of the way, to make sure the
		// model really is empty, otherwise the getAllRelationships call
		// would cause the model to be loaded
		File tmpFile = new File(modelFile.getPath() + ".tmp");
		copy(modelFile, tmpFile);
		modelFile.delete();

		assertTrue("Failed to create temporary model file", tmpFile.exists());
		assertTrue("Failed to delete model file", !modelFile.exists());

		allRels = AJModel.getInstance().getAllRelationships(project, rels);
		assertTrue("Model should be empty after saving and clearing",
				(allRels == null) || (allRels.size() == 0));

		AJModel.getInstance().clearMap(project);

		// copy back to model file
		copy(tmpFile, modelFile);
		tmpFile.delete();

		assertTrue("Failed to restore model file", modelFile.exists());

		// now we ask for relationships again, but this time the persisted model
		// should be detected and loaded
		allRels = AJModel.getInstance().getAllRelationships(project, rels);
		assertTrue(
				"Model should NOT be empty - persisted model should have been loaded",
				allRels.size() > 0);
		for (Iterator iter = allRels.iterator(); iter.hasNext();) {
			AJRelationship rel = (AJRelationship) iter.next();
			String sourceHandle = rel.getSource().getHandleIdentifier()
					.intern();
			// System.out.println("source: " + sourceHandle);
			String targetHandle = rel.getTarget().getHandleIdentifier()
					.intern();
			String relName = rel.getRelationship().getInternalName().intern();

			boolean found = false;
			for (int i = 0; !found && i < sourceList.size(); i++) {
				Object obj = sourceList.get(i);
				if (obj != null) {
					String s = (String) obj;
					if (s.equals(sourceHandle)) {
						// System.out.println("found sourceHandle at index: " +
						// i);
						String t = (String) targetList.get(i);
						String r = (String) relationList.get(i);
						if (t.equals(targetHandle) && (r.equals(relName))) {
							// System.out.println("found match: "+t);
							found = true;
							// remove match from list
							sourceList.set(i, null);
							targetList.set(i, null);
							relationList.set(i, null);
						}
					}
				}
			}
			assertTrue("Didn't find matching source and target", found);
		}

		String missed = "";
		// check for missing entries
		for (int i = 0; i < sourceList.size(); i++) {
			Object obj = sourceList.get(i);
			if (obj != null) {
				missed += (String) obj + " ";
			}
		}
		assertTrue("Missing elements in reloaded model: " + missed, missed
				.length() == 0);
	}
}