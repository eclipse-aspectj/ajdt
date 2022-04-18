/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Luzius Meisser - Copied into AJDT
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.parserbridge;

/*
 * Luzius:
 * Content has been copied from
 * org.eclipse.jdt.internal.core.util.CommentRecorderParser
 */

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.parser.Parser;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * Internal parser used for parsing source to create DOM AST nodes.
 *
 * @since 3.0
 */
public class CommentRecorderParser extends Parser {

	// support for comments
	int[] commentStops = new int[10];
	int[] commentStarts = new int[10];
	int commentPtr = -1; // no comment test with commentPtr value -1
	protected final static int CommentIncrement = 100;

	/**
	 * @param problemReporter
	 * @param optimizeStringLiterals
	 */
	public CommentRecorderParser(ProblemReporter problemReporter, boolean optimizeStringLiterals) {
		super(problemReporter, optimizeStringLiterals);
	}

	// old javadoc style check which doesn't include all leading comments into declaration
	// for backward compatibility with 2.1 DOM
	public void checkComment() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.Parser#consumeClassHeader()
	 */
	protected void consumeClassHeader() {
		pushOnCommentsStack(0, this.scanner.commentPtr);
		super.consumeClassHeader();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.Parser#consumeEmptyClassMemberDeclaration()
	 */
	protected void consumeEmptyClassMemberDeclaration() {
		pushOnCommentsStack(0, this.scanner.commentPtr);
		super.consumeEmptyTypeDeclaration();//EmptyClassMemberDeclaration();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.Parser#consumeEmptyTypeDeclaration()
	 */
	protected void consumeEmptyTypeDeclaration() {
		pushOnCommentsStack(0, this.scanner.commentPtr);
		super.consumeEmptyTypeDeclaration();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.Parser#consumeInterfaceHeader()
	 */
	protected void consumeInterfaceHeader() {
		pushOnCommentsStack(0, this.scanner.commentPtr);
		super.consumeInterfaceHeader();
	}

	/**
	 * Insure that start position is always positive.
	 * @see org.eclipse.jdt.internal.compiler.parser.Parser#containsComment(int, int)
	 */
	public boolean containsComment(int sourceStart, int sourceEnd) {
		int iComment = this.scanner.commentPtr;
		for (; iComment >= 0; iComment--) {
			int commentStart = this.scanner.commentStarts[iComment];
			if (commentStart < 0) {
				commentStart = -commentStart;
			}
			// ignore comments before start
			if (commentStart < sourceStart) continue;
			// ignore comments after end
			if (commentStart > sourceEnd) continue;
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.Parser#endParse(int)
	 */
	protected CompilationUnitDeclaration endParse(int act) {
		CompilationUnitDeclaration unit = super.endParse(act);
		if (unit.comments == null) {
			pushOnCommentsStack(0, this.scanner.commentPtr);
			unit.comments = getCommentsPositions();
		}
		return unit;
	}

	/* (non-Javadoc)
	 * Save all source comments currently stored before flushing them.
	 * @see org.eclipse.jdt.internal.compiler.parser.Parser#flushCommentsDefinedPriorTo(int)
	 */
	public int flushCommentsDefinedPriorTo(int position) {

		int lastCommentIndex = this.scanner.commentPtr;
		if (lastCommentIndex < 0) return position; // no comment

		// compute the index of the first obsolete comment
		int index = lastCommentIndex;
		int validCount = 0;
		while (index >= 0){
			int commentEnd = this.scanner.commentStops[index];
			if (commentEnd < 0) commentEnd = -commentEnd; // negative end position for non-javadoc comments
			if (commentEnd <= position){
				break;
			}
			index--;
			validCount++;
		}
		// if the source at <position> is immediately followed by a line comment, then
		// flush this comment and shift <position> to the comment end.
		if (validCount > 0){
			int immediateCommentEnd;
			while (index<lastCommentIndex && (immediateCommentEnd = -this.scanner.commentStops[index+1])  > 0){ // only tolerating non-javadoc comments (non-javadoc comment end positions are negative)
				// is there any line break until the end of the immediate comment ? (thus only tolerating line comment)
				immediateCommentEnd--; // comment end in one char too far
				if (this.scanner.getLineNumber(position) != this.scanner.getLineNumber(immediateCommentEnd)) break;
				position = immediateCommentEnd;
				validCount--; // flush this comment
				index++;
			}
		}

		if (index < 0) return position; // no obsolete comment
		pushOnCommentsStack(0, index); // store comment before flushing them

		if (validCount > 0){ // move valid comment infos, overriding obsolete comment infos
			System.arraycopy(this.scanner.commentStarts, index + 1, this.scanner.commentStarts, 0, validCount);
			System.arraycopy(this.scanner.commentStops, index + 1, this.scanner.commentStops, 0, validCount);
		}
		this.scanner.commentPtr = validCount - 1;
		return position;
	}

	/*
	 * Build a n*2 matrix of comments positions.
	 * For each position, 0 is for start position and 1 for end position of the comment.
	 */
	public int[][] getCommentsPositions() {
		int[][] positions = new int[this.commentPtr+1][2];
		for (int i = 0, max = this.commentPtr; i <= max; i++){
			positions[i][0] = this.commentStarts[i];
			positions[i][1] = this.commentStops[i];
		}
		return positions;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.parser.Parser#initialize()
	 */
	public void initialize() {
		super.initialize();
		this.commentPtr = -1;
	}

	/*
	 * Push all stored comments in stack.
	 */
	private void pushOnCommentsStack(int start, int end) {

		for (int i=start; i<=end; i++) {
			// First see if comment hasn't been already stored
			int scannerStart = this.scanner.commentStarts[i]<0 ? -this.scanner.commentStarts[i] : this.scanner.commentStarts[i];
			int commentStart = this.commentPtr == -1 ? -1 : (this.commentStarts[this.commentPtr]<0 ? -this.commentStarts[this.commentPtr] : this.commentStarts[this.commentPtr]);
			if (commentStart == -1 ||  scannerStart > commentStart) {
				int stackLength = this.commentStarts.length;
				if (++this.commentPtr >= stackLength) {
					System.arraycopy(
						this.commentStarts, 0,
						this.commentStarts = new int[stackLength + CommentIncrement], 0,
						stackLength);
					System.arraycopy(
						this.commentStops, 0,
						this.commentStops = new int[stackLength + CommentIncrement], 0,
						stackLength);
				}
				this.commentStarts[this.commentPtr] = this.scanner.commentStarts[i];
				this.commentStops[this.commentPtr] = this.scanner.commentStops[i];
			}
		}
	}
	/* (non-Javadoc)
	 * Save all source comments currently stored before flushing them.
	 * @see org.eclipse.jdt.internal.compiler.parser.Parser#resetModifiers()
	 */
	protected void resetModifiers() {
		pushOnCommentsStack(0, this.scanner.commentPtr);
		super.resetModifiers();
	}
}
