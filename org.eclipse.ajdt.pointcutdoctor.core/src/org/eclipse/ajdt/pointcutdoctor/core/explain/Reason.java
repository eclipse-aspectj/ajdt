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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aspectj.util.FuzzyBoolean;
import org.eclipse.ajdt.pointcutdoctor.core.utils.StringUtils;



public class Reason {
//	public static enum ExplainOption {
//		True,
//		False,
//		Maybe
//	}

	private Set<ReasonConjunction> responsibleParts = new HashSet<ReasonConjunction>();
	//private Map<Part, String> partTextualMap = new HashMap<Part, String>();
//	private ExplainOption option;
	private FuzzyBoolean matchResult;
	
	public Reason(FuzzyBoolean matchResult, AtomicPart... parts) {
		this.matchResult = matchResult;
		for (AtomicPart part:parts)
			responsibleParts.add(new ReasonConjunction(part));
	}

	public Reason and(Reason rightReason) {
		if (this.isEmpty() || rightReason.isEmpty())
			return new Reason(matchResult); // empty reason
		return union(rightReason);
		//XXX the following code has exponential complexity...
//		Set<ReasonConjunction> newParts = new HashSet<ReasonConjunction>();
//		for (ReasonConjunction left:responsibleParts)
//			for(ReasonConjunction right:rightReason.responsibleParts)
//				newParts.add(left.and(right));
//		Reason newReason = new Reason(matchResult);
//		newReason.responsibleParts = newParts;
//		return newReason;
	}

	public Reason union(Reason rightReason) {
		Reason newReason = new Reason(matchResult);
		newReason.responsibleParts.addAll(responsibleParts);
		newReason.responsibleParts.addAll(rightReason.responsibleParts);
		return newReason;
	}
	
	public String toString() {
		return StringUtils.join(",", responsibleParts);
	}
	
	static class ReasonConjunction {
		private Set<AtomicPart> conParts = new HashSet<AtomicPart>();
		public ReasonConjunction(AtomicPart... parts) {
			for(AtomicPart p:parts)
				conParts.add(p);
		}
		public ReasonConjunction and(ReasonConjunction right) {
			ReasonConjunction newReason = new ReasonConjunction();
			newReason.conParts.addAll(conParts);
			newReason.conParts.addAll(right.conParts);
			return newReason;
		}
		public String toString() {
			return StringUtils.join("^", conParts);
		}
		//TODO equals and hashCode has not been implemented yet.
	}

	public int getTextStart() {
		List<AtomicPart> allParts = collectAllParts();
		return allParts.size()>0 ? allParts.get(0).getOffset() : -1;
	}

	public int getTextLength() {
		List<AtomicPart> allParts = collectAllParts();
		AtomicPart lastPart = allParts.size()>0 ? allParts.get(allParts.size()-1) : null;
		return  lastPart!=null ? lastPart.getOffset()+lastPart.getLength() - allParts.get(0).getOffset()+1: -1;
	}

	private List<AtomicPart> collectAllParts() {
		//TODO not optimized
		List<AtomicPart> allParts = new ArrayList<AtomicPart>();
		for (ReasonConjunction rs:responsibleParts) 
			for (AtomicPart p:rs.conParts)
				if (p.getLength()>0)
					allParts.add(p);
		Collections.sort(allParts, new Comparator<AtomicPart>() {
			public int compare(AtomicPart arg0, AtomicPart arg1) {
				int o0 = arg0.getOffset();
				int o1 = arg1.getOffset();
				return o0==o1 ? 0 : (o0>o1 ? 1: -1);
			}
		});
		return allParts;
	}

	public List<AtomicPart> getAllParts() {
		return collectAllParts();
	}

	public boolean isEmpty() {
		return responsibleParts.isEmpty();
	}

	public String getFullTextExplanation() {
//		if (partTextualMap.isEmpty())
//			return null;
//		// TODO better way to represent AND, OR in reason?
//		String nl="\n";
//		String start = "Not matched because:"+nl;
//		String s = "";
//		for (Part p:partTextualMap.keySet()) {
//			if (s.length()>0) s+=nl;
//			s+=" - "+partTextualMap.get(p);
//		}
//		return start+s;
		List<AtomicPart> parts = collectAllParts();
		// TODO better way to represent AND, OR in reason?
		String nl="\n";
		String matchedS;
		if (matchResult.alwaysTrue())
			 matchedS = "Matched";
		else if (matchResult.alwaysFalse())
			 matchedS = "Not matched";
		else matchedS = "Maybe";
		String start = matchedS+" because:"+nl;
		String s = "";
		for (AtomicPart p:parts) {
			if (s.length()>0) s+=nl;
			if (p.getTextual().length()>0) s+=" - "+p.getTextual();
		}
		if (s.length()>0)
			return start+s;
		else return "";
	}

	
	public String getTexualReasonByFileAndOffset(String filename, int offset) {
		AtomicPart part = findPartByFileAndOffset(filename, offset);
		if (part!=null)
			return part.getTextual();
		else return null;
	}

	private AtomicPart findPartByFileAndOffset(String filename, int offset) {
		// TODO a bit slow
		List<AtomicPart> parts = collectAllParts();
		for (AtomicPart p:parts) {
			File f = p.getEnclosingFile();
			//TODO this is basically an error, but we keep it for now,
			// since we don't know how to get the full path from a JavaElement
			// toPath().toOSString only returns a path relative to workspace
			if (f.getAbsolutePath().endsWith(filename) && 
					(offset>=p.getOffset()&& offset<=p.getOffset()+p.getLength()))
				return p;
		}
		return null;
	}

}
