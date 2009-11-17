package aj.bug.innerclass;


public aspect AspectB {
	public AspectA.ClassA classA;
	public AspectA.ClassA classAMethod() { return null; };
}  