/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

/**
 * @author Luzius Meisser
 */
public class CodeTemplatesTest extends UITestCase {
	
	/**
	 * Checks if aj code templates are available.
	 * (picks and tests three)
	 */
	public void testIfAJCodeTemplatesAvailable() {
		TemplateStore codeTemplates = JavaPlugin.getDefault().getTemplateStore();
		//pick three (random) code templates and check if they are available	
		Template templ = codeTemplates.findTemplate("decw"); //$NON-NLS-1$
		if (templ != null)
			templ = codeTemplates.findTemplate("around"); //$NON-NLS-1$
		if (templ != null)
			templ = codeTemplates.findTemplate("within"); //$NON-NLS-1$
		if (templ == null)
			fail("Code template not found. -> AJ code templates have not been installed properly."); //$NON-NLS-1$
		if ((templ.getDescription() == null) || (templ.getPattern() == null))
			fail("Code template description or pattern missing."); //$NON-NLS-1$
	}
}
