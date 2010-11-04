/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.contribution.jdt.preferences;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Andrew Eisenberg
 * @created Oct 1, 2009
 *
 */
public class AskToReindexJob extends UIJob {

    public AskToReindexJob() {
        super("Ask to reindex projects");
    }

    @Override
    public IStatus runInUIThread(IProgressMonitor monitor) {
        boolean res = MessageDialog.openQuestion(getShell(), "Reindex projects?", 
                "The JDT Weaving service has been enabled.\n" +
        		"Do you want to reindex affected projects now\n" +
        		"so that JDT can more effectively search your projects?");
        if (res) {
            new ReindexingJob().schedule();
        }
        // only ask once
        JDTWeavingPreferences.setAskToReindex(false);

        return Status.OK_STATUS;
    }

    private Shell getShell() {
        try {
            Display display = getDisplay();
            return display == null ?
                    Display.getCurrent().getActiveShell()
                    : display.getActiveShell();
        } catch (NullPointerException e) {
            return null;
        }
    }

}
