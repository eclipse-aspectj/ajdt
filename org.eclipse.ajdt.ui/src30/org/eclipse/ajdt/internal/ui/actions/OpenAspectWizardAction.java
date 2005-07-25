/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
           Adrian Colyer - initial version

**********************************************************************/
package org.eclipse.ajdt.internal.ui.actions;

import org.eclipse.ajdt.internal.ui.wizards.NewAspectWizard;
import org.eclipse.jdt.internal.ui.wizards.AbstractOpenWizardAction;
import org.eclipse.jface.wizard.Wizard;

/**
 * @author colyer
 * This action is called from the AJDT welcome page to launch the new
 * aspect wizard.
 */
public class OpenAspectWizardAction extends AbstractOpenWizardAction {

	/**
	 * @see org.eclipse.jdt.internal.ui.wizards.AbstractOpenWizardAction#createWizard()
	 */
	protected Wizard createWizard() {
		return new NewAspectWizard( );
	}


	protected boolean shouldAcceptElement(Object obj) { 
		return isOnBuildPath(obj) && !isInArchive(obj);
	}

}
