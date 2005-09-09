/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * This wizard presents two lists for the user.  On the left are the new resources
 * detected in the project.  On the right are the existing .lst files in the project.
 * A number of resources are selected on the left to be added to a number of .lst files
 * on the right.  ASCFIXME: Currently the IFiles for the .lst files are adjusted directly,
 * it would be better for an AJDE API to add/remove members from a model.
 * 
 * @author Andy Clement
 */
public class ResourceAddedWizard extends Wizard implements INewWizard {

	IWorkbench workbench;
	IStructuredSelection selection;
	
	// Single page within the wizard
	ResourceAddedPage mainPage;
	
	// New resources to be presented in the left hand list
	List newResourcesList;

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		setWindowTitle(AspectJUIPlugin.getResourceString("newResourcesWizard.wizardTitle")); //$NON-NLS-1$
		newResourcesList = null;
	}

	/**
	 * Called after init() to tell the wizard what to present in the left hand page.
	 */
	public void setNewResources(List newResources) {
		newResourcesList = newResources;
	}

    /**
     * Standard wizard method - it adds the single page to the wizard.
     */
	public void addPages() {
		mainPage = new ResourceAddedPage(newResourcesList);
		addPage(mainPage);
	}

	/**
	 * Called when 'finish' is clicked on the wizard panel, this method looks at the
	 * list of checked resources and the list of checked config files to update -
	 * it then adds each of the resources to the config files.
	 * 
	 */
	public boolean performFinish() {
		
		
		// Determine information filled in on the wizard page
		List resourcesToAdd = mainPage.getSelectedResources();
		List buildConfigFilesToUpdate = mainPage.getSelectedBuildConfigFiles();
		
		// Is there anything to do?
		if (buildConfigFilesToUpdate.size() == 0 || resourcesToAdd.size() == 0)
			return true;

		// Determine the project we are working with and the path to that project
		IProject project = ((IResource) resourcesToAdd.get(0)).getProject();
		IPath workspacePath = project.getLocation();
		workspacePath = workspacePath.removeLastSegments(1);

		// Build a stringbuffer which are the updates to apply to each .lst file

		StringBuffer newLines = new StringBuffer();
		
		Iterator resourceIterator = resourcesToAdd.iterator();
		while (resourceIterator.hasNext()) {
			IResource resourceFile = (IResource) resourceIterator.next();
			String resourcePath =
				workspacePath.toOSString() + resourceFile.getFullPath().toOSString();
			// .lst file entries in other .lst files need to be preceeded with a '@'
			if (resourcePath.endsWith(".lst")) //$NON-NLS-1$
				newLines.append("@"); //$NON-NLS-1$
			newLines.append(resourcePath);
			newLines.append("\n"); //$NON-NLS-1$
		}
		
		// Go through the list of config files and add that string buffer of new
		// data to each.
		Iterator configIterator = buildConfigFilesToUpdate.iterator();
		while (configIterator.hasNext()) {
			IResource configurationFile = (IResource) configIterator.next();
			IFile ifile = ((IFile) configurationFile);

			try {
				// Work out if the config file currently ends with a return character,
				// if it does not then one needs to be added.
				boolean trailingReturnRequired = true;

				InputStream is = ifile.getContents();
				byte[] inputdata = new byte[is.available()];
				is.read(inputdata);
				if (inputdata[inputdata.length - 1] == (new String("\n").getBytes()[0])) trailingReturnRequired = false; //$NON-NLS-1$

				// Build an appropriate input stream to pass to appendContents()
				ByteArrayInputStream bais = null;
				if (trailingReturnRequired)
					bais =
						new ByteArrayInputStream(new String("\n" + newLines.toString()).getBytes()); //$NON-NLS-1$
				else
					bais = new ByteArrayInputStream(newLines.toString().getBytes());

				// ASCFIXME - That first true ought to be a false (it says whether
				// to only update if the file system matches the IFile) - but for some
				// reason this code won't run if I dont say true (i.e. force an update
				// regardless) - I dunno whose touching the file in the meantime?

				ifile.appendContents(bais, true, false, null);
			} catch (Exception e) {
				AspectJUIPlugin.getDefault().getErrorHandler().handleError(
				AspectJUIPlugin.getResourceString("newResourcesWizard.exceptionAppendingToBuildConfigFile")+ //$NON-NLS-1$
				  ifile.getFullPath().toOSString(),e);
			}
		}
		return true;
	}

}