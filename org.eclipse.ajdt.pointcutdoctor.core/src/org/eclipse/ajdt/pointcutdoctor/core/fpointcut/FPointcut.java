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
package org.eclipse.ajdt.pointcutdoctor.core.fpointcut;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.patterns.Bindings;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.FastMatchInfo;
import org.aspectj.weaver.patterns.IScope;
import org.aspectj.weaver.patterns.PatternNodeVisitor;
import org.aspectj.weaver.patterns.Pointcut;

public abstract class FPointcut extends Pointcut implements Cloneable {

	private boolean naiveSuggested = false;
	private FPointcut parent;
	
	public void setNaiveSuggested(boolean b) {
		naiveSuggested = b;
	}
	
	public boolean isNaiveSuggested(){
		return naiveSuggested;
	}


	/********************** START ALL DUMMY METHODS ************************/
	@Override
	public FuzzyBoolean fastMatch(FastMatchInfo info) {return null;}
	@Override
	public int couldMatchKinds() {return 0;}
	@Override
	protected FuzzyBoolean matchInternal(Shadow shadow) {return null;}
	@Override
	protected void resolveBindings(IScope scope, Bindings bindings) {}
	@Override
	protected Pointcut concretize1(ResolvedType inAspect, ResolvedType declaringType, IntMap bindings) {return null;}
	@Override
	protected Test findResidueInternal(Shadow shadow, ExposedState s) {	return null;}
	@Override
	public Pointcut parameterizeWith(Map<String, UnresolvedType> typeVariableMap, World w) {	return null;}
	@Override
	public void write(CompressingDataOutputStream s) throws IOException {}
	@Override
	public Object accept(PatternNodeVisitor visitor, Object data) {	return null;}
	/********************** END ALL DUMMY METHODS ************************/

	public FPointcut getParent() {
		return parent;
	}

	public void setParent(FPointcut parent) {
		this.parent = parent;
	}

	public void replaceChild(FPointcut fpct, FPointcut newfpct) {
		// the implementation is in FAndPointcut, FOrPointcut, FNotPointcut
	}

	/**
	 * clone the whole tree for fpct, and returns the pointcut corresponding to
	 * fpct
	 * @param fpct
	 * @return
	 * @throws CloneNotSupportedException 
	 */
	public static FPointcut cloneTreeAndMark(FPointcut fpct) throws CloneNotSupportedException {
		FPointcut root = fpct.getAncestor();
		List<FPointcut> newpct = new ArrayList<FPointcut>();
		root.cloneAndMark(fpct, newpct);
		if (newpct.size()==1) return newpct.get(0);
		else return null;
	}

	//TODO not conform to java convention!
	public FPointcut clone() throws CloneNotSupportedException {
		return cloneAndMark(null, null);
	}
	
	protected abstract FPointcut cloneAndMark(FPointcut fpct, List<FPointcut> newPct) throws CloneNotSupportedException;
 

	public FPointcut getAncestor() {
		FPointcut p = this;
		while(p.parent!=null) p = p.parent;
		return p;
	}

//	/**
//	 * @return all primitive pointcuts (those are not And,Or,Not pointcuts) as a list
//	 */
//	public List<FPointcut> flattern() {
//		List<FPointcut> lst = new ArrayList<FPointcut>();
//		visitPointcut(this, lst);
//		return lst;
//	}
//
//	private void visitPointcut(FPointcut fpct, List<FPointcut> lst) {
//		if (fpct instanceof FAndPointcut) {
//			FAndPointcut fapct = (FAndPointcut) fpct;
//			visitPointcut(fapct.getLeft(), lst);
//			visitPointcut(fapct.getRight(), lst);
//		} else if (fpct instanceof FOrPointcut) {
//			FOrPointcut fopct = (FOrPointcut) fpct;
//			visitPointcut(fopct.getLeft(), lst);
//			visitPointcut(fopct.getRight(), lst);
//		} else if (fpct instanceof FNotPointcut) {
//			visitPointcut(((FNotPointcut)fpct).getNegatePointcut(), lst);		
//		} else {
//			lst.add(fpct);
//		}
//	}
	

}
