/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.codeconversion;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.BufferChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.JavaModelException;

/**
 * This Wrapper forwards changes to the real buffer, but
 * reads contents from a fake buffer.
 * Purpose: to make jdt operations like "organize imports" produce reasonable
 *   results for .aj files.
 * 
 * @author Luzius Meisser
 *
 */
public class JavaCompatibleBuffer implements IBuffer, IBufferChangedListener{

    private IBuffer realBuffer;
    private IBuffer fakeBuffer;
    
    private ArrayList insertionTable;
    
    private boolean upToDate = false;
    private ConversionOptions conversionOptions = ConversionOptions.STANDARD;
    
    public JavaCompatibleBuffer(IBuffer real, IBuffer fake){
        realBuffer = real;
        fakeBuffer = fake;
        real.addBufferChangedListener(this);
        
    }
    
    public void reinitialize(IBuffer buf){
        if (buf != realBuffer){
            realBuffer = buf;
            realBuffer.addBufferChangedListener(this);
            upToDate = false;
        }
    }
    
    public IBuffer getRealBuffer() {
        return realBuffer;
    }

    public void close() {
    }
    
    public char getChar(int position) {
        ensureUpToDate();
        return fakeBuffer.getChar(position);
    }
    public char[] getCharacters() {
        ensureUpToDate();
        return fakeBuffer.getCharacters();
    }
    public String getContents() {
        ensureUpToDate();
        return fakeBuffer.getContents();
    }
    public int getLength() {
        ensureUpToDate();
        return fakeBuffer.getLength();
    }
    public String getText(int offset, int length) {
        ensureUpToDate();
        return fakeBuffer.getText(offset, length);
    }
    public String toString() {
        ensureUpToDate();
        return fakeBuffer.toString();
    }
    public void addBufferChangedListener(IBufferChangedListener listener) {
        realBuffer.addBufferChangedListener(listener);
    }
    public void append(char[] text) {
        realBuffer.append(text);
        upToDate = false;
    }
    public void append(String text) {
        realBuffer.append(text);
        upToDate = false;
    }
    public boolean equals(Object obj) {
        return realBuffer.equals(obj);
    }
    public IOpenable getOwner() {
        return realBuffer.getOwner();
    }
    public IResource getUnderlyingResource() {
        return realBuffer.getUnderlyingResource();
    }
    public int hashCode() {
        return realBuffer.hashCode();
    }
    public boolean hasUnsavedChanges() {
        return realBuffer.hasUnsavedChanges();
    }
    public boolean isClosed() {
        return realBuffer.isClosed();
    }
    public boolean isReadOnly() {
        return realBuffer.isReadOnly();
    }
    public void removeBufferChangedListener(IBufferChangedListener listener) {
        realBuffer.removeBufferChangedListener(listener);
    }
    public void replace(int position, int length, char[] text) {
        position = translatePositionToReal(position);
        if (position != -1) {
            realBuffer.replace(position, length, text);
            upToDate = false;
        }
    }
    public void replace(int position, int length, String text) {
        position = translatePositionToReal(position);
        if (position != -1) {
            realBuffer.replace(position, length, text);
            upToDate = false;
        }
    }
    public void save(IProgressMonitor progress, boolean force)
            throws JavaModelException {
        realBuffer.save(progress, force);
    }
    public void setContents(char[] contents) {
        realBuffer.setContents(contents);
    }
    public void setContents(String contents) {
        realBuffer.setContents(contents);
    }
    
    private void ensureUpToDate(){
        if (!upToDate) {
            
            fakeBuffer.setContents((char[])realBuffer.getCharacters().clone());
            AspectsConvertingParser conv = new AspectsConvertingParser((char[])realBuffer.getCharacters().clone());
            
            IOpenable owner = getOwner();
            if (owner instanceof ICompilationUnit) {
                conv.setUnit((ICompilationUnit) owner);
            }
            insertionTable = conv.convert(conversionOptions);
            fakeBuffer.setContents(conv.content);
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
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.IBufferChangedListener#bufferChanged(org.eclipse.jdt.core.BufferChangedEvent)
     */
    public void bufferChanged(BufferChangedEvent event) {
        if (realBuffer.isClosed())
            fakeBuffer.close();
        upToDate = false;
    }
    
    public ConversionOptions getConversionOptions() {
        return conversionOptions;
    }
    
    public void setConversionOptions(ConversionOptions conversionOptions) {
        this.conversionOptions = conversionOptions;
        upToDate = false;
    }
    
    public ArrayList getInsertionTable() {
        ensureUpToDate();
        return insertionTable;
    }
}
