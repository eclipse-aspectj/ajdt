package org.eclipse.ajdt.core;

import java.lang.reflect.Field;

import org.eclipse.ajdt.core.javaelements.AspectElement;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;

public class AJMementoTokenizer extends MementoTokenizer {
    private static final String COUNT = Character.toString(JavaElement.JEM_COUNT);
    private static final String JAVAPROJECT = Character.toString(JavaElement.JEM_JAVAPROJECT);
    private static final String PACKAGEFRAGMENTROOT = Character.toString(JavaElement.JEM_PACKAGEFRAGMENTROOT);
    private static final String PACKAGEFRAGMENT = Character.toString(JavaElement.JEM_PACKAGEFRAGMENT);
    private static final String FIELD = Character.toString(JavaElement.JEM_FIELD);
    private static final String METHOD = Character.toString(JavaElement.JEM_METHOD);
    private static final String INITIALIZER = Character.toString(JavaElement.JEM_INITIALIZER);
    private static final String COMPILATIONUNIT = Character.toString(JavaElement.JEM_COMPILATIONUNIT);
    private static final String CLASSFILE = Character.toString(JavaElement.JEM_CLASSFILE);
    private static final String TYPE = Character.toString(JavaElement.JEM_TYPE);
    private static final String PACKAGEDECLARATION = Character.toString(JavaElement.JEM_PACKAGEDECLARATION);
    private static final String IMPORTDECLARATION = Character.toString(JavaElement.JEM_IMPORTDECLARATION);
    private static final String LOCALVARIABLE = Character.toString(JavaElement.JEM_LOCALVARIABLE);
    private static final String TYPE_PARAMETER = Character.toString(JavaElement.JEM_TYPE_PARAMETER);
    private static final String ANNOTATION = Character.toString(JavaElement.JEM_ANNOTATION);

    // begin AspectJ change
    private static final String ASPECT_CU = Character
            .toString(AspectElement.JEM_ASPECT_CU);

    private static final String ADVICE = Character
            .toString(AspectElement.JEM_ADVICE);

    private static final String ASPECT_TYPE = Character
            .toString(AspectElement.JEM_ASPECT_TYPE);

    private static final String CODEELEMENT = Character
            .toString(AspectElement.JEM_CODEELEMENT);

    private static final String ITD = Character.toString(AspectElement.JEM_ITD);

    private static final String DECLARE = Character
            .toString(AspectElement.JEM_DECLARE);

    private static final String POINTCUT = Character
            .toString(AspectElement.JEM_POINTCUT);

    // end AspectJ change

    private final char[] memento;

    private final int length;

    private int index = 0;

    public AJMementoTokenizer(String memento) {
        super(memento);
        this.memento = memento.toCharArray();
        this.length = this.memento.length;
    }

    public AJMementoTokenizer(MementoTokenizer tokenizer) {
        super(String.valueOf(getMemento(tokenizer)));
        memento = getMemento(tokenizer);
        length = memento.length;
        index = getIndex(new String(memento));
        setIndex(this, index);
    }

    private static Field mementoField;

    private static char[] getMemento(MementoTokenizer tokenizer) {
        if (mementoField == null) {
            try {
                mementoField = MementoTokenizer.class
                        .getDeclaredField("memento"); //$NON-NLS-1$
                mementoField.setAccessible(true);
            } catch (Exception e) {
                return null;
            }
        }
        try {
            return (char[]) mementoField.get(tokenizer);
        } catch (IllegalArgumentException e) {} catch (IllegalAccessException e) {}
        return null;
    }

    private static Field indexField;

    private static int getIndex(String memento) {
        return Math.max(memento.indexOf(AspectElement.JEM_ASPECT_TYPE), memento
                .indexOf(JavaElement.JEM_TYPE));
    }

    private static void setIndex(MementoTokenizer tokenizer, int val) {
        if (indexField == null) {
            try {
                indexField = MementoTokenizer.class.getDeclaredField("index"); //$NON-NLS-1$
                indexField.setAccessible(true);
            } catch (Exception e) {}
        }
        try {
            indexField.setInt(tokenizer, val);
        } catch (IllegalArgumentException e) {} catch (IllegalAccessException e) {}
    }

    void setIndexTo(int newIndex) {
        this.index = newIndex;
    }

    public boolean hasMoreTokens() {
        return this.index < this.length;
    }

    public String nextToken() {
        int start = this.index;
        StringBuffer buffer = null;
        switch (this.memento[this.index++]) {
            case JavaElement.JEM_ESCAPE:
                buffer = new StringBuffer();
                buffer.append(this.memento[this.index]);
                start = ++this.index;
                break;
            case JavaElement.JEM_COUNT:
                return COUNT;
            case JavaElement.JEM_JAVAPROJECT:
                return JAVAPROJECT;
            case JavaElement.JEM_PACKAGEFRAGMENTROOT:
                return PACKAGEFRAGMENTROOT;
            case JavaElement.JEM_PACKAGEFRAGMENT:
                return PACKAGEFRAGMENT;
            case JavaElement.JEM_FIELD:
                return FIELD;
            case JavaElement.JEM_METHOD:
                return METHOD;
            case JavaElement.JEM_INITIALIZER:
                return INITIALIZER;
            case JavaElement.JEM_COMPILATIONUNIT:
                return COMPILATIONUNIT;
            case JavaElement.JEM_CLASSFILE:
                return CLASSFILE;
            case JavaElement.JEM_TYPE:
                return TYPE;
            case JavaElement.JEM_PACKAGEDECLARATION:
                return PACKAGEDECLARATION;
            case JavaElement.JEM_IMPORTDECLARATION:
                return IMPORTDECLARATION;
            case JavaElement.JEM_LOCALVARIABLE:
                return LOCALVARIABLE;
            case JavaElement.JEM_TYPE_PARAMETER:
                return TYPE_PARAMETER;
                // begin AspectJ change
                // comment out annotation, because it is the same as aspect
//            case JavaElement.JEM_ANNOTATION:
//                return ANNOTATION;
            case AspectElement.JEM_ASPECT_CU:
                return ASPECT_CU;
            case AspectElement.JEM_ADVICE:
                return ADVICE;
            case AspectElement.JEM_ASPECT_TYPE:
                return ASPECT_TYPE;
            case AspectElement.JEM_CODEELEMENT:
                return CODEELEMENT;
            case AspectElement.JEM_ITD:
                return ITD;
            case AspectElement.JEM_DECLARE:
                return DECLARE;
            case AspectElement.JEM_POINTCUT:
                return POINTCUT;
                // end AspectJ change
        }
        loop: while (this.index < this.length) {
            switch (this.memento[this.index]) {
                case JavaElement.JEM_ESCAPE:
                    if (buffer == null)
                        buffer = new StringBuffer();
                    buffer.append(this.memento, start, this.index - start);
                    start = ++this.index;
                    break;
                case JavaElement.JEM_COUNT:
                case JavaElement.JEM_JAVAPROJECT:
                case JavaElement.JEM_PACKAGEFRAGMENTROOT:
                case JavaElement.JEM_PACKAGEFRAGMENT:
                case JavaElement.JEM_FIELD:
                case JavaElement.JEM_METHOD:
                case JavaElement.JEM_INITIALIZER:
                case JavaElement.JEM_COMPILATIONUNIT:
                case JavaElement.JEM_CLASSFILE:
                case JavaElement.JEM_TYPE:
                case JavaElement.JEM_PACKAGEDECLARATION:
                case JavaElement.JEM_IMPORTDECLARATION:
                case JavaElement.JEM_LOCALVARIABLE:
                case JavaElement.JEM_TYPE_PARAMETER:
                    // begin AspectJ change
//                case JavaElement.JEM_ANNOTATION:
                case AspectElement.JEM_ASPECT_CU:
                case AspectElement.JEM_ADVICE:
                case AspectElement.JEM_ASPECT_TYPE:
                case AspectElement.JEM_CODEELEMENT:
                case AspectElement.JEM_ITD:
                case AspectElement.JEM_DECLARE:
                case AspectElement.JEM_POINTCUT:
                    // end AspectJ change
                    break loop;
            }
            this.index++;
        }
        if (buffer != null) {
            buffer.append(this.memento, start, this.index - start);
            return buffer.toString();
        }
        return new String(this.memento, start, this.index - start);
    }
}