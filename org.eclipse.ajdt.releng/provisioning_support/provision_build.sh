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

install_feature org.aspectj http://download.eclipse.org/tools/ajdt/aspectj/update
#install_feature org.eclipse.ajdt.pde.build http://download.eclipse.org/tools/ajdt/35/dev/update

touch $ECLIPSE_DIR/.provisioned

echo "Build Eclipse successfully provisioned into $ECLIPSE_DIR"