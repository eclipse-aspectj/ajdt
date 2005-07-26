/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ben Dalziel     - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.internal.console.ConsoleMessages;
import org.eclipse.ui.internal.console.ConsolePluginImages;
import org.eclipse.ui.internal.console.IInternalConsoleConstants;

/**
 * Clears the output of text.
 * <p>
 * Clients may instantiate this class; this class is not intended to be
 * subclassed.
 * </p>
 * 
 * @since 3.0
 */
public class ClearEventTraceAction extends Action {

	private Text fText;

	/**
	 * Constructs a clear output action.
	 * 
	 * @since 3.1
	 */
	private ClearEventTraceAction() {
		super(ConsoleMessages.getString("ClearOutputAction.title")); //$NON-NLS-1$
		setToolTipText(ConsoleMessages.getString("ClearOutputAction.toolTipText")); //$NON-NLS-1$
		setHoverImageDescriptor(ConsolePluginImages
				.getImageDescriptor(IConsoleConstants.IMG_LCL_CLEAR));
		setDisabledImageDescriptor(ConsolePluginImages
				.getImageDescriptor(IInternalConsoleConstants.IMG_DLCL_CLEAR));
		setImageDescriptor(ConsolePluginImages
				.getImageDescriptor(IInternalConsoleConstants.IMG_ELCL_CLEAR));
		// Only implemented in AJDT for Eclipse 3.1
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
//				IConsoleHelpContextIds.CLEAR_CONSOLE_ACTION);
	}

	/**
	 * Constructs an action to clear the document associated with text.
	 */
	public ClearEventTraceAction(Text text) {
		this();
		fText = text;
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
						fText.setText("");
					}
				});
	}
}
