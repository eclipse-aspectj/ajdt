package ajdt.renamepackagebug2;

import ajdt.renamepackagebug1.A;

public aspect B {
    void doNothing() {
        A.class.getName();
    }
}
