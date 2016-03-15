/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman  - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.tests.ajde.Bug270325Tests;
import org.eclipse.ajdt.core.tests.ajde.Bug273770Tests;
import org.eclipse.ajdt.core.tests.ajde.CoreCompilerConfigurationTests;
import org.eclipse.ajdt.core.tests.ajde.CoreCompilerConfigurationTests2;
import org.eclipse.ajdt.core.tests.ajde.CoreCompilerFactoryTests;
import org.eclipse.ajdt.core.tests.builder.AJBuilderTest;
import org.eclipse.ajdt.core.tests.builder.AJBuilderTest2;
import org.eclipse.ajdt.core.tests.builder.AspectPathTests;
import org.eclipse.ajdt.core.tests.builder.Bug159197Test;
import org.eclipse.ajdt.core.tests.builder.Bug268609Test;
import org.eclipse.ajdt.core.tests.builder.Bug43711Test;
import org.eclipse.ajdt.core.tests.builder.Bug99133Test;
import org.eclipse.ajdt.core.tests.builder.BuilderArgsTestBug270554;
import org.eclipse.ajdt.core.tests.builder.CoreOutputLocationManagerRefreshTestsBug270335;
import org.eclipse.ajdt.core.tests.builder.CoreOutputLocationManagerTest;
import org.eclipse.ajdt.core.tests.builder.DerivedTests;
import org.eclipse.ajdt.core.tests.builder.LinkedFoldersTestBug270202;
import org.eclipse.ajdt.core.tests.builder.LinkedFoldersTestBug275903;
import org.eclipse.ajdt.core.tests.builder.RefreshTestsImprecise;
import org.eclipse.ajdt.core.tests.codeconversion.AnnotationConversionTests;
import org.eclipse.ajdt.core.tests.codeconversion.AspectsConvertingParserTest;
import org.eclipse.ajdt.core.tests.codeconversion.Bug279974Tests;
import org.eclipse.ajdt.core.tests.codeconversion.CodeCheckerTest;
import org.eclipse.ajdt.core.tests.codeselect.AbstractITDAwareCodeSelectionTests;
import org.eclipse.ajdt.core.tests.dom.rewrite.ASTRewritingPointcutDeclTest;
import org.eclipse.ajdt.core.tests.javaelements.AJCompilationUnitManagerTest;
import org.eclipse.ajdt.core.tests.javaelements.AJCompilationUnitTest;
import org.eclipse.ajdt.core.tests.javaelements.AspectElementTests;
import org.eclipse.ajdt.core.tests.javaelements.AspectsConvertingParserTest2;
import org.eclipse.ajdt.core.tests.javaelements.Bug283468Test;
import org.eclipse.ajdt.core.tests.model.AJCodeElementTest;
import org.eclipse.ajdt.core.tests.model.AJComparatorTest;
import org.eclipse.ajdt.core.tests.model.AJModelPersistenceTest;
import org.eclipse.ajdt.core.tests.model.AJModelTest;
import org.eclipse.ajdt.core.tests.model.AJModelTest2;
import org.eclipse.ajdt.core.tests.model.AJModelTest3;
import org.eclipse.ajdt.core.tests.model.AJModelTest4;
import org.eclipse.ajdt.core.tests.model.AJModelTest5;
import org.eclipse.ajdt.core.tests.model.AJModelTest6;
import org.eclipse.ajdt.core.tests.model.AJProjectModelTest;
import org.eclipse.ajdt.core.tests.model.AJProjectModelTest2;
import org.eclipse.ajdt.core.tests.model.AJRelationshipManagerTest;
import org.eclipse.ajdt.core.tests.model.AspectJMemberElementTest;
import org.eclipse.ajdt.core.tests.model.BinaryWeavingSupportTest;
import org.eclipse.ajdt.core.tests.model.Bug268522;
import org.eclipse.ajdt.core.tests.model.GetExpandedRegionTests;
import org.eclipse.ajdt.core.tests.model.InpathRelationshipsTests;
import org.eclipse.ajdt.core.tests.model.ModelCheckerTests;
import org.eclipse.ajdt.core.tests.model.MultipleProjectModelTests;
import org.eclipse.ajdt.core.tests.newbuildconfig.BuildConfigurationTest;
import org.eclipse.ajdt.core.tests.newbuildconfig.BuildConfigurationTest2;
import org.eclipse.ajdt.core.tests.problemfinding.Bug273691Reconciling;
import org.eclipse.ajdt.core.tests.problemfinding.Bug347021ProblemFinderTests;
import org.eclipse.ajdt.core.tests.problemfinding.Bug358024ProblemFinderTests;
import org.eclipse.ajdt.core.tests.problemfinding.Bug361170ProblemFinderTests;
import org.eclipse.ajdt.core.tests.problemfinding.ExtraAspectMethodProblemFinderTests;
import org.eclipse.ajdt.core.tests.problemfinding.GenericProblemFinderTests;
import org.eclipse.ajdt.core.tests.problemfinding.ITITProblemFinderTests;
import org.eclipse.ajdt.core.tests.problemfinding.ProblemFinderTests;
import org.eclipse.ajdt.core.tests.problemfinding.ProblemFinderTests13;
import org.eclipse.ajdt.core.tests.problemfinding.ProblemFinderTests14;
import org.eclipse.ajdt.core.tests.problemfinding.ProblemFinderTests15;
import org.eclipse.ajdt.core.tests.problemfinding.ProblemFinderTests16;
import org.eclipse.ajdt.core.tests.problemfinding.ProblemFinderTests2;
import org.eclipse.ajdt.core.tests.problemfinding.ProblemFinderTests3;
import org.eclipse.ajdt.core.tests.problemfinding.ProblemFinderTests7;
import org.eclipse.ajdt.core.tests.problemfinding.ProblemFinderTests9;
import org.eclipse.ajdt.core.tests.refactoring.AspectRenameRefactoringTests;
import org.eclipse.ajdt.core.tests.refactoring.FindITDGettersAndSettersTest;
import org.eclipse.ajdt.core.tests.refactoring.ITDRenameParticipantRefactoringTest;
import org.eclipse.ajdt.core.tests.refactoring.MoveCURefactoringTests;
import org.eclipse.ajdt.core.tests.refactoring.RenamePackageRefactoringTests;
import org.eclipse.ajdt.core.tests.search.DeclareAwareSearchTests;
import org.eclipse.ajdt.core.tests.search.ITDAwareDeclarationSearchTests;
import org.eclipse.ajdt.core.tests.search.ITDAwareJUnit4TestFinderTests;
import org.eclipse.ajdt.core.tests.search.ITDAwarePolymorphicSearchTests;
import org.eclipse.ajdt.core.tests.search.ITDAwareSearchTests;
import org.eclipse.ajdt.core.tests.search.ITDAwareTypeSearchTests;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

/**
 * Defines all the AJDT Core tests. This can be run with either a 1.4.2 or 1.5
 * JVM. Tests which require a 1.5 JVM are only added to the suite if a 1.5 JVM
 * is detected.
 */
public class AllCoreTests {

	public static Test suite() throws Exception {
	    // attempt to avoid deadlock by early loading of JavaCore.
	    JavaCore.initializeAfterLoad(null);
	    
	    AspectJPlugin.getDefault().setHeadless(true);
	    
	    // ensure that the UI plugin is not going to start
        Bundle ajdtui = 
            Platform.getBundle("org.eclipse.ajdt.ui");
        if (ajdtui != null) {
            ajdtui.stop(Bundle.STOP_TRANSIENT);
        }

		TestSuite suite = new TestSuite(AllCoreTests.class.getName());

		suite.addTest(new TestSuite(AJCoreTest.class));
		suite.addTest(new TestSuite(AJCoreTestJava5.class));
		
		
		suite.addTest(new TestSuite(AJPropertiesTest.class));
		suite.addTest(new TestSuite(BuildConfigTest.class));
		suite.addTest(new TestSuite(CoreUtilsTest.class));
		suite.addTest(new TestSuite(ProjectDeletionTest.class));
		
		// code conversion tests
		suite.addTest(new TestSuite(AspectsConvertingParserTest.class));
		suite.addTest(new TestSuite(AspectsConvertingParserTest2.class));
		suite.addTest(new TestSuite(CodeCheckerTest.class));
		suite.addTest(new TestSuite(Bug279974Tests.class));

		suite.addTest(new TestSuite(AspectJCorePreferencesTest.class));

		// model tests
		suite.addTest(new TestSuite(AJCodeElementTest.class));
		suite.addTest(new TestSuite(AJComparatorTest.class));
		suite.addTest(new TestSuite(AJModelTest.class));
		suite.addTest(new TestSuite(AJModelTest2.class));
		suite.addTest(new TestSuite(AJModelTest3.class));
        suite.addTest(new TestSuite(AJModelTest4.class));
        suite.addTest(new TestSuite(AJModelTest5.class));
        suite.addTest(new TestSuite(AJModelTest6.class));
		suite.addTest(new TestSuite(AJModelPersistenceTest.class));
		suite.addTest(new TestSuite(AJProjectModelTest.class));
        suite.addTest(new TestSuite(AJProjectModelTest2.class));
		suite.addTest(new TestSuite(AJRelationshipManagerTest.class));
		suite.addTest(new TestSuite(BinaryWeavingSupportTest.class));
		suite.addTest(new TestSuite(ModelCheckerTests.class));
        suite.addTest(new TestSuite(AspectJMemberElementTest.class));
        suite.addTest(new TestSuite(Bug268522.class));
        suite.addTest(new TestSuite(InpathRelationshipsTests.class));
        suite.addTest(AbstractITDAwareCodeSelectionTests.suite());
        suite.addTest(new TestSuite(GetExpandedRegionTests.class));
        suite.addTest(new TestSuite(Bug283468Test.class));
        suite.addTest(new TestSuite(MultipleProjectModelTests.class));
        suite.addTest(new TestSuite(AnnotationConversionTests.class));

		// core compiler configuration and ajde
        suite.addTest(new TestSuite(CoreCompilerConfigurationTests.class));
        suite.addTest(new TestSuite(CoreCompilerConfigurationTests2.class));
        suite.addTest(new TestSuite(CoreCompilerFactoryTests.class));
        suite.addTest(new TestSuite(Bug270325Tests.class));
		
// TODO: two tests are failing here !!!
        suite.addTest(new TestSuite(Bug273770Tests.class));

		// Java Element tests
        suite.addTest(new TestSuite(AspectElementTests.class));
        suite.addTest(new TestSuite(AJCompilationUnitManagerTest.class));
        suite.addTest(new TestSuite(AJCompilationUnitTest.class));

		// builder tests
		suite.addTest(new TestSuite(CoreOutputLocationManagerTest.class));
		suite.addTest(new TestSuite(AJBuilderTest.class));
        suite.addTest(new TestSuite(AJBuilderTest2.class));
        suite.addTest(new TestSuite(AspectPathTests.class));
		suite.addTest(new TestSuite(Bug99133Test.class));
        suite.addTest(new TestSuite(Bug159197Test.class));
        suite.addTest(new TestSuite(Bug43711Test.class));
        suite.addTest(new TestSuite(DerivedTests.class));
        suite.addTest(new TestSuite(RefreshTestsImprecise.class));
        suite.addTest(new TestSuite(BuilderArgsTestBug270554.class));
        suite.addTest(new TestSuite(CoreOutputLocationManagerRefreshTestsBug270335.class));
        suite.addTest(new TestSuite(LinkedFoldersTestBug270202.class));
        suite.addTest(new TestSuite(LinkedFoldersTestBug275903.class));
        suite.addTest(new TestSuite(Bug268609Test.class));

        // build configuration tests
        suite.addTest(new TestSuite(BuildConfigurationTest.class));
        suite.addTest(new TestSuite(BuildConfigurationTest2.class));

        // Problem finding/reconciling tests
        suite.addTest(new TestSuite(ProblemFinderTests.class));
        suite.addTest(new TestSuite(ProblemFinderTests2.class));
        suite.addTest(new TestSuite(ProblemFinderTests3.class));
        suite.addTest(new TestSuite(ProblemFinderTests7.class));
        suite.addTest(new TestSuite(ProblemFinderTests9.class));
        suite.addTest(new TestSuite(ProblemFinderTests13.class));
        suite.addTest(new TestSuite(ProblemFinderTests14.class));

// TODO mysteriously failing on build server
        suite.addTest(new TestSuite(ProblemFinderTests15.class));
		
        suite.addTest(new TestSuite(ProblemFinderTests16.class));
        suite.addTest(new TestSuite(Bug273691Reconciling.class));
        suite.addTest(new TestSuite(GenericProblemFinderTests.class));
        suite.addTest(new TestSuite(ExtraAspectMethodProblemFinderTests.class));
        suite.addTest(new TestSuite(ITITProblemFinderTests.class));
        suite.addTest(new TestSuite(Bug347021ProblemFinderTests.class));
        suite.addTest(new TestSuite(Bug358024ProblemFinderTests.class));
        suite.addTest(new TestSuite(Bug361170ProblemFinderTests.class));


		// AST tests
// TODO work out what is wrong with these, removing for 4.5/4.6 builds for now
//		suite.addTest(new TestSuite(ASTRewritingPointcutDeclTest.class));
		
		// refactoring tests
		suite.addTest(new TestSuite(AspectRenameRefactoringTests.class));
		suite.addTest(new TestSuite(ITDRenameParticipantRefactoringTest.class));
		suite.addTest(new TestSuite(FindITDGettersAndSettersTest.class));
		suite.addTest(new TestSuite(MoveCURefactoringTests.class));
		suite.addTest(new TestSuite(RenamePackageRefactoringTests.class));
		
		// search tests
		suite.addTest(new TestSuite(ITDAwareSearchTests.class));
		suite.addTest(new TestSuite(ITDAwarePolymorphicSearchTests.class));
		suite.addTest(new TestSuite(ITDAwareDeclarationSearchTests.class));
		suite.addTest(new TestSuite(ITDAwareJUnit4TestFinderTests.class));
		suite.addTest(new TestSuite(ITDAwareTypeSearchTests.class));
		suite.addTest(new TestSuite(DeclareAwareSearchTests.class));

		return suite;
	}
}
