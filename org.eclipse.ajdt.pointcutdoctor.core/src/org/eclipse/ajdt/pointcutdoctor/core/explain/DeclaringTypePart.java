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

import java.util.Arrays;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.JoinPointSignature;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SignaturePattern;
import org.aspectj.weaver.patterns.TypePattern;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil;
import org.eclipse.ajdt.pointcutdoctor.core.utils.TypeUtil;



public class DeclaringTypePart extends SigPart {

	TypePattern declaringType;
	
	public DeclaringTypePart(KindedPointcut pointcut, TypePattern dt) {
		super(pointcut,dt);
		declaringType = dt;
	}

	@Override
	protected FuzzyBoolean matchesExactly(JoinPointSignature aMethod, World world) {
		if(declaringType.matchesStatically(aMethod.getDeclaringType().resolve(world)))
			return FuzzyBoolean.YES;
		else return FuzzyBoolean.MAYBE;
	}

	public String toString() {
		return "dt.matches("+declaringType+")"+super.toString();
	}
	
	@Override
	protected String explainMisMatchTextual(Shadow shadow) {
		String msg = null;
		if (shadow.getKind()==Shadow.ConstructorCall)
			msg = explainMisMatchedConstructorCall(shadow);
		else if (shadow.getKind()==Shadow.ConstructorExecution) 
			msg = explainMisMatchedConstructorExec(shadow);
		//TODO initializatoiin and others?
		else if (shadow.getKind()==Shadow.MethodCall)
			msg =  explainMisMatchedMethodCall(shadow);
		else if (shadow.getKind()==Shadow.MethodExecution)
			msg =  explainMisMatchedMethodExec(shadow);
		
		if (msg==null) return super.explainMisMatchTextual(shadow);
		else return msg;
	}

	private String explainMisMatchedMethodExec(Shadow shadow) {
		World world = shadow.getIWorld();
		if (declaringType instanceof ExactTypePattern) {
			ResolvedType dtp = ((ExactTypePattern)declaringType).getType().resolve(world);
			Member method = shadow.getSignature();
			ResolvedType dt = method.getDeclaringType().resolve(world);
			String dtpName = dtp.getName();
			String dtName = dt.getName();
			if (TypeUtil.isSubTypeOf(dt, dtp) && methodOnlyDeclaredInSubclass(method, dtp)) {
				String methodSig = method.getName()+ paramsToString(method.getParameterTypes());
				String msg = formatAndUpdateExplainMessage(ExplainMessage.MSGDT3,
						methodSig, dtpName, dtName, dtpName, dtpName, dtpName);
				return msg;
			} else if (TypeUtil.isSubTypeOf(dtp, dt)) {
				String suggestion = String.format("%s&&this(%s)", 
						removeDeclaringTypeInPtcString(pointcut, world), dtpName);
				return formatAndUpdateExplainMessage(ExplainMessage.MSGDT6,
						dtName, dtpName, suggestion, dtpName);
			}
		} 
		return null;
	}

	private String paramsToString(UnresolvedType[] parameterTypes) {
		String result = Arrays.toString(parameterTypes);
		return "("+result.substring(1,result.length()-1)+")";
	}

	private String removeDeclaringTypeInPtcString(Pointcut p, World world) {
		if (p instanceof KindedPointcut) {
			KindedPointcut kptc = ((KindedPointcut)p);
			SignaturePattern sig = kptc.getSignature();
			SignaturePattern newSig = PointcutUtil.relaxSignature(sig, PointcutUtil.Place.DeclaringType, 
					PointcutUtil.Op.ToAny, world);
			return new KindedPointcut(kptc.getKind(), newSig).toString();
		} 
		else return null;
	}

	private String explainMisMatchedMethodCall(Shadow shadow) {
		World world = shadow.getIWorld();
		if (declaringType instanceof ExactTypePattern) {
			ResolvedType dtp = ((ExactTypePattern)declaringType).getType().resolve(world);
			Member method = shadow.getSignature();
			ResolvedType dt = method.getDeclaringType().resolve(world);
			String dtpName = dtp.getName();
			String dtName = dt.getName();
			if (TypeUtil.isSubTypeOf(dt, dtp) && methodOnlyDeclaredInSubclass(method, dtp)) {
				String methodSig = method.getName()+ paramsToString(method.getParameterTypes());
				String msg = formatAndUpdateExplainMessage(ExplainMessage.MSGDT3,
						methodSig, dtpName, dtName, dtpName, dtpName, dtpName);
				return msg;
			} else if (TypeUtil.isSubTypeOf(dtp, dt)) {
				String suggestion = String.format("%s&&target(%s)", 
						removeDeclaringTypeInPtcString(pointcut, world), dtpName);
				return formatAndUpdateExplainMessage(ExplainMessage.MSGDT4,
						dtName, dtpName, dtpName, suggestion, dtpName);
			}
		} 
		return null;
	}

	private String explainMisMatchedConstructorExec(Shadow shadow) {
		World world = shadow.getIWorld();
		if (declaringType instanceof ExactTypePattern) {
			ResolvedType dtp = ((ExactTypePattern)declaringType).getType().resolve(world);
			ResolvedType dt = shadow.getSignature().getDeclaringType().resolve(world);
			if (TypeUtil.isSubTypeOf(dt, dtp)) {
				String msg = formatAndUpdateExplainMessage(ExplainMessage.MSGDT1,
						dtp.getName(), dt.getName(), dtp.getName());
				return msg;
			}
		} 
		return null;
	}

	private String explainMisMatchedConstructorCall(Shadow shadow) {
		World world = shadow.getIWorld();
		if (declaringType instanceof ExactTypePattern) {
			ResolvedType dtp = ((ExactTypePattern)declaringType).getType().resolve(world);
			ResolvedType dt = shadow.getSignature().getDeclaringType().resolve(world);
			if (TypeUtil.isSubTypeOf(dt, dtp)) {
				String msg = formatAndUpdateExplainMessage(ExplainMessage.MSGDT0,
						dtp.getName(), dt.getName(), dtp.getName());
				return msg;
			}
		} 
		return null;
	}

	private boolean methodOnlyDeclaredInSubclass(Member method, ResolvedType targetType ) {
		//TODO any problem for static method, <init> <cinit> etc?
		ResolvedType superType = targetType.getSuperclass();
		String methodSig = method.getSignature();
		
		while (superType!=null) {
			ResolvedMember[] methods = superType.getDeclaredMethods();
			for (ResolvedMember m:methods)
				if (m.getName().equals(method.getName()) && methodSig.equals(m.getSignature()))
					return false;
			superType = superType.getSuperclass();
		}
		return true;
	}

	@Override
	protected void computeOffsetLengthInSource() {
		offset = declaringType.getStart();
		length = declaringType.getEnd() - declaringType.getStart()+1;
		// the length will include name, we will need to fix it
		String ptc = this.readPointcutSource();
		int start = offset-pointcut.getStart();
		int end = start+length;
		// when declaring Type is omitted, the start and end will become weird numbers... TODO why?
		if (start>=0 && start<ptc.length() && end>=0 && end<ptc.length()) {
			String dt = ptc.substring(start, end);
			int idxOfDot = dt.lastIndexOf(".");
			if (idxOfDot>=0) {
				length = idxOfDot;
			}
		} else length = 0;  // something went wrong, so we don't highlight it...
	}

	
	@Override
	protected String getJoinPointPartName() {
		return "declaring type";
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return shadow.getSignature().getDeclaringType().getClassName();
	}

	@Override
	protected String explainMatchTextual(Shadow shadow) {
		String msg = null;
		if (shadow.getKind()==Shadow.MethodExecution)
			msg =  explainMatchTextualMethodExec(shadow);
		//TODO initializatoiin and others?
		
		if (msg==null) return super.explainMatchTextual(shadow);
		else return msg;
	}
	
	private String explainMatchTextualMethodExec(Shadow shadow) {
		World world = shadow.getIWorld();
		Member method = shadow.getEnclosingCodeSignature(); 
		ResolvedType dt = method.getDeclaringType().resolve(world);

		KindedPointcut kptc = (KindedPointcut)pointcut;
		TypePattern dtp = kptc.getSignature().getDeclaringType();

		if (dtp instanceof ExactTypePattern) {
			ResolvedType rdtp = ((ExactTypePattern)dtp).getType().resolve(world);
			if (TypeUtil.isSubTypeOf(dt, rdtp) && 
					TypeUtil.isApplicableToType(method, dt)	&& TypeUtil.isApplicableToType(method, rdtp)) {
				String methodDtp = String.format("%s.%s(%s)", rdtp.getClassName(), method.getName(), 
						Arrays.toString(method.getParameterTypes()));
				return formatAndUpdateExplainMessage(ExplainMessage.M_MSGDT0,dt, dtp, methodDtp, 
						"inherited", dtp);
			}
		}
		return null;
	}
}
