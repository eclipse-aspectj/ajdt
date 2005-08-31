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
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AdviceElement;
import org.eclipse.ajdt.core.javaelements.PointcutElement;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlink;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Assert;
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
		if (input instanceof AJCompilationUnit) {
			AJCompilationUnit ajcu = (AJCompilationUnit) input;
			try {
				IJavaElement el = ajcu.getElementAt(offset);
				if ((el instanceof AdviceElement)
						|| (el instanceof PointcutElement)) {
					// now narrow down to after the colon and before
					// the start of the advice body or the end of the pointcut
					ajcu.requestOriginalContentMode();
					String source = ajcu.getSource();
					ajcu.discardOriginalContentMode();
					ISourceRange range = ((ISourceReference) el)
							.getSourceRange();
					int start = range.getOffset();
					int end = start + range.getLength();
					int colon = findNextChar(source, start, end, ':');
					// we need to be after a colon
					if ((colon != -1) && (offset > colon)) {
						int openBrace = findNextChar(source, colon, end, '{');
						int endZone = openBrace;
						if (endZone == -1) {
							int semiColon = findNextChar(source, colon, end,
									';');
							endZone = semiColon;
						}
						// we need to be before the end zone
						if ((endZone > 0) && (offset < endZone)) {
							IRegion reg = selectWord(source, offset);
							if (reg != null) {
								return new IHyperlink[] { new JavaElementHyperlink(
										reg, openAction) };
							}
						}
					}
				}
			} catch (JavaModelException e) {
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
		if (isAjKeyword(document.substring(start + 1, end))) {
			return null;
		}
		return new Region(start + 1, end - start - 1);
	}

	private boolean isAjKeyword(String word) {
		for (int i = 0; i < AspectJCodeScanner.ajKeywords.length; i++) {
			if (AspectJCodeScanner.ajKeywords[i].equals(word)) {
				return true;
			}
		}
		// "this" and "if" are not in the aj list as they are java keywords
		if ("this".equals(word)) { //$NON-NLS-1$
			return true;
		}
		if ("if".equals(word)) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	/**
	 * Returns the index of the first occurrence of the given character in the
	 * source string, between the start offset and the limit. Returns -1 if the
	 * character is not found in the defined range.
	 * 
	 * @param source
	 * @param offset
	 * @param limit
	 * @param c
	 * @return
	 */
	private int findNextChar(String source, int offset, int limit, char c) {
		while (source.charAt(offset) != c) {
			offset++;
			if ((offset == limit) || (offset == source.length())) {
				return -1;
			}
		}
		return offset;
	}
}
