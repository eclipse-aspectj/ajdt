/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     SpringSource    - Conversion to AspectJ
 *******************************************************************************/
package org.eclipse.ajdt.core.parserbridge;

import java.util.ArrayList;
import java.util.Map;

import org.aspectj.ajdt.internal.compiler.ast.AdviceDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.AspectDeclaration;
import org.aspectj.ajdt.internal.compiler.ast.PointcutDeclaration;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.AnnotationMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ExplicitConstructorCall;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;

/**
 * AspectJ changes marked with
 * // AspectJ change
 * @author andrew
 *
 */
public class AJSourceElementNotifier {
	/**
	 * An ast visitor that visits local type declarations.
	 */
	public class LocalDeclarationVisitor extends ASTVisitor {
		ArrayList<TypeDeclaration> declaringTypes;
		public void pushDeclaringType(TypeDeclaration declaringType) {
			if (this.declaringTypes == null) {
				this.declaringTypes = new ArrayList<>();
			}
			this.declaringTypes.add(declaringType);
		}
		public void popDeclaringType() {
			this.declaringTypes.remove(this.declaringTypes.size()-1);
		}
		public TypeDeclaration peekDeclaringType() {
			if (this.declaringTypes == null) return null;
			int size = this.declaringTypes.size();
			if (size == 0) return null;
			return this.declaringTypes.get(size - 1);
		}
		public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
			notifySourceElementRequestor(typeDeclaration, true, peekDeclaringType());
			return false; // don't visit members as this was done during notifySourceElementRequestor(...)
		}
		public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
			notifySourceElementRequestor(typeDeclaration, true, peekDeclaringType());
			return false; // don't visit members as this was done during notifySourceElementRequestor(...)
		}
	}

	AJCompilationUnitStructureRequestor requestor; // AspectJ change
	boolean reportReferenceInfo;
	char[][] typeNames;
	char[][] superTypeNames;
	int nestedTypeIndex;
	LocalDeclarationVisitor localDeclarationVisitor = null;

	HashtableOfObjectToInt sourceEnds;
  Map<? extends ASTNode, ? extends char[][]> nodesToCategories;

	int initialPosition;
	int eofPosition;

public AJSourceElementNotifier(AJCompilationUnitStructureRequestor requestor, boolean reportLocalDeclarations) { // AspectJ Change
	this.requestor = requestor;
	if (reportLocalDeclarations) {
		this.localDeclarationVisitor = new LocalDeclarationVisitor();
	}
	typeNames = new char[4][];
	superTypeNames = new char[4][];
	nestedTypeIndex = 0;
}
protected char[][][] getArguments(Argument[] arguments) {
	int argumentLength = arguments.length;
	char[][] argumentTypes = new char[argumentLength][];
	char[][] argumentNames = new char[argumentLength][];
	for (int i = 0; i < argumentLength; i++) {
		argumentTypes[i] = CharOperation.concatWith(arguments[i].type.getParameterizedTypeName(), '.');
		argumentNames[i] = arguments[i].name;
	}

	return new char[][][] {argumentTypes, argumentNames};
}
protected char[][] getInterfaceNames(TypeDeclaration typeDeclaration) {
	char[][] interfaceNames = null;
	int superInterfacesLength = 0;
	TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
	if (superInterfaces != null) {
		superInterfacesLength = superInterfaces.length;
		interfaceNames = new char[superInterfacesLength][];
	} else {
		if ((typeDeclaration.bits & ASTNode.IsAnonymousType) != 0) {
			// see PR 3442
			QualifiedAllocationExpression alloc = typeDeclaration.allocation;
			if (alloc != null && alloc.type != null) {
				superInterfaces = new TypeReference[] { alloc.type};
				superInterfacesLength = 1;
				interfaceNames = new char[1][];
			}
		}
	}
	if (superInterfaces != null) {
		for (int i = 0; i < superInterfacesLength; i++) {
			interfaceNames[i] =
				CharOperation.concatWith(superInterfaces[i].getParameterizedTypeName(), '.');
		}
	}
	return interfaceNames;
}
protected char[] getSuperclassName(TypeDeclaration typeDeclaration) {
	TypeReference superclass = typeDeclaration.superclass;
	return superclass != null ? CharOperation.concatWith(superclass.getParameterizedTypeName(), '.') : null;
}
protected char[][] getThrownExceptions(AbstractMethodDeclaration methodDeclaration) {
	char[][] thrownExceptionTypes = null;
	TypeReference[] thrownExceptions = methodDeclaration.thrownExceptions;
	if (thrownExceptions != null) {
		int thrownExceptionLength = thrownExceptions.length;
		thrownExceptionTypes = new char[thrownExceptionLength][];
		for (int i = 0; i < thrownExceptionLength; i++) {
			thrownExceptionTypes[i] =
				CharOperation.concatWith(thrownExceptions[i].getParameterizedTypeName(), '.');
		}
	}
	return thrownExceptionTypes;
}
protected char[][] getTypeParameterBounds(TypeParameter typeParameter) {
	TypeReference firstBound = typeParameter.type;
	TypeReference[] otherBounds = typeParameter.bounds;
	char[][] typeParameterBounds = null;
	if (firstBound != null) {
		if (otherBounds != null) {
			int otherBoundsLength = otherBounds.length;
			char[][] boundNames = new char[otherBoundsLength+1][];
			boundNames[0] = CharOperation.concatWith(firstBound.getParameterizedTypeName(), '.');
			for (int j = 0; j < otherBoundsLength; j++) {
				boundNames[j+1] =
					CharOperation.concatWith(otherBounds[j].getParameterizedTypeName(), '.');
			}
			typeParameterBounds = boundNames;
		} else {
			typeParameterBounds = new char[][] { CharOperation.concatWith(firstBound.getParameterizedTypeName(), '.')};
		}
	} else {
		typeParameterBounds = CharOperation.NO_CHAR_CHAR;
	}

	return typeParameterBounds;
}
private ISourceElementRequestor.TypeParameterInfo[] getTypeParameterInfos(TypeParameter[] typeParameters) {
	if (typeParameters == null) return null;
	int typeParametersLength = typeParameters.length;
	ISourceElementRequestor.TypeParameterInfo[] result = new ISourceElementRequestor.TypeParameterInfo[typeParametersLength];
	for (int i = 0; i < typeParametersLength; i++) {
		TypeParameter typeParameter = typeParameters[i];
		char[][] typeParameterBounds = this.getTypeParameterBounds(typeParameter);
		ISourceElementRequestor.TypeParameterInfo typeParameterInfo = new ISourceElementRequestor.TypeParameterInfo();
		typeParameterInfo.declarationStart = typeParameter.declarationSourceStart;
		typeParameterInfo.declarationEnd = typeParameter.declarationSourceEnd;
		typeParameterInfo.name = typeParameter.name;
		typeParameterInfo.nameSourceStart = typeParameter.sourceStart;
		typeParameterInfo.nameSourceEnd = typeParameter.sourceEnd;
		typeParameterInfo.bounds = typeParameterBounds;
		result[i] = typeParameterInfo;
	}
	return result;
}
/*
 * Checks whether one of the annotations is the @Deprecated annotation
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=89807)
 */
private boolean hasDeprecatedAnnotation(Annotation[] annotations) {
	if (annotations != null) {
    for (Annotation annotation : annotations) {
      if (CharOperation.equals(annotation.type.getLastToken(), TypeConstants.JAVA_LANG_DEPRECATED[2])) {
        return true;
      }
    }
	}
	return false;
}
/*
 * Update the bodyStart of the corresponding parse node
 */
protected void notifySourceElementRequestor(AbstractMethodDeclaration methodDeclaration) {

	// range check
	boolean isInRange =
				initialPosition <= methodDeclaration.declarationSourceStart
				&& eofPosition >= methodDeclaration.declarationSourceEnd;

	if (methodDeclaration.isClinit()) {
		this.visitIfNeeded(methodDeclaration);
		return;
	}

	if (methodDeclaration.isDefaultConstructor()) {
		if (reportReferenceInfo) {
			ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) methodDeclaration;
			ExplicitConstructorCall constructorCall = constructorDeclaration.constructorCall;
			if (constructorCall != null) {
				switch(constructorCall.accessMode) {
					case ExplicitConstructorCall.This :
						requestor.acceptConstructorReference(
							typeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length,
							constructorCall.sourceStart);
						break;
					case ExplicitConstructorCall.Super :
					case ExplicitConstructorCall.ImplicitSuper :
						requestor.acceptConstructorReference(
							superTypeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length,
							constructorCall.sourceStart);
						break;
				}
			}
		}
		return;
	}
	char[][] argumentTypes = null;
	char[][] argumentNames = null;
	boolean isVarArgs = false;
	Argument[] arguments = methodDeclaration.arguments;
	if (arguments != null) {
		char[][][] argumentTypesAndNames = this.getArguments(arguments);
		argumentTypes = argumentTypesAndNames[0];
		argumentNames = argumentTypesAndNames[1];

		isVarArgs = arguments[arguments.length-1].isVarArgs();
	}
	char[][] thrownExceptionTypes = getThrownExceptions(methodDeclaration);
	// by default no selector end position
	int selectorSourceEnd = -1;
	if (methodDeclaration.isConstructor()) {
		selectorSourceEnd = this.sourceEnds.get(methodDeclaration);
		if (isInRange){
			int currentModifiers = methodDeclaration.modifiers;
			if (isVarArgs)
				currentModifiers |= ClassFileConstants.AccVarargs;

			// remember deprecation to not lose it below
			boolean deprecated = (currentModifiers & ClassFileConstants.AccDeprecated) != 0 || hasDeprecatedAnnotation(methodDeclaration.annotations);

			ISourceElementRequestor.MethodInfo methodInfo = new ISourceElementRequestor.MethodInfo();
			methodInfo.isConstructor = true;
			methodInfo.declarationStart = methodDeclaration.declarationSourceStart;
			methodInfo.modifiers = deprecated ? (currentModifiers & ExtraCompilerModifiers.AccJustFlag) | ClassFileConstants.AccDeprecated : currentModifiers & ExtraCompilerModifiers.AccJustFlag;
			methodInfo.name = methodDeclaration.selector;
			methodInfo.nameSourceStart = methodDeclaration.sourceStart;
			methodInfo.nameSourceEnd = selectorSourceEnd;
			methodInfo.parameterTypes = argumentTypes;
			methodInfo.parameterNames = argumentNames;
			methodInfo.exceptionTypes = thrownExceptionTypes;
			methodInfo.typeParameters = getTypeParameterInfos(methodDeclaration.typeParameters());
			methodInfo.categories = this.nodesToCategories.get(methodDeclaration);
			methodInfo.annotations = methodDeclaration.annotations;
			methodInfo.node = methodDeclaration;
			requestor.enterConstructor(methodInfo);
		}
		if (reportReferenceInfo) {
			ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) methodDeclaration;
			ExplicitConstructorCall constructorCall = constructorDeclaration.constructorCall;
			if (constructorCall != null) {
				switch(constructorCall.accessMode) {
					case ExplicitConstructorCall.This :
						requestor.acceptConstructorReference(
							typeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length,
							constructorCall.sourceStart);
						break;
					case ExplicitConstructorCall.Super :
					case ExplicitConstructorCall.ImplicitSuper :
						requestor.acceptConstructorReference(
							superTypeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length,
							constructorCall.sourceStart);
						break;
				}
			}
		}
		this.visitIfNeeded(methodDeclaration);
		if (isInRange){
			requestor.exitConstructor(methodDeclaration.declarationSourceEnd);
		}
		return;
	}
	selectorSourceEnd = this.sourceEnds.get(methodDeclaration);

	// AspectJ Change Begin
	// recreate source locations for Pointcuts and Advice
	org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration ajmDec = new org.aspectj.org.eclipse.jdt.internal.compiler.ast.MethodDeclaration(new org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult(methodDeclaration.compilationResult.fileName, methodDeclaration.compilationResult.unitIndex, methodDeclaration.compilationResult.totalUnitsKnown, 500));
	if (ajmDec instanceof PointcutDeclaration) {
		selectorSourceEnd = methodDeclaration.sourceStart + methodDeclaration.selector.length - 1;
	}
	if (ajmDec instanceof AdviceDeclaration) {
		selectorSourceEnd = methodDeclaration.sourceStart + ((AdviceDeclaration) ajmDec).kind.getName().length() - 1;
	}
	// AspectJ Change End

	if (isInRange) {
		int currentModifiers = methodDeclaration.modifiers;
		if (isVarArgs)
			currentModifiers |= ClassFileConstants.AccVarargs;

		// remember deprecation so as to not lose it below
		boolean deprecated = (currentModifiers & ClassFileConstants.AccDeprecated) != 0 || hasDeprecatedAnnotation(methodDeclaration.annotations);

		TypeReference returnType = methodDeclaration instanceof MethodDeclaration
			? ((MethodDeclaration) methodDeclaration).returnType
			: null;
		ISourceElementRequestor.MethodInfo methodInfo = new ISourceElementRequestor.MethodInfo();
		methodInfo.isAnnotation = methodDeclaration instanceof AnnotationMethodDeclaration;
		methodInfo.declarationStart = methodDeclaration.declarationSourceStart;
		methodInfo.modifiers = deprecated ? (currentModifiers & ExtraCompilerModifiers.AccJustFlag) | ClassFileConstants.AccDeprecated : currentModifiers & ExtraCompilerModifiers.AccJustFlag;
		methodInfo.returnType = returnType == null ? null : CharOperation.concatWith(returnType.getParameterizedTypeName(), '.');
		methodInfo.name = methodDeclaration.selector;
		methodInfo.nameSourceStart = methodDeclaration.sourceStart;
		methodInfo.nameSourceEnd = selectorSourceEnd;
		methodInfo.parameterTypes = argumentTypes;
		methodInfo.parameterNames = argumentNames;
		methodInfo.exceptionTypes = thrownExceptionTypes;
		methodInfo.typeParameters = getTypeParameterInfos(methodDeclaration.typeParameters());
		methodInfo.categories = this.nodesToCategories.get(methodDeclaration);
		methodInfo.annotations = methodDeclaration.annotations;
		methodInfo.node = methodDeclaration;
		requestor.enterMethod(methodInfo);
	}

	this.visitIfNeeded(methodDeclaration);

	if (isInRange) {
		if (methodDeclaration instanceof AnnotationMethodDeclaration) {
			AnnotationMethodDeclaration annotationMethodDeclaration = (AnnotationMethodDeclaration) methodDeclaration;
			Expression expression = annotationMethodDeclaration.defaultValue;
			if (expression != null) {
				requestor.exitMethod(methodDeclaration.declarationSourceEnd, expression);
				return;
			}
		}
		requestor.exitMethod(methodDeclaration.declarationSourceEnd, (org.aspectj.org.eclipse.jdt.internal.compiler.ast.Expression) null);
	}
}

/*
 * Update the bodyStart of the corresponding parse node
 */
public void notifySourceElementRequestor(
  CompilationUnitDeclaration parsedUnit,
  int sourceStart,
  int sourceEnd,
  boolean reportReference,
  HashtableOfObjectToInt sourceEndsMap,
  Map<? extends ASTNode, ? extends char[][]> nodesToCategoriesMap) {

	this.initialPosition = sourceStart;
	this.eofPosition = sourceEnd;

	this.reportReferenceInfo = reportReference;
	this.sourceEnds = sourceEndsMap;
	this.nodesToCategories = nodesToCategoriesMap;

	try {
		// range check
		boolean isInRange =
					initialPosition <= parsedUnit.sourceStart
					&& eofPosition >= parsedUnit.sourceEnd;

		// collect the top level ast nodes
		int length = 0;
		ASTNode[] nodes = null;
		if (isInRange) {
			requestor.enterCompilationUnit();
		}
		ImportReference currentPackage = parsedUnit.currentPackage;
		ImportReference[] imports = parsedUnit.imports;
		TypeDeclaration[] types = parsedUnit.types;
		length =
			(currentPackage == null ? 0 : 1)
			+ (imports == null ? 0 : imports.length)
			+ (types == null ? 0 : types.length);
		nodes = new ASTNode[length];
		int index = 0;
		if (currentPackage != null) {
			nodes[index++] = currentPackage;
		}
		if (imports != null) {
      for (ImportReference anImport : imports) {
        nodes[index++] = anImport;
      }
		}
		if (types != null) {
      for (TypeDeclaration type : types) {
        nodes[index++] = type;
      }
		}

		// notify the nodes in the syntactical order
		if (length > 0) {
			quickSort(nodes, 0, length-1);
			for (int i=0;i<length;i++) {
				ASTNode node = nodes[i];
				if (node instanceof ImportReference) {
					ImportReference importRef = (ImportReference)node;
          notifySourceElementRequestor(importRef, node == parsedUnit.currentPackage);
				} else { // instanceof TypeDeclaration
					notifySourceElementRequestor((TypeDeclaration)node, true, null);
				}
			}
		}

		if (isInRange) {
			requestor.exitCompilationUnit(parsedUnit.sourceEnd);
		}
	} finally {
		this.reset();
	}
}

/*
* Update the bodyStart of the corresponding parse node
*/
protected void notifySourceElementRequestor(FieldDeclaration fieldDeclaration, TypeDeclaration declaringType) {

	// range check
	boolean isInRange =
				initialPosition <= fieldDeclaration.declarationSourceStart
				&& eofPosition >= fieldDeclaration.declarationSourceEnd;

	switch(fieldDeclaration.getKind()) {
		case AbstractVariableDeclaration.ENUM_CONSTANT:
			if (this.reportReferenceInfo) {
				// accept constructor reference for enum constant
				if (fieldDeclaration.initialization instanceof AllocationExpression) {
					AllocationExpression alloc = (AllocationExpression) fieldDeclaration.initialization;
					requestor.acceptConstructorReference(
						declaringType.name,
						alloc.arguments == null ? 0 : alloc.arguments.length,
						alloc.sourceStart);
				}
			}
			// fall through next case
		case AbstractVariableDeclaration.FIELD:
			int fieldEndPosition = this.sourceEnds.get(fieldDeclaration);
			if (fieldEndPosition == -1) {
				// use the declaration source end by default
				fieldEndPosition = fieldDeclaration.declarationSourceEnd;
			}
			if (isInRange) {
				int currentModifiers = fieldDeclaration.modifiers;

				// remember deprecation so as to not lose it below
				boolean deprecated = (currentModifiers & ClassFileConstants.AccDeprecated) != 0 || hasDeprecatedAnnotation(fieldDeclaration.annotations);

				char[] typeName = null;
				if (fieldDeclaration.type == null) {
					// enum constant
					typeName = declaringType.name;
					currentModifiers |= ClassFileConstants.AccEnum;
				} else {
					// regular field
					typeName = CharOperation.concatWith(fieldDeclaration.type.getParameterizedTypeName(), '.');
				}
				ISourceElementRequestor.FieldInfo fieldInfo = new ISourceElementRequestor.FieldInfo();
				fieldInfo.declarationStart = fieldDeclaration.declarationSourceStart;
				fieldInfo.name = fieldDeclaration.name;
				fieldInfo.modifiers = deprecated ? (currentModifiers & ExtraCompilerModifiers.AccJustFlag) | ClassFileConstants.AccDeprecated : currentModifiers & ExtraCompilerModifiers.AccJustFlag;
				fieldInfo.type = typeName;
				fieldInfo.nameSourceStart = fieldDeclaration.sourceStart;
				fieldInfo.nameSourceEnd = fieldDeclaration.sourceEnd;
				fieldInfo.categories = this.nodesToCategories.get(fieldDeclaration);
				fieldInfo.annotations = fieldDeclaration.annotations;
				fieldInfo.node = fieldDeclaration;
				requestor.enterField(fieldInfo);
			}
			this.visitIfNeeded(fieldDeclaration, declaringType);
			if (isInRange){
				requestor.exitField(
					// filter out initializations that are not a constant (simple check)
					(fieldDeclaration.initialization == null
							|| fieldDeclaration.initialization instanceof ArrayInitializer
							|| fieldDeclaration.initialization instanceof AllocationExpression
							|| fieldDeclaration.initialization instanceof ArrayAllocationExpression
							|| fieldDeclaration.initialization instanceof Assignment
							|| fieldDeclaration.initialization instanceof ClassLiteralAccess
							|| fieldDeclaration.initialization instanceof MessageSend
							|| fieldDeclaration.initialization instanceof ArrayReference
							|| fieldDeclaration.initialization instanceof ThisReference) ?
						-1 :
						fieldDeclaration.initialization.sourceStart,
					fieldEndPosition,
					fieldDeclaration.declarationSourceEnd);
			}
			break;
		case AbstractVariableDeclaration.INITIALIZER:
			if (isInRange){
				requestor.enterInitializer(
					fieldDeclaration.declarationSourceStart,
					fieldDeclaration.modifiers);
			}
			this.visitIfNeeded((Initializer)fieldDeclaration);
			if (isInRange){
				requestor.exitInitializer(fieldDeclaration.declarationSourceEnd);
			}
			break;
	}
}
protected void notifySourceElementRequestor(
	ImportReference importReference,
	boolean isPackage) {
	if (isPackage) {
		requestor.acceptPackage(importReference);
	} else {
		requestor.acceptImport(
			importReference.declarationSourceStart,
			importReference.declarationSourceEnd,
			importReference.tokens,
			(importReference.bits & ASTNode.OnDemand) != 0,
			importReference.modifiers);
	}
}
protected void notifySourceElementRequestor(TypeDeclaration typeDeclaration, boolean notifyTypePresence, TypeDeclaration declaringType) {

	if (CharOperation.equals(TypeConstants.PACKAGE_INFO_NAME, typeDeclaration.name)) return;

    //	AspectJ change begin
	boolean isAspect = false;
	org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration ajtypeDeclaration =
	    new org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration(
	        new org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult(
	                typeDeclaration.compilationResult.fileName, typeDeclaration.compilationResult.unitIndex,
	                typeDeclaration.compilationResult.totalUnitsKnown, 500));
	if (ajtypeDeclaration instanceof AspectDeclaration) {
		isAspect = true;
	}
    //	AspectJ change end


	// range check
	boolean isInRange =
		initialPosition <= typeDeclaration.declarationSourceStart
		&& eofPosition >= typeDeclaration.declarationSourceEnd;

	FieldDeclaration[] fields = typeDeclaration.fields;
	AbstractMethodDeclaration[] methods = typeDeclaration.methods;
	TypeDeclaration[] memberTypes = typeDeclaration.memberTypes;
	int fieldCounter = fields == null ? 0 : fields.length;
	int methodCounter = methods == null ? 0 : methods.length;
	int memberTypeCounter = memberTypes == null ? 0 : memberTypes.length;
	int fieldIndex = 0;
	int methodIndex = 0;
	int memberTypeIndex = 0;

	if (notifyTypePresence){
		char[][] interfaceNames = getInterfaceNames(typeDeclaration);
		int kind = TypeDeclaration.kind(typeDeclaration.modifiers);
		char[] implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_OBJECT;
		if (isInRange) {
			int currentModifiers = typeDeclaration.modifiers;

			// remember deprecation to not lose it below
			boolean deprecated = (currentModifiers & ClassFileConstants.AccDeprecated) != 0 || hasDeprecatedAnnotation(typeDeclaration.annotations);

			boolean isEnumInit = typeDeclaration.allocation != null && typeDeclaration.allocation.enumConstant != null;
			char[] superclassName;
			if (isEnumInit) {
				currentModifiers |= ClassFileConstants.AccEnum;
				superclassName = declaringType.name;
			} else {
				superclassName = getSuperclassName(typeDeclaration);
			}
			ISourceElementRequestor.TypeInfo typeInfo = new ISourceElementRequestor.TypeInfo();
			if (typeDeclaration.allocation == null) {
				typeInfo.declarationStart = typeDeclaration.declarationSourceStart;
			} else if (isEnumInit) {
				typeInfo.declarationStart = typeDeclaration.allocation.enumConstant.sourceStart;
			} else {
				typeInfo.declarationStart = typeDeclaration.allocation.sourceStart;
			}
			typeInfo.modifiers = deprecated ? (currentModifiers & ExtraCompilerModifiers.AccJustFlag) | ClassFileConstants.AccDeprecated : currentModifiers & ExtraCompilerModifiers.AccJustFlag;
			typeInfo.name = typeDeclaration.name;
			typeInfo.nameSourceStart = isEnumInit ? typeDeclaration.allocation.enumConstant.sourceStart : typeDeclaration.sourceStart;
			typeInfo.nameSourceEnd = sourceEnd(typeDeclaration);
			typeInfo.superclass = superclassName;
			typeInfo.superinterfaces = interfaceNames;
			typeInfo.typeParameters = getTypeParameterInfos(typeDeclaration.typeParameters);
			typeInfo.categories = this.nodesToCategories.get(typeDeclaration);
			typeInfo.secondary = typeDeclaration.isSecondary();
			typeInfo.anonymousMember = typeDeclaration.allocation != null && typeDeclaration.allocation.enclosingInstance != null;
			typeInfo.annotations = typeDeclaration.annotations;
			typeInfo.node = typeDeclaration;
			requestor.enterType(typeInfo, isAspect, (isAspect ? ((AspectDeclaration) ajtypeDeclaration).isPrivileged : false)); // AspectJ change
			switch (kind) {
				case TypeDeclaration.CLASS_DECL :
					if (superclassName != null)
						implicitSuperclassName = superclassName;
					break;
				case TypeDeclaration.INTERFACE_DECL :
					implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_OBJECT;
					break;
				case TypeDeclaration.ENUM_DECL :
					implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_ENUM;
					break;
				case TypeDeclaration.ANNOTATION_TYPE_DECL :
					implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_ANNOTATION_ANNOTATION;
					break;
			}
		}
		if (this.nestedTypeIndex == this.typeNames.length) {
			// need a resize
			System.arraycopy(this.typeNames, 0, (this.typeNames = new char[this.nestedTypeIndex * 2][]), 0, this.nestedTypeIndex);
			System.arraycopy(this.superTypeNames, 0, (this.superTypeNames = new char[this.nestedTypeIndex * 2][]), 0, this.nestedTypeIndex);
		}
		this.typeNames[this.nestedTypeIndex] = typeDeclaration.name;
		this.superTypeNames[this.nestedTypeIndex++] = implicitSuperclassName;
	}
	while ((fieldIndex < fieldCounter)
			|| (memberTypeIndex < memberTypeCounter)
			|| (methodIndex < methodCounter)) {
		FieldDeclaration nextFieldDeclaration = null;
		AbstractMethodDeclaration nextMethodDeclaration = null;
		TypeDeclaration nextMemberDeclaration = null;

		int position = Integer.MAX_VALUE;
		int nextDeclarationType = -1;
		if (fieldIndex < fieldCounter) {
			nextFieldDeclaration = fields[fieldIndex];
			if (nextFieldDeclaration.declarationSourceStart < position) {
				position = nextFieldDeclaration.declarationSourceStart;
				nextDeclarationType = 0; // FIELD
			}
		}
		if (methodIndex < methodCounter) {
			nextMethodDeclaration = methods[methodIndex];
			if (nextMethodDeclaration.declarationSourceStart < position) {
				position = nextMethodDeclaration.declarationSourceStart;
				nextDeclarationType = 1; // METHOD
			}
		}
		if (memberTypeIndex < memberTypeCounter) {
			nextMemberDeclaration = memberTypes[memberTypeIndex];
			if (nextMemberDeclaration.declarationSourceStart < position) {
				position = nextMemberDeclaration.declarationSourceStart;
				nextDeclarationType = 2; // MEMBER
			}
		}
		switch (nextDeclarationType) {
			case 0 :
				fieldIndex++;
				notifySourceElementRequestor(nextFieldDeclaration, typeDeclaration);
				break;
			case 1 :
				methodIndex++;
				notifySourceElementRequestor(nextMethodDeclaration);
				break;
			case 2 :
				memberTypeIndex++;
				notifySourceElementRequestor(nextMemberDeclaration, true, null);
		}
	}
	if (notifyTypePresence){
		if (isInRange){
			requestor.exitType(typeDeclaration.declarationSourceEnd);
		}
		nestedTypeIndex--;
	}
}
/*
 * Sort the given ast nodes by their positions.
 */
private static void quickSort(ASTNode[] sortedCollection, int left, int right) {
	int original_left = left;
	int original_right = right;
	ASTNode mid = sortedCollection[left +  (right - left) / 2];
	do {
		while (sortedCollection[left].sourceStart < mid.sourceStart) {
			left++;
		}
		while (mid.sourceStart < sortedCollection[right].sourceStart) {
			right--;
		}
		if (left <= right) {
			ASTNode tmp = sortedCollection[left];
			sortedCollection[left] = sortedCollection[right];
			sortedCollection[right] = tmp;
			left++;
			right--;
		}
	} while (left <= right);
	if (original_left < right) {
		quickSort(sortedCollection, original_left, right);
	}
	if (left < original_right) {
		quickSort(sortedCollection, left, original_right);
	}
}
private void reset() {
	typeNames = new char[4][];
	superTypeNames = new char[4][];
	nestedTypeIndex = 0;

	this.sourceEnds = null;
}
private int sourceEnd(TypeDeclaration typeDeclaration) {
	if ((typeDeclaration.bits & ASTNode.IsAnonymousType) != 0) {
		QualifiedAllocationExpression allocation = typeDeclaration.allocation;
		if (allocation.enumConstant != null) // case of enum constant body
			return allocation.enumConstant.sourceEnd;
		return allocation.type.sourceEnd;
	} else {
		return typeDeclaration.sourceEnd;
	}
}
private void visitIfNeeded(AbstractMethodDeclaration method) {
	if (this.localDeclarationVisitor != null
		&& (method.bits & ASTNode.HasLocalType) != 0) {
			if (method instanceof ConstructorDeclaration) {
				ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) method;
				if (constructorDeclaration.constructorCall != null) {
					constructorDeclaration.constructorCall.traverse(this.localDeclarationVisitor, method.scope);
				}
			}
			if (method.statements != null) {
				int statementsLength = method.statements.length;
				for (int i = 0; i < statementsLength; i++)
					method.statements[i].traverse(this.localDeclarationVisitor, method.scope);
			}
	}
}

private void visitIfNeeded(FieldDeclaration field, TypeDeclaration declaringType) {
	if (this.localDeclarationVisitor != null
		&& (field.bits & ASTNode.HasLocalType) != 0) {
			if (field.initialization != null) {
				try {
					this.localDeclarationVisitor.pushDeclaringType(declaringType);
					field.initialization.traverse(this.localDeclarationVisitor, (MethodScope) null);
				} finally {
					this.localDeclarationVisitor.popDeclaringType();
				}
			}
	}
}

private void visitIfNeeded(Initializer initializer) {
	if (this.localDeclarationVisitor != null
		&& (initializer.bits & ASTNode.HasLocalType) != 0) {
			if (initializer.block != null) {
				initializer.block.traverse(this.localDeclarationVisitor, null);
			}
	}
}
}
