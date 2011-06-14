import p.Aspect2;


public aspect Aspect {
	int Class.x;
	
	int hhh;
	int h() {
		Aspect2.aspectOf().hhh++;
		return 9;
	}
}
 