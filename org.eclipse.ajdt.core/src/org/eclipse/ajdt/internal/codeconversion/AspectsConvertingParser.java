/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.codeconversion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.core.compiler.InvalidInputException;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * The purpose of this parser is to convert AspectJ code into similar Java code
 * which allows us to reuse for example for jdt formatting or comment
 * generation.
 * 
 * Depending on the ConversionOptions it gets called with, it does: - replace
 * the keyword "aspect" by "class " - replace all the '.'s in intertype
 * declarations by an '$'s to make them look like an ordinary declarations. -
 * erase the keywords "returning", "throwing", "privileged", "issingleton" e.g.:
 * "after() throwing (Exception e)" -> "after( Exception e)" - erase pointcut
 * designators (includes "percflow" & co.) - add dummy references to all erased
 * class references to end of buffer to make "organize imports" work correctly -
 * add a reference to the target class inside intertype method declarations to
 * simulate the context switch necessary to get proper code completion.
 * (A detailed description of how code completion works in AJDT can be found in
 * bug 74419.)
 * 
 * Restrictions: - class names inside pointcut designators must begin with a
 * capital letter to be recognised as such
 * 
 * 
 * @author Luzius Meisser
 */
public class AspectsConvertingParser implements TerminalTokens {

	public char[] content;

	private Set typeReferences;

	private Set usedIdentifiers;

	private ConversionOptions options;

	//list of replacements
	//by convetion: sorted by posBefore in ascending order
	private ArrayList replacements;

	protected Scanner scanner;

	public AspectsConvertingParser(char[] content) {
		this.content = content;
		this.typeReferences = new HashSet();
		this.usedIdentifiers = new HashSet();
		replacements = new ArrayList(5);
	}

	private static final char[] throwing = "throwing".toCharArray();

	private static final char[] returning = "returning".toCharArray();

	private static final char[] percflow = "percflow".toCharArray();

	private static final char[] percflowbelow = "percflowbelow".toCharArray();

	private static final char[] perthis = "perthis".toCharArray();

	private static final char[] pertarget = "pertarget".toCharArray();

	private static final char[] issingleton = "issingleton".toCharArray();

	private static final char[] classs = "class ".toCharArray();

	private static final char[] privileged = "          ".toCharArray();

	private static final String thizString = "thiz";

	private boolean insidePointcutDesignator;

	private boolean insideAspect;

	private boolean insideAspectDeclaration;
	
	public class Replacement {
		//the position in the original char[]
		public int posBefore;

		//the position in the new char[], or -1 if not yet applied
		public int posAfter;

		//the number of chars that get replaced
		public int length;

		//the content to be inserted
		public char[] text;

		//the number of additional chars (lengthAdded == text.length - length)
		public int lengthAdded;

		public Replacement(int pos, int length, char[] text) {
			this.posBefore = pos;
			this.posAfter = -1;
			this.length = length;
			this.text = text;
			lengthAdded = text.length - length;
		}

	}

	int posColon;

	//returns a list of Insertions to let the client now what has been inserted
	//into the buffer so he can translate positions from the old into the new
	//buffer
	public ArrayList convert(ConversionOptions options) {
		this.options = options;
		boolean insertThisJoinPointReferences = options
				.isThisJoinPointReferencesEnabled();
		boolean addReferencesForOrganizeImports = options
				.isDummyTypeReferencesForOrganizeImportsEnabled();
		boolean isSimulateContextSwitchNecessary = (options.getTargetType() != null);

		scanner = new Scanner();
		scanner.setSource(content);

		insidePointcutDesignator = false;
		insideAspect = false;
		insideAspectDeclaration = false;
		boolean insideBlock = false;
		replacements.clear();
		typeReferences.clear();
		usedIdentifiers.clear();

		int tok;
		int pos;
		while (true) {

			try {
				tok = scanner.getNextToken();
			} catch (InvalidInputException e) {
				continue;
			}
			if (tok == TokenNameEOF)
				break;

			switch (tok) {
			case TokenNameIdentifier:
				if (!insideAspect)
					break;

				char[] name = scanner.getCurrentIdentifierSource();

				if (insideAspectDeclaration && !insidePointcutDesignator) {
					if (CharOperation.equals(percflow, name))
						startPointcutDesignator();
					else if (CharOperation.equals(percflowbelow, name))
						startPointcutDesignator();
					else if (CharOperation.equals(perthis, name))
						startPointcutDesignator();
					else if (CharOperation.equals(pertarget, name))
						startPointcutDesignator();
					else if (CharOperation.equals(issingleton, name))
						startPointcutDesignator();
				}

				if (CharOperation.equals(throwing, name))
					consumeRetOrThro();
				else if (CharOperation.equals(returning, name))
					consumeRetOrThro();
				else if (insidePointcutDesignator
						&& Character.isUpperCase(name[0]))
					typeReferences.add(new String(name));

				if (isSimulateContextSwitchNecessary) {
					usedIdentifiers.add(new String(name));
				}
				break;

			case TokenNameCOLON:
				if (!insideAspect)
					break;
				if (insideBlock)
					break;
				startPointcutDesignator();
				break;

			case TokenNameSEMICOLON:
				if (insidePointcutDesignator)
					endPointcutDesignator();
				break;

			case TokenNameDOT:
				if (!insideAspect)
					break;
				if (insidePointcutDesignator)
					break;
				processPotentialIntertypeDeclaration();
				break;

			case TokenNameLBRACE:
				if (insidePointcutDesignator) {
					endPointcutDesignator();
					//must be start of advice body -> insert tjp reference
					if (insertThisJoinPointReferences
							&& !insideAspectDeclaration)
						addReplacement(
								scanner.getCurrentTokenStartPosition() + 1, 0,
								tjpRefs2);
				}
				insideAspectDeclaration = false;
				insideBlock = true;
				break;
			case TokenNameRBRACE:
				insideBlock = false;
				break;
			case TokenNameaspect:
				insideAspect = true;
				insideAspectDeclaration = true;
				pos = scanner.getCurrentTokenStartPosition();
				addReplacement(pos, classs.length, classs);
				break;

			case TokenNameprivileged:
				pos = scanner.getCurrentTokenStartPosition();
				addReplacement(pos, privileged.length, privileged);
				break;

			}

		}

		if (addReferencesForOrganizeImports)
			addReferences();

		if (isSimulateContextSwitchNecessary)
			simulateContextSwitch(options.getCodeCompletePosition(), options
					.getTargetType());

		applyReplacements();

		//System.out.println(new String(content));
		return replacements;
	}

	/**
	 * Inserts a reference to targetType at the given position. Thanks to this,
	 * we can simulate the context switch necessary in intertype method
	 * declarations.
	 * 
	 * Transformations: - Insertion of local variable 'TargetType thiz' (or, if
	 * thiz is already used, a number is added to thiz to make it unique) at
	 * start of method mody
	 *  - if code completion on code like 'this.methodcall().more...', 'this
	 * gets replaced by thiz
	 *  - if code completion on code like 'methodcall().more...', 'thiz.' gets
	 * added in front.
	 * 
	 * How the correct place for insertion is found: -
	 * 
	 *  
	 */
	private void simulateContextSwitch(int position, char[] targetType) {
		int pos = findInsertionPosition(position - 1) + 1;
		//if code completion on 'this' -> overwrite the this keyword
		int len = 0;
		if ((content[pos] == 't') && (content[pos + 1] == 'h')
				&& (content[pos + 2] == 'i') && (content[pos + 3] == 's')
				&& !Character.isJavaIdentifierPart(content[pos + 4]))
			len = 4;

		String ident = findFreeIdentifier();
		char[] toInsert = (new String(targetType) + ' ' + ident + ';' + ident + '.')
				.toCharArray();
		addReplacement(pos, len, toInsert);

	}

	/**
	 * @return An unused identifier
	 */
	private String findFreeIdentifier() {
		int i = 0;
		String ident = thizString + i;
		while (usedIdentifiers.contains(ident)) {
			i++;
			ident = thizString + i;
		}
		return ident;
	}

	/**
	 * @param pos -
	 *            a code position
	 * @return the position that defines the context of the current one at the
	 *         highest level
	 * 
	 * e.g. ' this.doSomthing().get' with pos on the last 't' returns the
	 * position of the char before the first 't'
	 */
	private int findInsertionPosition(int pos) {
		char ch = content[pos];
		int currentPos = pos;

		if (Character.isWhitespace(ch)) {
			currentPos = findPreviousNonSpace(pos);
			if (currentPos == -1)
				return pos;

			ch = content[currentPos];
			if (ch == '.')
				return findInsertionPosition(--currentPos);
			else
				return pos;
		}

		if (Character.isJavaIdentifierPart(ch)) {
			while (Character.isJavaIdentifierPart(ch)) {
				currentPos--;
				ch = content[currentPos];
			}
			return findInsertionPosition(currentPos);
		}

		if (ch == '.') {
			return findInsertionPosition(--pos);
		}

		if (ch == ')') {
			currentPos--;
			int bracketCounter = 1;
			while (currentPos >= 0) {
				ch = content[currentPos];
				if (bracketCounter == 0)
					break;
				if (ch == ')')
					bracketCounter++;
				if (ch == '(') {
					bracketCounter--;
					if (bracketCounter < 0)
						return -1;
				}
				currentPos--;
			}
			return findInsertionPosition(currentPos);
		}

		return pos;
	}

	char[] tjpRefs2 = "org.aspectj.lang.JoinPoint thisJoinPoint; org.aspectj.lang.JoinPoint.StaticPart thisJoinPointStaticPart;"
			.toCharArray();

	char[] tjpRefs = "".toCharArray();

	//	Same as applyReplacements, but without creating new char[] all the time.
	//	Seems to work, but surprisingly, it does not seem to be significantly
	// faster
	//	-> kept using the simple version
	//	
	//	private void applyReplacementsFast() {
	//		Iterator iter = replacements.listIterator();
	//		int totalLengthToAdd = 0;
	//		while (iter.hasNext()) {
	//			Replacement ins = (Replacement) iter.next();
	//			totalLengthToAdd += ins.lengthAdded;
	//		}
	//		
	//		iter = replacements.listIterator();
	//		if (totalLengthToAdd == 0){
	//			//content still has same length, we do not need
	//			//to allocate a new char[]
	//			while (iter.hasNext()) {
	//				Replacement ins = (Replacement) iter.next();
	//				System.arraycopy(ins.text, 0, content, ins.pos, ins.text.length);
	//			}
	//		} else {
	//			char[] temp = new char[content.length + totalLengthToAdd];
	//			//content gets longer -> new char[] necessary
	//			int offset = 0;
	//			int currentPosSource = 0;
	//			int currentPosDest = 0;
	//			while (iter.hasNext()) {
	//				Replacement ins = (Replacement) iter.next();
	//				int len = ins.pos - currentPosSource;
	//				//copy piece between last replacement and this one
	//				System.arraycopy(content, currentPosSource, temp, currentPosDest, len);
	//				currentPosSource += len;
	//				currentPosDest += len;
	//				//insert replacement
	//				System.arraycopy(ins.text, 0, temp, currentPosDest, ins.text.length);
	//				currentPosDest += ins.text.length;
	//				currentPosSource += ins.length;
	//				
	//				//convention: update position of edits to be relative to new content
	//				ins.pos += offset;
	//				offset += ins.lengthAdded;
	//			}
	//			//copy last piece of content
	//			System.arraycopy(content, currentPosSource, temp, currentPosDest,
	// content.length - currentPosSource);
	//			content = temp;
	//		}
	//	}

	private void applyReplacements() {
		Iterator iter = replacements.listIterator();
		int offset = 0;
		while (iter.hasNext()) {
			Replacement ins = (Replacement) iter.next();
			ins.posAfter = ins.posBefore + offset;
			replace(ins.posAfter, ins.length, ins.text);
			offset += ins.lengthAdded;
		}
	}

	private void replace(int pos, int length, char[] text) {
		if (length != text.length) {
			int toAdd = text.length - length;
			char[] temp = new char[content.length + toAdd];
			System.arraycopy(content, 0, temp, 0, pos);
			System.arraycopy(content, pos, temp, pos + toAdd, content.length
					- pos);
			content = temp;
		}
		System.arraycopy(text, 0, content, pos, text.length);
	}

	private void startPointcutDesignator() {
		if (insidePointcutDesignator)
			return;
		insidePointcutDesignator = true;
		posColon = scanner.getCurrentTokenStartPosition();
	}

	/**
	 *  
	 */
	private void endPointcutDesignator() {
		insidePointcutDesignator = false;
		int posSemi = scanner.getCurrentTokenStartPosition();
		int len = posSemi - posColon;
		char[] empty = new char[len];
		for (int i = 0; i < empty.length; i++) {
			empty[i] = ' ';
		}
		addReplacement(posColon, len, empty);
	}

	private char[] spaceAndDot = { ' ', '.' };

	//identifies intertype declaration of form 'type qualifier.membername'
	//and replaces every '.' by '$'.
	//e.g. "int tracing.Circle.x;" -> "int tracing$Circle$x;"
	private void processPotentialIntertypeDeclaration() {

		//pos points to the '.'
		int pos = scanner.getCurrentTokenStartPosition();

		//check if valid identifier char on left side of dot
		//(to sort out construct like '(new Object()).' )
		int nonspace1 = findPreviousNonSpace(pos - 1);
		if (nonspace1 == -1)
			return;
		if (!Character.isJavaIdentifierPart(content[nonspace1]))
			return;

		//check if there is another java identifier before qualifier,
		//if no, return (to sort out method calls and the like)
		int space = findPreviousSpace(nonspace1);
		if (space == -1)
			return;
		int nonspace2 = findPreviousNonSpace(space);
		if (nonspace2 == -1)
			return;
		if (!Character.isJavaIdentifierPart(content[nonspace2]))
			return;

		//check if rightmost part of qualifier starts with Capital letter,
		//and if yes, assume it is a Class name -> intertype declaration
		int spaceordot = findPrevious(spaceAndDot, nonspace1);
		if (spaceordot == -1)
			return;
		if (Character.isUpperCase(content[spaceordot + 1])) {

			//assume intertype declaration and replace all '.' by '$'
			char[] rep = new char[] { '$' };
			addReplacement(pos, 1, rep);

			if (content[spaceordot] == ' ') {
				String type = new String(content, space + 1, pos - space - 1);
				boolean validIdentifier = true;
				for (int i = 0; validIdentifier && (i < type.length()); i++) {
					char c = type.charAt(i);
					if (i==0) {
						if (!Character.isJavaIdentifierStart(c)) {
							validIdentifier = false;
						}
					} else if (!Character.isJavaIdentifierPart(c)) {
						validIdentifier = false;
					}
				}
				if (validIdentifier) {
					typeReferences.add(type);
				}
			} else {
				do {
					addReplacement(spaceordot, 1, rep);
					spaceordot = findPrevious(spaceAndDot, --spaceordot);
				} while (content[spaceordot] == '.');
			}

			//if requested, add ajc$ in front of intertype declaration
			//e.g. "public int Circle$x;" -> "public int ajc$Circle$x;"
			if (options.isAddAjcTagToIntertypesEnabled()) {
				addReplacement(spaceordot + 1, 0, "ajc$".toCharArray());
			}

		} //else {
		//			System.out.println("skipped because of upper case rule: " + new
		// String(content, space+1, pos-space-1));
		//		}
	}

	public int findPrevious(char ch, int pos) {
		while (pos >= 0) {
			if (content[pos] == ch)
				return pos;
			pos--;
		}
		return -1;
	}

	public int findPrevious(char[] chs, int pos) {
		while (pos >= 0) {
			for (int i = 0; i < chs.length; i++) {
				if (content[pos] == chs[i])
					return pos;
			}
			pos--;
		}
		return -1;
	}

	public int findPreviousSpace(int pos) {
		while (pos >= 0) {
			if (Character.isWhitespace(content[pos]))
				return pos;
			pos--;
		}
		return -1;
	}

	public int findPreviousNonSpace(int pos) {
		while (pos >= 0) {
			if (!Character.isWhitespace(content[pos]))
				return pos;
			pos--;
		}
		return -1;
	}

	public int findNext(char[] chs, int pos) {
		while (pos < content.length) {
			for (int i = 0; i < chs.length; i++) {
				if (content[pos] == chs[i])
					return pos;
			}
			pos++;
		}
		return -1;
	}

	char[] endThrow = new char[] { '(', ':' };

	public void consumeRetOrThro() {
		int pos = scanner.getCurrentTokenStartPosition();
		char[] content = scanner.source;

		int end = findNext(endThrow, pos);
		if (end == -1)
			return;

		char[] temp = null;
		if (content[end] == endThrow[0]) {
			pos = findPrevious(')', pos);
			if (pos == -1)
				return;
			int advicebracket = findPrevious('(', pos);
			if (advicebracket == -1)
				return;
			temp = new char[end - pos + 1];
			if (bracketsContainSomething(advicebracket)
					&& bracketsContainSomething(end))
				temp[0] = ',';
			else
				temp[0] = ' ';
			for (int i = 1; i < temp.length; i++) {
				temp[i] = ' ';
			}
		} else {
			temp = new char[end - pos];
			for (int i = 0; i < temp.length; i++) {
				temp[i] = ' ';
			}
		}
		addReplacement(pos, temp.length, temp);
	}

	/**
	 * @param end
	 * @return
	 */
	private boolean bracketsContainSomething(int start) {
		while (++start < content.length) {
			if (content[start] == ')')
				return false;
			if (Character.isJavaIdentifierPart(content[start]))
				return true;
		}
		return false;
	}

	private int findLast(char ch) {
		int pos = content.length;
		while (--pos >= 0) {
			if (content[pos] == ch)
				break;
		}
		return pos;
	}

	//adds references to all used type -> organize imports will work
	void addReferences() {
		if (typeReferences == null)
			return;

		//char[] decl = new char[] { ' ', 'x', ';' };
		int pos = findLast('}');
		if (pos < 0)
			return;
		StringBuffer temp = new StringBuffer(typeReferences.size() * 10);
		Iterator iter = typeReferences.iterator();
		int varCount=1;
		while (iter.hasNext()) {
			String ref = (String) iter.next();
			temp.append(ref);
			temp.append(" x"); //$NON-NLS-1$
			temp.append(varCount++);
			temp.append(';');
		}
		char[] decls = new char[temp.length()];
		temp.getChars(0, decls.length, decls, 0);
		addReplacement(pos, 0, decls);
	}

	//adds a replacement to list
	//pre: list sorted, post: list sorted
	void addReplacement(int pos, int length, char[] text) {
		int last = replacements.size() - 1;
		while (last >= 0) {
			if (((Replacement) replacements.get(last)).posBefore < pos)
				break;
			last--;
		}
		replacements.add(last + 1, new Replacement(pos, length, text));
	}

	public static boolean conflictsWithAJEdit(int offset, int length,
			ArrayList replacements) {
		Replacement ins;
		for (int i = 0; i < replacements.size(); i++) {
			ins = (Replacement) replacements.get(i);
			if ((offset >= ins.posAfter) && (offset < ins.posAfter + ins.length)) {
				return true;
			}
			if ((offset < ins.posAfter) && (offset + length > ins.posAfter)) {
				return true;
			}
		}
		return false;
	}
	
	//translates a position from after to before changes
	//if the char at that position did not exist before, it returns the
	// position before the inserted area
	public static int translatePositionToBeforeChanges(int posAfter,
			ArrayList replacements) {
		Replacement ins;
		int offset = 0, i;

		for (i = 0; i < replacements.size(); i++) {
			ins = (Replacement) replacements.get(i);
			if (ins.posAfter > posAfter)
				break;
			offset += ins.lengthAdded;
		}
		if (i > 0) {
			ins = (Replacement) replacements.get(i - 1);
			if (ins.posAfter + ins.text.length > posAfter) {
				//diff must be > 0
				int diff = posAfter - ins.posAfter;
				if (diff > ins.length)
					//we are in inserted area -> return pos directly before
					// that area
					offset += diff - ins.length;
			}
		}

		return posAfter - offset;
	}

	//translates a position from before to after changes
	public static int translatePositionToAfterChanges(int posBefore,
			ArrayList replacements) {
		for (int i = 0; i < replacements.size(); i++) {
			Replacement ins = (AspectsConvertingParser.Replacement) replacements
					.get(i);
			if (ins.posAfter <= posBefore)
				posBefore += ins.lengthAdded;
			else
				return posBefore;
		}
		return posBefore;
	}

}