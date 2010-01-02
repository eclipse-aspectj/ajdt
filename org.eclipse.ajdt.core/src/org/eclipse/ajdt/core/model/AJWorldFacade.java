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

package org.eclipse.ajdt.core.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.ajde.core.AjCompiler;
import org.aspectj.ajdt.internal.core.builder.AjBuildManager;
import org.aspectj.ajdt.internal.core.builder.AjState;
import org.aspectj.ajdt.internal.core.builder.IncrementalStateManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IProgramElement.Accessibility;
import org.aspectj.asm.internal.ProgramElement;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.Signature;

/**
 * @author Andrew Eisenberg
 * @created Jan 13, 2009
 * 
 * This class provides AJDT access to the {@link World} object.
 * Clients must not hold a reference to any world object 
 * for longer than necessary
 */
public final class AJWorldFacade {
    public static class ErasedTypeSignature {
        public ErasedTypeSignature(String returnType, String[] paramTypes) {
            super();
            this.returnType = returnType;
            this.paramTypes = paramTypes;
        }
        public final String returnType;
        public final String[] paramTypes;
    }
    
    public static class ITDInfo {
        
        static ITDInfo create(ConcreteTypeMunger cMunger) {
            ResolvedType aspectType = cMunger.getAspectType();
            if (aspectType != null) {
                ResolvedMember sig = cMunger.getSignature();
                Accessibility a = sig != null ? 
                        ProgramElement.genAccessibility(sig.getModifiers()) :
                        Accessibility.PUBLIC;
                String packageDeclaredIn = aspectType.getPackageName();
                String topLevelAspectName = aspectType.getOutermostType().getClassName();
                return new ITDInfo(a, packageDeclaredIn, topLevelAspectName);
            } else { 
                return null;
            }
        }
        
        public ITDInfo(Accessibility accessibility, String packageDeclaredIn,
                String topLevelAspectName) {
            this.accessibility = accessibility;
            this.packageDeclaredIn = packageDeclaredIn;
            this.topLevelAspectName = topLevelAspectName;
        }
        public final Accessibility accessibility;
        public final String packageDeclaredIn;
        public final String topLevelAspectName;
    }

    private final AjBuildManager manager;
    private final World world;
    
    public AJWorldFacade(IProject project) {
        AjCompiler compiler = AspectJPlugin.getDefault().getCompilerFactory().getCompilerForProject(project);
        AjState state = IncrementalStateManager.retrieveStateFor(compiler.getId());
        if (state != null) {
            manager = state.getAjBuildManager();
            world = manager.getWorld();
        } else {
            manager = null;
            world = null;
        }
    }
    
    private Map /* char[] -> List<ConcreteTypeMunger> */ cachedMungers;
    
    private void cacheMunger(char[] typeName, List mungers) {
        if (cachedMungers == null) {
            cachedMungers = new HashMap();
        }
        cachedMungers.put(typeName, mungers);
    }

    public ITDInfo findITDInfoIfExists(char[] targetTypeSignature, char[] name) {
        if (world == null || targetTypeSignature == null) {
            return null;
        }
        List itds;
        String nameStr = new String(name);
        if (cachedMungers != null && cachedMungers.containsKey(targetTypeSignature)) {
            itds = (List) cachedMungers.get(targetTypeSignature);
            if (itds == null) {
                return null;
            }
        } else {
            ResolvedType type = null;
            try {
                String sig = createSignature(targetTypeSignature);
                type = world.getCoreType(UnresolvedType.forSignature(sig));
            } catch (Exception e) {
                // don't cache
                return null;
            }
            if (type == null || type.isMissing()) {
                cacheMunger(targetTypeSignature, null);
                return null;
            }
            itds = type.getInterTypeMungersIncludingSupers();
            cacheMunger(targetTypeSignature, itds);
        }
        
        for (Iterator iterator = itds.iterator(); iterator.hasNext();) {
            ConcreteTypeMunger cMunger = (ConcreteTypeMunger) iterator.next();
            ResolvedTypeMunger munger = cMunger.getMunger();
            if (munger == null) {
                continue;
            }
            if (munger.getKind() == ResolvedTypeMunger.Field) {
                if (munger.getSignature().getName().equals(nameStr)) {
                    return ITDInfo.create(cMunger);
                }
            }
            
            if (munger.getKind() == ResolvedTypeMunger.Method) {
                // also need to compare parameters, but parameters
                // are expensive to calculate
                if (munger.getSignature().getName().equals(nameStr)) {
                    return ITDInfo.create(cMunger);
                }
            }
        }
        return null;
    }

    private String createSignature(char[] targetTypeSignature) {
        char[] copy = new char[targetTypeSignature.length];
        System.arraycopy(targetTypeSignature, 0, copy, 0, copy.length);
        
        // AspectJ parameterized signatures start with 'P'
        boolean isGeneric = false;
        for (int i = 0; i < copy.length; i++) {
            if (copy[i] == '<') {
                isGeneric = true;
            }
        }
        
        if (isGeneric) {
            copy[0] = 'P';
        }
        String sig = new String(copy);
        sig = sig.replace('.', '/');
        
        return sig;
    }

    public ErasedTypeSignature getTypeParameters(String typeSignature, IProgramElement elt) {
        if (world == null) {
            return null;
        }
        ResolvedType type = world.resolve(UnresolvedType.forSignature(typeSignature));
        if (type == null || type.isMissing()) {
            return null;
        }
        List itds = type.getInterTypeMungersIncludingSupers();
        ConcreteTypeMunger myMunger = null;
        for (Iterator iterator = itds.iterator(); iterator.hasNext();) {
            ConcreteTypeMunger munger = (ConcreteTypeMunger) iterator.next();
            if (equalSignatures(elt, munger)) {
                myMunger = munger;
                break;
            }
        }
        if (myMunger == null) {
            return null;
        }
        
        String returnType = myMunger.getSignature().getReturnType().getErasureSignature();
        returnType = returnType.replaceAll("/", "\\.");
        returnType = Signature.toString(returnType);
        UnresolvedType[] parameterTypes = myMunger.getSignature().getParameterTypes();
        String[] parameterTypesStr = new String[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypesStr[i] = parameterTypes[i].getErasureSignature();
            parameterTypesStr[i] = parameterTypesStr[i].replaceAll("/", "\\.");
            parameterTypesStr[i] = Signature.toString(parameterTypesStr[i]);
        }
        return new ErasedTypeSignature(returnType, parameterTypesStr);
    }

    private boolean equalSignatures(IProgramElement elt, ConcreteTypeMunger munger) {
        try {
            return equalNames(elt, munger) && equalParams(elt, munger);
        } catch (NullPointerException e) {
            // lots of things can be null
            return false;
        }
    }

    private boolean equalNames(IProgramElement elt, ConcreteTypeMunger munger) {
        ResolvedMember signature = munger.getSignature();
        return signature != null && (qualifiedElementName(signature)).equals(
                qualifiedElementName(elt));
    }

    private String qualifiedElementName(ResolvedMember signature) {
        return signature.getDeclaringType().getBaseName() + "." + signature.getName();
    }

    private String qualifiedElementName(IProgramElement elt) {
        String packageName = elt.getPackageName();
        if (packageName != null && packageName.length() > 0) {
            return elt.getPackageName() + "." + elt.getName();
        } else {
            return elt.getName();
        }
    }

    private boolean equalParams(IProgramElement elt, ConcreteTypeMunger munger) {
        UnresolvedType[] unresolvedTypes = munger.getSignature().getParameterTypes();
        List eltTypes = elt.getParameterTypes();
        int unresolvedTypesLength = unresolvedTypes == null ? 0 : unresolvedTypes.length;
        int eltTypesLength = eltTypes == null ? 0 : eltTypes.size();
        if (unresolvedTypesLength != eltTypesLength) {
            return false;
        }
        for (int i = 0; i < unresolvedTypesLength; i++) {
            String eltParamType = new String( (char[]) eltTypes.get(i));
            int genericStart = eltParamType.indexOf('<');
            if (genericStart > -1) {
                eltParamType = eltParamType.substring(0, genericStart);
            }
            String unresolvedTypeName = unresolvedTypes[i].getName();
            genericStart = unresolvedTypeName.indexOf('<');
            if (genericStart > -1) {
                unresolvedTypeName = unresolvedTypeName.substring(0, genericStart);
            }
            if (! unresolvedTypeName.equals(eltParamType)) {
                return false;
            }
        }
        return true;
    }
}
