public aspect AspectWithDotClass {
	int x1;
	private boolean method() {
		Object o = new Object();
		if (!o.equals(void.class)) {
			this.x1 ++ ;
			return true;
		}
		return false;
	}
}
   