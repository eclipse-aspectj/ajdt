#!/bin/bash
# Install given feature into Eclipse
install_feature () {
        ECLIPSELOCATION=`ls $ECLIPSE_DIR/plugins/org.eclipse.equinox.launcher_*`
        $JAVA_HOME/bin/java -jar $ECLIPSELOCATION -nosplash -application org.eclipse.equinox.p2.director \
                -metadataRepository $2 \
                -artifactRepository $2 \
                -installIU $1.feature.group
}


ECLIPSE_TAR=$1
ECLIPSE_INSTALL_DIR=$2
ECLIPSE_DIR=$ECLIPSE_INSTALL_DIR/eclipse

mkdir $ECLIPSE_INSTALL_DIR

tar -xf $ECLIPSE_TAR -C $ECLIPSE_INSTALL_DIR

install_feature org.eclipse.mylyn_feature http://download.eclipse.org/releases/galileo
install_feature org.eclipse.mylyn.context_feature http://download.eclipse.org/releases/galileo
install_feature org.eclipse.mylyn.ide_feature http://download.eclipse.org/releases/galileo
install_feature org.eclipse.mylyn.java_feature http://download.eclipse.org/releases/galileo

touch $ECLIPSE_DIR/.provisioned_target

echo "Target Eclipse successfully provisioned into $ECLIPSE_DIR"