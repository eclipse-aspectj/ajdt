/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ... 
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.refactoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * 
 */
public class ReaderInputStream extends InputStream {

	private Reader reader;
	
	public ReaderInputStream(Reader reader){
		this.reader = reader;
	}
	
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		return reader.read();
	}

	
    public void close() throws IOException {
    	reader.close();
    } 
	
}
