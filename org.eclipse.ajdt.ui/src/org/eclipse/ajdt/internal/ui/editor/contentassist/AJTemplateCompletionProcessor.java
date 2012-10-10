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
package org.eclipse.ajdt.internal.ui.editor.contentassist;

import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.corext.template.java.JavaContext;
import org.eclipse.jdt.internal.corext.template.java.JavaContextType;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.java.AbstractTemplateCompletionProposalComputer;
import org.eclipse.jdt.internal.ui.text.template.contentassist.TemplateEngine;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.TemplateContextType;

/**
 * 
 * @author Andrew Eisenberg
 * @created Mar 13, 2012
 */
public class AJTemplateCompletionProcessor extends AbstractTemplateCompletionProposalComputer {
    private static final String ASPECTJ = "aspectj";
    private final TemplateEngine aspectJTemplateEngine;
    
    public AJTemplateCompletionProcessor() {
        ContextTypeRegistry templateContextRegistry= JavaPlugin.getDefault().getTemplateContextRegistry();
        aspectJTemplateEngine = createTemplateEngine(templateContextRegistry);
    }
    
    private TemplateEngine createTemplateEngine(
            ContextTypeRegistry templateContextRegistry) {
        TemplateContextType contextType= templateContextRegistry.getContextType(ASPECTJ);
        if (contextType == null) {
            contextType = new JavaContextType() {
                @Override
                protected void initializeContext(JavaContext context) {
                    context.addCompatibleContextType(ASPECTJ);
                }
            };
            contextType.setId(ASPECTJ);
            templateContextRegistry.addContextType(contextType);
        }
        return new TemplateEngine(contextType);    
    }

    @Override
    protected TemplateEngine computeCompletionEngine(
            JavaContentAssistInvocationContext context) {
        ICompilationUnit unit= context.getCompilationUnit();
        if (!(unit instanceof AJCompilationUnit)) {
            return null;
        }
        IJavaProject javaProject= unit.getJavaProject();
        if (javaProject == null) {
            return null;
        }
        
//        CompletionContext coreContext= context.getCoreContext();
//        if (coreContext != null) {
//            int tokenLocation= coreContext.getTokenLocation();
//            if ((tokenLocation & CompletionContext.TL_MEMBER_START) != 0) {
//                return aspectJTemplateEngine;
//            }
//            if ((tokenLocation & CompletionContext.TL_STATEMENT_START) != 0) {
//                return aspectJTemplateEngine;
//            }
//        }
        return aspectJTemplateEngine;
    }
    

}
