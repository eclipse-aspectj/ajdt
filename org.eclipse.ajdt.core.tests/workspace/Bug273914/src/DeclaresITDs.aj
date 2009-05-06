
public aspect DeclaresITDs {

	static class A { }
	static interface B { }
	
	static class W extends A { }
	static interface X { }
	
	
	static class Y extends A implements B { }
	static interface Z extends B { }
	
	static class C { }
	static interface D { }
	
	
	declare parents : Y implements X, W;  // should be both extends and implements, but AspectJ allows this
	declare parents : Z implements X;  // should be extends, but AspectJ allows this
	declare parents : C implements X, W, B;  // should be both extends and implements, but AspectJ allows this
	declare parents : D implements X;  // should be extends, but AspectJ allows this
}
 