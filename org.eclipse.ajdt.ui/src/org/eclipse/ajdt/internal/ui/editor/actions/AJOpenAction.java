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
package org.eclipse.ajdt.internal.ui.editor.actions;

import java.util.Iterator;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.PointcutElement;
import org.eclipse.ajdt.core.javaelements.PointcutUtilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.IJavaStatusConstants;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.ActionMessages;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.actions.OpenActionUtil;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IEditorStatusLine;

/* copied from org.eclipse.jdt.ui.actions.OpenAction
 * changes marked // AspectJ change
 */
public class AJOpenAction extends SelectionDispatchAction {

	private JavaEditor fEditor;

	/**
	 * Creates a new <code>OpenAction</code>. The action requires that the
	 * selection provided by the site's selection provider is of type <code>
	 * org.eclipse.jface.viewers.IStructuredSelection</code>.
	 * 
	 * @param site
	 *            the site providing context information for this action
	 */
	public AJOpenAction(IWorkbenchSite site) {
		super(site);
		setText(ActionMessages.getString("OpenAction.label")); //$NON-NLS-1$
		setToolTipText(ActionMessages.getString("OpenAction.tooltip")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("OpenAction.description")); //$NON-NLS-1$		
		WorkbenchHelp.setHelp(this, IJavaHelpContextIds.OPEN_ACTION);
	}

	/**
	 * Note: This constructor is for internal use only. Clients should not call
	 * this constructor.
	 * 
	 * @param editor
	 *            the Java editor
	 */
	public AJOpenAction(JavaEditor editor) {
		this(editor.getEditorSite());
		fEditor = editor;
		setText(ActionMessages.getString("OpenAction.declaration.label")); //$NON-NLS-1$
		setEnabled(SelectionConverter.canOperateOn(fEditor));
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(ITextSelection selection) {
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void selectionChanged(IStructuredSelection selection) {
		setEnabled(checkEnabled(selection));
	}

	private boolean checkEnabled(IStructuredSelection selection) {
		if (selection.isEmpty())
			return false;
		for (Iterator iter = selection.iterator(); iter.hasNext();) {
			Object element = iter.next();
			if (element instanceof ISourceReference)
				continue;
			if (element instanceof IFile)
				continue;
			if (element instanceof IStorage)
				continue;
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void run(ITextSelection selection) {
		if (!ActionUtil.isProcessable(getShell(), fEditor))
			return;
		try {
			IJavaElement element = SelectionConverter.codeResolve(fEditor,
					getShell(), getDialogTitle(),
					ActionMessages.getString("OpenAction.select_element")); //$NON-NLS-1$
			// begin AspectJ change
			if (element == null) {
				// it might be a pointcut
				IJavaElement input = SelectionConverter.getInput(fEditor);
				if (input instanceof AJCompilationUnit) {
					AJCompilationUnit ajcu = (AJCompilationUnit) input;
					String sel = selection.getText();
					if (selection.getLength() == 0) {
						ajcu.requestOriginalContentMode();
						String source = ajcu.getSource();
						ajcu.discardOriginalContentMode();
						sel = PointcutUtilities.findIdentifier(source, selection.getOffset());
					}
					if ((sel != null) && (sel.length() > 0)) {
						IJavaElement el = ajcu.getElementAt(selection
								.getOffset());
						if ((el instanceof AdviceElement)
								|| (el instanceof PointcutElement)) {
							element = PointcutUtilities.findPointcut(el, sel);
						}
					}
				}
			}
			// end AspectJ change
			if (element == null) {
				IEditorStatusLine statusLine = (IEditorStatusLine) fEditor
						.getAdapter(IEditorStatusLine.class);
				if (statusLine != null)
					statusLine
							.setMessage(
									true,
									ActionMessages.getString("OpenAction.error.messageBadSelection"), //$NON-NLS-1$
									null);
				getShell().getDisplay().beep();
				return;
			}
			IJavaElement input = SelectionConverter.getInput(fEditor);
			int type = element.getElementType();
			if (type == IJavaElement.JAVA_PROJECT
					|| type == IJavaElement.PACKAGE_FRAGMENT_ROOT
					|| type == IJavaElement.PACKAGE_FRAGMENT)
				element = input;
			run(new Object[] { element });
		} catch (JavaModelException e) {
			showError(e);
		}
	}

	/*
	 * (non-Javadoc) Method declared on SelectionDispatchAction.
	 */
	public void run(IStructuredSelection selection) {
		if (!checkEnabled(selection))
			return;
		run(selection.toArray());
	}

	/**
	 * Note: this method is for internal use only. Clients should not call this
	 * method.
	 * 
	 * @param elements
	 *            the elements to process
	 */
	public void run(Object[] elements) {
		if (elements == null)
			return;
		for (int i = 0; i < elements.length; i++) {
			Object element = elements[i];
			try {
				element = getElementToOpen(element);
				boolean activateOnOpen = fEditor != null ? true : OpenStrategy
						.activateOnOpen();
				OpenActionUtil.open(element, activateOnOpen);
			} catch (JavaModelException e) {
				JavaPlugin.log(new Status(IStatus.ERROR, JavaPlugin
						.getPluginId(), IJavaStatusConstants.INTERNAL_ERROR,
						ActionMessages.getString("OpenAction.error.message"), e)); //$NON-NLS-1$

				ErrorDialog.openError(getShell(), getDialogTitle(),
						ActionMessages.getString("OpenAction.error.messageProblems"), //$NON-NLS-1$
						e.getStatus());

			} catch (PartInitException x) {

				String name = null;

				if (element instanceof IJavaElement) {
					name = ((IJavaElement) element).getElementName();
				} else if (element instanceof IStorage) {
					name = ((IStorage) element).getName();
				} else if (element instanceof IResource) {
					name = ((IResource) element).getName();
				}

				if (name != null) {
					MessageDialog
							.openError(
									getShell(),
									ActionMessages.getString("OpenAction.error.messageProblems"), //$NON-NLS-1$
					ActionMessages.getFormattedString("OpenAction.error.messageArgs",  //$NON-NLS-1$
							new String[] { name, x.getMessage() } ));			
				}
			}
		}
	}

	/**
	 * Note: this method is for internal use only. Clients should not call this
	 * method.
	 * 
	 * @param object
	 *            the element to open
	 * @return the real element to open
	 * @throws JavaModelException
	 *             if an error occurs while accessing the Java model
	 */
	public Object getElementToOpen(Object object) throws JavaModelException {
		return object;
	}

	private String getDialogTitle() {
		return ActionMessages.getString("OpenAction.error.title"); //$NON-NLS-1$
	}

	private void showError(CoreException e) {
		ExceptionHandler.handle(e, getShell(), getDialogTitle(), ActionMessages.getString("OpenAction.error.message")); //$NON-NLS-1$
	}
}
