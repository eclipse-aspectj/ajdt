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
package org.eclipse.ajdt.pointcutdoctor.core.almost;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.ajdt.core.javaelements.AJCodeElement;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.core.javaelements.AspectJMemberElement;
import org.eclipse.ajdt.pointcutdoctor.core.PointcutDoctorCorePlugin;
import org.eclipse.ajdt.pointcutdoctor.core.PointcutMunger;
import org.eclipse.ajdt.pointcutdoctor.core.PointcutRelaxMungerFactory;
import org.eclipse.ajdt.pointcutdoctor.core.ShadowWrapper;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutData;
import org.eclipse.ajdt.pointcutdoctor.core.virtual.VirtualShadow;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;


/**
 * This class is used by the ui plugin to build and retrieve almost JPS.
 * 
 * @author Linton
 */
public class AlmostJPSPluginFacade {
	
	public AlmostJPSPluginFacade(PointcutDoctorCorePlugin plugin) {
	}


	Map<IProject, PointcutRelaxMungerFactory> factoryMap = new HashMap<IProject, PointcutRelaxMungerFactory>();

	private Map<Shadow, IJavaElement> shadowToije = new HashMap<Shadow, IJavaElement>();
	private PointcutData pointcutData;

	public IJavaElement findJavaElementForShadow(Shadow shadow,
			IJavaProject project) {
		IJavaElement el = shadowToije.get(shadow);

		PointcutRelaxMungerFactory factory = getPointcutRelaxMungerFactory(project
				.getProject());

		if (el != null)
			return el;
		else {

			if (shadow instanceof VirtualShadow) {
				el = new VirtualJavaElement();
			} else {

				ISourceLocation sl = findSourceLocationForShadow(shadow,
						factory);
				if (sl == null)
					return null;

				int offset = sl.getOffset();
				// in some versions of ajde, code elements have an offset of
				// zero - in cases like this, we go with the offset of the
				// parent instead
				if (offset == 0) {
					// offset =
					// location.getParent().getSourceLocation().getOffset();
					// TODO what to do?
				}
				try {
					ICompilationUnit unit = getCompilationUnitByLocation(
							project, sl);
					el = unit.getElementAt(offset);
					if (isCodeShadow(shadow) || el == null) { // TODO???
						IJavaElement parent = el;
						el = new AJCodeElement((JavaElement) parent,
								/* sl.getLine(), */ 
								shadow.toString());// TODO
						// shadow.toString?
					}
					// jeLinkNames.put(el, createLinkLableString(pe));
				} catch (JavaModelException e) {
					// TODO what to do with this?
					e.printStackTrace();
				}
			}
			shadowToije.put(shadow, el);
			return el;
		}
	}

	private boolean isCodeShadow(Shadow shadow) {
		Shadow.Kind[] codekinds = new Shadow.Kind[] { Shadow.MethodCall,
				Shadow.FieldGet, Shadow.FieldSet, Shadow.ConstructorCall,
				Shadow.SynchronizationLock, Shadow.ExceptionHandler };
		for (Shadow.Kind k : codekinds)
			if (shadow.getKind() == k)
				return true;
		return false;
	}

	private ISourceLocation findSourceLocationForShadow(Shadow shadow,
			PointcutRelaxMungerFactory factory) {
		for (ShadowMunger m : factory.getAllPointcutMungers()) {
			ISourceLocation sl = ((PointcutMunger) m).getSourceLocationForShadow(shadow);
			if (sl != null)
				return sl;
		}
		return null;
	}

	private ICompilationUnit getCompilationUnitByLocation(IJavaProject project,
			ISourceLocation sl) {
		IFile file = getIFileByLocation(project, sl);
		// TODO what if a file consists of more than one class?
		ICompilationUnit unit = AJCompilationUnitManager.INSTANCE
				.getAJCompilationUnit(file);
		if (unit == null) {
			// get java cu
			unit = JavaModelManager.createCompilationUnitFrom(file, project);
		}
		return unit;
	}

	private IFile getIFileByLocation(IJavaProject jp, ISourceLocation sl) {
		final List<IFile> ajcus = new ArrayList<IFile>();
		final String fileName = sl.getSourceFile().getName();
		try {
			jp.getProject().accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					if (resource instanceof IFile
							&& resource.getName().equals(fileName)) {
						ajcus.add((IFile) resource);
					}
					return resource.getType() == IResource.FOLDER
							|| resource.getType() == IResource.PROJECT;
				}
			});
		} catch (CoreException e) {
			// TODO what to do with this?
			e.printStackTrace();
		}
		return (ajcus.size() > 0) ? ajcus.get(0) : null;
	}

	public Pointcut getOldPointcut(AspectJMemberElement o) {
		File file = o.getResource().getFullPath().toFile();
		int offset;
		try {
			offset = o.getSourceRange().getOffset();
			return pointcutData.getPointcutByLocation(file, offset);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	// public Reason explain(Pointcut ptc, ShadowWrapper shadowWrapper) {
	// Part part = pointcutToPart(ptc);
	// // ExplainOption option;
	// // if (matchResult.alwaysTrue()) option = ExplainOption.True;
	// // else if(matchResult.alwaysFalse()) option = ExplainOption.False;
	// // else option = ExplainOption.Maybe;
	// Reason reason = part.explain(shadowWrapper.getShadow(), shadowWrapper
	// .getMatchResult());
	// lastReasonExplained = reason;
	// return reason;
	// }
	//
	// public Part pointcutToPart(Pointcut ptc) {
	// return (Part) new PointcutToPartConverter().visit(ptc, null);
	// }
	//
	// static class PointcutToPartConverter extends PointcutVisitor {
	//
	// @Override
	// protected Object visitAndPointcut(AndPointcut pointcut, Object data) {
	// Part left = (Part) visit(pointcut.getLeft(), data);
	// Part right = (Part) visit(pointcut.getRight(), data);
	// return new AndPart(left, right);
	// }
	//
	// @Override
	// protected Object visitNotPointcut(NotPointcut pointcut, Object data) {
	// Part other = (Part) visit(pointcut.getNegatedPointcut(), data);
	// return new NotPart(other);
	// }
	//
	// @Override
	// protected Object visitOrPointcut(OrPointcut pointcut, Object data) {
	// Part left = (Part) visit(pointcut.getLeft(), data);
	// Part right = (Part) visit(pointcut.getRight(), data);
	// return new OrPart(left, right);
	// }
	//
	// @Override
	// protected Object visitArgsPointcut(ArgsPointcut pointcut, Object data) {
	// return new ArgsPart(pointcut);
	// }
	//
	// @Override
	// protected Object visitHandlerPointcut(HandlerPointcut pointcut,
	// Object data) {
	// return new HandlerPart(pointcut);
	// }
	//
	// @Override
	// protected Object visitKindedPointcut(KindedPointcut pointcut,
	// Object data) {
	// Part kpart = new KindPart(pointcut, pointcut.getKind());
	// SignaturePattern sig = pointcut.getSignature();
	// Part apart = new AnnotationPart(pointcut, sig
	// .getAnnotationPattern());
	// Part mpart = new ModifiersPart(pointcut, sig.getModifiers());
	// Part rpart = createReturnPart(pointcut, sig);
	// Part dpart = new DeclaringTypePart(pointcut, sig.getDeclaringType());
	// Part npart = createNamePart(pointcut, sig);
	// Part ppart = createParamsPart(pointcut, sig);
	// Part tpart = new ThrowsPart(pointcut, sig.getThrowsPattern());
	// return createAndPart(kpart, apart, mpart, rpart, dpart, npart,
	// ppart, tpart);
	// }
	//
	// @Override
	// protected Object visitCflowPointcut(CflowPointcut pointcut, Object data)
	// {
	// return new CflowPart(pointcut);
	// }
	//
	// @Override
	// protected Object visitIfPointcut(IfPointcut pointcut, Object data) {
	// return new IfPart(pointcut);
	// }
	//
	// private ReturnTypePart createReturnPart(KindedPointcut pointcut,
	// SignaturePattern sig) {
	// if (pointcut.getKind() == Shadow.ConstructorCall
	// || pointcut.getKind() == Shadow.ConstructorExecution)
	// return null;
	// return new ReturnTypePart(pointcut, sig.getReturnType());
	// }
	//
	// private ParamsPart createParamsPart(KindedPointcut pointcut,
	// SignaturePattern sig) {
	// Kind[] noparams = { Shadow.FieldGet, Shadow.FieldSet };
	// if (findKind(noparams, pointcut.getKind()))
	// return null;
	// else
	// return new ParamsPart(pointcut, sig.getParameterTypes());
	// }
	//
	// private NamePart createNamePart(KindedPointcut pointcut,
	// SignaturePattern sig) {
	// Kind[] nonames = { Shadow.ConstructorCall,
	// Shadow.ConstructorExecution };
	// if (findKind(nonames, pointcut.getKind()))
	// return null;
	// else
	// return new NamePart(pointcut, sig.getName());
	// }
	//
	// private boolean findKind(Kind[] kinds, Kind kind) {
	// for (Kind k : kinds)
	// if (k == kind)
	// return true;
	// return false;
	// }
	//
	// private Part createAndPart(Part... parts) {
	// if (parts.length > 0) {
	// Part result = parts[0];
	// for (int i = 1; i < parts.length; i++) {
	// if (parts[i] != null)
	// result = new AndPart(result, parts[i]);
	// }
	// return result;
	// } else
	// return null;
	// }
	//
	// @Override
	// protected Object visitReferencedPointcut(ReferencePointcut pointcut,
	// Object data) {
	// return new ReferencePart(pointcut, findConcretePointcut(pointcut));
	// }
	//
	// private Pointcut findConcretePointcut(ReferencePointcut refPtc,
	// PointcutRelaxMungerFactory factory) {
	// List<PointcutMunger> mungers = factory.getAllPointcutMungers();
	// for (PointcutMunger pm : mungers) {
	// if (refPtc.name.equals(pm.getDeclaredName())// XXX should
	// // consider
	// // declaring type
	// && (refPtc.onType == null // declared in the same
	// // aspect?
	// || refPtc.onType.getName().equals(
	// pm.getDeclaringTypeOfPtcOrAdvice().getName())))
	// return pm.getConcretePointcutToMatch();
	// }
	// return null;
	// }
	//
	// @Override
	// protected Object visitThisOrTargetPointcut(
	// ThisOrTargetPointcut pointcut, Object data) {
	// return new ThisOrTargetPart(pointcut);
	// }
	//
	// @Override
	// protected Object visitWithincodePointcut(WithincodePointcut pointcut,
	// Object data) {
	// return new WithincodePart(pointcut);
	// }
	//
	// @Override
	// protected Object visitWitinPointcut(WithinPointcut pointcut, Object data)
	// {
	// return new WithinPart(pointcut);
	// }
	//
	// }

	// public void keep(Collection<ShadowMunger> newMungers) {
	// // pointcutMungers.clear();
	// for (ShadowMunger munger:newMungers)
	// if (munger instanceof PointcutMunger) {
	// PointcutMunger pm = (PointcutMunger)munger;
	// // only save the latest one
	// if (pointcutMungers.contains(pm)) pointcutMungers.remove(pm);
	// pointcutMungers.add(pm);
	// }
	// }

	public Pointcut getMainPointcutForJavaElement(AspectJMemberElement o) {
		PointcutRelaxMungerFactory factory = getPointcutRelaxMungerFactory(o
				.getParent().getJavaProject().getProject());
		PointcutMunger munger = getMungerForAJMember(o, factory);
		if (munger != null)
			return munger.getOldPointcut();
		else
			return null;
	}

	public List<ShadowWrapper> getAlmostMatchedJPSs(AspectJMemberElement o,
			PointcutRelaxMungerFactory factory) {
		PointcutMunger munger = getMungerForAJMember(o, factory);
		if (munger != null)
			return munger.getAlmostMatchedShadows();
		else
			return null;
	}

	public List<ShadowWrapper> getMatchedJPSs(AspectJMemberElement o,
			PointcutRelaxMungerFactory factory) {
		PointcutMunger munger = getMungerForAJMember(o, factory);
		if (munger != null)
			return munger.getMatchedShadows();
		else
			return null;
	}

	private PointcutMunger getMungerForAJMember(AspectJMemberElement o,
			PointcutRelaxMungerFactory factory) {
		ISourceLocation sl = createSouceLocationForAJMember(o);
		PointcutMunger munger = getMungerByLocation(sl, factory);
		return munger;
	}

	private PointcutMunger getMungerByLocation(ISourceLocation sl,
			PointcutRelaxMungerFactory factory) {
		if (factory != null)
			for (ShadowMunger munger : factory.getAllPointcutMungers()) {
				ISourceLocation slm = munger.getSourceLocation();
				if (slm != null) { // TODO why sometimes slm==null???
					File sfm = slm.getSourceFile();
					if (sfm.equals(sl.getSourceFile())
							&& java.lang.Math.abs(slm.getOffset()
									- sl.getOffset()) < 10) // TODO
						// sometimes
						// the
						// offsets
						// don't
						// agree
						// exactly,
						// why?
						return (PointcutMunger) munger;
				}
			}
		return null;
	}

	private ISourceLocation createSouceLocationForAJMember(
			AspectJMemberElement o) {
		File file = o.getResource().getLocation().toFile();
		int offset = 0;
		try {
			// this offset is relative to the declaration not the pointcut,
			// so it's actual the offset of pointcut ptc(...)..., we need to
			// skip the prefix
			offset = declOffToPointcutOff(o.getSourceRange().getOffset(), o);
		} catch (JavaModelException e) {
			// TODO what to do?
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SourceLocation sl = new SourceLocation(file, 0);
		sl.setOffset(offset);
		return sl;
	}

	// this offset is relative to the declaration not the pointcut,
	// so it's actual the offset of pointcut ptc(...)..., we need to
	// skip the prefix
	// TODO but there should be a better to handle this
	private int declOffToPointcutOff(int offset, JavaElement decl)
			throws IOException, CoreException {
		int adjustment = 0;
		boolean foundComma = false;
		IFile file = (IFile) decl.getResource();
		InputStream is = file.getContents();
		try {
			is.skip(offset);
			int ich;
			while ((ich = is.read()) > 0) {
				char ch = (char) ich;
				if (foundComma && ch != ' ' && ch != '\t' && ch != '\n'
						&& ch != '\r')// TODO other white spaces?
					break;
				if (ch == ':')
					foundComma = true;
				adjustment++;
			}
		} finally {
			is.close();
		}
		return adjustment + offset;
	}

	public PointcutRelaxMungerFactory getPointcutRelaxMungerFactory(IProject project) {
//		AjCompiler compiler = AspectJPlugin.getDefault().getCompilerFactory()
//				.getCompilerForProject(project);
//		// LTODO what if there are other custom munger factories?
//		return (PointcutRelaxMungerFactory) compiler.getCustomMungerFactory();
		PointcutRelaxMungerFactory factory = factoryMap.get(project);
		if (factory == null) {
			factory = new PointcutRelaxMungerFactory();
			factoryMap.put(project, factory);
		}
		return factory;
	}

}
