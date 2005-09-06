/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.editor;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.ui.text.AbstractJavaScanner;
import org.eclipse.jdt.internal.ui.text.JavaWhitespaceDetector;
import org.eclipse.jdt.internal.ui.text.JavaWordDetector;
import org.eclipse.jdt.ui.text.IColorManager;
import org.eclipse.jdt.ui.text.IJavaColorConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * An AspectJ code scanner - identical to the Java one except that we
 * add in the AspectJ keywords.
 * Better would be a proper extension mechanism in JDT that lets us do this
 * cleanly!
 */
public final class AspectJCodeScanner extends AbstractJavaScanner {

	private static class VersionedWordRule extends WordRule {

		private final String fVersion;
		private final boolean fEnable;

		private String fCurrentVersion;

		public VersionedWordRule(
			IWordDetector detector,
			String version,
			boolean enable,
			String currentVersion) {
			super(detector);

			fVersion = version;
			fEnable = enable;
			fCurrentVersion = currentVersion;
		}

		public void setCurrentVersion(String version) {
			fCurrentVersion = version;
		}

		/*
		 * @see IRule#evaluate
		 */

		public IToken evaluate(ICharacterScanner scanner) {
			IToken token = super.evaluate(scanner);

			if (fEnable) {
				if (fCurrentVersion.equals(fVersion))
					return token;

				return Token.UNDEFINED;

			} else {
				if (fCurrentVersion.equals(fVersion))
					return Token.UNDEFINED;

				return token;
			}
		}
	}

	private static final String SOURCE_VERSION =
		"org.eclipse.jdt.core.compiler.source"; //$NON-NLS-1$

	private static String[] fgKeywords = { "abstract", //$NON-NLS-1$
		"break", //$NON-NLS-1$
		"case", //$NON-NLS-1$
			"catch", //$NON-NLS-1$
			"class", //$NON-NLS-1$
			"const", //$NON-NLS-1$
			"continue", //$NON-NLS-1$
		"default", "do", //$NON-NLS-2$ //$NON-NLS-1$
		"else", "extends", //$NON-NLS-2$ //$NON-NLS-1$
		"final", "finally", "for", //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
		"goto", //$NON-NLS-1$
		"if", //$NON-NLS-1$
			"implements", //$NON-NLS-1$
			"import", //$NON-NLS-1$
			"instanceof", //$NON-NLS-1$
			"interface", //$NON-NLS-1$
		"native", "new", //$NON-NLS-2$ //$NON-NLS-1$
		"package", //$NON-NLS-1$
			"private", //$NON-NLS-1$
			"protected", //$NON-NLS-1$
			"public", //$NON-NLS-1$
		"return", //$NON-NLS-1$
		"static", //$NON-NLS-1$
			"super", //$NON-NLS-1$
			"switch", //$NON-NLS-1$
			"synchronized", //$NON-NLS-1$
		"this", //$NON-NLS-1$
			"throw", //$NON-NLS-1$
			"throws", //$NON-NLS-1$
			"transient", //$NON-NLS-1$
			"try", //$NON-NLS-1$
		"volatile", //$NON-NLS-1$
		"while" //$NON-NLS-1$
	};

	// AspectJ keywords
    private static String[] ajKeywords = { "aspect", "pointcut", "privileged", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Pointcut designators: methods and constructora
		"call", "execution", "initialization", "preinitialization" , //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		// Pointcut designators: exception handlers
		"handler", //$NON-NLS-1$
		// Pointcut designators: fields
		"get", "set", //$NON-NLS-1$ //$NON-NLS-2$
		// Pointcut designators: static initialization
		"staticinitialization", //$NON-NLS-1$
		// Pointcut designators: object
		// (this already a Java keyword)
		"target", "args", //$NON-NLS-1$ //$NON-NLS-2$
		// Pointcut designators: lexical extents
		"within", "withincode", //$NON-NLS-1$ //$NON-NLS-2$
		// Pointcut designators: control flow
		"cflow", "cflowbelow", //$NON-NLS-1$ //$NON-NLS-2$
		// Pointcut Designators for annotations
		"annotation", //$NON-NLS-1$
		// Advice
		"before", "after", "around", "proceed", "throwing" , "returning" , //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		"adviceexecution" , //$NON-NLS-1$
		// Declarations
		"declare", "parents" , "warning" , "error", "soft" , "precedence" , //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		// variables
		"thisJoinPoint" , "thisJoinPointStaticPart" , "thisEnclosingJoinPointStaticPart" , //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Associations
		"issingleton", "perthis", "pertarget", "percflow", "percflowbelow", "pertypewithin" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	private static String[] fgNewKeywords = { "assert" }; //$NON-NLS-1$

	private static String[] fgTypes =
		{
			"void", //$NON-NLS-1$
			"boolean", //$NON-NLS-1$
			"char", //$NON-NLS-1$
			"byte", //$NON-NLS-1$
			"short", //$NON-NLS-1$
			"strictfp", //$NON-NLS-1$
			"int", //$NON-NLS-1$
			"long", //$NON-NLS-1$
			"float", //$NON-NLS-1$
			"double" }; //$NON-NLS-1$
	
	private static String[] fgConstants = { "false", "null", "true" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private static String[] fgTokenProperties =
		{
			IJavaColorConstants.JAVA_KEYWORD,
			IJavaColorConstants.JAVA_STRING,
			IJavaColorConstants.JAVA_DEFAULT };

	private VersionedWordRule fVersionedWordRule;

	/**
	 * Creates a Java code scanner
	 */
	public AspectJCodeScanner(IColorManager manager, IPreferenceStore store) {
		super(manager, store);

		initialize();
	}

	/*
	 * @see AbstractJavaScanner#getTokenProperties()
	 */
	protected String[] getTokenProperties() {
		return fgTokenProperties;
	}

	/*
	 * @see AbstractJavaScanner#createRules()
	 */
	protected List createRules() {

		//		System.err.println("AJCodeScanner.createRules() called");
		List rules = new ArrayList();

		// Add rule for strings and character constants.
		Token token = getToken(IJavaColorConstants.JAVA_STRING);
		rules.add(new SingleLineRule("\"", "\"", token, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
		rules.add(new SingleLineRule("'", "'", token, '\\')); //$NON-NLS-1$ //$NON-NLS-2$
	
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new JavaWhitespaceDetector()));

		// Add word rule for new keywords, 4077
		String version = getPreferenceStore().getString(SOURCE_VERSION);//JavaCore.getOptions().get(SOURCE_VERSION);
		//if (version instanceof String) {
		fVersionedWordRule =
			new VersionedWordRule(new JavaWordDetector(), "1.4", true, version); //$NON-NLS-1$

		token = getToken(IJavaColorConstants.JAVA_KEYWORD);
		for (int i = 0; i < fgNewKeywords.length; i++)
			fVersionedWordRule.addWord(fgNewKeywords[i], token);
		rules.add(fVersionedWordRule);
//		}

		// Add word rule for keywords, types, and constants.
		token = getToken(IJavaColorConstants.JAVA_DEFAULT);
		WordRule wordRule = new WordRule(new JavaWordDetector(), token);
		WordRule ajKeywordRule = new DotWordRule(new JavaWordDetector());
		token = getToken(IJavaColorConstants.JAVA_KEYWORD);

		for (int i = 0; i < fgKeywords.length; i++)
			wordRule.addWord(fgKeywords[i], token);

		for (int i = 0; i < fgTypes.length; i++)
			wordRule.addWord(fgTypes[i], token);

		for (int i = 0; i < fgConstants.length; i++)
			wordRule.addWord(fgConstants[i], token);
	
		for (int i = 0; i < ajKeywords.length; i++)
			ajKeywordRule.addWord(ajKeywords[i], token);

		//important: add ajKeywordRule before wordRule 
		rules.add(ajKeywordRule);
		rules.add(wordRule);

		setDefaultReturnToken(getToken(IJavaColorConstants.JAVA_DEFAULT));
		return rules;
	}

	/*
	 * @see RuleBasedScanner#setRules(IRule[])
	 */
	public void setRules(IRule[] rules) {
		int i;
		for (i = 0; i < rules.length; i++)
			if (rules[i].equals(fVersionedWordRule))
				break;

		// not found - invalidate fVersionedWordRule
		if (i == rules.length)
			fVersionedWordRule = null;

		super.setRules(rules);
	}

	/*
	 * @see AbstractJavaScanner#affectsBehavior(PropertyChangeEvent)
	 */
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return event.getProperty().equals(SOURCE_VERSION)
			|| super.affectsBehavior(event);
	}

	/*
	 * @see AbstractJavaScanner#adaptToPreferenceChange(PropertyChangeEvent)
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {

		if (event.getProperty().equals(SOURCE_VERSION)) {
			Object value = event.getNewValue();

			if (value instanceof String) {
				String s = (String) value;

				if (fVersionedWordRule != null)
					fVersionedWordRule.setCurrentVersion(s);
			}

		} else if (super.affectsBehavior(event)) {
			super.adaptToPreferenceChange(event);
		}
	}

	/**
	 * Utility method which returns true if the given string is a keyword that
	 * can appear in a pointcut definition.
	 * 
	 * @param word
	 * @return
	 */
	public static boolean isAjPointcutKeyword(String word) {
		for (int i = 0; i < ajKeywords.length; i++) {
			if (ajKeywords[i].equals(word)) {
				return true;
			}
		}
		// "this" and "if" are not in the aj list as they are java keywords
		if ("this".equals(word)) { //$NON-NLS-1$
			return true;
		}
		if ("if".equals(word)) { //$NON-NLS-1$
			return true;
		}
		return false;
	}
}
