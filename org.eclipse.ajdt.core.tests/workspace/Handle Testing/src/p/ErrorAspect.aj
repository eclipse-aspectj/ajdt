package p;

public aspect ErrorAspect {
    // errors are not being build make this correct
	class x {  }
//	class x {  }
	int x() { }
//	int x() { }
	int x;
//	int x;
	
	int y() {
		int x;
//		int x;
		
		class x {};
//		class x {};
	}
}
