/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.eclipse.ajdt.pointcutdoctor.core.explain.AtomicPart;
import org.eclipse.ajdt.pointcutdoctor.core.explain.Reason;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;


public class ReasonHighlighter {

	Map<StyledText,StyleRange[]> oldStyleRangesMap = new HashMap<StyledText,StyleRange[]>();
	private Map<StyledText, Integer> oldStartMap = new HashMap<StyledText, Integer>();
	private Map<StyledText, Integer> oldLengthMap = new HashMap<StyledText, Integer>();
	
	public void highlight(Reason reason) {
		IWorkbenchWindow activeWindow = JavaPlugin.getActiveWorkbenchWindow();
		if (activeWindow != null) {
			IWorkbenchPage activePage = activeWindow.getActivePage();
			if (activePage != null) {
				JavaEditor editor = (JavaEditor)activePage.getActiveEditor();
				StyledText text = editor.getViewer().getTextWidget();
				removePreviousHighlight(text);
				
				int start = fixOffsetForViewer(reason.getTextStart(), editor.getViewer());
				int length = reason.getTextLength();
				oldStyleRangesMap.put(text, text.getStyleRanges(start, length));
				oldStartMap.put(text, start);
				oldLengthMap.put(text, length);
				
				
				StyleRange[] newStyleRanges = createStyleRangesForReason(editor, reason); 
//				removeIllegalRangs(newStyleRanges);
//				text.replaceStyleRanges(start, length, newStyleRanges);
				for (StyleRange r:newStyleRanges)
					text.setStyleRange(r);
			}
		}
	}

	private static int fixOffsetForViewer(int offset, ITextViewer viewer) {
//		int invisibleChars = viewer.getBottomIndexEndOffset()-viewer.getDocument().getLength();
//		return offset - invisibleChars;
		int noffset = ((TextViewer)viewer).modelOffset2WidgetOffset(offset); 
		return noffset;
	}

	public void removePreviousHighlight(StyledText text) {
		StyleRange[] oldStyleRanges = oldStyleRangesMap.get(text);
		if (oldStyleRanges!=null) {
			text.replaceStyleRanges(oldStartMap.get(text), oldLengthMap.get(text), oldStyleRanges);
		}
	}

	private StyleRange[] createStyleRangesForReason(JavaEditor editor, Reason reason) {
		StyledText text = editor.getViewer().getTextWidget();
		List<AtomicPart> allParts = reason.getAllParts();
		List<StyleRange> ranges = new ArrayList<StyleRange>();
		for (AtomicPart p:allParts) {
			if (p.getOffset()>0 && p.getLength()>0) {
				Color bg = null;
				FuzzyBoolean matchResult = p.getMatchResult();
				if (matchResult.alwaysTrue())
					bg = UIConstants.GREEN; 
				else if (matchResult.alwaysFalse())
					bg = UIConstants.RED; 
				else
//					case Maybe:
					bg = UIConstants.YELLOW; 
//				StyleRange oldStyle = text.getStyleRangeAtOffset(p.getOffset());
				int offset = fixOffsetForViewer(p.getOffset(), editor.getViewer());
				StyleRange[] oldStyles = text.getStyleRanges(offset, p.getLength());
				for (StyleRange oldStyle:oldStyles) {
					ranges.add(new StyleRange(oldStyle.start, oldStyle.length, 
							oldStyle.foreground, bg, oldStyle.fontStyle));
				}
			}
		}
		return ranges.toArray(new StyleRange[ranges.size()]);
	}

}
