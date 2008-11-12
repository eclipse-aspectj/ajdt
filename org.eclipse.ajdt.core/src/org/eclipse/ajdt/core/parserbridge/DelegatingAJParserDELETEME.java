package org.eclipse.ajdt.core.parserbridge;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.internal.core.parserbridge.AJCompilationUnitDeclarationWrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.RecoveredElement;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * This parser delegates to an AJDT parser, but
 * its hierarchy is consistent with the JDT parser 
 * family
 * @author andrew
 *
 */
public class DelegatingAJParserDELETEME extends AJSourceElementParser2 {

    public DelegatingAJParserDELETEME(AJSourceElementParser ajdtParser, 
            IProblemFactory problemFactory,
            boolean reportLocalDeclarations, boolean optimizeStringLiterals) {
        super((AJCompilationUnitStructureRequestor) ajdtParser.requestor, problemFactory, new CompilerOptions(ajdtParser.options.getMap()),
                reportLocalDeclarations, optimizeStringLiterals);
        this.ajdtParser = ajdtParser;
    }

    private final AJSourceElementParser ajdtParser;
    
    public AJSourceElementParser getAjdtParser() {
        return ajdtParser;
    }
    
    public CompilationUnitDeclaration parseCompilationUnit(
            final ICompilationUnit unit, boolean fullParse, IProgressMonitor pm) {
        
        org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration unitDecl = ajdtParser.parseCompilationUnit(new org.aspectj.org.eclipse.jdt.internal.compiler.env.ICompilationUnit() {
            public char[] getContents() {
                return unit.getContents();
            }
            public char[] getMainTypeName() {
                return unit.getMainTypeName();
            }
            public char[][] getPackageName() {
                return unit.getPackageName();
            }
            public char[] getFileName() {
                return unit.getFileName();
            }
        }, fullParse /*full parse to find local elements*/);

        return new AJCompilationUnitDeclarationWrapper(unitDecl, (AJCompilationUnit) unit);
    }

    public void addUnknownRef(NameReference nameRef) {
        // TODO Auto-generated method stub
        super.addUnknownRef(nameRef);
    }

    public void checkComment() {
        // TODO Auto-generated method stub
        super.checkComment();
    }

    public MethodDeclaration convertToMethodDeclaration(
            ConstructorDeclaration c, CompilationResult compilationResult) {
        // TODO Auto-generated method stub
        return super.convertToMethodDeclaration(c, compilationResult);
    }

    public TypeReference getTypeReference(int dim) {
        // TODO Auto-generated method stub
        return super.getTypeReference(dim);
    }

    public NameReference getUnspecifiedReference() {
        // TODO Auto-generated method stub
        return super.getUnspecifiedReference();
    }

    public NameReference getUnspecifiedReferenceOptimized() {
        // TODO Auto-generated method stub
        return super.getUnspecifiedReferenceOptimized();
    }

    public int[][] getCommentsPositions() {
        // TODO Auto-generated method stub
        return super.getCommentsPositions();
    }

    public void initialize() {
        // TODO Auto-generated method stub
        super.initialize();
    }

    public void initialize(boolean initializeNLS) {
        // TODO Auto-generated method stub
        super.initialize(initializeNLS);
    }

    public void initializeScanner() {
        // TODO Auto-generated method stub
        super.initializeScanner();
    }

    protected void resetModifiers() {
        // TODO Auto-generated method stub
        super.resetModifiers();
    }

    public void arrayInitializer(int length) {
        // TODO Auto-generated method stub
        super.arrayInitializer(length);
    }

    public RecoveredElement buildInitialRecoveryState() {
        // TODO Auto-generated method stub
        return super.buildInitialRecoveryState();
    }

    public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit,
            CompilationResult compilationResult) {
        // TODO Auto-generated method stub
        return super.dietParse(sourceUnit, compilationResult);
    }

    public int getFirstToken() {
        // TODO Auto-generated method stub
        return super.getFirstToken();
    }

    public int[] getJavaDocPositions() {
        // TODO Auto-generated method stub
        return super.getJavaDocPositions();
    }

    public void getMethodBodies(CompilationUnitDeclaration unit) {
//        ajdtParser.getMethodBodies(unit);
    }

    public ProblemReporter problemReporter() {
        // TODO Auto-generated method stub
        return super.problemReporter();
    }
    
    
//    org.aspectj.org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration wrapUnit(CompilationUnitDeclaration unit) {
//        
//    }
}
