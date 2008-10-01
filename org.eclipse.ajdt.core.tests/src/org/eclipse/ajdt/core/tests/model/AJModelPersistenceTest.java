/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;

/**
 * Tests the element map persistence funtionality in AJProjectModel
 * 
 * Persistence has been turned off
 */
public class AJModelPersistenceTest extends AJDTCoreTestCase {

    public void testNothing() {
        // Yay!
    }
    
//	private static final String MODEL_FILE = ".elementMap"; //$NON-NLS-1$
//
//	private String getFileName(IProject project) {
//		return AspectJPlugin.getDefault().getStateLocation().append(
//				project.getName() + MODEL_FILE).toOSString();
//	}
//
//	/**
//	 * @throws Exception
//	 */
//	public void testReloadingModel() throws Exception {
//		IProject project = createPredefinedProject("TJP Example"); //$NON-NLS-1$
//		AJRelationshipType[] rels = new AJRelationshipType[] {
//				AJRelationshipManager.ADVISED_BY, AJRelationshipManager.ADVISES };
//		compareAfterReloadingModel(rels, project);
//	}
//
//	public void testReloadingModel2() throws Exception {
//		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
//		AJRelationshipType[] rels = new AJRelationshipType[] {
//				AJRelationshipManager.ADVISED_BY,
//				AJRelationshipManager.ADVISES,
//				AJRelationshipManager.DECLARED_ON,
//				AJRelationshipManager.ASPECT_DECLARATIONS };
//		compareAfterReloadingModel(rels, project);
//	}
//
//	public void testReloadingModel3() throws Exception {
//		IProject project = createPredefinedProject("MarkersTest"); //$NON-NLS-1$
//		AJRelationshipType[] rels = new AJRelationshipType[] {
//				AJRelationshipManager.ADVISED_BY,
//				AJRelationshipManager.ADVISES,
//				AJRelationshipManager.DECLARED_ON,
//				AJRelationshipManager.ASPECT_DECLARATIONS,
//				AJRelationshipManager.MATCHED_BY,
//				AJRelationshipManager.MATCHES_DECLARE };
//		compareAfterReloadingModel(rels, project);
//	}
//
//	public void testReloadingModel4() throws Exception {
//		IProject project = createPredefinedProject("Spacewar Example"); //$NON-NLS-1$
//		AJRelationshipType[] rels = new AJRelationshipType[] {
//				AJRelationshipManager.ADVISED_BY,
//				AJRelationshipManager.ADVISES,
//				AJRelationshipManager.DECLARED_ON,
//				AJRelationshipManager.ASPECT_DECLARATIONS,
//				AJRelationshipManager.MATCHED_BY,
//				AJRelationshipManager.MATCHES_DECLARE };
//		compareAfterReloadingModel(rels, project);
//	}
//
//	public void testReloadingModel5() throws Exception {
//		IProject libProject = (IProject) getWorkspaceRoot().findMember(
//				"MyAspectLibrary"); //$NON-NLS-1$
//		if (libProject == null) {
//			libProject = createPredefinedProject14("MyAspectLibrary"); //$NON-NLS-1$
//		}
//		IProject weaveMeProject = createPredefinedProject("WeaveMe"); //$NON-NLS-1$
//		AJRelationshipType[] rels = new AJRelationshipType[] { AJRelationshipManager.ADVISED_BY };
//		compareAfterReloadingModel(rels, weaveMeProject);
//	}
//
//	// Sian: added test for project with aspects in .java files
//	public void testReloadingModel6() throws Exception {
//		IProject project = createPredefinedProject("MarkersTestWithAspectsInJavaFiles"); //$NON-NLS-1$
//		AJRelationshipType[] rels = new AJRelationshipType[] {
//				AJRelationshipManager.ADVISED_BY,
//				AJRelationshipManager.ADVISES,
//				AJRelationshipManager.DECLARED_ON,
//				AJRelationshipManager.ASPECT_DECLARATIONS,
//				AJRelationshipManager.MATCHED_BY,
//				AJRelationshipManager.MATCHES_DECLARE };
//		compareAfterReloadingModel(rels, project);
//	}
//
//	public void testReloadingModelBinaryWeaving() throws Exception {
//		if (!BinaryWeavingSupport.isActive) {
//			return;
//		}
//		IProject libProject = createPredefinedProject("MyAspectLibrary2"); //$NON-NLS-1$
//		IProject weaveMeProject = createPredefinedProject("WeaveMe2"); //$NON-NLS-1$
//		AJRelationshipType[] rels = new AJRelationshipType[] {
//				AJRelationshipManager.ADVISED_BY,
//				AJRelationshipManager.ADVISES,
//				AJRelationshipManager.DECLARED_ON,
//				AJRelationshipManager.ASPECT_DECLARATIONS,
//				AJRelationshipManager.MATCHED_BY,
//				AJRelationshipManager.MATCHES_DECLARE };
//		compareAfterReloadingModel(rels, libProject, weaveMeProject);
//	}
//
//	public void testLoadingModelFromFile() throws Exception {
//		IProject project = createPredefinedProject("Spacewar Example"); //$NON-NLS-1$
//		IProject project2 = null;
//		IPath ajmap = null;
//
//		try {
//			AJRelationshipType[] rels = new AJRelationshipType[] {
//					AJRelationshipManager.ADVISED_BY,
//					AJRelationshipManager.ADVISES,
//					AJRelationshipManager.DECLARED_ON,
//					AJRelationshipManager.ASPECT_DECLARATIONS,
//					AJRelationshipManager.MATCHED_BY,
//					AJRelationshipManager.MATCHES_DECLARE };
//			IResource res = project.findMember("Spacewar Example.ajmap"); //$NON-NLS-1$
//			assertNotNull("Couldn't find ajmap file", res); //$NON-NLS-1$
//
//			// copy ajmap file somewhere safe
//			ajmap = AspectJPlugin.getDefault().getStateLocation().append(
//					"test.ajmap"); //$NON-NLS-1$
//			copy(res.getLocation().toFile(), ajmap.toFile());
//
//			// delete project to clear model
//			deleteProject(project,true);
//
//			// make sure project model is clear
//			IProjectModel model = AJModel.getInstance().getModelForProject(
//					project);
//			assertNull("Project model should be null", model); //$NON-NLS-1$
//			List allRels = AJModel.getInstance().getAllRelationships(project,
//					rels);
//			if (allRels != null && allRels.size() > 0) {
//				fail("Deleted project should have no relationships"); //$NON-NLS-1$
//			}
//
//			// now test loading model from map file
//			// need to have a project in order to load model, doesn't matter
//			// which one
//			project2 = createPredefinedProject("TJP Example"); //$NON-NLS-1$
//			model = ProjectModelFactory.createProjectModel(project2);
//			boolean success = model.loadModel(ajmap);
//			assertTrue("Failed to load model from file: " + ajmap, success); //$NON-NLS-1$
//			allRels = model.getAllRelationships(rels);
//			assertNotNull(
//					"Loaded model should have non-null relationship list", //$NON-NLS-1$
//					allRels);
//			assertTrue("Loaded model should have non-empty relationship list", //$NON-NLS-1$
//					allRels.size() > 0);
//		} finally {
//			if (ajmap != null) {
//				ajmap.toFile().delete();
//			}
//		}
//	}
//
//	public void testModelFileDeletion() throws Exception {
//		IProject project = createPredefinedProject("Bean Example"); //$NON-NLS-1$
//		AJModel.getInstance().saveModel(project);
//		File modelFile = new File(getFileName(project));
//		assertTrue("File has not been saved", modelFile.exists()); //$NON-NLS-1$
//
//		AJModel.getInstance().getModelForProject(project).deleteModelFile();
//
//		// Check that the file has been deleted
//		assertFalse("File has not been deleted", modelFile.exists()); //$NON-NLS-1$
//	}
//
//	// can be used to read the contents of an element map file
//	public static void main(String[] args) {
//		new AJModelPersistenceTest().readModelFile(new File(args[0]));
//	}
//
//	private void readModelFile(File f) {
//		try {
//			FileInputStream fis = new FileInputStream(f);
//			ObjectInputStream ois = new ObjectInputStream(fis);
//			int version = ois.readInt();
//			System.out.println("===="); //$NON-NLS-1$
//			System.out.println("loading model version: " + version); //$NON-NLS-1$
//			if (version == 103) {
//				int numElements = ois.readInt();
//				System.out.println("numElements: " + numElements); //$NON-NLS-1$
//
//				for (int i = 0; i < numElements; i++) {
//					String handleIdentifier = (String) ois.readObject();
//					System.out.println("handle: " + handleIdentifier); //$NON-NLS-1$
//					String linkName = (String) ois.readObject();
//					System.out.println("linkName: " + linkName); //$NON-NLS-1$
//					Integer lineNum = new Integer(ois.readInt());
//					System.out.println("lineNum:" + lineNum); //$NON-NLS-1$
//				}
//
//				int numRelTypes = ois.readInt();
//
//				System.out.println("===="); //$NON-NLS-1$
//				System.out.println("num rel types=" + numRelTypes); //$NON-NLS-1$
//
//				for (int i = 0; i < numRelTypes; i++) {
//					int relType = ois.readInt();
//					int numRels = ois.readInt();
//					System.out.println(numRels + " rels of type: " + relType); //$NON-NLS-1$
//					for (int j = 0; j < numRels; j++) {
//						int sourceID = ois.readInt();
//						System.out.print("source: " + sourceID + " targets: "); //$NON-NLS-1$ //$NON-NLS-2$
//						int numTargets = ois.readInt();
//						for (int k = 0; k < numTargets; k++) {
//							int targetID = ois.readInt();
//							System.out.print(targetID + " "); //$NON-NLS-1$
//						}
//						System.out.println();
//					}
//				}
//
//				System.out.println("===="); //$NON-NLS-1$
//
//				int numParents = ois.readInt();
//				System.out.println("num parents: " + numParents); //$NON-NLS-1$
//				for (int i = 0; i < numParents; i++) {
//					String parentHandle = (String) ois.readObject();
//					System.out.println("parent: " + parentHandle); //$NON-NLS-1$
//					int numChildren = ois.readInt();
//					System.out.print("extra children: "); //$NON-NLS-1$
//					for (int j = 0; j < numChildren; j++) {
//						int childID = ois.readInt();
//						System.out.print(childID + " "); //$NON-NLS-1$
//					}
//					System.out.println();
//				}
//
//				System.out.println("===="); //$NON-NLS-1$
//				System.out.println("end of model"); //$NON-NLS-1$
//			} else {
//				System.out.println("unknown model version: " + version); //$NON-NLS-1$
//			}
//			ois.close();
//			fis.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private void compareAfterReloadingModel(AJRelationshipType[] rels,
//			IProject project) throws IOException {
//		compareAfterReloadingModel(rels, project, null);
//	}
//
//	/*
//	 * Check that persistence restores relationships in the given project - if
//	 * project2 is non-null, the relationships from both projects are combined.
//	 */
//	private void compareAfterReloadingModel(AJRelationshipType[] rels,
//			IProject project1, IProject project2) throws IOException {
//		List allRels = AJModel.getInstance()
//				.getAllRelationships(project1, rels);
//		if (project2 != null) {
//			allRels.addAll(AJModel.getInstance().getAllRelationships(project2,
//					rels));
//		}
//
//		List sourceList = new ArrayList(allRels.size());
//		List targetList = new ArrayList(allRels.size());
//		List relationList = new ArrayList(allRels.size());
//
//		for (Iterator iter = allRels.iterator(); iter.hasNext();) {
//			AJRelationship rel = (AJRelationship) iter.next();
//			IJavaElement source = rel.getSource();
//			sourceList.add(source.getHandleIdentifier().intern());
//			IJavaElement target = rel.getTarget();
//			targetList.add(target.getHandleIdentifier().intern());
//			relationList.add(rel.getRelationship().getInternalName().intern());
//		}
//		AJModel.getInstance().saveModel(project1);
//		File modelFile = new File(getFileName(project1));
//		assertTrue(
//				"Serialized project model file does not exist: " + modelFile, //$NON-NLS-1$
//				modelFile.exists());
//		assertTrue("Serialized project model file should not be empty: " //$NON-NLS-1$
//				+ modelFile, modelFile.length() > 0);
//		File modelFile2 = null;
//		if (project2 != null) {
//			AJModel.getInstance().saveModel(project2);
//			modelFile2 = new File(getFileName(project2));
//			assertTrue(
//					"Serialized project model file does not exist: " + modelFile2, //$NON-NLS-1$
//					modelFile2.exists());
//			assertTrue("Serialized project model file should not be empty: " //$NON-NLS-1$
//					+ modelFile2, modelFile2.length() > 0);
//
//		}
//
//		// we have to move the saved file out of the way, to make sure the
//		// model really is empty, otherwise the getAllRelationships call
//		// would cause the model to be loaded
//		File tmpFile = new File(modelFile.getPath() + ".tmp"); //$NON-NLS-1$
//		copy(modelFile, tmpFile);
//		AJModel.getInstance().clearMap(project1, true);
//		assertTrue("Failed to create temporary model file", tmpFile.exists()); //$NON-NLS-1$
//		assertTrue("Failed to delete model file", !modelFile.exists()); //$NON-NLS-1$
//		File tmpFile2 = null;
//		if (project2 != null) {
//			tmpFile2 = new File(modelFile2.getPath() + ".tmp"); //$NON-NLS-1$
//			copy(modelFile2, tmpFile2);
//			AJModel.getInstance().clearMap(project2, true);
//			assertTrue(
//					"Failed to create temporary model file", tmpFile2.exists()); //$NON-NLS-1$
//			assertTrue("Failed to delete model file", !modelFile2.exists()); //$NON-NLS-1$	
//		}
//
//		allRels = AJModel.getInstance().getAllRelationships(project1, rels);
//		if (project2 != null) {
//			allRels.addAll(AJModel.getInstance().getAllRelationships(project2,
//					rels));
//		}
//		assertTrue("Model should be empty after saving and clearing", //$NON-NLS-1$
//				(allRels == null) || (allRels.size() == 0));
//
//		AJModel.getInstance().clearMap(project1, true);
//		// copy back to model file
//		copy(tmpFile, modelFile);
//		tmpFile.delete();
//		assertTrue("Failed to restore model file", modelFile.exists()); //$NON-NLS-1$
//		if (project2 != null) {
//			AJModel.getInstance().clearMap(project2, true);
//			// copy back to model file
//			copy(tmpFile2, modelFile2);
//			tmpFile2.delete();
//			assertTrue("Failed to restore model file", modelFile2.exists()); //$NON-NLS-1$			
//		}
//
//		// now we ask for relationships again, but this time the persisted model
//		// should be detected and loaded
//		allRels = AJModel.getInstance().getAllRelationships(project1, rels);
//		if (project2 != null) {
//			allRels.addAll(AJModel.getInstance().getAllRelationships(project2,
//					rels));
//		}
//
//		assertTrue(
//				"Model should NOT be empty - persisted model should have been loaded", //$NON-NLS-1$
//				allRels.size() > 0);
//		for (Iterator iter = allRels.iterator(); iter.hasNext();) {
//			AJRelationship rel = (AJRelationship) iter.next();
//			String sourceHandle = rel.getSource().getHandleIdentifier()
//					.intern();
//			String targetHandle = rel.getTarget().getHandleIdentifier()
//					.intern();
//			String relName = rel.getRelationship().getInternalName().intern();
//
//			boolean found = false;
//			for (int i = 0; !found && i < sourceList.size(); i++) {
//				Object obj = sourceList.get(i);
//				if (obj != null) {
//					String s = (String) obj;
//					if (s.equals(sourceHandle)) {
//						String t = (String) targetList.get(i);
//						String r = (String) relationList.get(i);
//						if (t.equals(targetHandle) && (r.equals(relName))) {
//							found = true;
//							// remove match from list
//							sourceList.set(i, null);
//							targetList.set(i, null);
//							relationList.set(i, null);
//						}
//					}
//				}
//			}
//			assertTrue("Didn't find matching source and target", found); //$NON-NLS-1$
//		}
//
//		String missed = ""; //$NON-NLS-1$
//		// check for missing entries
//		for (int i = 0; i < sourceList.size(); i++) {
//			Object obj = sourceList.get(i);
//			if (obj != null) {
//				missed += (String) obj + "\n"; //$NON-NLS-1$
//			}
//		}
//		assertTrue("Missing elements in reloaded model: " + missed, missed //$NON-NLS-1$
//				.length() == 0);
//	}
}