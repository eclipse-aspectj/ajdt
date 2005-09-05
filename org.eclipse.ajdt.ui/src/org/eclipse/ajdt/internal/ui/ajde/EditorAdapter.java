/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
Contributors:
Adrian Colyer - intial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.io.IOException;
import java.util.List;

import org.aspectj.bridge.ISourceLocation;
//import org.aspectj.ajde.EditorAdapter;
/**
 * Implements the Ajde EditorAdapter interface. Not clear when this is ever called,
 * currently there is no implementation!!
 */
public class EditorAdapter implements org.aspectj.ajde.EditorAdapter {

	public void showSourceLine(
		String filePath,
		int lineNumber,
		boolean highlight) {
		System.err.println( "EditorAdapter::showSourceLine NOT IMPLEMENTED"); //$NON-NLS-1$
	}
	
	  
   public void showSourceLine(ISourceLocation sl, boolean highlight) {
   	 // Delegates to the above method ... but that does nothing!
   	 showSourceLine(sl.getSourceFile().getAbsolutePath(), sl.getLine(),highlight);	 
   }

	public void showSourceLine(int lineNumber, boolean highlight) {
		System.err.println( "EditorAdapter::showSourceLine NOT IMPLEMENTED"); //$NON-NLS-1$
	}

	public String getCurrFile() {
		System.err.println( "EditorAdapter::getCurrFile NOT IMPLEMENTED"); //$NON-NLS-1$
		return ""; //$NON-NLS-1$
	}

	public void saveContents() throws IOException {
		System.err.println( "EditorAdapter::saveContents NOT IMPLEMENTED"); //$NON-NLS-1$
	}

	public void pasteToCaretPos(String text) {
		System.err.println( "EditorAdapter::pasteToCaretos NOT IMPLEMENTED"); //$NON-NLS-1$
	}

   public void showSourcelineAnnotation(String s, int i, List l) {
   	 // ASCFIXME - should this do something?
   }
 
}