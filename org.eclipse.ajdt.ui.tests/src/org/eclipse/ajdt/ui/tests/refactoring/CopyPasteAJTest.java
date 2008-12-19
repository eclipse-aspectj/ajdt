/*******************************************************************************
 * Copyright (c) 2008 SpringSourceand others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial version
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.refactoring;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.refactoring.reorg.JavaElementTransfer;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.refactoring.reorg.PasteAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

/**
 * 
 * @author andrew
 * @created Dec 11, 2008
 * 
 * Copy and pasting of AJ compilation units
 * 
 * Bug 78341
 *
 */
public class CopyPasteAJTest extends UITestCase {

    protected void setUp() throws Exception {
        super.setUp();
        if (!AspectJPlugin.USING_CU_PROVIDER) {
            fail("Must be using JDT Weaving");
        }
    }
    
    public void testCopyPasteAJ() throws Exception {
        IProject proj = createPredefinedProject("Bug 254431"); //$NON-NLS-1$
        IFile file = proj.getFile("src/ajdt/renamepackagebug1/A.aj"); //$NON-NLS-1$
        IJavaProject jProj = JavaCore.create(proj);
        IJavaElement elt = AspectJCore.create(file);
        Clipboard clipboard = new Clipboard(Display.getDefault());
        clipboard.setContents(new Object[] { new IJavaElement[] { elt }     }, new Transfer[] { JavaElementTransfer.getInstance() });
        
        PasteAction paste = new PasteAction(EditorUtility.openInEditor(elt).getEditorSite(), clipboard);
        
        // can't paste to the same location because a dialog appears and there is no way to OK it
        // instead paste to "ajdt" package
        paste.run(new StructuredSelection(jProj.getPackageFragmentRoot(
                proj.getFolder("src")).getPackageFragment("ajdt"))); //$NON-NLS-1$ //$NON-NLS-2$
        
        IFile newFile = proj.getFile("src/ajdt/A.aj"); //$NON-NLS-1$
        assertTrue("Paste operation should have created a new AJ compilation unit", newFile.exists()); //$NON-NLS-1$
        AJCompilationUnit newUnit = (AJCompilationUnit) AspectJCore.create(newFile);
        IPackageDeclaration[] packDecls = newUnit.getPackageDeclarations();
        assertEquals("New compilation unit should have only one package declaration", 1, packDecls.length); //$NON-NLS-1$
        assertEquals("wrong name for package declaration", "ajdt", packDecls[0].getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
    }
}