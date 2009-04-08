/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ben Dalziel     - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.tracing;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.ConsolePlugin;

/**
 * Prints crosscutting model to the StyledText
 * <p>
 * Clients may instantiate this class; this class is not intended to be
 * subclassed.
 * </p>
 * 
 * @since 3.0
 */
public class PrintCrossCuttingModelAction extends Action {

    private final static String toolTipText = "Prints the Crosscutting model of all AspectJ projects in the workspace to the log";

	public PrintCrossCuttingModelAction() {
		super("Print Crosscutting Model"); 
        setToolTipText(toolTipText); 
		setHoverImageDescriptor(AspectJImages.COMPARISON.getImageDescriptor());
		setDisabledImageDescriptor(AspectJImages.COMPARISON.getImageDescriptor());
		setImageDescriptor(AspectJImages.COMPARISON.getImageDescriptor());
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		BusyIndicator.showWhile(ConsolePlugin.getStandardDisplay(),
				new Runnable() {
					public void run() {
					    printCrossCuttingModel();
					}
				});
	}
	
	private void printCrossCuttingModel() {
	    IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	    AJLog.log("Printing crosscutting model for all AspectJ projects in the workspace");
	    for (int i = 0; i < allProjects.length; i++) {
	        IProject project = allProjects[i];
            if (AspectJPlugin.isAJProject(project)) {
                AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(project);
                AJLog.log("");
                AJLog.log("--------------------------------------");
                AJLog.log("Printing crosscutting model for " + project.getName());
                AJLog.log(model.getModelAsString());
                AJLog.log("--------------------------------------");
                AJLog.log("");
            }
        }
	}
	
   public void fillActionBars(IActionBars actionBars) {
        fillToolBar(actionBars.getToolBarManager());
        fillViewMenu(actionBars.getMenuManager());
    }
   public void fillViewMenu(IMenuManager viewMenu) {
   }
   

   private void fillToolBar(IToolBarManager tooBar) {
       Action showDialogAction = new PrintCrossCuttingModelAction();
       showDialogAction.setToolTipText(toolTipText);
       tooBar.add(showDialogAction);

   }

}
