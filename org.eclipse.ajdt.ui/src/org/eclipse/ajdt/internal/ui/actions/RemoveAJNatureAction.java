/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
Adrian Colyer, Andy Clement, Tracy Gardner - initial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.actions;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


/**
 * This action is driven from the popup menu of a project that has
 * the AspectJ nature. It removes the AJ nature and builder etc.,
 * reverting the project to its original form, or if the project
 * was intially created as an aspectJ project, it converts to a 
 * Java project.
 */
public class RemoveAJNatureAction implements IObjectActionDelegate {

	private Vector selected = new Vector();

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction arg0)
	{
		for (Iterator iter = selected.iterator(); iter.hasNext();)
		{
			IProject project = (IProject) iter.next();
			try
			{
				// Reove the AspectJ nature from the project and attempt
				// to update the build classpath by removing the aspectjrt.jar	
				AspectJUIPlugin.convertFromAspectJProject(project);
			}
			catch (CoreException e) {
			}
		}
	}

	/**
	 * From IActionDelegate - set the availability or otherwise of this
	 * action.
	 */
	public void selectionChanged(IAction action, ISelection sel) {
		selected.clear();
		boolean enable = true;
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			for (Iterator iter = selection.iterator(); iter.hasNext();) {
				Object object = iter.next();
				if (object instanceof IAdaptable) {
					IProject project = (IProject) ((IAdaptable)object).getAdapter(IProject.class);	
					if(project != null) {
						selected.add(project);
					} else {
						enable = false;
						break;
					}
		
				} else {
					enable = false;
					break;
				}
			}
			action.setEnabled(enable);
		}
	}

	/**
	 * From IObjectActionDelegate
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}