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
package org.eclipse.ajdt.pointcutdoctor.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Advice;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.AbstractPatternNodeVisitor;
import org.aspectj.weaver.patterns.HandlerPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.ajdt.pointcutdoctor.core.relaxer.PointcutRelaxController;
import org.eclipse.ajdt.pointcutdoctor.core.relaxer.RelaxedPointcut;
import org.eclipse.ajdt.pointcutdoctor.core.virtual.VirtualShadow;




public class PointcutMunger extends ShadowMunger {
	private static final int MAX_SHADOWS_TO_KEEP = 100;
	
	private UnresolvedType inAspect;
	private UnresolvedType declaringTypeOfPtcOrAdvice;
	private int arity;
	
	private Pointcut oldPointcut;
	private List<RelaxedPointcut> relaxedPointcuts = null;
	private List<ShadowWrapper> matchedShadows = new ArrayList<ShadowWrapper>();
	private List<ShadowWrapper> almostMatchedShadows = new ArrayList<ShadowWrapper>();
//	private Set<ShadowWrapper> maybeMatchedShadows = new HashSet<ShadowWrapper>();
	private Map<Shadow, ISourceLocation> shadowLocationMap = new HashMap<Shadow, ISourceLocation>();
	private Pointcut concretePointcutToMatch;
	private String declaredName;  //name of the pointcut, could be anonymous
	private World world;
	private ISourceLocation mungerSourceLocation;
	
	
	@Override
	public void setPointcut(Pointcut pointcut) {
		if (pointcut.toString().length()>0)
			super.setPointcut(pointcut);
		// else it's a Pointcut.MatchNothingPointcut, we don't replace it, otherwise the whole PointcutMunger will not be used for matching
	}

	//TODO why static factory method?  why not constructors?
	public static PointcutMunger createPointcutMunger(ResolvedPointcutDefinition ptcDef) {
		Pointcut ptc = ptcDef.getPointcut();
		PointcutMunger munger = createMungerPerPointcut(ptc, false);
		munger.inAspect = ptcDef.getDeclaringType(); //TODO how to get its inAspect?
		munger.declaringTypeOfPtcOrAdvice = ptcDef.getDeclaringType();
		munger.arity = ptcDef.getArity();
		
		munger.declaredName = ptcDef.getName();
		
		munger.mungerSourceLocation = ptcDef.getSourceLocation();
		
		return munger;
	}
	
	public static PointcutMunger createPointcutMunger(Advice advice) {
		PointcutMunger munger = createMungerPerPointcut(advice.getPointcut(), true);
		munger.inAspect = advice.getDeclaringAspect();
		munger.declaringTypeOfPtcOrAdvice = advice.getDeclaringType();
		munger.arity = advice.getSignature().getArity();
		
		munger.declaredName = "[anonymous]";
		
		munger.mungerSourceLocation = advice.getSourceLocation();
		
		return munger;
	}
	
	public ISourceLocation getMungerSourceLocation() {
		return mungerSourceLocation;
	}

	private static PointcutMunger createMungerPerPointcut(Pointcut ptc, boolean advice) {
		PointcutMunger munger = new PointcutMunger(ptc, ptc.getStart(), ptc.getEnd(), ptc.getSourceContext(), advice);
		return munger;
	}

	
	public ISourceLocation getSourceLocationForShadow(Shadow shadow) {
		return shadowLocationMap.get(shadow);
	}


	public PointcutMunger(Pointcut pointcut, int start, int end, ISourceContext sourceContext, boolean advice) {
		super(pointcut, start, end, sourceContext, ShadowMungerDeow);
		oldPointcut = pointcut;
		concretePointcutToMatch = null;  // will be concretized in match method
//		sourceLocation = pointcut.getSourceLocation();
	}
	
//	ISourceLocation sourceLocation;
//	public ISourceLocation getSourceLocation() {
//		return sourceLocation;
//	}
	
	public Pointcut getOldPointcut() {
		return oldPointcut;
	}
	
	//TODO what to do with concretizing cflow ?
	// this aspect is used to skip the concretizing process for cflow pointcut:
	// we will get a null pointer exception when concretizing a cflowpointcut with parameters
	// since we don't have an advice at this point... TODO what about other pointcuts with parameters?
//	static aspect CflowConcretizing {
//		pointcut concretizeInPointcutMunger(Pointcut cflowPtc):
//			cflowbelow(execution(public boolean PointcutMunger.match(..)))
//				&&execution(public Pointcut CflowPointcut.concretize1(ResolvedType, ResolvedType, IntMap))
//				&&this(cflowPtc);
//		
//		Pointcut around(Pointcut cflowPtc):concretizeInPointcutMunger(cflowPtc) {
//			return cflowPtc;
//		}
//	}

//	private FastMatchInfo info = null;
	
	private boolean isSynthetic(Shadow shadow) {
		Member sig = shadow.getSignature();
		return sig.getName().contains("$");
	}
	
	@Override
	public boolean match(Shadow shadow, World w) {
		this.world = w; //TODO not a nice way to save the reference of world here...
		
		if (isSynthetic(shadow)) return false;
		
		// we chose to concretize the pointcut here instead of in the constructor
		// becase we need the "world" to resolve types.
		if (concretePointcutToMatch==null) {
			concretePointcutToMatch = pointcut;
			if (pointcut.state!=Pointcut.CONCRETE) {
				// need to resolve or concrecize it
				ResolvedType rinAspect;
				if (!(inAspect instanceof ResolvedType)) rinAspect = inAspect.resolve(w);
				else rinAspect = (ResolvedType)inAspect;

//				UnresolvedType declaringType = getDeclaringType();
				ResolvedType rdeclaringType;
				if (!(declaringTypeOfPtcOrAdvice instanceof ResolvedType)) 
					rdeclaringType = declaringTypeOfPtcOrAdvice.resolve(w);
				else rdeclaringType = (ResolvedType)declaringTypeOfPtcOrAdvice;

				IntMap idMap = IntMap.idMap(arity);
				idMap.setConcreteAspect(rinAspect); //TODO is this correct?
				concretePointcutToMatch = pointcut.concretize(rinAspect, rdeclaringType, idMap);
			}
		}

		if (relaxedPointcuts==null) {
			// we have to relax it now, because we need the "world" to do the relaxing			
			//TODO let user to change the relax depth
			relaxedPointcuts = new PointcutRelaxController(6).createRelaxedPointcuts(concretePointcutToMatch, w);
		}
		
		if (hasKindedPointcut(concretePointcutToMatch)) {
//			FastMatchInfo info = new FastMatchInfo(shadow.getEnclosingType().resolve(world), shadow.getKind());
			
			FuzzyBoolean matchResultOldPtc = matchAndKeepShadow(concretePointcutToMatch, shadow, matchedShadows, false);
			if (!matchResultOldPtc.maybeTrue()) {
				if (relaxedPointcuts!=null) {
					for (RelaxedPointcut rptc:relaxedPointcuts) {
						Pointcut ptc = rptc.getPointcut();
						matchAndKeepShadow(ptc, shadow, almostMatchedShadows, true);
					}
				}
			}
		}
		//the return value has to be false to prevent any side effect on the weaving process
		return false;
	}

	private FuzzyBoolean matchAndKeepShadow(Pointcut toMatch, Shadow shadow, List<ShadowWrapper> shadowList, boolean almost) {
		FuzzyBoolean matchResult = toMatch.match(shadow);
		//add to almostMatchedShadows
		if (matchResult.maybeTrue())
			addToShadowList(shadow, shadowList, matchResult, almost);
		return matchResult;
	}

	private boolean addToShadowList(Shadow shadow, List<ShadowWrapper> shadows, FuzzyBoolean matchResult, boolean almost) {
		if (shadows.size()>=MAX_SHADOWS_TO_KEEP) return false; //an workaround for memory problem
		
		boolean isVirtual = shadow instanceof VirtualShadow;
		if ((!isVirtual && contains(shadows, shadow))
				||(isVirtual && hasShadowWithSameSig(shadows, shadow))) 
			return false;
		
		//if a virtual shadow with the same signature has already been put into the list, we need to remove it
		int indexOfFirstShadowWithSameSig = getIndexOfFirstShadowWithSameSig(shadows, shadow);
		if (!isVirtual && indexOfFirstShadowWithSameSig>=0)
			if (shadows.get(indexOfFirstShadowWithSameSig).getShadow() instanceof VirtualShadow)
				shadows.remove(indexOfFirstShadowWithSameSig);
		
		FuzzyBoolean shadowMatchResult;
		if (almost)
			shadowMatchResult = FuzzyBoolean.NO; //TODO could we have MABYE in almost matched jp?
		else
			shadowMatchResult = matchResult;//.alwaysTrue() ? FuzzyBoolean.YES : FuzzyBoolean.MAYBE;
		shadows.add(new ShadowWrapper(shadow, shadowMatchResult));

		// It seems that once the compilation is complete, we will get a NullPointer exception
		// when calling shadow.getSourceLocation(), so we have to save the location now
//		if (!isVirtual)
			shadowLocationMap.put(shadow, shadow.getSourceLocation()); 
		return true;
	}

	private int getIndexOfFirstShadowWithSameSig(List<ShadowWrapper> shadows,
			Shadow shadow) {
		if (shadows!=null) {
			Member sig2 = shadow.getSignature();
			for (int i=0; i<shadows.size(); i++) {
				ShadowWrapper shw = shadows.get(i);
				Member sig1 = shw.getShadow().getSignature();
				//TODO should there be better way than sig1.toString?
				if (sig1!=null && sig2!=null && sig1.toString().equals(
						sig2.toString()))
					return i;
			}
		} 
		return -1;
	}

	private boolean contains(List<ShadowWrapper> shadows, Shadow shadow) {
		for (ShadowWrapper w:shadows)
			if (w.getShadow()==shadow) return true;
		return false;
	}

	private boolean hasKindedPointcut(Pointcut ptc) {
		if (ptc==null) return false;
		final List<Pointcut> foundKinded = new ArrayList<Pointcut>();
		ptc.traverse(new AbstractPatternNodeVisitor(){
			public Object visit(HandlerPointcut node, Object data) {
				foundKinded.add(node);
				return null;
			}

			public Object visit(KindedPointcut node, Object data) {
				foundKinded.add(node);
				return null;
			}
		}, null);
		
		return foundKinded.size()>0;
	}

	private boolean hasShadowWithSameSig(List<ShadowWrapper> shadows, Shadow shadow) {
		return getIndexOfFirstShadowWithSameSig(shadows, shadow)>=0;
	}

	public List<ShadowWrapper> getAlmostMatchedShadows() {
		return almostMatchedShadows;
	}


	public List<ShadowWrapper> getMatchedShadows() {
		return matchedShadows;
	}

	public String toString() {
		return getPointcut().toString();
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof PointcutMunger) {
			ISourceLocation sl1 = getSourceLocation();
			ISourceLocation sl2 = ((PointcutMunger)arg0).getSourceLocation();
			return sl1!=null && sl1.equals(sl2);
		}
		else return false;
	}

	@Override
	public int hashCode() {
		ISourceLocation sl = getSourceLocation();
		if (sl!=null) return sl.hashCode();
		else return 0;//TODO anything wrong?
	}

	@Override
	public ShadowMunger concretize(ResolvedType fromType, World w,
			PerClause clause) {
		return this;  //TODO what should we do about this?
	}

	@Override
	public ISourceLocation getSourceLocation() {
		ISourceLocation sl = super.getSourceLocation();
		if (sl==null)
			sl = getOldPointcut().getSourceLocation();
		return sl;
	}

	public int compareTo(Object other) {
		int h1 = hashCode();
		int h2 = other.hashCode();
		return (h1==h2)?0 : ((h1>h2)?1:-1);
	}

	@Override
	public Collection<ResolvedType> getThrownExceptions() {
		return null;
	}

	@Override
	public boolean implementOn(Shadow shadow) {
		return false;
	}

	@Override
	public boolean mustCheckExceptions() {
		return false;
	}

	@Override
	public ShadowMunger parameterizeWith(ResolvedType declaringType,
			Map<String, UnresolvedType> typeVariableMap) {
		return null;
	}

	@Override
	public void specializeOn(Shadow shadow) {

	}

	public Pointcut getConcretePointcutToMatch() {
		return concretePointcutToMatch;
	}

	public String getDeclaredName() {
		return declaredName;
	}

	public UnresolvedType getDeclaringTypeOfPtcOrAdvice() {
		return declaringTypeOfPtcOrAdvice;
	}

	public World getWorld() {
		return world;
	}

	public void setMungerSourceLocation(ISourceLocation sl) {
		mungerSourceLocation = sl;
	}

	@Override
	public ResolvedType getConcreteAspect() {
		return null;
	}
}
