/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.ajdocexport;

import java.io.File;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Copied from org.eclipse.jdt.internal.ui.javadocexport.JavadocWizardPage
 * Changes marked with // AspectJ Extension
 */
public abstract class AJdocWizardPage extends NewElementWizardPage {

	protected AJdocWizardPage(String pageName) {
		super(pageName);
//		 AspectJ Extension - message
		setTitle(UIMessages.ajdocWizardPage_javadocwizardpage_description);
	}

	protected Button createButton(Composite composite, int style, String message, GridData gd) {
		Button button= new Button(composite, style);
		button.setText(message);
		button.setLayoutData(gd);
		return button;
	}

	protected GridLayout createGridLayout(int columns) {
		return new GridLayout(columns, false);

	}

	protected GridData createGridData(int flag, int hspan, int vspan, int indent) {
		GridData gd= new GridData(flag);
		gd.horizontalIndent= indent;
		gd.horizontalSpan= hspan;
		gd.verticalSpan= vspan;
		return gd;
	}

	protected GridData createGridData(int flag, int hspan, int indent) {
		GridData gd= new GridData(flag);
		gd.horizontalIndent= indent;
		gd.horizontalSpan= hspan;
		return gd;
	}

	protected GridData createGridData(int hspan) {
		GridData gd= new GridData();
		gd.horizontalSpan= hspan;
		return gd;
	}

	protected Label createLabel(Composite composite, int style, String message, GridData gd) {
		Label label= new Label(composite, style);
		label.setText(message);
		label.setLayoutData(gd);
		return label;
	}

	protected Text createText(Composite composite, int style, String message, GridData gd) {
		Text text= new Text(composite, style);
		if (message != null)
			text.setText(message);
		text.setLayoutData(gd);
		return text;
	}

	protected Combo createCombo(Composite composite, int style, String message, GridData gd) {
		Combo combo= new Combo(composite, style);
		if (message != null)
			combo.setText(message);
		combo.setLayoutData(gd);
		return combo;
	}

	protected void handleFileBrowseButtonPressed(Text text, String[] extensions, String title) {
		FileDialog dialog= new FileDialog(text.getShell());
		dialog.setText(title);
		dialog.setFilterExtensions(extensions);
		String dirName= text.getText();
		if (!dirName.equals("")) { //$NON-NLS-1$
			File path= new File(dirName);
			if (path.exists())
				dialog.setFilterPath(dirName);

		}
		String selectedDirectory= dialog.open();
		if (selectedDirectory != null)
			text.setText(selectedDirectory);
	}

	protected String handleFolderBrowseButtonPressed(String dir, String title, String message) {
		DirectoryDialog dialog= new DirectoryDialog(getShell());
		dialog.setFilterPath(dir);
		dialog.setText(title);
		dialog.setMessage(message);
		String res= dialog.open();
		if (res != null) {
			File file= new File(res);
			if (file.isDirectory())
				return res;
		}
		return dir;
	}

	protected static class EnableSelectionAdapter extends SelectionAdapter {
		private final Control[] fEnable;
		private final Control[] fDisable;

		protected EnableSelectionAdapter(Control[] enable, Control[] disable) {
			super();
			fEnable= enable;
			fDisable= disable;
		}

		public void widgetSelected(SelectionEvent e) {
      for (Control value : fEnable) {
        value.setEnabled(true);
      }
      for (Control control : fDisable) {
        control.setEnabled(false);
      }
			validate();
		}
		//copied from  WizardNewProjectCreationPage
		public void validate() {
		}

	} //end class EnableSelectionAdapter

	protected static class ToggleSelectionAdapter extends SelectionAdapter {
		Control[] controls;

		protected ToggleSelectionAdapter(Control[] controls) {
			this.controls= controls;
		}

		public void widgetSelected(SelectionEvent e) {

      for (Control control : controls) {
        control.setEnabled(!control.getEnabled());
      }
			validate();
		}

		public void validate() {
		}

	} //end class ToggleSelection Adapter

}
