
 
public aspect AdvisesLinkedDefault {
	int InDefault.x = 9;
	int InDefault.Inner.x = 9;
	int InDefault.Inner3.x = 9;
	int InDefault.Inner.Inner2.x = 9;
	int InDefault.Inner3.Inner.x = 9;
	
	int InDefaultBinary.x = 9;
	int InDefaultBinary.Inner.x = 9;
	int InDefaultBinary.Inner3.x = 9;
	int InDefaultBinary.Inner.Inner2.x = 9;
	int InDefaultBinary.Inner3.Inner.x = 9;
	
	public static void main(String[] args) {
	}
	
}