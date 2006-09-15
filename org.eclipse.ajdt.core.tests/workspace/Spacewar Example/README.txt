Spacewar Example.ajmap needs regenerating if:

1. AjProjectModel.MODEL_VERSION is increased
2. There are underlying changes in the programelements
3. There is a change to the relationship map

Note that the version for Eclipse 3.1 and later cannot be used with 
Eclipse 3.0. This results in ArrayIndexOutOfBoundsExceptions within 
AJMementoTokenizer.nextToken(). This is because for the method:
"public static void main(String[] args)" in Game.java, in Eclipse 3.1 
and later we have the handle:

=Spacewar Example/src<spacewar{Game.java[Game~main~\[QString;?constructor-call(void spacewar.Game.<init>(java.lang.String))!44!0!0!0!I

whereas with Eclipse 3.0 we have

=Spacewar Example/src<spacewar{Game.java[Game~main~[QString;?constructor-call(void spacewar.Game.<init>(java.lang.String))!44!0!0!0!I

The difference is with the parameters and the way Eclipse handles them.
If the Eclipse 3.1 handle is used within Eclipse 3.0 it thinks its an
array. It therefore asks for the next token and there isn't one.