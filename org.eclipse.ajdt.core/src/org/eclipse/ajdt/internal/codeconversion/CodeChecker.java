/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.codeconversion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.aspectj.org.eclipse.jdt.core.compiler.InvalidInputException;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 *  
 */
public class CodeChecker {

	/**
	 * Determines whether the given file contains any AspectJ-specific syntax,
	 * such as an aspect, an inner aspect inside a class, or just a pointcut
	 * inside a class.
	 * 
	 * @param file
	 * @return
	 */
	public static boolean containsAspectJConstructs(IFile file) {
		IPath path = file.getRawLocation();
		if (path == null) {
			return false;
		}
		File f = path.toFile();
		if (!f.exists()) {
			return false;
		}

		try {
			// read contents of file into char array
			FileReader fr = new FileReader(f);
			int size = (int) f.length();
			char[] data = new char[size];
			fr.read(data, 0, size);
			fr.close();

			Scanner scanner = new Scanner();
			scanner.setSource(data);

			int tok;
			while (true) {
				try {
					tok = scanner.getNextToken();
				} catch (InvalidInputException e) {
					continue;
				}
				if (tok == TerminalTokens.TokenNameEOF)
					break;

				switch (tok) {
				case TerminalTokens.TokenNameaspect:
					return true;
				case TerminalTokens.TokenNamepointcut:
					return true;
				default:
					break;
				}
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}

		return false;
	}
}
