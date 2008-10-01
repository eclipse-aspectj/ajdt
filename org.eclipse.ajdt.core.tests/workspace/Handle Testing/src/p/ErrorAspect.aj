package p;

public aspect ErrorAspect {
	class x {  }
	class x {  }
	int x() { }
	int x() { }
	int x;
	int x;
	
	int y() {
		int x;
		int x;
		
		class x {};
		class x {};
	}
}
