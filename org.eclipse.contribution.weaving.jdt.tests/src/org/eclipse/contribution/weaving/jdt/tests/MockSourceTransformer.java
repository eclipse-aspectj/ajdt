package org.eclipse.contribution.weaving.jdt.tests;

import org.eclipse.contribution.jdt.sourceprovider.ISourceTransformer;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

public class MockSourceTransformer implements ISourceTransformer {

    public static final String MOCK_CLASS_NAME = "Mock";
    
    public static int ensureRealBufferCalled = 0;
    
    public char[] convert(char[] toConvert) {
        return ("class " + MOCK_CLASS_NAME + " {\n\tint x;\n\tint y;\n}").toCharArray();
    }

    public IBuffer ensureRealBuffer(ICompilationUnit unit)
            throws JavaModelException {
        ensureRealBufferCalled++;
        return unit.getBuffer();
    }
}
