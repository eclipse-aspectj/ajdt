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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SignaturePattern;

public class FlatternedPointcut extends FPointcut {
	private Kind kind;
	private List<Pointcut> children;

	public enum Kind {
		AND, OR;
	}	
	
	public FlatternedPointcut(Kind kind, Pointcut left, Pointcut right) {
		if (!flatternable(kind, left, right)) 
			throw new Error(left+" and "+right+" are not flatternable!"); //TODO use own exception?
		this.kind = kind;
		children = new ArrayList<Pointcut>();
		addChildren(left);
		addChildren(right);
//		sortChildren();
	}

	public FlatternedPointcut(Kind kind) {
		this(kind, null, null);
	}

	public void sortChildren() {	
		// sort the list according to:
		Collections.sort(children, new Comparator<Pointcut>(){
			public int compare(Pointcut p1, Pointcut p2) {
                if (p1 instanceof KindedPointcut && p2 instanceof KindedPointcut) {
                    KindedPointcut k1 = (KindedPointcut) p1;
                    KindedPointcut k2 = (KindedPointcut) p2;
    				if (k1.getKind()==k2.getKind()) {
    					SignaturePattern s1 = k1.getSignature();
    					SignaturePattern s2 = k2.getSignature();
    					String name1 = s1.getName().toString();
    					String name2 = s2.getName().toString();
    					if (name1.equals(name2)) {
    						return 0; //TODO compare returnType, decalringType, ...
    					} else {
    						int l1 = name1.length(), l2 = name2.length();
    						if (l1!=l2) 
    							return l1>l2 ? -1 : 1; 
    						else
    							return name1.compareTo(name2);
    					}
    				} else {
    				    return k1.getKind().toString().compareTo(k2.getKind().toString());
    				}
                } else {
                    return p1.getClass().getName().compareTo(p2.getClass().getName()); //TODO
                }
			}
		});
	}

	public void addChildren(Pointcut node) {
		if (node!=null) {
			if (node instanceof FlatternedPointcut && 
					((FlatternedPointcut)node).getKind()==kind) children.addAll(((FlatternedPointcut)node).children);
			else children.add(node);
		}
	}
	
	/**
	 * Remove redundant items in the children list, e.g.:
	 *  <UL>
	 *    <LI> 0) *key, *key, *key => *key
	 *    <LI> 1) *key*, *key, key => *key*
	 *    <LI> 2) *key, *key, key  => *key
	 *    <LI> 3) *key, key*, key  => *key* ??
	 *  </UL>   
	 */
	public void merge() {
		sortChildren();
		List<Pointcut> newChildren = new ArrayList<Pointcut>();
		String lastName = null;
		for(Pointcut ptc:children) {
			if (!(ptc instanceof KindedPointcut))
				//TODO what about other pointcuts, e.g. WithinPointcut?
				newChildren.add(ptc);
			else {
				String name = ((KindedPointcut)ptc).getSignature().getName().toString();
				if (!subseteq(name, lastName)) {
					newChildren.add(ptc);
					lastName = name;
				}
			}
		}
		//TODO example 3 not implemented yet
		children = newChildren;
	}

	private boolean subseteq(String name1, String name2) {
		if (name2==null) return false;
		else
			return name2.contains(name1);
	}

	public String toString() {
		String op = (kind==Kind.AND) ? "&&" : "||";
		String sChildren = "";
		for (Pointcut child:children) {
			if (sChildren.length()>0) sChildren+=",";
			sChildren+=child;
		}			
		return "["+op+" ["+sChildren+"]]";
	}

	//TODO NotPointcut not considered yet
	public static boolean flatternable(FlatternedPointcut.Kind kind, Pointcut left, Pointcut right) {
		PointcutType leftType = PointcutType.getPointcutType(left);
		PointcutType rightType = PointcutType.getPointcutType(right);
		
		boolean result = leftType==PointcutType.PRIMITIVE && rightType==PointcutType.PRIMITIVE;
		result = result || (leftType==PointcutType.PRIMITIVE && rightType==PointcutType.FLATTERNED && 
				((FlatternedPointcut)right).kind==kind);
		result = result || (rightType==PointcutType.PRIMITIVE && leftType==PointcutType.FLATTERNED && 
				((FlatternedPointcut)left).kind==kind);
		result = result || (leftType==PointcutType.FLATTERNED && rightType==PointcutType.FLATTERNED &&
				(((FlatternedPointcut)left).kind==kind && 
						((FlatternedPointcut)left).kind == ((FlatternedPointcut)right).kind));
		return true; //TODO NotPointcut not considered yet
	}

	enum PointcutType {
		PRIMITIVE, FLATTERNED, COMPOSED;
		public static PointcutType getPointcutType(Pointcut ptc) {
			if (ptc instanceof AndPointcut || ptc instanceof OrPointcut || ptc instanceof NotPointcut)
				return COMPOSED;
			else if (ptc instanceof FlatternedPointcut)
				return FLATTERNED;
			else return PRIMITIVE;
		}
	}


	@Override
	protected FPointcut cloneAndMark(FPointcut fpct, List<FPointcut> newPct) {
		// TODO do we need this?
		return null;
	}

	public Pointcut toHierachichalPointcut() {
		if (children.size()==0) return null;
		Pointcut child0 = hireach(children.get(0));
		if (children.size()==1) return child0;
		
		Pointcut ptc;
		Pointcut child1 = hireach(children.get(1));
		if (kind==Kind.AND) ptc = new AndPointcut(child0, child1);
		else ptc = new OrPointcut(child0, child1);
		for (int i=2; i<children.size(); i++) {
			Pointcut child = hireach(children.get(i));
			switch(kind) {
			case AND: 
				ptc = new AndPointcut(ptc, child);
				break;
			case OR: 
				ptc = new OrPointcut(ptc, child);
				break;
			}
		}
		return ptc;
	}

	private Pointcut hireach(Pointcut pointcut) {
		if (pointcut instanceof FlatternedPointcut)
			return ((FlatternedPointcut)pointcut).toHierachichalPointcut();
		else return pointcut;
	}

	/*** START GENERATED GETTERS/SETTERS ***/
    public FlatternedPointcut.Kind getKind() { return kind; }
    public java.util.List<Pointcut> getChildren() { return children; }
    /*** END GENERATED GETTERS/SETTERS ***/
}
