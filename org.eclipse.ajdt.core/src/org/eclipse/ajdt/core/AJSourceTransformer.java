package org.eclipse.ajdt.core;

import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.codeconversion.ConversionOptions;
import org.eclipse.ajdt.core.codeconversion.JavaCompatibleBuffer;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.contribution.jdt.sourceprovider.ISourceTransformer;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

public class AJSourceTransformer implements ISourceTransformer {

    public char[] convert(char[] toConvert) {
        AspectsConvertingParser parser = new AspectsConvertingParser((char[]) toConvert.clone());
        parser.convert(ConversionOptions.CONSTANT_SIZE_KEEP_POINTCUTS);
        return parser.content;
    }

    public IBuffer ensureRealBuffer(ICompilationUnit unit) throws JavaModelException {
        IBuffer buffer = unit.getBuffer();
        if (buffer instanceof JavaCompatibleBuffer) {
            buffer = ((JavaCompatibleBuffer) buffer).getRealBuffer();
        }
        return buffer;
    }

}
