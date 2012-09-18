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

import java.util.List;

import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.Shadow.Kind;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.ArgsPointcut;
import org.aspectj.weaver.patterns.CflowPointcut;
import org.aspectj.weaver.patterns.HandlerPointcut;
import org.aspectj.weaver.patterns.IfPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.ReferencePointcut;
import org.aspectj.weaver.patterns.SignaturePattern;
import org.aspectj.weaver.patterns.ThisOrTargetPointcut;
import org.aspectj.weaver.patterns.WithinPointcut;
import org.aspectj.weaver.patterns.WithincodePointcut;
import org.eclipse.ajdt.pointcutdoctor.core.PointcutMunger;
import org.eclipse.ajdt.pointcutdoctor.core.PointcutRelaxMungerFactory;
import org.eclipse.ajdt.pointcutdoctor.core.ShadowWrapper;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutVisitor;


public class Explainer {

	public Explainer() {}


	public Reason explain(Pointcut ptc, ShadowWrapper shadowWrapper, PointcutRelaxMungerFactory factory) {
		Part part = pointcutToPart(ptc, factory);
		Reason reason = part.explain(shadowWrapper.getShadow(), shadowWrapper
				.getMatchResult());
		return reason;
	}

	public Part pointcutToPart(Pointcut ptc, PointcutRelaxMungerFactory factory) {
		return (Part) new PointcutToPartConverter(factory).visit(ptc, null);
	}

	static class PointcutToPartConverter extends PointcutVisitor {

		private PointcutRelaxMungerFactory factory;

		public PointcutToPartConverter(PointcutRelaxMungerFactory factory) {
			this.factory = factory;
		}

		@Override
		protected Object visitAndPointcut(AndPointcut pointcut, Object data) {
			Part left = (Part) visit(pointcut.getLeft(), data);
			Part right = (Part) visit(pointcut.getRight(), data);
			return new AndPart(left, right);
		}

		@Override
		protected Object visitNotPointcut(NotPointcut pointcut, Object data) {
			Part other = (Part) visit(pointcut.getNegatedPointcut(), data);
			return new NotPart(other);
		}

		@Override
		protected Object visitOrPointcut(OrPointcut pointcut, Object data) {
			Part left = (Part) visit(pointcut.getLeft(), data);
			Part right = (Part) visit(pointcut.getRight(), data);
			return new OrPart(left, right);
		}

		@Override
		protected Object visitArgsPointcut(ArgsPointcut pointcut, Object data) {
			return new ArgsPart(pointcut);
		}

		@Override
		protected Object visitHandlerPointcut(HandlerPointcut pointcut,
				Object data) {
			return new HandlerPart(pointcut);
		}

		@Override
		protected Object visitKindedPointcut(KindedPointcut pointcut,
				Object data) {
			Part kpart = new KindPart(pointcut, pointcut.getKind());
			SignaturePattern sig = pointcut.getSignature();
			Part apart = new AnnotationPart(pointcut, sig
					.getAnnotationPattern());
			Part mpart = new ModifiersPart(pointcut, sig.getModifiers());
			Part rpart = createReturnPart(pointcut, sig);
			Part dpart = new DeclaringTypePart(pointcut, sig.getDeclaringType());
			Part npart = createNamePart(pointcut, sig);
			Part ppart = createParamsPart(pointcut, sig);
			Part tpart = new ThrowsPart(pointcut, sig.getThrowsPattern());
			return createAndPart(kpart, apart, mpart, rpart, dpart, npart,
					ppart, tpart);
		}

		@Override
		protected Object visitCflowPointcut(CflowPointcut pointcut, Object data) {
			return new CflowPart(pointcut);
		}

		@Override
		protected Object visitIfPointcut(IfPointcut pointcut, Object data) {
			return new IfPart(pointcut);
		}

		private ReturnTypePart createReturnPart(KindedPointcut pointcut,
				SignaturePattern sig) {
			if (pointcut.getKind() == Shadow.ConstructorCall
					|| pointcut.getKind() == Shadow.ConstructorExecution)
				return null;
			return new ReturnTypePart(pointcut, sig.getReturnType());
		}

		private ParamsPart createParamsPart(KindedPointcut pointcut,
				SignaturePattern sig) {
			Kind[] noparams = { Shadow.FieldGet, Shadow.FieldSet };
			if (findKind(noparams, pointcut.getKind()))
				return null;
			else
				return new ParamsPart(pointcut, sig.getParameterTypes());
		}

		private NamePart createNamePart(KindedPointcut pointcut,
				SignaturePattern sig) {
			Kind[] nonames = { Shadow.ConstructorCall,
					Shadow.ConstructorExecution };
			if (findKind(nonames, pointcut.getKind()))
				return null;
			else
				return new NamePart(pointcut, sig.getName());
		}

		private boolean findKind(Kind[] kinds, Kind kind) {
			for (Kind k : kinds)
				if (k == kind)
					return true;
			return false;
		}

		private Part createAndPart(Part... parts) {
			if (parts.length > 0) {
				Part result = parts[0];
				for (int i = 1; i < parts.length; i++) {
					if (parts[i] != null)
						result = new AndPart(result, parts[i]);
				}
				return result;
			} else
				return null;
		}

		@Override
		protected Object visitReferencedPointcut(ReferencePointcut pointcut,
				Object data) {
			return new ReferencePart(pointcut, findConcretePointcut(pointcut, factory));
		}

		private Pointcut findConcretePointcut(ReferencePointcut refPtc, PointcutRelaxMungerFactory f) {
			//FIXME there should be better ways to resolve a reference pointcut
			List<ShadowMunger> mungers = f.getAllPointcutMungers();
			for (ShadowMunger pm : mungers) {
				if (refPtc.name.equals(((PointcutMunger) pm).getDeclaredName())// XXX should
															// consider
															// declaring type
						&& (refPtc.onType == null // declared in the same
													// aspect?
						|| refPtc.onType.getName().equals(
								((PointcutMunger) pm).getDeclaringTypeOfPtcOrAdvice().getName())))
					return ((PointcutMunger) pm).getConcretePointcutToMatch();
			}
			return null;
		}

		@Override
		protected Object visitThisOrTargetPointcut(
				ThisOrTargetPointcut pointcut, Object data) {
			return new ThisOrTargetPart(pointcut);
		}

		@Override
		protected Object visitWithincodePointcut(WithincodePointcut pointcut,
				Object data) {
			return new WithincodePart(pointcut);
		}

		@Override
		protected Object visitWitinPointcut(WithinPointcut pointcut, Object data) {
			return new WithinPart(pointcut);
		}

	}

}
