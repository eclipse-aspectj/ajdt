/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.ajdt.internal.core.parserbridge;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.ajdt.internal.core.ras.NoFFDC;
import org.eclipse.jdt.internal.compiler.ast.FalseLiteral;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LongLiteral;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.ast.NumberLiteral;
import org.eclipse.jdt.internal.compiler.ast.TrueLiteral;

/**
 * Changes to the constructors of several compiler AST nodes has occurred between 3.7.0 and 3.7.1.
 * We need to use reflection to create these objects so that AJDT can be compatible with both older and newer 
 * versions of JDT.
 * @author Andrew Eisenberg
 * @created Sep 23, 2011
 */
public class CompilerASTNodeCompatibilityWrapper implements NoFFDC {

    public static TrueLiteral createJDTTrueLiteral(org.aspectj.org.eclipse.jdt.internal.compiler.ast.TrueLiteral ajdtLiteral) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException {
        return new TrueLiteral(ajdtLiteral.sourceStart, ajdtLiteral.sourceEnd);
    }
    
    public static FalseLiteral createJDTFalseLiteral(org.aspectj.org.eclipse.jdt.internal.compiler.ast.FalseLiteral ajdtLiteral) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException {
        return new FalseLiteral(ajdtLiteral.sourceStart, ajdtLiteral.sourceEnd);
    }
    
    public static NullLiteral createJDTNullLiteral(org.aspectj.org.eclipse.jdt.internal.compiler.ast.NullLiteral ajdtLiteral) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException {
        return new NullLiteral(ajdtLiteral.sourceStart, ajdtLiteral.sourceEnd);
    }
    
    public static IntLiteral createJDTIntLiteral(org.aspectj.org.eclipse.jdt.internal.compiler.ast.IntLiteral ajdtLiteral) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException {
        return (IntLiteral) buildLiteral(ajdtLiteral.source(), ajdtLiteral.sourceStart, ajdtLiteral.sourceEnd, true);
    }
    
    public static IntLiteral createJDTIntLiteralMinValue(org.aspectj.org.eclipse.jdt.internal.compiler.ast.IntLiteralMinValue ajdtLiteral) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException {
        return (IntLiteral) buildLiteral(ajdtLiteral.source(), ajdtLiteral.sourceStart, ajdtLiteral.sourceEnd, true);
    }
    
    public static LongLiteral createJDTLongLiteral(org.aspectj.org.eclipse.jdt.internal.compiler.ast.LongLiteral ajdtLiteral) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException {
        return (LongLiteral) buildLiteral(ajdtLiteral.source(), ajdtLiteral.sourceStart, ajdtLiteral.sourceEnd, false);
    }
    
    public static LongLiteral createJDTLongLiteralMinValue(org.aspectj.org.eclipse.jdt.internal.compiler.ast.LongLiteralMinValue ajdtLiteral) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException, NoSuchMethodException {
        return (LongLiteral) buildLiteral(ajdtLiteral.source(), ajdtLiteral.sourceStart, ajdtLiteral.sourceEnd, false);
    }
    
    private static NumberLiteral buildLiteral(char[] source, int sourceStart, int sourceEnd, boolean isInt) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, InstantiationException, SecurityException, NoSuchMethodException {
        // first try to get the buildXXXLiteral method. if it doesn't exist, then assume
        // that we are on 3.7.
        Method buildXXXLiteralMethod = getXXXLiteralMethod(isInt);
        if (buildXXXLiteralMethod != null) {
            return (NumberLiteral) buildXXXLiteralMethod.invoke(null, source, sourceStart, sourceEnd);
        } else {
            Constructor<? extends NumberLiteral> constructXXXLiteral = getXXXConstructor(isInt);
            if (constructXXXLiteral == null) {
                // shouldn't get here
                return null;
            }
            return constructXXXLiteral.newInstance(source, sourceStart, sourceEnd);
        }
    }

    private static Constructor<? extends NumberLiteral> getXXXConstructor(boolean isInt) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
        if (isInt) {
            if (intLiteralConstructorChecked) {
                return intLiteralConstructorCached;
            } else {
                intLiteralConstructorChecked = true;
            }
        } else {
            if (longLiteralConstructorChecked) {
                return longLiteralConstructorCached;
            } else {
                longLiteralConstructorChecked = true;
            }
        }
        
        @SuppressWarnings("unchecked")
        Class<? extends NumberLiteral> clazz = (Class<? extends NumberLiteral>) Class.forName("org.eclipse.jdt.internal.compiler.ast." + (isInt ? "Int" : "Long") + "Literal");
        Constructor<? extends NumberLiteral> cons = clazz.getConstructor(char[].class, int.class, int.class);
        if (isInt) {
            intLiteralConstructorCached = cons;
        } else {
            longLiteralConstructorCached = cons;
        }
        return cons;
    }

    private static Method getXXXLiteralMethod(boolean isInt) throws ClassNotFoundException {
        if (isInt) {
            if (buildIntLiteralChecked) {
                return buildIntLiteralCached;
            } else {
                buildIntLiteralChecked = true;
            }
        } else {
            if (buildLongLiteralChecked) {
                return buildLongLiteralCached;
            } else {
                buildLongLiteralChecked = true;
            }
        }
        @SuppressWarnings("unchecked")
        Class<? extends NumberLiteral> clazz = (Class<? extends NumberLiteral>) Class.forName("org.eclipse.jdt.internal.compiler.ast." + (isInt ? "Int" : "Long") + "Literal");
        Method buildXXXMethod;
        try {
            buildXXXMethod = clazz.getMethod("build" + (isInt ? "Int" : "Long") + "Literal", char[].class, int.class, int.class);
            if (isInt) {
                buildIntLiteralCached = buildXXXMethod;
            } else {
                buildLongLiteralCached = buildXXXMethod;
            }
        } catch (SecurityException e) {
            buildXXXMethod = null;
        } catch (NoSuchMethodException e) {
            buildXXXMethod = null;
        }
        return buildXXXMethod;
    }
    
    private static boolean buildLongLiteralChecked = false;
    private static boolean buildIntLiteralChecked = false;
    
    private static Method buildLongLiteralCached = null;
    private static Method buildIntLiteralCached = null;
    
    private static boolean longLiteralConstructorChecked = false;
    private static boolean intLiteralConstructorChecked = false;
    
    private static Constructor<? extends NumberLiteral>  longLiteralConstructorCached = null;
    private static Constructor<? extends NumberLiteral>  intLiteralConstructorCached = null;
    
}
