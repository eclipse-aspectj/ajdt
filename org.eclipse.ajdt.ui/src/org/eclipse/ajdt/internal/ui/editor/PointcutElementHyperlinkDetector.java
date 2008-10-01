/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.PointcutUtilities;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlink;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.ui.texteditor.ITextEditor;

public class PointcutElementHyperlinkDetector implements IHyperlinkDetector {
	private ITextEditor fTextEditor;

	/**
	 * Creates a new Java element hyperlink detector.
	 * 
	 * @param editor
	 *            the editor in which to detect the hyperlink
	 */
	public PointcutElementHyperlinkDetector(ITextEditor editor) {
		Assert.isNotNull(editor);
		fTextEditor = editor;
	}

	public IHyperlink[] detectHyperlinks(ITextViewer textViewer,
			IRegion region, boolean canShowMultipleHyperlinks) {
		if (region == null || canShowMultipleHyperlinks
				|| !(fTextEditor instanceof AspectJEditor)) {
			return null;
		}
		IAction openAction = fTextEditor.getAction("OpenEditor"); //$NON-NLS-1$
		if (openAction == null) {
			return null;
		}
		int offset = region.getOffset();

		IJavaElement input = SelectionConverter
				.getInput((JavaEditor) fTextEditor);
		if (input instanceof ICompilationUnit) {
			input = AJCompilationUnitManager.mapToAJCompilationUnit((ICompilationUnit)input);
		}
		if (input instanceof AJCompilationUnit) {
			String source = PointcutUtilities.isInPointcutContext((AJCompilationUnit)input,offset);
			if (source != null) {
				IRegion reg = selectWord(source, offset);
				if (reg != null) {
					return new IHyperlink[] { 
							new JavaElementHyperlink(reg, (SelectionDispatchAction) openAction, input, false) };
				}
			}
		}
		return null;
	}

	private IRegion selectWord(String document, int anchor) {
		int offset = anchor;
		char c;

		while (offset >= 0) {
			c = document.charAt(offset);
			if (!Character.isJavaIdentifierPart(c))
				break;
			--offset;
		}

		int start = offset;

		offset = anchor;
		int length = document.length();

		while (offset < length) {
			c = document.charAt(offset);
			if (!Character.isJavaIdentifierPart(c))
				break;
			++offset;
		}

		int end = offset;

		if (start == end) {
			return null;
		}
		if (PointcutUtilities.isAjPointcutKeyword(document.substring(start + 1, end))) {
			return null;
		}
		return new Region(start + 1, end - start - 1);
	}

}