package aj.bug.innerclass;


public aspect AspectB {
    public AspectB() { }
	public AspectA.ClassA classA;
	public AspectA.ClassA classAMethod() { return null; };
}  