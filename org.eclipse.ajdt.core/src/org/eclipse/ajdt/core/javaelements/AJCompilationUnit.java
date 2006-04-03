/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *     Sian January - added eager parsing support
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.lookup.AjLookupEnvironment;
import org.aspectj.asm.IProgramElement;
import org.aspectj.org.eclipse.jdt.core.compiler.IProblem;
import org.aspectj.org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.ajdt.codeconversion.ConversionOptions;
import org.eclipse.ajdt.codeconversion.JavaCompatibleBuffer;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.contentassist.ProposalRequestorFilter;
import org.eclipse.ajdt.internal.contentassist.ProposalRequestorWrapper;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.ajdt.internal.parserbridge.AJCompilationUnitDeclarationWrapper;
import org.eclipse.ajdt.parserbridge.AJCompilationUnitProblemFinder;
import org.eclipse.ajdt.parserbridge.AJCompilationUnitStructureRequestor;
import org.eclipse.ajdt.parserbridge.AJSourceElementParser;
import org.eclipse.ajdt.parserbridge.AJSourceElementParser2;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.core.BecomeWorkingCopyOperation;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;


/**
 * An ICompilationUnit for .aj files.
 * 
 * In order to obtain better interoperability with jdt, AJCompilationUnits pretend
 * to have java syntax compatible contents. To get the real contents,
 * requestOriginalContentMode()
 * must be called before getting the Buffer. Please make sure to call
 * discardOriginalContentMode()
 * afterwards to get back into non-original mode.
 * 
 * @author Luzius Meisser
 */
public class AJCompilationUnit extends CompilationUnit{
	
	int originalContentMode = 0;
	private IFile ajFile;
	protected JavaCompatibleBuffer javaCompBuffer;
	

	
	public boolean isInOriginalContentMode(){
		return originalContentMode > 0;
	}
	
	public void requestOriginalContentMode(){
		originalContentMode++;
	}
	
	public void discardOriginalContentMode(){
		originalContentMode--;
	}

	public AJCompilationUnit(IFile ajFile){
		super(CompilationUnitTools.getParentPackage(ajFile), ajFile.getName(), AJWorkingCopyOwner.INSTANCE);
		this.ajFile = ajFile;
	}

	/**
	 * @param fragment
	 * @param elementName
	 * @param workingCopyOwner
	 */
	public AJCompilationUnit(PackageFragment fragment, String elementName, WorkingCopyOwner workingCopyOwner) {
		super(fragment, elementName, workingCopyOwner);
		if(fragment.getResource() instanceof IProject) {
			IProject p = (IProject)fragment.getResource();
			this.ajFile = (IFile)p.findMember(elementName);
		} else {
			IFolder f = (IFolder)fragment.getResource();
			this.ajFile = (IFile)f.findMember(elementName);
		}
	}
	
	public char[] getMainTypeName(){
		if (AspectJPlugin.usingCUprovider) {
			return super.getMainTypeName();
		}
		String elementName = name;
		//remove the .aj
		elementName = elementName.substring(0, elementName.length() - ".aj".length());
		return elementName.toCharArray();
	}
	
	/* Eclipse 3.1M3: prior to this we overrode isValidCompilationUnit, but now we need to
	 * override validateCompilationUnit, otherwise the check for valid name will fail on
	 * .aj files
	 */
//	protected IStatus validateCompilationUnit(IResource resource) {
//		IPackageFragmentRoot root = getPackageFragmentRoot();
//		try {
//			if (!(resource.getProject().exists()) || root.getKind() != IPackageFragmentRoot.K_SOURCE) 
//				return new JavaModelStatus(IJavaModelStatusConstants.INVALID_ELEMENT_TYPES, root);
//		} catch (JavaModelException e) {
//			return e.getJavaModelStatus();
//		}
//		return JavaModelStatus.OK_STATUS;
//	}
	
	/* Eclipse 3.2M6: bypass buffer cache to ensure fake buffer is used
	 */
	/**
	 * @see org.eclipse.jdt.internal.compiler.env.ICompilationUnit#getContents()
	 */
	public char[] getContents() {
		try {
			IBuffer buffer = this.getBuffer();
			return buffer == null ? CharOperation.NO_CHAR : buffer.getCharacters();
		} catch (JavaModelException e) {
			return CharOperation.NO_CHAR;
		}
	}
	
	public IResource getResource(){
		if (AspectJPlugin.usingCUprovider) {
			return super.getResource();
		}
		return ajFile;
	}
	
	/*
	 * needs to return real path for organize imports 
	 */
	public IPath getPath() {
		if (AspectJPlugin.usingCUprovider || ajFile == null) {
			return super.getPath();
		}
		return ajFile.getFullPath();
	}
	
	public IResource getUnderlyingResource() throws JavaModelException {
		if (AspectJPlugin.usingCUprovider) {
			return super.getUnderlyingResource();
		}
		return ajFile;
	}
	
	protected void generateInfos(Object info, HashMap newElements, IProgressMonitor monitor) throws JavaModelException {
		if (!(info instanceof AJCompilationUnitInfo)){
			info = new AJCompilationUnitInfo();
		}
		super.generateInfos(info, newElements, monitor);
	}
	
	
	protected boolean buildStructure(OpenableElementInfo info, final IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {
		
		if(ajFile == null) {
			return false;
		}
		this.requestOriginalContentMode();
		
		AJCompilationUnitInfo unitInfo;
		try {
			// check if this compilation unit can be opened
			if (!isWorkingCopy()) { // no check is done on root kind or exclusion pattern for working copies
				IStatus status = validateCompilationUnit(underlyingResource);
				if (!status.isOK()) throw newJavaModelException(status);
			}
			
			// prevents reopening of non-primary working copies (they are closed when they are discarded and should not be reopened)
			if (!isPrimary() && getPerWorkingCopyInfo() == null) {
				throw newNotPresentException();
			}

			unitInfo = (AJCompilationUnitInfo) info;

			// get buffer contents
			IBuffer buffer = getBufferManager().getBuffer(AJCompilationUnit.this);
			if (buffer == null) {
				buffer = openBuffer(pm, unitInfo); // open buffer independently from the info, since we are building the info
			}
			final char[] contents = buffer == null ? null : buffer.getCharacters();

			// generate structure and compute syntax problems if needed
			AJCompilationUnitStructureRequestor requestor = new AJCompilationUnitStructureRequestor(this, unitInfo, newElements);
			JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = getPerWorkingCopyInfo();
			IJavaProject project = getJavaProject();
			boolean computeProblems = JavaProject.hasJavaNature(project.getProject()) && perWorkingCopyInfo != null && perWorkingCopyInfo.isActive();
			IProblemFactory problemFactory = new DefaultProblemFactory();
			Map options = project.getOptions(true);
			AJSourceElementParser parser = new AJSourceElementParser(
				requestor, 
				problemFactory, 
				new CompilerOptions(options),
				true/*report local declarations*/);
			parser.reportOnlyOneSyntaxError = !computeProblems;
			
			//we need to set the source already here so the requestor can init
			//its jdt version of the parser (see requestor.setParser)
			parser.scanner.source = contents;
			requestor.setParser(parser);
			
			CompilationUnitDeclaration unit = parser.parseCompilationUnit(new org.aspectj.org.eclipse.jdt.internal.compiler.env.ICompilationUnit() {
					public char[] getContents() {
						return contents;
					}
					public char[] getMainTypeName() {
						return AJCompilationUnit.this.getMainTypeName();
					}
					public char[][] getPackageName() {
						return AJCompilationUnit.this.getPackageName();
					}
					public char[] getFileName() {
						return AJCompilationUnit.this.getFileName();
					}
				}, true /*full parse to find local elements*/);
			
			
			// update timestamp (might be IResource.NULL_STAMP if original does not exist)
			if (underlyingResource == null) {
				underlyingResource = getResource();
			}
			unitInfo.setTimestamp(((IFile)underlyingResource).getModificationStamp());
			
			// compute other problems if needed
			org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration compilationUnitDeclaration = null;
			try {
			if (computeProblems){
				perWorkingCopyInfo.beginReporting();
				
				AJCompilationUnitDeclarationWrapper ajcudw = new AJCompilationUnitDeclarationWrapper(unit, this);
				AJCompilationUnitStructureRequestor ajcusr = new AJCompilationUnitStructureRequestor(this, (AJCompilationUnitInfo)getElementInfo(), null);
				AJSourceElementParser2 parser2 = new AJSourceElementParser2(ajcusr, new org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory(), new org.eclipse.jdt.internal.compiler.impl.CompilerOptions(options), true); 

				AjLookupEnvironment le =
					new AjLookupEnvironment(null, new CompilerOptions(options), null, null);		
				unit.scope = new CompilationUnitScope(unit, le);
				compilationUnitDeclaration = AJCompilationUnitProblemFinder.process(ajcudw, this, contents, parser2, null, perWorkingCopyInfo, new org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory(), false/*don't cleanup cu*/, null);
				
//				provisional -- only reports syntax errors				
				IProblem[] problems = unit.compilationResult.problems;
				if (problems != null){
				for (int i = 0; i < problems.length; i++) {
					IProblem problem = problems[i];
					if (problem == null)
						continue;
					perWorkingCopyInfo.acceptProblem(new DefaultProblem(
					problem.getOriginatingFileName(),
					problem.getMessage(),
					problem.getID(),
					problem.getArguments(),
					problem.isError()?ProblemSeverities.Error:ProblemSeverities.Warning,
					problem.getSourceStart(),
					problem.getSourceEnd(),
					problem.getSourceLineNumber()));
				}
				}
				perWorkingCopyInfo.endReporting();
				
			}
				
				if (info instanceof ASTHolderAJCUInfo && compilationUnitDeclaration != null) {
					
					int astLevel = ((ASTHolderAJCUInfo) info).astLevel;
					org.eclipse.jdt.core.dom.CompilationUnit cu = AST.convertCompilationUnit(astLevel, compilationUnitDeclaration, contents, options, computeProblems, (CompilationUnit)perWorkingCopyInfo.getWorkingCopy(), pm);
					((ASTHolderAJCUInfo) info).ast = cu;
				} 
			} finally {
			    if (compilationUnitDeclaration != null) {
			        compilationUnitDeclaration.cleanUp();
			    }
			}
		} finally {
		    this.discardOriginalContentMode();
		}
			
		return unitInfo.isStructureKnown();

	}

	public boolean isPrimary() {
		return this.owner == AJWorkingCopyOwner.INSTANCE;
	}

	protected Object createElementInfo() {
		return new AJCompilationUnitInfo();
	}

	
	public org.eclipse.jdt.core.dom.CompilationUnit makeConsistent(int astLevel, boolean resolveBindings, HashMap problems, IProgressMonitor monitor) throws JavaModelException {
		if (isConsistent()) return null;
		
		// create a new info and make it the current info
		// (this will remove the info and its children just before storing the new infos)
		if (astLevel != NO_AST || problems != null) {
			ASTHolderAJCUInfo info = new ASTHolderAJCUInfo();
			info.astLevel = astLevel;
			info.resolveBindings = resolveBindings;
			info.problems = problems;
			openWhenClosed(info, monitor);
			org.eclipse.jdt.core.dom.CompilationUnit result = info.ast;
			info.ast = null;
			return result;
		} else {
			openWhenClosed(createElementInfo(), monitor);
			return null;
		}		
	}

	/**
	 * @see ICompilationUnit#getWorkingCopy(WorkingCopyOwner, IProblemRequestor, IProgressMonitor)
	 */
	public ICompilationUnit getWorkingCopy(WorkingCopyOwner workingCopyOwner, IProblemRequestor problemRequestor, IProgressMonitor monitor) throws JavaModelException {
		if (!isPrimary()) return this;
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		
		CompilationUnit workingCopy = new AJCompilationUnit((PackageFragment)getParent(), getElementName(), workingCopyOwner);
		JavaModelManager.PerWorkingCopyInfo perWorkingCopyInfo = 
			manager.getPerWorkingCopyInfo(workingCopy, false/*don't create*/, true/*record usage*/, null/*not used since don't create*/);
		if (perWorkingCopyInfo != null) {
			return perWorkingCopyInfo.getWorkingCopy(); // return existing handle instead of the one created above
		}
		BecomeWorkingCopyOperation op = new BecomeWorkingCopyOperation(workingCopy, problemRequestor);
		op.runOperation(monitor);
		return workingCopy;
	}

	public IBuffer getBuffer() throws JavaModelException {
		return convertBuffer(super.getBuffer());
	}


	public IBuffer convertBuffer(IBuffer buf) {
		if (isInOriginalContentMode() || (buf == null))
			return buf;
		
		if (javaCompBuffer == null){
			BufferManager bm = BufferManager.getDefaultBufferManager();
			IBuffer myBuffer = bm.createBuffer(this);
			javaCompBuffer = new JavaCompatibleBuffer(buf, myBuffer);
		} else {
			if (buf != javaCompBuffer)
				javaCompBuffer.reinitialize(buf);
		}

		return javaCompBuffer;
	}	
	
	// copied from super, but changed to use an AJReconcileWorkingCopyOperation
	public org.eclipse.jdt.core.dom.CompilationUnit reconcile(int astLevel,
			boolean forceProblemDetection,
			boolean enableStatementsRecovery,
			WorkingCopyOwner workingCopyOwner,
			IProgressMonitor monitor) throws JavaModelException {
		if (!isWorkingCopy()) return null; // Reconciling is not supported on non working copies
		if (workingCopyOwner == null) workingCopyOwner = AJWorkingCopyOwner.INSTANCE;
		
		boolean createAST = false;
		if (astLevel == AST.JLS2) {
			// client asking for level 2 AST; these are supported
			createAST = true;
		} else if (astLevel == AST.JLS3) {
			// client asking for level 3 ASTs; these are supported
			createAST = true;
		} else {
			// client asking for no AST (0) or unknown ast level
			// either way, request denied
			createAST = false;
		}
		AJReconcileWorkingCopyOperation op = new AJReconcileWorkingCopyOperation(this, createAST, astLevel, forceProblemDetection, workingCopyOwner);
		op.runOperation(monitor);
		return op.ast;
	}

	public IJavaElement[] codeSelect(int offset, int length,
			WorkingCopyOwner workingCopyOwner) throws JavaModelException {
		IJavaElement[] res = super.codeSelect(offset, length, workingCopyOwner);
		return res;
	}
	
	protected void closeBuffer() {
		if (javaCompBuffer != null){
			javaCompBuffer.close();
			javaCompBuffer = null;
		}
		super.closeBuffer();
	}
	
	private static final String moveCuUpdateCreator = "org.eclipse.jdt.internal.corext.refactoring.reorg.MoveCuUpdateCreator"; //$NON-NLS-1$
	private static final int lenOfMoveCuUpdateCreator = moveCuUpdateCreator.length();
	
	public IType[] getAllTypes() throws JavaModelException {
		//tell MoveCuUpdateCreator that we do not contain any Types, otherwise it tries to find
		//them using java search which will cause an ugly exception
		String caller = (new RuntimeException()).getStackTrace()[1].getClassName();
		if ((lenOfMoveCuUpdateCreator == caller.length()) && moveCuUpdateCreator.equals(caller))
			return new IType[0];
		return super.getAllTypes();
	}

	/**
	 * Hook for code completion support for AspectJ content.
	 * 
     * A description of how code completion works in AJDT can be found in bug 74419.
     * 
	 *  (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.Openable#codeComplete(org.eclipse.jdt.internal.compiler.env.ICompilationUnit, org.eclipse.jdt.internal.compiler.env.ICompilationUnit, int, org.eclipse.jdt.core.CompletionRequestor, org.eclipse.jdt.core.WorkingCopyOwner)
	 */
	protected void codeComplete(org.eclipse.jdt.internal.compiler.env.ICompilationUnit cu, org.eclipse.jdt.internal.compiler.env.ICompilationUnit unitToSkip, int position, CompletionRequestor requestor, WorkingCopyOwner owner) throws JavaModelException {
		ConversionOptions myConversionOptions; int pos;
		
		if(javaCompBuffer == null) {
			convertBuffer(super.getBuffer());
		}
		ConversionOptions optionsBefore = javaCompBuffer.getConversionOptions();
		
		//check if inside intertype method declaration
		char[] targetType = isInIntertypeMethodDeclaration(position, this);
		if (targetType != null){
			
			//we are inside an intertype method declaration -> simulate context switch to target class
			myConversionOptions = ConversionOptions.getCodeCompletionOptionWithContextSwitch(position, targetType);
			javaCompBuffer.setConversionOptions(myConversionOptions);
			pos = javaCompBuffer.translatePositionToFake(position);
			// we call codeComplete twice in this case to combine the context specific completions with the
			// completions for things like local variables.
			super.codeComplete(cu, unitToSkip, pos, requestor, owner);				
			//set up proposal filter to filter away all the proposals that would be wrong because of context switch
			requestor = new ProposalRequestorFilter(requestor, javaCompBuffer);
			((ProposalRequestorFilter)requestor).setAcceptMemberMode(false);
		} else {
			requestor = new ProposalRequestorWrapper(requestor, javaCompBuffer);
		}
		myConversionOptions = ConversionOptions.CODE_COMPLETION;
		
		javaCompBuffer.setConversionOptions(myConversionOptions);
		pos = javaCompBuffer.translatePositionToFake(position);
		super.codeComplete(cu, unitToSkip, pos, requestor, owner);
		javaCompBuffer.setConversionOptions(optionsBefore);
		
	}

	//return null if outside intertype method declaration or the name of the target type otherwise
	private char[] isInIntertypeMethodDeclaration(int pos, JavaElement elem) throws JavaModelException{
		IJavaElement[] elems = elem.getChildren();
		for (int i = 0; i < elems.length; i++) {
			IJavaElement element = elems[i];
			if (element instanceof IntertypeElement){
				if (((IAspectJElement)element).getAJKind() == IProgramElement.Kind.INTER_TYPE_METHOD){
					ISourceRange range = ((IntertypeElement)element).getSourceRange();
					if ((pos >= range.getOffset()) && (pos < range.getOffset() + range.getLength()))
						return ((IntertypeElement)element).getTargetType();
				}
			}
			char[] res = isInIntertypeMethodDeclaration(pos, (JavaElement)element);
			if (res != null)
				return res;
		}
		return null;
	}


	// hack: need to use protected constructor in SourceType
	private JavaElement getType(JavaElement type, String typeName) {
		try {
			Constructor cons = SourceType.class.getDeclaredConstructor(new Class[]{JavaElement.class,String.class});
			cons.setAccessible(true);
			Object obj = cons.newInstance(new Object[]{type,typeName});
			return (JavaElement)obj;
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		}
		return null;
	}
	
	/*
	 * @see JavaElement
	 */
	public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
		JavaElement type = this;
		// need to handle types ourselves, because they may contain inner aspects
		// (or inner classes containing inner aspects etc)
		while ((token.charAt(0) == AspectElement.JEM_ASPECT_TYPE) ||
				(token.charAt(0) == JavaElement.JEM_TYPE)) {
			if (!memento.hasMoreTokens()) return type;
			String typeName = memento.nextToken();
			if (token.charAt(0) == AspectElement.JEM_ASPECT_TYPE) {
				type = new AspectElement(type, typeName);
			} else if (token.charAt(0) == JavaElement.JEM_TYPE) {
				type = getType(type,typeName);
				if (type == null) type = (JavaElement)getType(typeName);
			}
			if (!memento.hasMoreTokens()) return type;
			token = memento.nextToken();
		}
		// handle pointcuts in a class (bug 124992)
		if (!(type instanceof AspectElement)
				&& (token.charAt(0) == AspectElement.JEM_POINTCUT)) {
			String name = memento.nextToken();
			ArrayList params = new ArrayList();
			nextParam: while (memento.hasMoreTokens()) {
				token = memento.nextToken();
				switch (token.charAt(0)) {
					case JEM_TYPE:
					case JEM_TYPE_PARAMETER:
						break nextParam;
					case AspectElement.JEM_POINTCUT:
						if (!memento.hasMoreTokens()) return this;
						String param = memento.nextToken();
						StringBuffer buffer = new StringBuffer();
						while (param.length() == 1 && Signature.C_ARRAY == param.charAt(0)) { // backward compatible with 3.0 mementos
							buffer.append(Signature.C_ARRAY);
							if (!memento.hasMoreTokens()) return this;
							param = memento.nextToken();
						}
						params.add(buffer.toString() + param);
						break;
					default:
						break nextParam;
				}
			}
			String[] parameters = new String[params.size()];
			params.toArray(parameters);
			JavaElement pointcut = new PointcutElement(type, name, parameters);
			return pointcut.getHandleFromMemento(memento, workingCopyOwner);
		}
		return type.getHandleFromMemento(token, memento, workingCopyOwner);
		}
	
	/**
	 * @see JavaElement#getHandleMementoDelimiter()
	 */
	protected char getHandleMementoDelimiter() {
		if (AspectJPlugin.usingCUprovider) {
			return super.getHandleMementoDelimiter();
		}
		return AspectElement.JEM_ASPECT_CU;
	}
	
	public String getHandleIdentifier() {
		if (AspectJPlugin.usingCUprovider) {
			return super.getHandleIdentifier();
		}
		final String deletionClass = "org.eclipse.jdt.internal.corext.refactoring.changes.DeleteSourceManipulationChange"; //$NON-NLS-1$
		String callerName = (new RuntimeException()).getStackTrace()[1]
				.getClassName();
		// are we being called in the context of a delete operation?
		if (callerName.equals(deletionClass)) {
			AJCompilationUnitManager.INSTANCE.removeFileFromModel((IFile) getResource());
			// need to return a handle identifier that JDT can use (bug 74426)
			String handleIdentifier = JavaCore.create(ajFile).getHandleIdentifier();
			ajFile = null;			
			return handleIdentifier;
		}
		return super.getHandleIdentifier();
	}
	
}
