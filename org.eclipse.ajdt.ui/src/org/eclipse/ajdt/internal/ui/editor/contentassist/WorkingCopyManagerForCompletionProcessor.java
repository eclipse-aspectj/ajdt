/**********************************************************************
Copyright (c) 2004 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    Luzius Meisser - Initial implementation
**********************************************************************/
package org.eclipse.ajdt.internal.ui.editor.contentassist;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * @author Luzius Meisser
 * 
 * Creates temporary compilation units for .java with aspects and replaces aspect
 * with class to make code completion work.
 * 
 * A description of how code completion works in AJDT can be found in bug 74419.
 * 
 */
public class WorkingCopyManagerForCompletionProcessor implements
		IWorkingCopyManager {

	IWorkingCopyManager wrapped;
	IDocumentProvider provider;
	private ICompilationUnit workingCopy = null;

	WorkingCopyManagerForCompletionProcessor(IWorkingCopyManager w, IDocumentProvider prov) {
		wrapped = w;
		provider = prov;
	}

	public void connect(IEditorInput input) throws CoreException {
		wrapped.connect(input);

	}

	public void disconnect(IEditorInput input) {
		wrapped.disconnect(input);
	}

	/*
	 * Make sure to call discardWorkingCopy() after having called this method and before calling
	 * it again to avoid a memory leak
	 */
	public ICompilationUnit getWorkingCopy(IEditorInput input) {
		ICompilationUnit unit = wrapped.getWorkingCopy(input);
		
		//in case of an AJCompilationUnit, we do not need to do anything
		if (unit instanceof AJCompilationUnit){
			return unit;
		}
		
		//in case of a .java file, we need to replace the aspect keyword (if there)
		try {
			IBuffer buff = unit.getBuffer();
			String documentContents = buff.getContents();
			int aspectindex = locateKeyword(documentContents, "aspect ");
			int bracketindex = documentContents.indexOf("{");
			if (aspectindex != -1 && aspectindex < bracketindex) {
				workingCopy = unit.getWorkingCopy(null);
				buff = workingCopy.getBuffer();
				buff.replace(aspectindex, 7, "class  ");
				return workingCopy;
			}
		} catch (JavaModelException e) {
		}
		return unit;
	}
	
	public void discardWorkingCopy(){
		//to prevent a memory leak, we have to discard the working
		//copies we created.
		if (workingCopy != null){
			try {
				workingCopy.discardWorkingCopy();
				workingCopy = null;
			} catch (JavaModelException e) {
			}
		}
	}
	
	//copied from AspectJEditor
	private int locateKeyword(String where, String what) {
		int location = -1;
		int offset = 0;
		String whereCopy = where;
		boolean cont=true;

		while (cont) {
			int whatIndex = whereCopy.indexOf(what);
			//int oneLineComment = whereCopy.indexOf("//");
			int multiLineCommentStart = whereCopy.indexOf("/*");
			int multiLineCommentEnd = whereCopy.indexOf("*/");
			if (whatIndex==-1) {cont = false;break;}
			if (multiLineCommentStart == -1 || (whatIndex<multiLineCommentStart)) {location = whatIndex+offset; cont=false;} 
			if (multiLineCommentStart!=-1) {
				if (multiLineCommentEnd==-1) cont =false ;
				else {
				offset = offset + multiLineCommentEnd-multiLineCommentStart+2;
				whereCopy = whereCopy.substring(0,multiLineCommentStart)+
							whereCopy.substring(multiLineCommentEnd+2);
				}
			}
		}
		return location;	
	}

	public void shutdown() {
		wrapped.shutdown();
	}
}