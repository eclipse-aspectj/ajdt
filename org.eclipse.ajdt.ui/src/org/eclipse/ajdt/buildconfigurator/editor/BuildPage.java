/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Matt Chapman - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.buildconfigurator.editor;

import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ScrolledForm;

public class BuildPage extends PDEFormPage {
	public static final String FORM_TITLE = "AJPropsEditor.BuildPage.title"; //$NON-NLS-1$
	public static final String PAGE_ID = "build"; //$NON-NLS-1$
	private BuildContentsSection srcSection;
	
	public BuildPage(FormEditor editor) {
		super(editor, PAGE_ID, AspectJUIPlugin.getResourceString("BuildPage.name"));  //$NON-NLS-1$
	}

	protected void createFormContent(IManagedForm mform) {
		super.createFormContent(mform);
		//FormToolkit toolkit = mform.getToolkit();
		GridLayout layout = new GridLayout();
		ScrolledForm form = mform.getForm();
		form.setText(AspectJUIPlugin.getResourceString(FORM_TITLE));
		layout.numColumns = 2;
		layout.marginWidth = 10;
		layout.horizontalSpacing = 15;
		layout.verticalSpacing = 10;
		layout.makeColumnsEqualWidth = true;
		form.getBody().setLayout(layout);


		
		srcSection = new SrcSection(this, form.getBody());
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		srcSection.getSection().setLayoutData(gd);

		mform.addPart(srcSection);
	}

	
	public void enableAllSections(boolean enable){
		srcSection.enableSection(enable);
	}

}
