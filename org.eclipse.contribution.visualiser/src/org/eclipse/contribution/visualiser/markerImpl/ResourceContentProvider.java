/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.contribution.visualiser.markerImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.contribution.visualiser.VisualiserPlugin;
import org.eclipse.contribution.visualiser.core.ProviderManager;
import org.eclipse.contribution.visualiser.core.resources.VisualiserImages;
import org.eclipse.contribution.visualiser.interfaces.IGroup;
import org.eclipse.contribution.visualiser.interfaces.IMarkupProvider;
import org.eclipse.contribution.visualiser.interfaces.IMember;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleContentProvider;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleGroup;
import org.eclipse.contribution.visualiser.utils.JDTUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Content provider that listens to selections in the workspace and shows file resources as memebers
 * and folder resources as groups when a project or folder is selected.
 */
public class ResourceContentProvider extends SimpleContentProvider implements ISelectionListener {

	IResource selectedResource;

	private boolean updateNeeded;

	public void initialise() {
		if (VisualiserPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null) {
			VisualiserPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService().addSelectionListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.contribution.visualiser.simpleImpl.SimpleContentProvider#getAllMembers()
	 */
	public List<IMember> getAllMembers() {
		if (updateNeeded) {
			updateData();
			IMarkupProvider mProv = ProviderManager.getMarkupProvider();
			if (mProv instanceof MarkerMarkupProvider) {
				((MarkerMarkupProvider) mProv).updateMarkups(super.getAllGroups());
			}
		}
		return super.getAllMembers();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.contribution.visualiser.simpleImpl.SimpleContentProvider#getAllGroups()
	 */
	public List<IGroup> getAllGroups() {
		if (updateNeeded) {
			updateData();
			IMarkupProvider mProv = ProviderManager.getMarkupProvider();
			if (mProv instanceof MarkerMarkupProvider)
				((MarkerMarkupProvider) mProv).updateMarkups(super.getAllGroups());
		}
		return super.getAllGroups();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.contribution.visualiser.simpleImpl.SimpleContentProvider#getAllMembers(org.eclipse.contribution.visualiser.interfaces.IGroup)
	 */
	public List<IMember> getAllMembers(IGroup group) {
		if (updateNeeded) {
			updateData();
			IMarkupProvider mProv = ProviderManager.getMarkupProvider();
			if (mProv instanceof MarkerMarkupProvider)
				((MarkerMarkupProvider) mProv).updateMarkups(super.getAllGroups());
		}
		return super.getAllMembers(group);
	}

	/**
	 * Workbench selection has changed
	 *
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!(ProviderManager.getContentProvider().equals(this)))
			return;
		boolean updateRequired = false;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object o = structuredSelection.getFirstElement();

			if (o != null) {
				if (o instanceof IResource) {
					IResource r = (IResource) o;
					if (selectedResource != r) { //Fix for bug 80920 - test to see whether or not the selection has *actually* changed.
						selectedResource = r;
						updateRequired = true;
					}
				}
				else if (o instanceof IJavaElement) {
					try {
						IResource r = ((IJavaElement) o).getCorrespondingResource();
						if (selectedResource != r) { //Fix for bug 80920 - test to see whether or not the selection has *actually* changed.
							selectedResource = r;
							updateRequired = true;
						}
					}
					catch (JavaModelException jme) {
						jme.printStackTrace();
					}
				}
			}
		}
		if (updateRequired && selectedResource != null) {
			updateNeeded = true;
			VisualiserPlugin.refresh();
		}
	}

	/**
	 * Update the data
	 */
	private void updateData() {
		if (selectedResource instanceof IContainer) {
			resetModel();
			IResource[] children;
			try {
				children = ((IContainer) selectedResource).members();

				boolean membersAreContainers = false;
				for (IResource iResource : children) {
					if (iResource instanceof IContainer) {
						membersAreContainers = true;
					}
				}
				if (!membersAreContainers) {
					IGroup group = new SimpleGroup(selectedResource.getName());
					for (IResource resource : children) {
						createNewMember(group, resource);
					}
					addGroup(group);
				}
				else {
					for (IResource child : children) {
						if (child instanceof IContainer) {
							IGroup group = new SimpleGroup(child.getName());
							addChildrenRecursively(group, (IContainer) child);
							addGroup(group);
						}
					}
				}
			}
			catch (CoreException e) {
				e.printStackTrace();
			}
		}
		else if (selectedResource instanceof IFile) {
			try {
				resetModel();
				IGroup group = new SimpleGroup(selectedResource.getParent().getName());
				createNewMember(group, selectedResource);
				addGroup(group);
			}
			catch (CoreException ce) {
				ce.printStackTrace();
			}
		}
		updateNeeded = false;
	}

	/**
	 * @param group
	 * @param container
	 */
	private void addChildrenRecursively(IGroup group, IContainer container) {
		try {
			IResource[] children = container.members();
			for (IResource resource : children) {
				if (resource instanceof IFile) {
					createNewMember(group, resource);
				}
				else if (resource instanceof IContainer) {
					addChildrenRecursively(group, (IContainer) resource);
				}
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param group
	 * @param resource
	 * @throws CoreException
	 */
	private void createNewMember(IGroup group, IResource resource) throws CoreException {
		if (resource instanceof IFile) {
			int length = 0;
			IMember member = new ResourceMember(resource.getName(), resource);
			BufferedReader in = new BufferedReader(new InputStreamReader(((IFile) resource).getContents()));
			try {
				while (in.readLine() != null) {
					length++;
				}
				member.setSize(length);
				group.add(member);
				in.close();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Process a mouse click on a member belonging to this provider.  This implemetation
	 * opens the associated resource in the editor.
	 *
	 * @see org.eclipse.contribution.visualiser.interfaces.IContentProvider#processMouseclick(IMember, boolean, int)
	 */
	public boolean processMouseclick(IMember member, boolean markupWasClicked, int buttonClicked) {
		if (buttonClicked == 1 && !markupWasClicked && member instanceof ResourceMember) {
			JDTUtils.openInEditor(((ResourceMember) member).getResource(), 0);
			return false;
		}
		return true;
	}

	/**
	 * @see org.eclipse.contribution.visualiser.interfaces.IContentProvider#getMemberViewIcon()
	 */
	public ImageDescriptor getMemberViewIcon() {
		return VisualiserImages.FILE_VIEW;
	}

	/**
	 * @see org.eclipse.contribution.visualiser.interfaces.IContentProvider#getGroupViewIcon()
	 */
	public ImageDescriptor getGroupViewIcon() {
		return VisualiserImages.FOLDER_VIEW;
	}

}
