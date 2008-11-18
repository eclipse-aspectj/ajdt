package org.eclipse.ajdt.core.codeconversion;

import java.lang.reflect.Field;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.ITDAwareSourceTypeInfo;
import org.eclipse.ajdt.core.javaelements.NotImplementedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.ISearchRequestor;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.CancelableNameEnvironment;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;

public class ITDAwareCancelableNameEnvironment extends
        CancelableNameEnvironment {

    public ITDAwareCancelableNameEnvironment(JavaProject project,
            WorkingCopyOwner owner, IProgressMonitor monitor)
            throws JavaModelException {
        super(project, owner, monitor);
    }


    protected NameEnvironmentAnswer find(String typeName, String packageName) {
        if (packageName == null)
            packageName = IPackageFragment.DEFAULT_PACKAGE_NAME;
        NameLookup.Answer answer =
            this.nameLookup.findType(
                typeName,
                packageName,
                false/*exact match*/,
                NameLookup.ACCEPT_ALL,
                this.checkAccessRestrictions);
        if (answer != null) {
            // construct name env answer
            if (answer.type instanceof BinaryType) { // BinaryType
                try {
                    return new NameEnvironmentAnswer((IBinaryType) ((BinaryType) answer.type).getElementInfo(), getRestriction(answer));
                } catch (JavaModelException npe) {
                    return null;
                }
            } else { // SourceType
                try {
                    SourceType sourceType = (SourceType) answer.type;
                    SourceTypeElementInfo sourceTypeInfo;
                    IType[] types;
                    
                    try {
                        // retrieve the requested type
                        sourceTypeInfo = (SourceTypeElementInfo) sourceType.getElementInfo();
                        // find all siblings (other types declared in same unit, since may be used for name resolution)
                        types = sourceTypeInfo.getHandle().getCompilationUnit().getTypes();
                    } catch (JavaModelException e) {
                        // this might be an AspectElement
                        // convert to an aspect element handle
                        // and then try to recreate
                        //  XXX this will only work if the type is a top-level aspect
                        //  OK for now.
                        String ajHandle = sourceType.getHandleIdentifier();
                        sourceType = ((SourceType) AspectJCore.create(
                                AspectJCore.convertToAspectHandle(ajHandle, sourceType)));
                        sourceTypeInfo = (SourceTypeElementInfo) sourceType.getElementInfo();
                        types = ((AJCompilationUnit) sourceType.getParent()).getTypes();
                    }
                    
                    ISourceType topLevelType = sourceTypeInfo;
                    while (topLevelType.getEnclosingType() != null) {
                        topLevelType = topLevelType.getEnclosingType();
                    }
                    
                    ISourceType[] sourceTypes = new ISourceType[types.length];
                    
                    // in the resulting collection, ensure the requested type is the first one
                    ITDAwareSourceTypeInfo newInfo = new ITDAwareSourceTypeInfo(sourceTypeInfo, sourceType); // AspectJ Change
                    sourceTypes[0] = newInfo;
                    int length = types.length;
                    for (int i = 0, index = 1; i < length; i++) {
                        ISourceType otherType =
                            (ISourceType) ((JavaElement) types[i]).getElementInfo();
                        if (!otherType.equals(topLevelType) && index < length) { // check that the index is in bounds (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=62861)
                            ITDAwareSourceTypeInfo newOtherInfo = 
                                    new ITDAwareSourceTypeInfo(otherType, (SourceType) types[i]); // AspectJ Change
                            sourceTypes[index++] = newOtherInfo; // AspectJ Change
                        }
                    }
                    return new NameEnvironmentAnswer(sourceTypes, getRestriction(answer));
                } catch (JavaModelException npe) {
                    return null;
                }
            }
        }
        return null;
    }


    public NameEnvironmentAnswer findType(char[] name, char[][] packageName) {
        return super.findType(name, packageName);
    }

    public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
        return super.findType(compoundTypeName);
    }

    public void findTypes(char[] prefix, boolean findMembers,
            boolean camelCaseMatch, int searchFor, ISearchRequestor storage) {
        super.findTypes(prefix, findMembers, camelCaseMatch, searchFor, storage);
//        throw new NotImplementedException();

    }

    public void findExactTypes(char[] name, boolean findMembers, int searchFor,
            ISearchRequestor storage) {
//        super.findExactTypes(name, findMembers, searchFor, storage);
        throw new NotImplementedException();
    }

    private static Field restrictionField;
    private AccessRestriction getRestriction(NameLookup.Answer answer) {
        try {
            if (restrictionField == null) {
                restrictionField = NameLookup.Answer.class.getDeclaredField("restriction");
                restrictionField.setAccessible(true);
            }
            return (AccessRestriction) restrictionField.get(answer);
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (SecurityException e) {
        } catch (NoSuchFieldException e) {
        }
        return null;
    }
    
    public void setUnitToSkip(ICompilationUnit unit) {
        this.unitToSkip = unit;
    }

}