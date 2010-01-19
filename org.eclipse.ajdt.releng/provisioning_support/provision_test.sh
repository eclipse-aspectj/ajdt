#!/bin/bash
# this file extracts the test features into the target eclipse
# at this point in the build process, the target eclipse must
# already exist, so all we need to do is provision it with 
# the newly created AJDT features

# Install given feature into Eclipse
install_feature () {
        ECLIPSELOCATION=`ls $ECLIPSE_DIR/plugins/org.eclipse.equinox.launcher_*`
        $JAVA_HOME/bin/java -jar $ECLIPSELOCATION -nosplash -application org.eclipse.equinox.p2.director \
                -metadataRepository $2 \
                -artifactRepository $2 \
                -installIU $1.feature.group
}


ECLIPSE_INSTALL_DIR=$1
UPDATE_SITE_URL=$2
ECLIPSE_DIR=$ECLIPSE_INSTALL_DIR/eclipse

echo "Update site url = $UPDATE_SITE_URL"
echo "Eclipse dir = $ECLIPSE_DIR/"


install_feature org.eclipse.equinox.weaving.sdk $UPDATE_SITE_URL
install_feature org.eclipse.ajdt $UPDATE_SITE_URL
install_feature org.eclipse.aspectj.feature_tests $UPDATE_SITE_URL

touch $ECLIPSE_DIR/.provisioned_test

echo "Test Eclipse successfully provisioned into $ECLIPSE_DIR"