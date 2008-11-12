package org.eclipse.ajdt.core.codeconversion;

import org.eclipse.ajdt.core.javaelements.ITDAwareSourceTypeDELETEME;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.core.IJavaElementRequestor;
import org.eclipse.jdt.internal.core.JavaElementRequestor;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.SourceType;

public class ITDAwareNameLookupDELETEME extends NameLookup {
    
    private NameLookup delegate;
    
    public ITDAwareNameLookupDELETEME(NameLookup delegate) {
        super(null, null, null, null);
        this.delegate = delegate;
    }


    public ICompilationUnit findCompilationUnit(String qualifiedTypeName) {
        return delegate.findCompilationUnit(qualifiedTypeName);
    }

    public IPackageFragment findPackageFragment(IPath path) {
        return delegate.findPackageFragment(path);
    }

    public IPackageFragment[] findPackageFragments(String name,
            boolean partialMatch, boolean patternMatch) {
        return delegate.findPackageFragments(name, partialMatch, patternMatch);
    }

    public IPackageFragment[] findPackageFragments(String name,
            boolean partialMatch) {
        return delegate.findPackageFragments(name, partialMatch);
    }

    public Answer findType(String name, boolean partialMatch, int acceptFlags,
            boolean considerSecondaryTypes, boolean waitForIndexes,
            boolean checkRestrictions, IProgressMonitor monitor) {
        Answer answer = delegate.findType(name, partialMatch, acceptFlags, considerSecondaryTypes,
                waitForIndexes, checkRestrictions, monitor);
        if (answer != null && answer.type instanceof SourceType) {
            answer.type = new ITDAwareSourceTypeDELETEME((SourceType) answer.type);
        }
        return answer;
    }

    public Answer findType(String name, boolean partialMatch, int acceptFlags,
            boolean checkRestrictions) {
        Answer answer = delegate.findType(name, partialMatch, acceptFlags, checkRestrictions);
        if (answer != null && answer.type instanceof SourceType) {
            answer.type = new ITDAwareSourceTypeDELETEME((SourceType) answer.type);
        }
        return answer;
    }

    public IType findType(String name, boolean partialMatch, int acceptFlags) {
        IType type = delegate.findType(name, partialMatch, acceptFlags);
        if (type instanceof SourceType) {
            type = new ITDAwareSourceTypeDELETEME((SourceType) type);
        }
        return type;
    }

    public IType findType(String name, IPackageFragment pkg,
            boolean partialMatch, int acceptFlags,
            boolean considerSecondaryTypes) {
        IType type = delegate.findType(name, pkg, partialMatch, acceptFlags,
                considerSecondaryTypes);
        if (type instanceof SourceType) {
            type = new ITDAwareSourceTypeDELETEME((SourceType) type);
        }
        return type;
    }

    public IType findType(String name, IPackageFragment pkg,
            boolean partialMatch, int acceptFlags) {
        IType type = delegate.findType(name, pkg, partialMatch, acceptFlags);
        if (type instanceof SourceType) {
            type = new ITDAwareSourceTypeDELETEME((SourceType) type);
        }
        return type;
    }

    public Answer findType(String typeName, String packageName,
            boolean partialMatch, int acceptFlags,
            boolean considerSecondaryTypes, boolean waitForIndexes,
            boolean checkRestrictions, IProgressMonitor monitor) {
        Answer answer = delegate.findType(typeName, packageName, partialMatch, acceptFlags,
                considerSecondaryTypes, waitForIndexes, checkRestrictions, monitor);
        if (answer != null &&answer.type instanceof SourceType) {
            answer.type = new ITDAwareSourceTypeDELETEME((SourceType) answer.type);
        }
        return answer;
    }

    public Answer findType(String typeName, String packageName,
            boolean partialMatch, int acceptFlags, boolean checkRestrictions) {
        Answer answer = delegate.findType(typeName, packageName, partialMatch, acceptFlags,
                checkRestrictions);
        if (answer != null && answer.type instanceof SourceType) {
            answer.type = new ITDAwareSourceTypeDELETEME((SourceType) answer.type);
        }
        return answer;
    }

    public boolean isPackage(String[] pkgName) {
        return delegate.isPackage(pkgName);
    }

    public void seekPackageFragments(String name, boolean partialMatch,
            IJavaElementRequestor requestor) {
        delegate.seekPackageFragments(name, partialMatch, requestor);
    }

    public void seekTypes(String name, IPackageFragment pkg,
            boolean partialMatch, int acceptFlags,
            IJavaElementRequestor requestor) {
        JavaElementRequestor innerRequestor = new JavaElementRequestor();
        
        delegate.seekTypes(name, pkg, partialMatch, acceptFlags, innerRequestor);
        IType[] types = innerRequestor.getMemberTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i] instanceof SourceType) {
                requestor.acceptMemberType(new ITDAwareSourceTypeDELETEME((SourceType) types[i]));
            } else {
                requestor.acceptMemberType(types[i]);
            }
        }
        types = innerRequestor.getTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i] instanceof SourceType) {
                requestor.acceptType(new ITDAwareSourceTypeDELETEME((SourceType) types[i]));
            } else {
                requestor.acceptType(types[i]);
            }
        }
    }

}
