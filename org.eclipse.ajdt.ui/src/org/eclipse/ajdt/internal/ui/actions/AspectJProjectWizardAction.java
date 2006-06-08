/**********************************************************************
Copyright (c) 2002, 2006 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
           Adrian Colyer - initial version

**********************************************************************/
package org.eclipse.ajdt.internal.ui.actions;

import org.eclipse.ajdt.internal.ui.wizards.AspectJProjectWizard;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.actions.AbstractOpenWizardAction;
import org.eclipse.ui.INewWizard;

/**
 * This action is called from the AJDT welcome page and cheatsheet
 * to launch the new aspectj project wizard.
 */
public class AspectJProjectWizardAction extends AbstractOpenWizardAction {

	protected INewWizard createWizard() throws CoreException {
		return new AspectJProjectWizard();
	}

}
