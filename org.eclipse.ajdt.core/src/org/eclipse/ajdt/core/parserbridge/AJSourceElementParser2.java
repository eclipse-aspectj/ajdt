/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     	IBM Corporation - initial API and implementation
 * 		Luzius Meisser - Adapted for use with AspectJ
 *      Andy Clement - updated for AspectJ 5
 *      Sian January - updated for eager parsing
 *      Andrew Eisenberg - updated for Eclipse 3.4
 *******************************************************************************/
package org.eclipse.ajdt.core.parserbridge;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.SourceJavadocParser;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.JavadocFieldReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocMessageSend;
import org.eclipse.jdt.internal.compiler.ast.JavadocQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.JavadocSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;
import org.eclipse.jdt.internal.core.util.Messages;

/**
 * A source element parser extracts structural and reference information
 * from a piece of source.
 *
 * also see @ISourceElementRequestor
 *
 * The structural investigation includes:
 * - the package statement
 * - import statements
 * - top-level types: package member, member types (member types of member types...)
 * - fields
 * - methods
 *
 * If reference information is requested, then all source constructs are
 * investigated and type, field & method references are provided as well.
 *
 * Any (parsing) problem encountered is also provided.
 */
/*
 * Luzius:
 * Most content of this has been copied from
 * org.eclipse.jdt.internal.compiler.SourceElementParser
 *
 * Sian:
 * Copied and edited AJSourceElementParser to use org.eclipse... JDT types
 * instead of org.aspectj.org.eclipse... JDT types
 *
 * ADE:
 * Recopied for use with 3.4.
 * now, the parser does not handle notifications itself.
 * rather it is passed to the notifier object
 * note that this change means that AJSourceElementParser2 and
 * AJSourceElementParser have diverged even more
 */
public class AJSourceElementParser2 extends CommentRecorderParser {

	AJCompilationUnitStructureRequestor requestor; // AspectJ change
	boolean reportReferenceInfo;
	boolean reportLocalDeclarations;
	HashtableOfObjectToInt sourceEnds = new HashtableOfObjectToInt();
	HashMap<ASTNode, char[][]> nodesToCategories = new HashMap<>(); // a map from ASTNode to char[][]
	boolean useSourceJavadocParser;

	AJSourceElementNotifier notifier; // AspectJ Change

public AJSourceElementParser2(
		final AJCompilationUnitStructureRequestor requestor, // AspectJ Change
		IProblemFactory problemFactory,
		CompilerOptions options,
		boolean reportLocalDeclarations,
		boolean optimizeStringLiterals) {
	this(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals, true/* use SourceJavadocParser */);
}

public AJSourceElementParser2(
		AJCompilationUnitStructureRequestor requestor, // AspectJ Change
		IProblemFactory problemFactory,
		CompilerOptions options,
		boolean reportLocalDeclarations,
		boolean optimizeStringLiterals,
		boolean useSourceJavadocParser) {

	super(
		new ProblemReporter(
			DefaultErrorHandlingPolicies.exitAfterAllProblems(),
			options,
			problemFactory),
		optimizeStringLiterals);

	this.reportLocalDeclarations = reportLocalDeclarations;

	// we want to notify all syntax error with the acceptProblem API
	// To do so, we define the record method of the ProblemReporter
	this.problemReporter = new ProblemReporter(
		DefaultErrorHandlingPolicies.exitAfterAllProblems(),
		options,
		problemFactory) {
		public void record(CategorizedProblem problem, CompilationResult unitResult, ReferenceContext context) {
			unitResult.record(problem, context); // TODO (jerome) clients are trapping problems either through factory or requestor... is result storing needed?
			AJSourceElementParser2.this.requestor.acceptProblem(problem);
		}
	};
	this.requestor = requestor;
	this.options = options;

	this.notifier = new AJSourceElementNotifier(this.requestor, reportLocalDeclarations); // AspectJ Change

	// set specific javadoc parser
	this.useSourceJavadocParser = useSourceJavadocParser;
	if (useSourceJavadocParser) {
		this.javadocParser = new SourceJavadocParser(this);
	}
}
private void acceptJavadocTypeReference(Expression expression) {
	if (expression instanceof JavadocSingleTypeReference) {
		JavadocSingleTypeReference singleRef = (JavadocSingleTypeReference) expression;
		this.requestor.acceptTypeReference(singleRef.token, singleRef.sourceStart);
	} else if (expression instanceof JavadocQualifiedTypeReference) {
		JavadocQualifiedTypeReference qualifiedRef = (JavadocQualifiedTypeReference) expression;
		this.requestor.acceptTypeReference(qualifiedRef.tokens, qualifiedRef.sourceStart, qualifiedRef.sourceEnd);
	}
}
public void addUnknownRef(NameReference nameRef) {
	// Note that:
	// - the only requestor interested in references is the SourceIndexerRequestor
	// - a name reference can become a type reference only during the cast case, it is then tagged later with the Binding.TYPE bit
	// However since the indexer doesn't make the distinction between name reference and type reference, there is no need
	// to report a type reference in the SourceElementParser.
	// This gained 3.7% in the indexing performance test.
	if (nameRef instanceof SingleNameReference) {
		requestor.acceptUnknownReference(((SingleNameReference) nameRef).token, nameRef.sourceStart);
	} else {
		//QualifiedNameReference
		requestor.acceptUnknownReference(((QualifiedNameReference) nameRef).tokens, nameRef.sourceStart, nameRef.sourceEnd);
	}
}
public void checkComment() {
	// discard obsolete comments while inside methods or fields initializer (see bug 74369)
	if (!(this.diet && this.dietInt==0) && this.scanner.commentPtr >= 0) {
		flushCommentsDefinedPriorTo(this.endStatementPosition);
	}

	int lastComment = this.scanner.commentPtr;

	if (this.modifiersSourceStart >= 0) {
		// eliminate comments located after modifierSourceStart if positionned
		while (lastComment >= 0 && Math.abs(this.scanner.commentStarts[lastComment]) > this.modifiersSourceStart) lastComment--;
	}
	if (lastComment >= 0) {
		// consider all remaining leading comments to be part of current declaration
		this.modifiersSourceStart = Math.abs(this.scanner.commentStarts[0]);

		// check deprecation in last comment if javadoc (can be followed by non-javadoc comments which are simply ignored)
		while (lastComment >= 0 && this.scanner.commentStops[lastComment] < 0) lastComment--; // non javadoc comment have negative end positions
		if (lastComment >= 0 && this.javadocParser != null) {
			int commentEnd = this.scanner.commentStops[lastComment] - 1; //stop is one over
			// do not report problem before last parsed comment while recovering code...
			if (this.javadocParser.shouldReportProblems) {
				this.javadocParser.reportProblems = this.currentElement == null || commentEnd > this.lastJavadocEnd;
			} else {
				this.javadocParser.reportProblems = false;
			}
			if (this.javadocParser.checkDeprecation(lastComment)) {
				checkAndSetModifiers(ClassFileConstants.AccDeprecated);
			}
			this.javadoc = this.javadocParser.docComment;	// null if check javadoc is not activated
			if (currentElement == null) this.lastJavadocEnd = commentEnd;
		}
	}

	if (this.reportReferenceInfo && this.javadocParser.checkDocComment && this.javadoc != null) {
		// Report reference info in javadoc comment @throws/@exception tags
		TypeReference[] thrownExceptions = this.javadoc.exceptionReferences;
		if (thrownExceptions != null) {
      for (TypeReference typeRef : thrownExceptions) {
        if (typeRef instanceof JavadocSingleTypeReference) {
          JavadocSingleTypeReference singleRef = (JavadocSingleTypeReference) typeRef;
          this.requestor.acceptTypeReference(singleRef.token, singleRef.sourceStart);
        }
        else if (typeRef instanceof JavadocQualifiedTypeReference) {
          JavadocQualifiedTypeReference qualifiedRef = (JavadocQualifiedTypeReference) typeRef;
          this.requestor.acceptTypeReference(qualifiedRef.tokens, qualifiedRef.sourceStart, qualifiedRef.sourceEnd);
        }
      }
		}

		// Report reference info in javadoc comment @see tags
		Expression[] references = this.javadoc.seeReferences;
		if (references != null) {
      for (Expression reference : references) {
        acceptJavadocTypeReference(reference);
        if (reference instanceof JavadocFieldReference) {
          JavadocFieldReference fieldRef = (JavadocFieldReference) reference;
          this.requestor.acceptFieldReference(fieldRef.token, fieldRef.sourceStart);
          if (fieldRef.receiver != null && !fieldRef.receiver.isThis()) {
            acceptJavadocTypeReference(fieldRef.receiver);
          }
        }
        else if (reference instanceof JavadocMessageSend) {
          JavadocMessageSend messageSend = (JavadocMessageSend) reference;
          int argCount = messageSend.arguments == null ? 0 : messageSend.arguments.length;
          this.requestor.acceptMethodReference(messageSend.selector, argCount, messageSend.sourceStart);
          this.requestor.acceptConstructorReference(messageSend.selector, argCount, messageSend.sourceStart);
          if (messageSend.receiver != null && !messageSend.receiver.isThis()) {
            acceptJavadocTypeReference(messageSend.receiver);
          }
        }
        else if (reference instanceof JavadocAllocationExpression) {
          JavadocAllocationExpression constructor = (JavadocAllocationExpression) reference;
          int argCount = constructor.arguments == null ? 0 : constructor.arguments.length;
          if (constructor.type != null) {
            char[][] compoundName = constructor.type.getParameterizedTypeName();
            this.requestor.acceptConstructorReference(compoundName[compoundName.length - 1], argCount, constructor.sourceStart);
            if (!constructor.type.isThis()) {
              acceptJavadocTypeReference(constructor.type);
            }
          }
        }
      }
		}
	}
}
protected void classInstanceCreation(boolean alwaysQualified) {

	boolean previousFlag = reportReferenceInfo;
	reportReferenceInfo = false; // not to see the type reference reported in super call to getTypeReference(...)
	super.classInstanceCreation(alwaysQualified);
	reportReferenceInfo = previousFlag;
	if (reportReferenceInfo){
		AllocationExpression alloc = (AllocationExpression)expressionStack[expressionPtr];
		TypeReference typeRef = alloc.type;
		requestor.acceptConstructorReference(
			typeRef instanceof SingleTypeReference
				? ((SingleTypeReference) typeRef).token
				: CharOperation.concatWith(alloc.type.getParameterizedTypeName(), '.'),
			alloc.arguments == null ? 0 : alloc.arguments.length,
			alloc.sourceStart);
	}
}
protected void consumeAnnotationAsModifier() {
	super.consumeAnnotationAsModifier();
	Annotation annotation = (Annotation)expressionStack[expressionPtr];
	if (reportReferenceInfo) { // accept annotation type reference
		this.requestor.acceptAnnotationTypeReference(annotation.type.getTypeName(), annotation.sourceStart, annotation.sourceEnd);
	}
}
protected void consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() {
	boolean previousFlag = reportReferenceInfo;
	reportReferenceInfo = false; // not to see the type reference reported in super call to getTypeReference(...)
	super.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments();
	reportReferenceInfo = previousFlag;
	if (reportReferenceInfo){
		AllocationExpression alloc = (AllocationExpression)expressionStack[expressionPtr];
		TypeReference typeRef = alloc.type;
		requestor.acceptConstructorReference(
			typeRef instanceof SingleTypeReference
				? ((SingleTypeReference) typeRef).token
				: CharOperation.concatWith(alloc.type.getParameterizedTypeName(), '.'),
			alloc.arguments == null ? 0 : alloc.arguments.length,
			alloc.sourceStart);
	}
}
protected void consumeAnnotationTypeDeclarationHeaderName() {
	int currentAstPtr = this.astPtr;
	super.consumeAnnotationTypeDeclarationHeaderName();
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		rememberCategories();
}
protected void consumeAnnotationTypeDeclarationHeaderNameWithTypeParameters() {
	int currentAstPtr = this.astPtr;
	super.consumeAnnotationTypeDeclarationHeaderNameWithTypeParameters();
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		rememberCategories();
}
protected void consumeClassHeaderName1() {
	int currentAstPtr = this.astPtr;
	super.consumeClassHeaderName1();
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		rememberCategories();
}
protected void consumeClassInstanceCreationExpressionWithTypeArguments() {
	boolean previousFlag = reportReferenceInfo;
	reportReferenceInfo = false; // not to see the type reference reported in super call to getTypeReference(...)
	super.consumeClassInstanceCreationExpressionWithTypeArguments();
	reportReferenceInfo = previousFlag;
	if (reportReferenceInfo){
		AllocationExpression alloc = (AllocationExpression)expressionStack[expressionPtr];
		TypeReference typeRef = alloc.type;
		requestor.acceptConstructorReference(
			typeRef instanceof SingleTypeReference
				? ((SingleTypeReference) typeRef).token
				: CharOperation.concatWith(alloc.type.getParameterizedTypeName(), '.'),
			alloc.arguments == null ? 0 : alloc.arguments.length,
			alloc.sourceStart);
	}
}
protected void consumeConstructorHeaderName() {
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr];
	int selectorSourceEnd = (int) selectorSourcePositions;
	int currentAstPtr = this.astPtr;
	super.consumeConstructorHeaderName();
	if (this.astPtr > currentAstPtr) { // if ast node was pushed on the ast stack
		this.sourceEnds.put(this.astStack[this.astPtr], selectorSourceEnd);
		rememberCategories();
	}
}
protected void consumeConstructorHeaderNameWithTypeParameters() {
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr];
	int selectorSourceEnd = (int) selectorSourcePositions;
	int currentAstPtr = this.astPtr;
	super.consumeConstructorHeaderNameWithTypeParameters();
	if (this.astPtr > currentAstPtr) { // if ast node was pushed on the ast stack
		this.sourceEnds.put(this.astStack[this.astPtr], selectorSourceEnd);
		rememberCategories();
	}
}
protected void consumeEnumConstantWithClassBody() {
	super.consumeEnumConstantWithClassBody();
	if ((currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON)
			&& astStack[astPtr] instanceof FieldDeclaration) {
		this.sourceEnds.put(this.astStack[this.astPtr], this.scanner.currentPosition - 1);
		rememberCategories();
	}
}
protected void consumeEnumConstantNoClassBody() {
	super.consumeEnumConstantNoClassBody();
	if ((currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON)
			&& this.astStack[this.astPtr] instanceof FieldDeclaration) {
		this.sourceEnds.put(this.astStack[this.astPtr], this.scanner.currentPosition - 1);
		rememberCategories();
	}
}
protected void consumeEnumHeaderName() {
	int currentAstPtr = this.astPtr;
	super.consumeEnumHeaderName();
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		rememberCategories();
}
protected void consumeEnumHeaderNameWithTypeParameters() {
	int currentAstPtr = this.astPtr;
	super.consumeEnumHeaderNameWithTypeParameters();
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		rememberCategories();
}
protected void consumeExitVariableWithInitialization() {
	// ExitVariableWithInitialization ::= $empty
	// the scanner is located after the comma or the semi-colon.
	// we want to include the comma or the semi-colon
	super.consumeExitVariableWithInitialization();
	if ((currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON)
			&& this.astStack[this.astPtr] instanceof FieldDeclaration) {
		this.sourceEnds.put(this.astStack[this.astPtr], this.scanner.currentPosition - 1);
		rememberCategories();
	}
}
protected void consumeExitVariableWithoutInitialization() {
	// ExitVariableWithoutInitialization ::= $empty
	// do nothing by default
	super.consumeExitVariableWithoutInitialization();
	if ((currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON)
			&& astStack[astPtr] instanceof FieldDeclaration) {
		this.sourceEnds.put(this.astStack[this.astPtr], this.scanner.currentPosition - 1);
		rememberCategories();
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'
	super.consumeFieldAccess(isSuperAccess);
	FieldReference fr = (FieldReference) expressionStack[expressionPtr];
	if (reportReferenceInfo) {
		requestor.acceptFieldReference(fr.token, fr.sourceStart);
	}
}
protected void consumeFormalParameter(boolean isVarArgs) {
	super.consumeFormalParameter(isVarArgs);

	// Flush comments prior to this formal parameter so the declarationSourceStart of the following parameter
	// is correctly set (see bug 80904)
	// Note that this could be done in the Parser itself, but this would slow down all parsers, when they don't need
	// the declarationSourceStart to be set
	flushCommentsDefinedPriorTo(this.scanner.currentPosition);
}
protected void consumeInterfaceHeaderName1() {
	int currentAstPtr = this.astPtr;
	super.consumeInterfaceHeaderName1();
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		rememberCategories();
}
protected void consumeMemberValuePair() {
	super.consumeMemberValuePair();
	MemberValuePair memberValuepair = (MemberValuePair) this.astStack[this.astPtr];
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(memberValuepair.name, 0, memberValuepair.sourceStart);
	}
}

@Override
protected void consumeMarkerAnnotation(boolean isTypeAnnotation) {
	super.consumeMarkerAnnotation(isTypeAnnotation);
	Annotation annotation = (Annotation)expressionStack[expressionPtr];
	if (reportReferenceInfo) { // accept annotation type reference
		this.requestor.acceptAnnotationTypeReference(annotation.type.getTypeName(), annotation.sourceStart, annotation.sourceEnd);
	}
}
protected void consumeMethodHeaderName(boolean isAnnotationMethod) {
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr];
	int selectorSourceEnd = (int) selectorSourcePositions;
	int currentAstPtr = this.astPtr;
	super.consumeMethodHeaderName(isAnnotationMethod);
	if (this.astPtr > currentAstPtr) { // if ast node was pushed on the ast stack
		this.sourceEnds.put(this.astStack[this.astPtr], selectorSourceEnd);
		rememberCategories();
	}
}

protected void consumeMethodHeaderNameWithTypeParameters(boolean isAnnotationMethod) {
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr];
	int selectorSourceEnd = (int) selectorSourcePositions;
	int currentAstPtr = this.astPtr;
	super.consumeMethodHeaderNameWithTypeParameters(isAnnotationMethod);
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		this.sourceEnds.put(this.astStack[this.astPtr], selectorSourceEnd);
		rememberCategories();
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationName() {
	// MethodInvocation ::= Name '(' ArgumentListopt ')'
	super.consumeMethodInvocationName();

	// when the name is only an identifier...we have a message send to "this" (implicit)
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector,
			args == null ? 0 : args.length,
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
protected void consumeMethodInvocationNameWithTypeArguments() {
	// MethodInvocation ::= Name '.' TypeArguments 'Identifier' '(' ArgumentListopt ')'
	super.consumeMethodInvocationNameWithTypeArguments();

	// when the name is only an identifier...we have a message send to "this" (implicit)
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector,
			args == null ? 0 : args.length,
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationPrimary() {
	super.consumeMethodInvocationPrimary();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector,
			args == null ? 0 : args.length,
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationPrimaryWithTypeArguments() {
	super.consumeMethodInvocationPrimaryWithTypeArguments();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector,
			args == null ? 0 : args.length,
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationSuper() {
	// MethodInvocation ::= 'super' '.' 'Identifier' '(' ArgumentListopt ')'
	super.consumeMethodInvocationSuper();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector,
			args == null ? 0 : args.length,
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
protected void consumeMethodInvocationSuperWithTypeArguments() {
	// MethodInvocation ::= 'super' '.' TypeArguments 'Identifier' '(' ArgumentListopt ')'
	super.consumeMethodInvocationSuperWithTypeArguments();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector,
			args == null ? 0 : args.length,
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}

@Override
protected void consumeNormalAnnotation(boolean isTypeAnnotation) {
	super.consumeNormalAnnotation(isTypeAnnotation);
	Annotation annotation = (Annotation)expressionStack[expressionPtr];
	if (reportReferenceInfo) { // accept annotation type reference
		this.requestor.acceptAnnotationTypeReference(annotation.type.getTypeName(), annotation.sourceStart, annotation.sourceEnd);
	}
}

@Override
protected void consumeSingleMemberAnnotation(boolean isTypeAnnotation) {
	super.consumeSingleMemberAnnotation(isTypeAnnotation);
	SingleMemberAnnotation member = (SingleMemberAnnotation) expressionStack[expressionPtr];
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(TypeConstants.VALUE, 0, member.sourceStart);
	}
}
protected void consumeSingleStaticImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' 'static' Name
	ImportReference impt;
	int length;
	char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
	this.identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
	System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = newImportReference(tokens, positions, false, ClassFileConstants.AccStatic));

	this.modifiers = ClassFileConstants.AccDefault;
	this.modifiersSourceStart = -1; // <-- see comment into modifiersFlag(int)

	if (this.currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = this.scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//this.endPosition is just before the ;
	impt.declarationSourceStart = this.intStack[this.intPtr--];

	if(!this.statementRecoveryActivated &&
			this.options.sourceLevel < ClassFileConstants.JDK1_5 &&
			this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
		impt.modifiers = ClassFileConstants.AccDefault; // convert the static import reference to a non-static importe reference
		this.problemReporter().invalidUsageOfStaticImports(impt);
	}

	// recovery
	if (this.currentElement != null){
		this.lastCheckPoint = impt.declarationSourceEnd+1;
		this.currentElement = this.currentElement.add(impt, 0);
		this.lastIgnoredToken = -1;
		this.restartRecovery = true; // used to avoid branching back into the regular automaton
	}
	if (reportReferenceInfo) {
		// Name for static import is TypeName '.' Identifier
		// => accept unknown ref on identifier
		int tokensLength = impt.tokens.length-1;
		int start = (int) (impt.sourcePositions[tokensLength] >>> 32);
		char[] last = impt.tokens[tokensLength];
		// accept all possible kind for last name, index users will have to select the right one...
		// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=86901
		requestor.acceptFieldReference(last, start);
		requestor.acceptMethodReference(last, 0,start);
		requestor.acceptTypeReference(last, start);
		// accept type name
		if (tokensLength > 0) {
			char[][] compoundName = new char[tokensLength][];
			System.arraycopy(impt.tokens, 0, compoundName, 0, tokensLength);
			int end = (int) impt.sourcePositions[tokensLength-1];
			requestor.acceptTypeReference(compoundName, impt.sourceStart, end);
		}
	}
}

protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name
	/* push an ImportRef build from the last name
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
	this.identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
	System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = newImportReference(tokens, positions, false, ClassFileConstants.AccDefault));

	if (this.currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = this.scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//this.endPosition is just before the ;
	impt.declarationSourceStart = this.intStack[this.intPtr--];

	// recovery
	if (this.currentElement != null){
		this.lastCheckPoint = impt.declarationSourceEnd+1;
		this.currentElement = this.currentElement.add(impt, 0);
		this.lastIgnoredToken = -1;
		this.restartRecovery = true; // used to avoid branching back into the regular automaton
	}
	if (reportReferenceInfo) {
		requestor.acceptTypeReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
protected void consumeStaticImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' 'static' Name '.' '*'
	/* push an ImportRef build from the last name
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
	this.identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
	System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = new ImportReference(tokens, positions, true, ClassFileConstants.AccStatic));

	this.modifiers = ClassFileConstants.AccDefault;
	this.modifiersSourceStart = -1; // <-- see comment into modifiersFlag(int)

	if (this.currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = this.scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//this.endPosition is just before the ;
	impt.declarationSourceStart = this.intStack[this.intPtr--];

	if(!this.statementRecoveryActivated &&
			options.sourceLevel < ClassFileConstants.JDK1_5 &&
			this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
		impt.modifiers = ClassFileConstants.AccDefault; // convert the static import reference to a non-static importe reference
		this.problemReporter().invalidUsageOfStaticImports(impt);
	}

	// recovery
	if (this.currentElement != null){
		this.lastCheckPoint = impt.declarationSourceEnd+1;
		this.currentElement = this.currentElement.add(impt, 0);
		this.lastIgnoredToken = -1;
		this.restartRecovery = true; // used to avoid branching back into the regular automaton
	}
	if (reportReferenceInfo) {
		requestor.acceptTypeReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
	/* push an ImportRef build from the last name
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
	this.identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
	System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = new ImportReference(tokens, positions, true, ClassFileConstants.AccDefault));

    impt.trailingStarPosition = this.intStack[this.intPtr--];
	if (this.currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = this.scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//this.endPosition is just before the ;
	impt.declarationSourceStart = this.intStack[this.intPtr--];

	// recovery
	if (this.currentElement != null){
		this.lastCheckPoint = impt.declarationSourceEnd+1;
		this.currentElement = this.currentElement.add(impt, 0);
		this.lastIgnoredToken = -1;
		this.restartRecovery = true; // used to avoid branching back into the regular automaton
	}
	if (reportReferenceInfo) {
		requestor.acceptUnknownReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
public MethodDeclaration convertToMethodDeclaration(ConstructorDeclaration c, CompilationResult compilationResult) {
	MethodDeclaration methodDeclaration = super.convertToMethodDeclaration(c, compilationResult);
	int selectorSourceEnd = this.sourceEnds.removeKey(c);
	if (selectorSourceEnd != -1)
		this.sourceEnds.put(methodDeclaration, selectorSourceEnd);
	char[][] categories = this.nodesToCategories.remove(c);
	if (categories != null)
		this.nodesToCategories.put(methodDeclaration, categories);

	return methodDeclaration;
}
protected CompilationUnitDeclaration endParse(int act) {
	if (compilationUnit != null) {
    return super.endParse(act);
	} else {
		return null;
	}
}
public TypeReference getTypeReference(int dim) {
	/* build a Reference on a variable that may be qualified or not
	 * This variable is a type reference and dim will be its dimensions
	 */
	int length = identifierLengthStack[identifierLengthPtr--];
	if (length < 0) { //flag for precompiled type reference on base types
		TypeReference ref = TypeReference.baseTypeReference(-length, dim);
		ref.sourceStart = intStack[intPtr--];
		if (dim == 0) {
			ref.sourceEnd = intStack[intPtr--];
		} else {
			intPtr--; // no need to use this position as it is an array
			ref.sourceEnd = endPosition;
		}
		if (reportReferenceInfo){
				requestor.acceptTypeReference(ref.getParameterizedTypeName(), ref.sourceStart, ref.sourceEnd);
		}
		return ref;
	} else {
		int numberOfIdentifiers = this.genericsIdentifiersLengthStack[this.genericsIdentifiersLengthPtr--];
		if (length != numberOfIdentifiers || this.genericsLengthStack[this.genericsLengthPtr] != 0) {
			// generic type
			TypeReference ref = getTypeReferenceForGenericType(dim, length, numberOfIdentifiers);
			if (reportReferenceInfo) {
				if (length == 1 && numberOfIdentifiers == 1) {
					ParameterizedSingleTypeReference parameterizedSingleTypeReference = (ParameterizedSingleTypeReference) ref;
					requestor.acceptTypeReference(parameterizedSingleTypeReference.token, parameterizedSingleTypeReference.sourceStart);
				} else {
					ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference = (ParameterizedQualifiedTypeReference) ref;
					requestor.acceptTypeReference(parameterizedQualifiedTypeReference.tokens, parameterizedQualifiedTypeReference.sourceStart, parameterizedQualifiedTypeReference.sourceEnd);
				}
			}
			return ref;
		} else if (length == 1) {
			// single variable reference
			this.genericsLengthPtr--; // pop the 0
			if (dim == 0) {
				SingleTypeReference ref =
					new SingleTypeReference(
						identifierStack[identifierPtr],
						identifierPositionStack[identifierPtr--]);
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.token, ref.sourceStart);
				}
				return ref;
			} else {
				ArrayTypeReference ref =
					new ArrayTypeReference(
						identifierStack[identifierPtr],
						dim,
						identifierPositionStack[identifierPtr--]);
				ref.sourceEnd = endPosition;
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.token, ref.sourceStart);
				}
				return ref;
			}
		} else {//Qualified variable reference
			this.genericsLengthPtr--;
			char[][] tokens = new char[length][];
			identifierPtr -= length;
			long[] positions = new long[length];
			System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
			System.arraycopy(
				identifierPositionStack,
				identifierPtr + 1,
				positions,
				0,
				length);
			if (dim == 0) {
				QualifiedTypeReference ref = new QualifiedTypeReference(tokens, positions);
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.tokens, ref.sourceStart, ref.sourceEnd);
				}
				return ref;
			} else {
				ArrayQualifiedTypeReference ref =
					new ArrayQualifiedTypeReference(tokens, dim, positions);
				ref.sourceEnd = endPosition;
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.tokens, ref.sourceStart, ref.sourceEnd);
				}
				return ref;
			}
		}
	}
}
public NameReference getUnspecifiedReference() {
	/* build a (unspecified) NameReference which may be qualified*/

	int length;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
		SingleNameReference ref =
			newSingleNameReference(
				identifierStack[identifierPtr],
				identifierPositionStack[identifierPtr--]);
		if (reportReferenceInfo) {
			this.addUnknownRef(ref);
		}
		return ref;
	} else {
		//Qualified variable reference
		char[][] tokens = new char[length][];
		identifierPtr -= length;
		System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
		long[] positions = new long[length];
		System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
		QualifiedNameReference ref =
			newQualifiedNameReference(
				tokens,
				positions,
				(int) (identifierPositionStack[identifierPtr + 1] >> 32), // sourceStart
				(int) identifierPositionStack[identifierPtr + length]); // sourceEnd
		if (reportReferenceInfo) {
			this.addUnknownRef(ref);
		}
		return ref;
	}
}
public NameReference getUnspecifiedReferenceOptimized() {
	/* build a (unspecified) NameReference which may be qualified
	The optimization occurs for qualified reference while we are
	certain in this case the last item of the qualified name is
	a field access. This optimization is IMPORTANT while it results
	that when a NameReference is build, the type checker should always
	look for that it is not a type reference */

	int length;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
		SingleNameReference ref =
			newSingleNameReference(
				identifierStack[identifierPtr],
				identifierPositionStack[identifierPtr--]);
		ref.bits &= ~ASTNode.RestrictiveFlagMASK;
		ref.bits |= Binding.LOCAL | Binding.FIELD;
		if (reportReferenceInfo) {
			this.addUnknownRef(ref);
		}
		return ref;
	}

	//Qualified-variable-reference
	//In fact it is variable-reference DOT field-ref , but it would result in a type
	//conflict tha can be only reduce by making a superclass (or inetrface ) between
	//nameReference and FiledReference or putting FieldReference under NameReference
	//or else..........This optimisation is not really relevant so just leave as it is

	char[][] tokens = new char[length][];
	identifierPtr -= length;
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
	long[] positions = new long[length];
	System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
	QualifiedNameReference ref =
		newQualifiedNameReference(
			tokens,
			positions,
			(int) (identifierPositionStack[identifierPtr + 1] >> 32),
	// sourceStart
	 (int) identifierPositionStack[identifierPtr + length]); // sourceEnd
	ref.bits &= ~ASTNode.RestrictiveFlagMASK;
	ref.bits |= Binding.LOCAL | Binding.FIELD;
	if (reportReferenceInfo) {
		this.addUnknownRef(ref);
	}
	return ref;
}
protected ImportReference newImportReference(char[][] tokens, long[] positions, boolean onDemand, int mod) {
	return new ImportReference(tokens, positions, onDemand, mod);
}
protected QualifiedNameReference newQualifiedNameReference(char[][] tokens, long[] positions, int sourceStart, int sourceEnd) {
	return new QualifiedNameReference(tokens, positions, sourceStart, sourceEnd);
}
protected SingleNameReference newSingleNameReference(char[] source, long positions) {
	return new SingleNameReference(source, positions);
}
public CompilationUnitDeclaration parseCompilationUnit(
	ICompilationUnit unit,
	boolean fullParse,
	IProgressMonitor pm) {

	boolean old = diet;
	CompilationUnitDeclaration parsedUnit = null;
	try {
		diet = true;
		this.reportReferenceInfo = fullParse;
		CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit);
		parsedUnit = parse(unit, compilationUnitResult);
		if (pm != null && pm.isCanceled())
			throw new OperationCanceledException(Messages.operation_cancelled);
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(compilationUnitResult.getLineSeparatorPositions());
		}
		int initialStart = this.scanner.initialPosition;
		int initialEnd = this.scanner.eofPosition;
		if (this.reportLocalDeclarations || fullParse){
			diet = false;
			this.getMethodBodies(parsedUnit);
		}
		this.scanner.resetTo(initialStart, initialEnd);
		this.notifier.notifySourceElementRequestor(
				parsedUnit,
				this.scanner.initialPosition,
				this.scanner.eofPosition,
				this.reportReferenceInfo,
				this.sourceEnds,
				this.nodesToCategories);
		return parsedUnit;
	} catch (AbortCompilation e) {
		// ignore this exception
	} finally {
		diet = old;
		reset();
	}
	return parsedUnit;
}
private void rememberCategories() {
	if (this.useSourceJavadocParser) {
		// AspectJ Change---called to access private variable
		char[][] categories =  getCategoriesField();
		if (categories.length > 0) {
			this.nodesToCategories.put(this.astStack[this.astPtr], categories);
			// AspectJ Change---called to access private variable
			setCategoriesField(CharOperation.NO_CHAR_CHAR);
		}
	}
}
public void reset() {
	this.sourceEnds = new HashtableOfObjectToInt();
	this.nodesToCategories = new HashMap<>();
}

// AspectJ Change
// this method should not be called because we only accept AJCompilationUnitStructureRequestor
//public void setRequestor(ISourceElementRequestor requestor) {
//	this.requestor = requestor;
//	this.notifier.requestor = requestor;
//}

// AspectJ Change Begin
/*
 * use reflection to gain access to private variable
 */
private Field categoriesField;
private char[][] getCategoriesField() {
	try {
		if (categoriesField == null) {
			categoriesField = SourceJavadocParser.class.getDeclaredField("categories");
			categoriesField.setAccessible(true);
		}
		return (char[][]) categoriesField.get(javadocParser);
	} catch (Exception e) {
		return null;
	}
}
private void setCategoriesField(char[][] newCategories) {
	try {
		if (categoriesField == null) {
			categoriesField = SourceJavadocParser.class.getDeclaredField("categories");
			categoriesField.setAccessible(true);
		}
		categoriesField.set(javadocParser, newCategories);
	} catch (Exception ignored) {
	}
}
// AspectJ Change end
}
