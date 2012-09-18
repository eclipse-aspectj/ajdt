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
package org.eclipse.ajdt.pointcutdoctor.core.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.weaver.MemberKind;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.AnnotationTypePattern;
import org.aspectj.weaver.patterns.ArgsPointcut;
import org.aspectj.weaver.patterns.BindingTypePattern;
import org.aspectj.weaver.patterns.CflowPointcut;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.aspectj.weaver.patterns.HandlerPointcut;
import org.aspectj.weaver.patterns.IfPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.ModifiersPattern;
import org.aspectj.weaver.patterns.NamePattern;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.ReferencePointcut;
import org.aspectj.weaver.patterns.SignaturePattern;
import org.aspectj.weaver.patterns.ThisOrTargetPointcut;
import org.aspectj.weaver.patterns.ThrowsPattern;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.TypePatternList;
import org.aspectj.weaver.patterns.WildTypePattern;
import org.aspectj.weaver.patterns.WithinPointcut;
import org.aspectj.weaver.patterns.WithincodePointcut;
import org.eclipse.ajdt.pointcutdoctor.core.fpointcut.FlatternedPointcut;



public class PointcutUtil {
	public enum Op {
		IncludeSubtypes, ToAny, PromoteToSuperType;
	}
	public enum Place {
		DeclaringType, ReturnType, Throws;
	}

	
	public static List<SignaturePattern> promoteDeclaringTypeForSignature(SignaturePattern sig, World world) {
		MemberKind newKind = sig.getKind();
		ModifiersPattern newModifiers = sig.getModifiers();
		TypePattern newReturnType = sig.getReturnType();
		TypePattern newDeclaringType = sig.getDeclaringType();
		NamePattern newName = sig.getName();
		TypePatternList newParameterTypes = sig.getParameterTypes();
		ThrowsPattern newThrowsPattern = sig.getThrowsPattern();
		AnnotationTypePattern newAnnotationPattern = sig.getAnnotationPattern();
		
		List<SignaturePattern> results = new ArrayList<SignaturePattern>();
		List<TypePattern> superTypePatterns = promoteTypePattern(newDeclaringType, world);
		if (superTypePatterns!=null)
			for (TypePattern superType:superTypePatterns) {
				results.add(new SignaturePattern(newKind, newModifiers,
						newReturnType, superType,
						newName, newParameterTypes,
						newThrowsPattern,
						newAnnotationPattern));
			}
		
		return results;
	}

	public static List<TypePattern> promoteTypePattern(TypePattern tp, World world) {
		if (tp instanceof ExactTypePattern) {
			List<TypePattern> results = new ArrayList<TypePattern>(); 
			ResolvedType type = ((ExactTypePattern)tp).getType().resolve(world);
			
			if (type.isInterface()) return null; //XXX for interface, we need to figure out a proper way...
				
			Iterator<ResolvedType> superTypeIter = type.getDirectSupertypes();

			while(superTypeIter.hasNext()) {
				ResolvedType superType = superTypeIter.next();
				if (!isObject(superType)) { 
					results.add(new ExactTypePattern(superType, tp.isIncludeSubtypes(), tp.isVarArgs()));
				}
			}
			return results;
		} else return null;
	}

	public static SignaturePattern relaxSignature(SignaturePattern sig, Place place, Op op, World world) {
		MemberKind newKind = sig.getKind();
		ModifiersPattern newModifiers = sig.getModifiers();
		TypePattern newReturnType = sig.getReturnType();
		TypePattern newDeclaringType = sig.getDeclaringType();
		NamePattern newName = sig.getName();
		TypePatternList newParameterTypes = sig.getParameterTypes();
		ThrowsPattern newThrowsPattern = sig.getThrowsPattern();
		AnnotationTypePattern newAnnotationPattern = sig.getAnnotationPattern();

		TypePattern tp = null;
		switch (place) {
		case DeclaringType:
			tp = sig.getDeclaringType(); break;
		case ReturnType:
			tp = sig.getReturnType(); break;
            default:
                break;
		}
		SignaturePattern newSig = null;
		switch (op) {
		case IncludeSubtypes:
			tp = cloneTypePatternAndModify(tp, Op.IncludeSubtypes);
			break;
		case ToAny:
			tp = TypePattern.ANY;
			break;
		case PromoteToSuperType:
			if (tp instanceof ExactTypePattern) {
				ResolvedType type = ((ExactTypePattern)tp).getType().resolve(world);
				ResolvedType superType = type.getSuperclass();
				if (isObject(superType) || type.isInterface()) //TODO how about super interfaces? 
					return null; //TODO any problem?
				tp = new ExactTypePattern(superType, tp.isIncludeSubtypes(), tp.isVarArgs());
			}//TODO otherwise??
			break;
		}
		switch (place) {
		case DeclaringType:
			newDeclaringType = tp; break;
		case ReturnType:
			newReturnType = tp; break;
		case Throws:
			newThrowsPattern = ThrowsPattern.ANY;
		}
		
		newSig = new SignaturePattern(newKind, newModifiers,
                newReturnType, newDeclaringType,
                newName, newParameterTypes,
                newThrowsPattern,
                newAnnotationPattern);
		return newSig;
	}
	
	public static boolean isObject(ResolvedType type) {
		return type!=null && "java.lang.Object".equals(type.toString());
	}

	public static TypePattern cloneTypePatternAndModify(TypePattern tp, PointcutUtil.Op op) {
		TypePattern newTp = tp;
		switch(op) {
		case IncludeSubtypes:
			boolean includeSubClass = true;
			if (tp instanceof ExactTypePattern) {
				newTp = new ExactTypePattern(((ExactTypePattern)tp).getType(), includeSubClass, tp.isVarArgs());
			} else if (tp instanceof WildTypePattern) {
				WildTypePattern wtp = (WildTypePattern)tp;
				List<NamePattern> names = new ArrayList<NamePattern>();
				for(NamePattern n : wtp.getNamePatterns()) {
				    names.add(n);
				}
				newTp = new WildTypePattern(names,includeSubClass,wtp.getDimensions(), 
						wtp.getEnd(), 
						wtp.isVarArgs(), 
						wtp.getTypeParameters(),
						wtp.getUpperBound(),
						wtp.getAdditionalIntefaceBounds(),
						wtp.getLowerBound());
			} //TODO other TypePatterns?
			break;
            default:
		}
		// TODO Other operation?
		return newTp;
	}

	/**
	 * Clone the pointcut up to SignaturePattern
	 * @author Linton
	 *
	 */
	static class ClonePointcutVisitor extends PointcutVisitor {
//		public static enum Op {
//			RecordPivot, ReplacePivot;
//		}
//		Pointcut pivot = null;
		
//		Set<Op> ops = new HashSet<Op>();

		private Pointcut newPtc = null;
		private Pointcut ptcToBeReplaced = null;

		private Map<Pointcut, Pointcut> oldNewPtcMap;
//		public ClonePointcutVisitor(Op... ops) {
//			for(Op op:ops)
//				this.ops.add(op);
//		}
		private List<Pointcut> ptcsToRemove;


		public ClonePointcutVisitor(Pointcut ptcToBeReplaced, Pointcut newPtc) {
			this.ptcToBeReplaced = ptcToBeReplaced;
			this.newPtc = newPtc;
		}
		public ClonePointcutVisitor( Map<Pointcut, Pointcut> oldNewPtcMap) {
			this.oldNewPtcMap = oldNewPtcMap;
		}
		public ClonePointcutVisitor(Pointcut ptcToBeReplaced, Pointcut newPtc, Map<Pointcut, Pointcut> oldNewPtcMap) {
			this(ptcToBeReplaced, newPtc);
			this.oldNewPtcMap = oldNewPtcMap;
		}

		public ClonePointcutVisitor(List<Pointcut> toRemove, Map<Pointcut, Pointcut> pivotsMap) {
			this(pivotsMap);
			ptcsToRemove = toRemove;
		}
		private Pointcut processPivot(Pointcut originalPtc, Pointcut clonedPtc) {
			Pointcut actualReplacedPtc = clonedPtc;
			if (ptcToBeReplaced==originalPtc) {
				actualReplacedPtc = newPtc;
			}
			if (oldNewPtcMap!=null && oldNewPtcMap.containsKey(originalPtc)) {
				oldNewPtcMap.put(originalPtc, actualReplacedPtc);
			}
			return actualReplacedPtc;
		}

		private SignaturePattern cloneSignaturePattern(SignaturePattern signature) {
			return new SignaturePattern(signature.getKind(), signature.getModifiers(),
	                         signature.getReturnType(), signature.getDeclaringType(),
	                         signature.getName(), signature.getParameterTypes(),
	                         signature.getThrowsPattern(),
							 signature.getAnnotationPattern());
		}

		@Override
		protected Object visitKindedPointcut(KindedPointcut pointcut, Object data) {
			SignaturePattern sigCloned = cloneSignaturePattern(pointcut.getSignature());
			KindedPointcut cloned = new KindedPointcut(pointcut.getKind(), sigCloned);
			return processPivot(pointcut,cloned);
		}

		@Override
		protected Object visitAndPointcut(AndPointcut pointcut, Object data) {
			Pointcut left = (Pointcut) visit(pointcut.getLeft(), data);
			Pointcut right = (Pointcut) visit(pointcut.getRight(),data);
			Pointcut cloned;
			if (toBeRemoved(left)) cloned = right;
			else if(toBeRemoved(right)) cloned = left;
			else cloned = new AndPointcut(left,right);
			
			return processPivot(pointcut,cloned);
		}

		@Override
		protected Object visitFlatternedPointcut(FlatternedPointcut pointcut, Object data) {
			FlatternedPointcut cloned = new FlatternedPointcut(pointcut.getKind());
			for (Pointcut child:pointcut.getChildren()) {
				Pointcut clonedChild = (Pointcut) super.visit(child,data);
				if (!toBeRemoved(clonedChild))
					cloned.addChildren(clonedChild);
			}				
			return processPivot(pointcut,cloned);
			
		}

		@Override
		protected Object visitNotPointcut(NotPointcut pointcut, Object data) {
			Pointcut nptc = (Pointcut)super.visit(pointcut.getNegatedPointcut(), data);
			Pointcut cloned;
			if (toBeRemoved(nptc)) cloned = null;
			else cloned =  new NotPointcut(nptc);
			return processPivot(pointcut,cloned);
			
		}

		@Override
		protected Object visitOrPointcut(OrPointcut pointcut, Object data) {
			Pointcut left = (Pointcut) visit(pointcut.getLeft(), data);
			Pointcut right = (Pointcut) visit(pointcut.getRight(),data);
			Pointcut cloned;
			if (toBeRemoved(left)) cloned = right;
			else if(toBeRemoved(right)) cloned = left;
			else cloned = new OrPointcut(left,right);
			return processPivot(pointcut,cloned);
			
		}

		private boolean toBeRemoved(Pointcut ptc) {
			return ptc==null || 
				(ptcsToRemove!=null && ptcsToRemove.contains(ptc));
		}
		
		@Override
		protected Object visitReferencedPointcut(ReferencePointcut pointcut, Object data) {
			Pointcut cloned = (Pointcut) super.visitReferencedPointcut(pointcut, data);
			return processPivot(pointcut,cloned);
			//TODO what to do with this?
			
		}

		@Override
		protected Object visitWitinPointcut(WithinPointcut pointcut, Object data) {
			WithinPointcut cloned = new WithinPointcut(pointcut.getTypePattern());
			return processPivot(pointcut,cloned);
			
		}

		@Override
		protected Object visitArgsPointcut(ArgsPointcut pointcut, Object data) {
			ArgsPointcut cloned = new ArgsPointcut(pointcut.getArguments());
			return processPivot(pointcut,cloned);
			
		}

		@Override
		protected Object visitThisOrTargetPointcut(ThisOrTargetPointcut pointcut, Object data) {
			ThisOrTargetPointcut cloned = new ThisOrTargetPointcut(pointcut.isThis(), pointcut.getType());
			return processPivot(pointcut,cloned);
			
		}

		@Override
		protected Object visitWithincodePointcut(WithincodePointcut pointcut, Object data) {
			WithincodePointcut cloned = new WithincodePointcut(cloneSignaturePattern(
					pointcut.getSignature()));
			return processPivot(pointcut,cloned);
			
		}
		@Override
		protected Object visitCflowPointcut(CflowPointcut pointcut, Object data) {
			int[] freevar = null; //TODO any problems?
			CflowPointcut cloned = new CflowPointcut(pointcut.getEntry(), pointcut.isCflowBelow(), freevar);
			return processPivot(pointcut,cloned);
		}
		@Override
		protected Object visitHandlerPointcut(HandlerPointcut pointcut, Object data) {
			HandlerPointcut cloned = new HandlerPointcut(getExceptionType(pointcut));
			return processPivot(pointcut,cloned);
		}
		@Override
		protected Object visitIfPointcut(IfPointcut pointcut, Object data) {
			IfPointcut cloned = new IfPointcut(pointcut.testMethod, pointcut.extraParameterFlags);
			return processPivot(pointcut,cloned);
		}
		
		
	}
	
	public static Pointcut cloneWithPivots(Pointcut pointcut, Map<Pointcut, Pointcut> pivotsMap) {
		ClonePointcutVisitor visitor = new ClonePointcutVisitor(pivotsMap);
		return (Pointcut) visitor.visit(pointcut, null);
	}

	public static TypePattern getExceptionType(HandlerPointcut pointcut) {
		try {
			Field exceptionField = HandlerPointcut.class.getDeclaredField("exceptionType");
			exceptionField.setAccessible(true);
			return (TypePattern) exceptionField.get(pointcut);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public static Pointcut cloneAndReplace(Pointcut pointcut, Pointcut oldPtc, Pointcut newPtc) {
		ClonePointcutVisitor visitor = new ClonePointcutVisitor(oldPtc,newPtc);
		return (Pointcut) visitor.visit(pointcut, oldPtc);
	}
	
	public static Pointcut clone(Pointcut pointcut) {
		ClonePointcutVisitor visitor = new ClonePointcutVisitor(null);
		return (Pointcut) visitor.visit(pointcut, null);
	}

	
	/**
	 * Clone the pointcut, replace oldPtc with newPtc, and put the corresponding
	 * cloned pointcuts into pivotsMap according to its original key set.
	 * 
	 * @param pointcut
	 * @param oldPtc
	 * @param newPtc
	 * @param pivotsMap
	 * @return
	 */
	public static Pointcut cloneAndReplaceWithPivots(Pointcut pointcut, Pointcut oldPtc, Pointcut newPtc, Map<Pointcut, Pointcut> pivotsMap) {
		ClonePointcutVisitor visitor = new ClonePointcutVisitor(oldPtc, newPtc, pivotsMap);
		return (Pointcut) visitor.visit(pointcut, oldPtc);
	}


	public static Pointcut flattern(Pointcut ptc) {
		return (Pointcut)new FlatternVisitor().visit(ptc, null);
	}

	/***************************************************************************************************************/
	/*				GetParametersAsString,  getPointcutAsString												       */
	/*  preparation for writing out resolved pointcuts (where some TypePatterns are resolved as BindingTypePattern */
	/***************************************************************************************************************/
	public static String getParametersAsString(Pointcut pointcut) {
		ToStringVisitor visitor = new ToStringVisitor(true);
		visitor.visit(pointcut, null);
		List<Param> params = visitor.getParams();
		String s = "";
		for (Param p:params) {
			if (s.length()>0) s+=", ";
			s+=p.getType()+" "+p.getName();
		}
		return s;
	}

	/**
	 * @param pointcut
	 * @param bindingTypeAsParam: 
	 * 	if it's true, it will show the binding parameters of This, Args, Target
	 * 	  as a list of p0,p1,.. e.g.  call(* Foo.bar(..))&&args(p0,p1) 
	 *  otherwise, it will show the type name, e.g. call(* Foo.bar(..))&&args(Foo)
	 * @return
	 */
	public static String getPointcutAsString(Pointcut pointcut, boolean bindingTypeAsParam) {
		return (String) new ToStringVisitor(bindingTypeAsParam).visit(pointcut, null);
	}
	
	static class Param {
		String type, name;
		public Param(String type, String name) {
			this.type = type;
			this.name = name;
		}
		public String getName() {
			return name;
		}
		public String getType() {
			return type;
		}
		
	}
	
	/**
	 * This visitor is mainly used to gather/show parameters
	 * @author Linton
	 *
	 */
	static class ToStringVisitor extends PointcutVisitor {
		Map<String,String> paramTypeMap = new HashMap<String,String>();
		private boolean bindingTypeAsParam;
		public ToStringVisitor(boolean bindingTypeAsParam) {
			this.bindingTypeAsParam = bindingTypeAsParam;
		}
		public List<Param> getParams() {
			List<Param> params = new ArrayList<Param>();
			for (String name:paramTypeMap.keySet())
				params.add(new Param(paramTypeMap.get(name), name));
			return params;
		}
		@Override
		protected Object visitAndPointcut(AndPointcut pointcut, Object data) {
			String left = (String) visit(pointcut.getLeft(), data);
			String right = (String) visit(pointcut.getRight(), data);
			return "("+left+" && "+right+")";
		}

		@Override
		protected Object visitCflowPointcut(CflowPointcut pointcut, Object data) {
			return pointcut.toString();
		}
		@Override
		protected Object visitHandlerPointcut(HandlerPointcut pointcut, Object data) {
			return pointcut.toString();
		}
		@Override
		protected Object visitIfPointcut(IfPointcut pointcut, Object data) {
			return pointcut.toString();
		}
		@Override
		protected Object visitArgsPointcut(ArgsPointcut pointcut, Object data) {
			String s = typePatternListToString(pointcut.getArguments());
			return "args("+s+")";
		}
		private String typePatternListToString(TypePatternList tps) {
			return PointcutUtil.typePatternListToString(tps, bindingTypeAsParam);
		}

		@Override
		protected Object visitThisOrTargetPointcut(ThisOrTargetPointcut pointcut, Object data) {
			String kind = pointcut.isThis()? "this":"target";
			return String.format("%s(%s)",kind,typePatternToString(pointcut.getType()));
		}

		private String typePatternToString(TypePattern type) {
			return PointcutUtil.typePatternToString(type, bindingTypeAsParam);
		}
		
		@Override
		protected Object visitKindedPointcut(KindedPointcut pointcut, Object data) {
			return pointcut.toString();
		}

		@Override
		protected Object visitNotPointcut(NotPointcut pointcut, Object data) {
			String neg = (String) visit(pointcut.getNegatedPointcut(), data);
			return "!("+neg+")";
		}

		@Override
		protected Object visitOrPointcut(OrPointcut pointcut, Object data) {
			String left = (String) visit(pointcut.getLeft(), data);
			String right = (String) visit(pointcut.getRight(), data);
			return "("+left+" || "+right+")";
		}

		@Override
		protected Object visitOtherPointcut(Pointcut ptc, Object data) {
			if (ptc!=null)
				return ptc.toString();
			else return "null";
		}

		@Override
		protected Object visitReferencedPointcut(ReferencePointcut pointcut, Object data) {
			return pointcut.name+"("+typePatternListToString(pointcut.arguments)+")";
		}

		@Override
		protected Object visitWithincodePointcut(WithincodePointcut pointcut, Object data) {
			return pointcut.toString();
		}

		@Override
		protected Object visitWitinPointcut(WithinPointcut pointcut, Object data) {
			return pointcut.toString();
		}
		
	}

	public static Pointcut cloneAndRemoveWithPivots(Pointcut root, Pointcut oldPtc, Map<Pointcut, Pointcut> pivotsMap) {
		//return cloneAndReplaceWithPivots(root, oldPtc, null, pivotsMap);
		List<Pointcut> toRemove = new ArrayList<Pointcut>();
		toRemove.add(oldPtc);
		return cloneAndRemoveWithPivots(root, toRemove, pivotsMap);
	}

	public static String typePatternToString(TypePattern type, boolean bindingTypeAsParam) {
		if (type instanceof BindingTypePattern) {
			String pname;
			BindingTypePattern btype = (BindingTypePattern)type;
			if (bindingTypeAsParam) {
				pname = "p"+btype.getFormalIndex();
//				paramTypeMap.put(pname, btype.getType().getName());
			} else pname = btype.getExactType().toString();
			return pname;
		} else return type.toString();
	}

	public static String typePatternToString(TypePattern type) {
		return typePatternToString(type, false);
	}
	
	public static String typePatternListToString(TypePatternList tps) {
		return typePatternListToString(tps, false);
	}
	
	public static String typePatternListToString(TypePatternList tps, boolean bindingTypeAsParam) {
		String s = "";
		for (TypePattern tp:tps.getTypePatterns()) {
			if (s.length()>0) s+=", ";
			s+=typePatternToString(tp, bindingTypeAsParam);
		}
		return s;
	}

	public static Pointcut cloneAndRemoveWithPivots(Pointcut root, List<Pointcut> toRemove, Map<Pointcut, Pointcut> pivotsMap) {
		ClonePointcutVisitor visitor = new ClonePointcutVisitor(toRemove, pivotsMap);
		return (Pointcut) visitor.visit(root, null);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//                      Resolve reference data
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// TODO reference resolving problem:
	//	pointcut ptc9():execution(* Foo*+.*(..));										
	//	pointcut ptc12():ptc9()&&within(Foo);											
	//	pointcut ptc13():ptc12();														
	// if ptc13 is resolved first, then when resolving ptc12, we will not get any reference pointcut record in pointcutData
	// for ptc12.

//	public static PointcutData resolveReferenceData(PointcutData pointcutData) {
//		Iterator peIter = pointcutData.pointcutPESet().iterator();
//		ReferenceResolveVisitor resolveVisitor = new ReferenceResolveVisitor(pointcutData);
//		while(peIter.hasNext()) {
//			IProgramElement pe = (IProgramElement)peIter.next();
//			Pointcut ptc = pointcutData.getCorrespondingPointcut(pe);
//			Pointcut resolvedPtc = (Pointcut) resolveVisitor.visit(ptc, pe); 
//			if (resolvedPtc!=null) pointcutData.addItem(pe, resolvedPtc);
////			if (pct instanceof ReferencePointcut) {
////				ReferencePointcut rpct = (ReferencePointcut)pct;
////				Pointcut referencedPct = pointcutData.findNamedPointcut(rpct.name);
////				if (referencedPct!=null) pointcutData.addItem(pe, referencedPct);
////			}
//		}
//		return pointcutData;
//	}
//
////	public String getCurrentConfgFilePath() {
////		return currentConfgFilePath;
////	}	
//	
//	static class ReferenceResolveVisitor extends ParentsAwarePointcutVisitor {
//
//		private PointcutData pointcutData;
//
//		public ReferenceResolveVisitor(PointcutData pointcutData) {
//			this.pointcutData = pointcutData;
//		}
//
//		@Override
//		protected Object visitReferencedPointcut(ReferencePointcut pointcut, Object data) {
//			Pointcut referencedPct = pointcutData.findNamedPointcut(pointcut.onType, pointcut.name);
//			if (referencedPct!=null) {
//				pointcutData.putReferencePointcut((IProgramElement)data, pointcut);
//				return referencedPct;
//			} else return super.visitReferencedPointcut(pointcut, data); 
//		}
//		
//	}



}
