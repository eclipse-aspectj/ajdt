/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.internal.core.ras.NoFFDC;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * @author Luzius Meisser
 */
public class AdviceElement extends AspectJMemberElement implements IAspectJElement, NoFFDC {
	
	public AdviceElement(JavaElement parent, String name, String[] parameterTypes) {
		super(parent, name, parameterTypes);
	}
	
	/**
	 */
	public String readableName() {

		StringBuffer buffer = new StringBuffer(super.readableName());
		buffer.append('(');
		String[] parameterTypes = this.getParameterTypes();
		int length;
		if (parameterTypes != null && (length = parameterTypes.length) > 0) {
			for (int i = 0; i < length; i++) {
				buffer.append(Signature.toString(parameterTypes[i]));
				if (i < length - 1) {
					buffer.append(", "); //$NON-NLS-1$
				}
			}
		}
		buffer.append(')');
		return buffer.toString();
	}
	
	protected void toStringName(StringBuffer buffer) {
		buffer.append(getElementName());
		buffer.append('(');
		String[] parameters = this.getParameterTypes();
		int length;
		if (parameters != null && (length = parameters.length) > 0) {
			for (int i = 0; i < length; i++) {
				buffer.append(Signature.toString(parameters[i]));
				if (i < length - 1) {
					buffer.append(", "); //$NON-NLS-1$
				}
			}
		}
		buffer.append(')');
		if (this.occurrenceCount > 1) {
			buffer.append("#"); //$NON-NLS-1$
			buffer.append(this.occurrenceCount);
		}
	}
		
	/**
	 * @see JavaElement#getHandleMemento()
	 */
	protected char getHandleMementoDelimiter() {
		return AspectElement.JEM_ADVICE;
	}
	
	protected Object createElementInfo() {
	    try {
    	    IProgramElement ipe = AJProjectModelFactory.getInstance().getModelForJavaElement(this)
    	            .javaElementToProgramElement(this);
    	    
    	    AdviceElementInfo info = new AdviceElementInfo();
    	    info.setAJExtraInfo(ipe.getExtraInfo());
    	    info.setName(name.toCharArray());
    	    info.setAJKind(IProgramElement.Kind.ADVICE);
    	    info.setAJModifiers(ipe.getModifiers());
    	    ISourceLocation sourceLocation = ipe.getSourceLocation();
            info.setSourceRangeStart(sourceLocation.getOffset());
            info.setNameSourceStart(sourceLocation.getOffset());
            info.setNameSourceEnd(sourceLocation.getOffset() + ipe.getName().length());
	    
    	    return info;
	    } catch (Exception e) {
	        // can fail for any of a number of reasons.
	        // return null so that we can try again later.
	        return null;
	    }
	}
	
//   public String getHandleIdentifier() {
//       try {
//           return super.getHandleIdentifier() + 
//               (occurrenceCount > 1 ? "!" + occurrenceCount : "") +
//               AspectElement.JEM_EXTRA_INFO + ((AdviceElementInfo) getElementInfo()).getSourceRange().getOffset();
//       } catch (JavaModelException e) {
//           return super.getHandleIdentifier() + 
//           (occurrenceCount > 1 ? "!" + occurrenceCount : "");
//       }
//    }
}
