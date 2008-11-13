package org.eclipse.ajdt.core;

import org.eclipse.ajdt.core.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.core.codeconversion.ConversionOptions;
import org.eclipse.contribution.jdt.sourceprovider.ISourceTransformer;

public class AJSourceTransformer implements ISourceTransformer {

    public char[] convert(char[] toConvert) {
        AspectsConvertingParser parser = new AspectsConvertingParser(toConvert);
        parser.convert(ConversionOptions.CONSTANT_SIZE);
        return parser.content;
    }

}
