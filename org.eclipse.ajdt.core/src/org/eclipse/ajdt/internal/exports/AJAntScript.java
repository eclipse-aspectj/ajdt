/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.exports;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.pde.internal.build.ant.AntScript;

/**
 * Extended AntScript and copied some protected methods so that they can be used 
 * in this package by the AspectJ Plugin Export Wizard. 
 */
public class AJAntScript extends AntScript {

	protected int indent = 0;
	protected OutputStream out;
	
	/**
	 * @param out
	 * @throws java.io.IOException
	 */
	public AJAntScript(OutputStream out) throws IOException {
		super(out);
	}

	/**
	 * Print the given number of tabs to the Ant script.
	 */
	protected void printTab() {
		super.printTab();
		
	}
	
	protected void printAttribute(String name, String value, boolean mandatory) {
		super.printAttribute(name, value, mandatory);
	}
	
	protected void printQuotes(String message) {
		super.printQuotes(message);
	}
}
