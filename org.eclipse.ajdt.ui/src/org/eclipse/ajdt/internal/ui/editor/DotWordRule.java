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
package org.eclipse.ajdt.internal.ui.editor;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * This word rule does not highlight keywords if there is a '.'
 * in front of them. (see bug 62265)
 * 
 * @author Luzius Meisser
 */
public class DotWordRule extends WordRule {
	
	public DotWordRule(IWordDetector detector) {
		super(detector, Token.UNDEFINED);
	}
	
	/*
	 * @see IRule#evaluate(ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		int pos = scanner.getColumn();
		if (pos != 0){
			scanner.unread();
			char first = (char)scanner.read();
			if (first == '.')
				return Token.UNDEFINED;
		}
		return super.evaluate(scanner);
	}
}
