/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.debug;

import org.eclipse.ajdt.ui.tests.UITestCase;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsole;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceConsoleFactory;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.texteditor.ITextEditor;

public class JavaConsoleHyperlinkTest extends UITestCase {
    
    public void testHyperlink() throws Exception {
        createPredefinedProject("ITDTesting"); //$NON-NLS-1$
        
        JavaStackTraceConsoleFactory factory = new JavaStackTraceConsoleFactory();
        factory.openConsole();
        IConsoleManager consoleManager =  ConsolePlugin.getDefault().getConsoleManager();
        JavaStackTraceConsole console = null;
        IConsole[] consoles = consoleManager.getConsoles();
        for (int i = 0; i < consoles.length; i++) {
            if (consoles[i] instanceof JavaStackTraceConsole) {
                console = (JavaStackTraceConsole) consoles[i];
                break;
            }
        }
        if (console == null) {
            fail("Couldn't find the Java Stack Trace console"); //$NON-NLS-1$
        }
        
        console.getDocument().set("Exception in thread \"main\" java.lang.NullPointerException\n" + //$NON-NLS-1$
                "at generics.DeleteActionAspect.main(DeleteActionAspect.aj:28)\n" + //$NON-NLS-1$
                "at generics.DeleteActionAspect.main(DeleteActionAspect.aj:8)\n" + //$NON-NLS-1$
                "at generics.DeleteActionAspect.main(DeleteActionAspect.aj:16)\n"); //$NON-NLS-1$

        waitForJobsToComplete();
        
        IHyperlink[] links = console.getHyperlinks();

        // this part has always worked
        assertEquals("Wrong number of hyperlinks found in console", 4, links.length); //$NON-NLS-1$
        
        // ignore the first hyper link because it goes to runtime exception
        // this works because of jdt weaving.  because the text must be
        // parsed before link can be followed
        followHyperlink(links[1], 28);
        followHyperlink(links[2], 8);
        followHyperlink(links[3], 16);
    }

    private void followHyperlink(IHyperlink link, int line) {
        link.linkActivated();
        waitForJobsToComplete();
        
        ITextEditor editor = (ITextEditor) JDIDebugUIPlugin.getActivePage().getActiveEditor();
        assertEquals("Wrong editor was opened", "DeleteActionAspect.aj", editor.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
        TextSelection sel = (TextSelection) editor.getSelectionProvider().getSelection();
        assertEquals("Wrong line was selected", line-1, sel.getStartLine()); //$NON-NLS-1$
        
    }
    
}
