package org.eclipse.contribution.weaving.jdt.tests;

import org.eclipse.contribution.jdt.sourceprovider.ISourceTransformer;

public class MockSourceTransformer implements ISourceTransformer {

    public static final String MOCK_CLASS_NAME = "Mock";
    
    public char[] convert(char[] toConvert) {
        return ("class " + MOCK_CLASS_NAME + " {\n\tint x;\n\tint y;\n}").toCharArray();
    }
}
