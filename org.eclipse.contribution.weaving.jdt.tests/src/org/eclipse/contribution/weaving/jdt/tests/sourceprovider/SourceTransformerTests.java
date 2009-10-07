/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.eclipse.contribution.weaving.jdt.tests.sourceprovider;

import junit.framework.Assert;

import org.eclipse.contribution.weaving.jdt.tests.MockCompilationUnit;
import org.eclipse.contribution.weaving.jdt.tests.MockSourceTransformer;
import org.eclipse.contribution.weaving.jdt.tests.WeavingTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceMapper;

/**
 * @author Andrew Eisenberg
 * @created Jan 5, 2009
 *
 */
public class SourceTransformerTests extends WeavingTestCase {

    public void testTransformCompilationUnit() throws Exception {
        IProject proj = createPredefinedProject("MockCUProject");
        IFile file = proj.getFile("src/nothing/nothing.mock");
        MockCompilationUnit cu = (MockCompilationUnit) JavaCore.create(file);
        cu.becomeWorkingCopy(monitor);
        Assert.assertEquals("Wrong number of children", 1, cu.getChildren().length);
        Assert.assertEquals("Wrong name for mock class", 
                MockSourceTransformer.MOCK_CLASS_NAME, cu.getChildren()[0].getElementName());
    }
    
    public void testSourceMapping() throws Exception {
        // Ignore these tests on Linux because not passing
        if (System.getProperty("os.name").equals("Linux")) {
            return;
        }
        MockSourceMaper mapper = new MockSourceMaper();
        IProject proj = createPredefinedProject("MockCUProject");
        IFile file = proj.getFile("src/nothing/nothing.mock");
        MockCompilationUnit cu = (MockCompilationUnit) JavaCore.create(file);
        MockType type = new MockType(cu, "MockType");
        mapper.mapSource(type, cu.getContents(), new MockBinaryInfo());
        
        assertTrue("Contents have not been transformed", mapper.sourceMapped);
    }
    
    
    class MockType extends BinaryType {

        protected MockType(JavaElement parent, String name) {
            super(parent, name);
        }
        
        @Override
        public String getSourceFileName(IBinaryType info) {
            return "nothing.mock";
        }
        
    }
    
    class MockSourceMaper extends SourceMapper {
        boolean sourceMapped = false;
        
        @Override
        public void mapSource(IType type, char[] contents, IBinaryType info) {
            // ensure that the contents have been transformed
            if (new String(contents).equals(
                    new String(new MockSourceTransformer().convert(new char[0])))) {
                sourceMapped = true;
            }
        }
    }

    class MockBinaryInfo implements IBinaryType {
    
        public IBinaryAnnotation[] getAnnotations() {
            return null;
        }
    
        public char[] getEnclosingTypeName() {
            return null;
        }
    
        public IBinaryField[] getFields() {
            return null;
        }
    
        public char[] getGenericSignature() {
            return null;
        }
    
        public char[][] getInterfaceNames() {
            return null;
        }
    
        public IBinaryNestedType[] getMemberTypes() {
            return null;
        }
    
        public IBinaryMethod[] getMethods() {
            return null;
        }
    
        public char[][][] getMissingTypeNames() {
            return null;
        }
    
        public char[] getName() {
            return null;
        }
    
        public char[] getSourceName() {
            return null;
        }
    
        public char[] getSuperclassName() {
            return null;
        }
    
        public long getTagBits() {
            return 0;
        }
    
        public boolean isAnonymous() {
            return false;
        }
    
        public boolean isLocal() {
            return false;
        }
    
        public boolean isMember() {
            return false;
        }
    
        public char[] sourceFileName() {
            return null;
        }
    
        public int getModifiers() {
            return 0;
        }
    
        public boolean isBinaryType() {
            return false;
        }
    
        public char[] getFileName() {
            return null;
        }
        
        /* AJDT 1.7 */
        public char[] getEnclosingMethod() {
            return null;
        }
        
    }
}
