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

import java.util.ArrayList;

import org.eclipse.ajdt.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.codeconversion.ConversionOptions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;

public class JavaCompatibleDocument implements IDocument, IDocumentListener {

	private IDocument realDocument;
	private IDocument fakeDocument;

	private ArrayList insertionTable;
	
	private boolean upToDate = false;
	private ConversionOptions conversionOptions = ConversionOptions.CODE_COMPLETION;

	public JavaCompatibleDocument(IDocument document) {
		super();
		this.realDocument = document;
		fakeDocument = new Document();
	}

	public void reinitialize(IDocument doc){
		if (doc != realDocument){
			realDocument = doc;
			realDocument.addDocumentListener(this);
			upToDate = false;
		}
	}
	
	public char getChar(int position) throws BadLocationException {
		ensureUpToDate();
		return fakeDocument.getChar(translatePositionToFake(position));
	}

	public int getLength() {
		ensureUpToDate();
		return fakeDocument.getLength();
	}
	
	public String toString() {
		ensureUpToDate();
		return fakeDocument.toString();
	}
	
	public boolean equals(Object obj) {
		return realDocument.equals(obj);
	}
	public int hashCode() {
		return realDocument.hashCode();
	}
	public void replace(int position, int length, String text) throws BadLocationException {
		position = translatePositionToReal(position);
		if (position != -1)
			realDocument.replace(position, length, text);
	}
	
	private void ensureUpToDate(){
		if (!upToDate){
			
			fakeDocument.set(realDocument.get());
			AspectsConvertingParser conv = new AspectsConvertingParser((char[])realDocument.get().toCharArray());
			insertionTable = conv.convert(conversionOptions);
			fakeDocument.set(new String(conv.content));
			upToDate = true;
			
		}
	}
	
	public int translatePositionToReal(int pos){
		this.ensureUpToDate();
		return AspectsConvertingParser.translatePositionToBeforeChanges(pos, insertionTable);
	}
	
	public int translatePositionToFake(int pos){
		this.ensureUpToDate();
		return AspectsConvertingParser.translatePositionToAfterChanges(pos, insertionTable);
	}
	
	public String get() {
		ensureUpToDate();
		return fakeDocument.get();
	}

	public String get(int offset, int length) throws BadLocationException {
		ensureUpToDate();
		return fakeDocument.get(translatePositionToFake(offset), length);
	}

	public void set(String text) {
		realDocument.set(text);
	}

	public void addDocumentListener(IDocumentListener listener) {
		realDocument.addDocumentListener(listener);
	}

	public void removeDocumentListener(IDocumentListener listener) {
		realDocument.removeDocumentListener(listener);
	}

	public void addPrenotifiedDocumentListener(IDocumentListener documentAdapter) {
		realDocument.addPrenotifiedDocumentListener(documentAdapter);
	}

	public void removePrenotifiedDocumentListener(IDocumentListener documentAdapter) {
		realDocument.removePrenotifiedDocumentListener(documentAdapter);
	}

	public void addPositionCategory(String category) {
		realDocument.addPositionCategory(category);
	}

	public void removePositionCategory(String category) throws BadPositionCategoryException {
		realDocument.removePositionCategory(category);
	}

	public String[] getPositionCategories() {
		return realDocument.getPositionCategories();
	}

	public boolean containsPositionCategory(String category) {
		return realDocument.containsPositionCategory(category);
	}

	public void addPosition(Position position) throws BadLocationException {
		realDocument.addPosition(position);
	}

	public void removePosition(Position position) {
		realDocument.removePosition(position);
	}

	public void addPosition(String category, Position position) throws BadLocationException, BadPositionCategoryException {
		realDocument.addPosition(category, position);
	}

	public void removePosition(String category, Position position) throws BadPositionCategoryException {
		realDocument.removePosition(category, position);
	}

	public Position[] getPositions(String category) throws BadPositionCategoryException {
		return realDocument.getPositions(category);
	}

	public boolean containsPosition(String category, int offset, int length) {
		return realDocument.containsPosition(category, offset, length);
	}

	public int computeIndexInCategory(String category, int offset) throws BadLocationException, BadPositionCategoryException {
		return realDocument.computeIndexInCategory(category, offset);
	}

	public void addPositionUpdater(IPositionUpdater updater) {
		realDocument.addPositionUpdater(updater);
	}

	public void removePositionUpdater(IPositionUpdater updater) {
		realDocument.removePositionUpdater(updater);
	}

	public void insertPositionUpdater(IPositionUpdater updater, int index) {
		realDocument.insertPositionUpdater(updater, index);
	}

	public IPositionUpdater[] getPositionUpdaters() {
		return realDocument.getPositionUpdaters();
	}

	public String[] getLegalContentTypes() {
		return realDocument.getLegalContentTypes();
	}

	public String getContentType(int offset) throws BadLocationException {
		return realDocument.getContentType(offset);
	}

	public ITypedRegion getPartition(int offset) throws BadLocationException {
		return realDocument.getPartition(offset);
	}

	public ITypedRegion[] computePartitioning(int offset, int length) throws BadLocationException {
		return realDocument.computePartitioning(offset, length);
	}

	public void addDocumentPartitioningListener(IDocumentPartitioningListener listener) {
		realDocument.addDocumentPartitioningListener(listener);
	}

	public void removeDocumentPartitioningListener(IDocumentPartitioningListener listener) {
		realDocument.removeDocumentPartitioningListener(listener);
	}

	public void setDocumentPartitioner(IDocumentPartitioner partitioner) {
		realDocument.setDocumentPartitioner(partitioner);
	}

	public IDocumentPartitioner getDocumentPartitioner() {
		return realDocument.getDocumentPartitioner();
	}

	public int getLineLength(int line) throws BadLocationException {
		ensureUpToDate();
		return fakeDocument.getLineLength(line);
	}

	public int getLineOfOffset(int offset) throws BadLocationException {
		ensureUpToDate();
		return fakeDocument.getLineOfOffset(offset);
	}

	public int getLineOffset(int line) throws BadLocationException {
		ensureUpToDate();
		return fakeDocument.getLineOffset(line);
	}

	public IRegion getLineInformation(int line) throws BadLocationException {
		ensureUpToDate();
		return fakeDocument.getLineInformation(line);
	}

	public IRegion getLineInformationOfOffset(int offset) throws BadLocationException {
		ensureUpToDate();
		return fakeDocument.getLineInformationOfOffset(offset);
	}

	public int getNumberOfLines() {
		ensureUpToDate();
		return fakeDocument.getNumberOfLines();
	}

	public int getNumberOfLines(int offset, int length) throws BadLocationException {
		ensureUpToDate();
		return fakeDocument.getNumberOfLines(offset, length);
	}

	public int computeNumberOfLines(String text) {
		ensureUpToDate();
		return fakeDocument.computeNumberOfLines(text);
	}

	public String[] getLegalLineDelimiters() {
		return realDocument.getLegalLineDelimiters();
	}

	public String getLineDelimiter(int line) throws BadLocationException {
		ensureUpToDate();
		return fakeDocument.getLineDelimiter(line);
	}

	public int search(int startOffset, String findString, boolean forwardSearch, boolean caseSensitive, boolean wholeWord) throws BadLocationException {
		ensureUpToDate();
		return fakeDocument.search(startOffset, findString, forwardSearch, caseSensitive, wholeWord);
	}

	public void documentAboutToBeChanged(DocumentEvent event) {
		
	}

	public void documentChanged(DocumentEvent event) {
		upToDate = false;
	}

}
