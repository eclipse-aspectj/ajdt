/**********************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
Adrian Colyer - intial version
...
**********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import java.io.IOException;
import java.util.List;

import org.aspectj.bridge.ISourceLocation;
/**
 * Implements the Ajde EditorAdapter interface. Not clear when this is ever called,
 * currently there is no implementation!!
 */
public class EditorAdapter implements org.aspectj.ajde.EditorAdapter {

	public void showSourceLine(
		String filePath,
		int lineNumber,
		boolean highlight) {
	}
	
	  
   public void showSourceLine(ISourceLocation sl, boolean highlight) {
   	 // Delegates to the above method ... but that does nothing!
   	 showSourceLine(sl.getSourceFile().getAbsolutePath(), sl.getLine(),highlight);	 
   }

	public void showSourceLine(int lineNumber, boolean highlight) {
	}

	public String getCurrFile() {
		return ""; //$NON-NLS-1$
	}

	public void saveContents() throws IOException {
	}

	public void pasteToCaretPos(String text) {
	}

   public void showSourcelineAnnotation(String s, int i, List l) {
   }
 
}