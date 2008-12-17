package org.eclipse.ajdt.ui.tests.reconciling;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

class MockProblemRequestor implements IProblemRequestor {

    List problems = new LinkedList();
    
    public void acceptProblem(IProblem problem) {
        problems.add(problem);
    }

    public String problemString() {
        StringBuffer sb = new StringBuffer();
        sb.append("["); //$NON-NLS-1$
        for (Iterator probIter = problems.iterator(); probIter.hasNext();) {
            DefaultProblem prob = (DefaultProblem) probIter.next();
            sb.append("\n\t" + prob.toString()); //$NON-NLS-1$
        }
        sb.append("\n]"); //$NON-NLS-1$
        return sb.toString();
    }

    public void beginReporting() {
    }

    public void endReporting() {
    }

    public boolean isActive() {
        return true;
    }
    
    /**
     * we can't get around having an unused import
     * we should be able to remove this soon
     */
    static HashMap filterProblems(HashMap problems) {
        for (Iterator iterator = problems.values().iterator(); iterator.hasNext();) {
            if (((CategorizedProblem[]) iterator.next())[0].toString()
                    .equals("Pb(388) The import java.util.List is never used")) { //$NON-NLS-1$
                iterator.remove();
            }
        }
        return problems;
    }
    
    static List filterProblems(List problems) {
        for (Iterator iterator = problems.iterator(); iterator.hasNext();) {
            if (((CategorizedProblem) iterator.next()).toString()
                    .equals("Pb(388) The import java.util.List is never used")) { //$NON-NLS-1$
                iterator.remove();
            }
        }
        return problems;
    }
}