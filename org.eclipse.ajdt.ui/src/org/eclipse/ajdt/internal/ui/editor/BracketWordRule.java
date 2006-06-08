/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sian January - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;

/**
 * This word rule does not highlight keywords if there is a '.'
 * in front of them or if there is NOT a '(' after them.
 * See bugs 62265 and 126769.
 */
public class BracketWordRule extends DotWordRule {

	public BracketWordRule(IWordDetector detector) {
		super(detector);
	}
	
	/*
	 * @see IRule#evaluate(ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		
		int c = scanner.read();
		int numToUnread = 1;
		while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c)) {
			c = scanner.read();
			numToUnread ++;
		}
		while (Character.isWhitespace((char)c)) {
			c = scanner.read();
			numToUnread ++;			
		}
		if((char)c != '(') {
			unread(scanner, numToUnread);
			return Token.UNDEFINED;
		}
		unread(scanner, numToUnread);
		return super.evaluate(scanner);
	}

	private void unread(ICharacterScanner scanner, int numToUnread) {
		for (int i = 0; i < numToUnread; i++) {
			scanner.unread();
		}		
	}

}
