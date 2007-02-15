/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Steve Young - initial version
 *******************************************************************************/
package org.eclipse.ajdt.examples.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Wrapper class for java.util.ZipFile, which overrides the entries() method
 * to filter out unwanted files.  This implementation only filters out files with 
 * the path "META-INF/eclipse.inf", which are added to JAR files as part of the 
 * conditioning steps for the Eclipse Update Manager, but not required or useful
 * to subsequent users of the JAR file's contents. 
 */
public class ZipFileWrapper extends ZipFile {

	private String fileToRemove = "META-INF/eclipse.inf"; //$NON-NLS-1$
	private ArrayList filteredZipEntries = null;
	private Enumeration filteredZipEntriesEnumeration = null;
	
	public ZipFileWrapper(String name) throws IOException {
		super(name);
	}

	/**
	 * Override the entries method to filter the visible list of files in the zip.
	 */
	public Enumeration entries(){
		
		// Return if already generated
		if(filteredZipEntriesEnumeration != null){
			return filteredZipEntriesEnumeration;
		}
		
		// Else generate the filtered Enumeration of elements
		Enumeration zipEntries = super.entries();
		filteredZipEntries = new ArrayList();
		
		while (zipEntries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
			
			if(! zipEntry.getName().equals(fileToRemove)){
				// Add to modified list
				filteredZipEntries.add(zipEntry);
			}
		}
		
		filteredZipEntriesEnumeration = Collections.enumeration(filteredZipEntries); 
		
		return filteredZipEntriesEnumeration;
	}
}
