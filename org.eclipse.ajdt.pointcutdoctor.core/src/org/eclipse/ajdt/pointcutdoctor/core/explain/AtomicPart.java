/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.core.explain;

import java.io.File;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.patterns.PatternNode;
import org.aspectj.weaver.patterns.Pointcut;

public abstract class AtomicPart extends AbstractPart {

	protected Pointcut pointcut;
	protected PatternNode node; //the fragment that corresponds to this part, e.g. declaringType, modifier etc

	protected int offset = -1;
	protected int length = -1;

	private String textual;

	protected ExplainMessage explainMessage;

	public AtomicPart(Pointcut pointcut, PatternNode node) {
		this.pointcut = pointcut;
		this.node = node;
	}

	public Reason explain(Shadow shadow, FuzzyBoolean matchResult) {
		match = isMatched(shadow);
		
		textual = explainTextual(shadow);
		
		Reason emptyReason = new Reason(matchResult);
		Reason thisAsReason = new Reason(matchResult,this);
		if (matchResult.alwaysTrue())
			return match.alwaysTrue()? thisAsReason : emptyReason; 
		else if (matchResult.alwaysFalse())
			return match.alwaysFalse()? thisAsReason : emptyReason;
		else // maybe
			return match.maybeTrue()&&!(match.alwaysTrue()) ? thisAsReason : emptyReason; //TODO??
	}

	private String explainTextual(Shadow shadow) {
//		if (match) return "Matched because "+explainMatchTextual(shadow);
//		else return "Not matched because "+explainMisMatchTextual(shadow);
		if (match.maybeTrue()) return explainMatchTextual(shadow);
		else return explainMisMatchTextual(shadow);
	}
	
	protected String formatAndUpdateExplainMessage(ExplainMessage msg, Object... params) {
		explainMessage = msg;
		return String.format(msg.getMessage(), params);
	}

	/**
	 * 	here, we specify the first level of the explanation.
	 *  this method is expected to be overriden to specify more specific explanations
	 * @param shadow
	 * @return the textual reason
	 */
	protected String explainMisMatchTextual(Shadow shadow) {
		String pattern = patternAsString();
		String part = getJoinPointPartName();
		String joinPointPartValue = getJoinPointPartValue(shadow);
		//[pattern] doesn't match the [part] of this [joinpoint]
		String txt = formatAndUpdateExplainMessage(ExplainMessage.MSG0, 
				part, joinPointPartValue, pattern);
		return txt;
	}

	protected abstract String getJoinPointPartValue(Shadow shadow);

	/**
	 * 	here, we specify the first level of the explanation.
	 *  this method is expected to be overriden to specify more specific explanations
	 * @param shadow
	 * @return the textual reason
	 */
	protected String explainMatchTextual(Shadow shadow) {
		String pattern = patternAsString();
		String part = getJoinPointPartName();
		String joinPointPartValue = getJoinPointPartValue(shadow);
		String txt = formatAndUpdateExplainMessage(ExplainMessage.M_MSG0, 
				part, joinPointPartValue, pattern);
		return txt;
	}

	/**
	 * @return the string representation of the pattern in a specific part. 
	 *  e.g. for ArgsPart, it's "args(...)", for DeclaringTypePart, it's "Foo"  
	 */
	protected String patternAsString() {
		return node.toString();
	}

	/**
	 * @return the part in the joinpoint to be matched against this part,
	 * e.g. for ArgsPart, it's "parameters", for DeclaringTypePart, it's "declaring type"; 
	 */
	protected abstract String getJoinPointPartName();

	protected abstract FuzzyBoolean isMatched(Shadow shadow);

	public int getLength() {
		if (length<0) computeOffsetLengthInSource();
		return length;
	}

	public int getOffset() {
		if (offset<0) computeOffsetLengthInSource();
		return offset;
	}

	protected void computeOffsetLengthInSource() {
		offset = node.getStart();
		if (offset<=0 && node.getEnd()<=0)
			length = 0;
		else
			length = node.getEnd()-offset+1;
	}

	public FuzzyBoolean getMatchResult() {
		return match;
	}

	public File getEnclosingFile() {
		if (pointcut!=null)
			return pointcut.getSourceLocation().getSourceFile();
		return null;
	}

	public String getTextual() {
		return textual;
	}

	public ExplainMessage getTextualCode() {
		return explainMessage;
	}

	@Override
	public String toString() {
	    return this.getClass().getName() + " : " + pointcut.toString();
	}
}
