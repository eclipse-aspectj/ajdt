/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sian January - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor.contentassist;

import org.eclipse.jface.text.IAutoIndentStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

public class TextViewerWrapper implements ITextViewer {

	private ITextViewer delegate;

	public TextViewerWrapper(ITextViewer viewer) {
		this.delegate = viewer;
	}

	public StyledText getTextWidget() {
		return delegate.getTextWidget();
	}

	public void setUndoManager(IUndoManager undoManager) {
		delegate.setUndoManager(undoManager);
	}

	public void setTextDoubleClickStrategy(ITextDoubleClickStrategy strategy,
			String contentType) {
		delegate.setTextDoubleClickStrategy(strategy, contentType);
	}

	public void setAutoIndentStrategy(IAutoIndentStrategy strategy,
			String contentType) {
		delegate.setAutoIndentStrategy(strategy, contentType);
	}

	public void setTextHover(ITextHover textViewerHover, String contentType) {
		delegate.setTextHover(textViewerHover, contentType);
	}

	public void activatePlugins() {
		delegate.activatePlugins();
	}

	public void resetPlugins() {
		delegate.resetPlugins();
	}

	public void addViewportListener(IViewportListener listener) {
		delegate.addViewportListener(listener);
	}

	public void removeViewportListener(IViewportListener listener) {
		delegate.removeViewportListener(listener);
	}

	public void addTextListener(ITextListener listener) {
		delegate.addTextListener(listener);
	}

	public void removeTextListener(ITextListener listener) {
		delegate.removeTextListener(listener);
	}

	public void addTextInputListener(ITextInputListener listener) {
		delegate.addTextInputListener(listener);
	}

	public void removeTextInputListener(ITextInputListener listener) {
		delegate.removeTextInputListener(listener);
	}

	public void setDocument(IDocument document) {
		delegate.setDocument(document);
	}

	public IDocument getDocument() {
		return new JavaCompatibleDocument(delegate.getDocument());
	}

	public void setEventConsumer(IEventConsumer consumer) {
		delegate.setEventConsumer(consumer);
	}

	public void setEditable(boolean editable) {
		delegate.setEditable(editable);
	}

	public boolean isEditable() {
		return delegate.isEditable();
	}

	public void setDocument(IDocument document, int modelRangeOffset,
			int modelRangeLength) {
		delegate.setDocument(document, modelRangeOffset, modelRangeLength);
	}

	public void setVisibleRegion(int offset, int length) {
		delegate.setVisibleRegion(offset, length);
	}

	public void resetVisibleRegion() {
		delegate.resetVisibleRegion();
	}

	public IRegion getVisibleRegion() {
		return delegate.getVisibleRegion();
	}

	public boolean overlapsWithVisibleRegion(int offset, int length) {
		return delegate.overlapsWithVisibleRegion(offset, length);
	}

	public void changeTextPresentation(TextPresentation presentation,
			boolean controlRedraw) {
		delegate.changeTextPresentation(presentation, controlRedraw);
	}

	public void invalidateTextPresentation() {
		delegate.invalidateTextPresentation();
	}

	public void setTextColor(Color color) {
		delegate.setTextColor(color);
	}

	public void setTextColor(Color color, int offset, int length,
			boolean controlRedraw) {
		delegate.setTextColor(color, offset, length, controlRedraw);
	}

	public ITextOperationTarget getTextOperationTarget() {
		return delegate.getTextOperationTarget();
	}

	public IFindReplaceTarget getFindReplaceTarget() {
		return delegate.getFindReplaceTarget();
	}

	public void setDefaultPrefixes(String[] defaultPrefixes, String contentType) {
		delegate.setDefaultPrefixes(defaultPrefixes, contentType);
	}

	public void setIndentPrefixes(String[] indentPrefixes, String contentType) {
		delegate.setIndentPrefixes(indentPrefixes, contentType);
	}

	public void setSelectedRange(int offset, int length) {
		delegate.setSelectedRange(offset, length);
	}

	public Point getSelectedRange() {
		return delegate.getSelectedRange();
	}

	public ISelectionProvider getSelectionProvider() {
		return delegate.getSelectionProvider();
	}

	public void revealRange(int offset, int length) {
		delegate.revealRange(offset, length);
	}

	public void setTopIndex(int index) {
		delegate.setTopIndex(index);
	}

	public int getTopIndex() {
		return delegate.getTopIndex();
	}

	public int getTopIndexStartOffset() {
		return delegate.getTopIndexStartOffset();
	}

	public int getBottomIndex() {
		return delegate.getBottomIndex();
	}

	public int getBottomIndexEndOffset() {
		return delegate.getBottomIndexEndOffset();
	}

	public int getTopInset() {
		return delegate.getTopInset();
	}

}
