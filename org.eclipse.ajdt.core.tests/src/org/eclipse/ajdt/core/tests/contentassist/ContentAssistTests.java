package org.eclipse.ajdt.core.tests.contentassist;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Tests a whole bunch of ways that ITDs can show up in content assist
 * @author andrew
 *
 */
public class ContentAssistTests extends AJDTCoreTestCase {
    AJCompilationUnit hasITDsUnit;
    AJCompilationUnit usesITDsUnit;
    
    String hasITDsContents;
    String usesITDsContents;
    
    protected void setUp() throws Exception {
        super.setUp();
        IProject proj = createPredefinedProject("ITDContentAssist");
        
        hasITDsUnit = (AJCompilationUnit) AspectJCore.create(proj.getFile("src/hasitds/HasITDs.aj"));
        hasITDsUnit.becomeWorkingCopy(null);
        JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(hasITDsUnit, true, false, null);
        
        usesITDsUnit = (AJCompilationUnit) AspectJCore.create(proj.getFile("src/uses/UsesITDs.aj"));
        usesITDsUnit.becomeWorkingCopy(null);
        JavaModelManager.getJavaModelManager().getPerWorkingCopyInfo(usesITDsUnit, true, false, null);
        
        hasITDsUnit.requestOriginalContentMode();
        hasITDsContents = new String(hasITDsUnit.getContents());
        hasITDsUnit.discardOriginalContentMode();

        usesITDsUnit.requestOriginalContentMode();
        usesITDsContents = new String(usesITDsUnit.getContents());
        usesITDsUnit.discardOriginalContentMode();
    }
    
    public void testITDField() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("this.lis") + "this.lis".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());
        assertEquals("Proposal should have been the 'list' field", 
                "list", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }
    
    public void testITDFieldInField() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("this.list.addAll") + "this.list.addAll".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 2 proposals, but found:\n" + requestor.toString(), 2, requestor.accepted.size());
        assertEquals("Proposal should have been the 'addAll' method\n" + requestor.accepted.get(0), 
                "addAll", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
        assertEquals("Proposal should have been the 'addAll' method\n" + requestor.accepted.get(1), 
                "addAll", new String(((CompletionProposal) requestor.accepted.get(1)).getName())); 
    }
    
    public void testITDMethod() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("this.makeL") + "this.makeL".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());
        assertEquals("Proposal should have been the 'makeList' method\n" + requestor.accepted.get(0),
                "makeList", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }
    
    public void testITDConstructor() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("new HasITDs(") + "new HasITDs(".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 2 proposals, but found:\n" + requestor.toString(), 2, requestor.accepted.size());

        // the anonymous class decl
        assertEquals("Signature of proposal should have been the 'HasITDs' constructor\n" + requestor.accepted.get(0), 
                "HasITDs", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
        // the constructor
        assertEquals("Proposal should have been the 'HasITDs' constructor\n" + requestor.accepted.get(1), 
                "Lhasitds.HasITDs;", new String(((CompletionProposal) requestor.accepted.get(1)).getDeclarationSignature()));
    }
    
    public void testFromDeclareParent() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("this.insid") + "this.insid".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        assertEquals("Signature of proposal should have been the 'Foo.inside' field\n" + requestor.accepted.get(0), 
                "inside", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }

    public void testFromITDOnDeclareParent() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = hasITDsContents.indexOf("this.valu") + "this.valu".length();
        hasITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        assertEquals("Signature of proposal should have been the 'Foo.value' field\n" + requestor.accepted.get(0), 
                "value", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }
    
    
    // the remaining tests try the same thing, but in a different class

    public void testITDFieldInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("h.lis") + "h.lis".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());
        assertEquals("Proposal should have been the 'list' field", 
                "list", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }
    
    public void testITDFieldInFieldInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("h.list.addAl") + "h.list.addAl".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 2 proposals, but found:\n" + requestor.toString(), 2, requestor.accepted.size());
        assertEquals("Proposal should have been the 'addAll' method\n" + requestor.accepted.get(0), 
                "addAll", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
        assertEquals("Proposal should have been the 'addAll' method\n" + requestor.accepted.get(1), 
                "addAll", new String(((CompletionProposal) requestor.accepted.get(1)).getName())); 
    }
    
    public void testITDMethodInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("h.makeL") + "h.makeL".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());
        assertEquals("Proposal should have been the 'makeList' method\n" + requestor.accepted.get(0),
                "makeList", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }
    
    public void testITDConstructorInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("new HasITDs(") + "new HasITDs(".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 2 proposals, but found:\n" + requestor.toString(), 2, requestor.accepted.size());

        // the anonymous class decl
        assertEquals("Signature of proposal should have been the 'HasITDs' constructor\n" + requestor.accepted.get(0), 
                "HasITDs", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
        // the constructor
        assertEquals("Proposal should have been the 'HasITDs' constructor\n" + requestor.accepted.get(1), 
                "Lhasitds.HasITDs;", new String(((CompletionProposal) requestor.accepted.get(1)).getDeclarationSignature()));
    }
    
    public void testFromDeclareParentInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("h.insid") + "h.insid".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        assertEquals("Signature of proposal should have been the 'Foo.inside' field\n" + requestor.accepted.get(0), 
                "inside", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }

    public void testFromITDOnDeclareParentInOtherClass() throws Exception {
        MockCompletionRequestor requestor = new MockCompletionRequestor();
        int offset = usesITDsContents.indexOf("h.valu") + "h.valu".length();
        usesITDsUnit.codeComplete(offset, requestor, AJWorkingCopyOwner.INSTANCE);
        
        assertEquals("Should have 1 proposal, but found:\n" + requestor.toString(), 1, requestor.accepted.size());

        assertEquals("Signature of proposal should have been the 'Foo.value' field\n" + requestor.accepted.get(0), 
                "value", new String(((CompletionProposal) requestor.accepted.get(0)).getName())); 
    }

}

class MockCompletionRequestor extends CompletionRequestor {
    
    List accepted = new LinkedList();
    
    public void accept(CompletionProposal proposal) {
        accepted.add(proposal);
    }
    
    public List getAccepted() {
        return accepted;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Accepted completion proposals:\n");
        if (accepted.size() > 0) {
            for (Iterator iterator = accepted.iterator(); iterator.hasNext();) {
                CompletionProposal proposal = (CompletionProposal) iterator.next();
                sb.append("\t" + proposal.toString() + "\n");
            }
        } else {
            sb.append("\t<none>\n");
        }
        return sb.toString();
    }
    
}