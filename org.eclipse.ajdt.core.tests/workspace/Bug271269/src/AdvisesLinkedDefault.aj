import g.G;
import g.Source;


 
public aspect AdvisesLinkedDefault {
	int InDefault.x = 9;
	int InDefault.Inner.x = 9;
	int InDefault.Inner3.x = 9;
	int InDefault.Inner.Inner2.x = 9;
	int InDefault.Inner3.Inner.x = 9;

	int InDefaultJar.x = 9;
	int InDefaultJar.Inner.x = 9;
	int InDefaultJar.Inner3.x = 9;
	int InDefaultJar.Inner.Inner2.x = 9;
	int InDefaultJar.Inner3.Inner.x = 9;

	int InDefaultBinary.x = 9;
	int InDefaultBinary.Inner.x = 9;
	int InDefaultBinary.Inner3.x = 9;
	int InDefaultBinary.Inner.Inner2.x = 9;
	int InDefaultBinary.Inner3.Inner.x = 9; 
	
	public static void main(String[] args) {
		System.out.println(new Source().x);  
		System.out.println(new G.Inner().x);
		System.out.println(new G.Inner.Inner2().x);
		System.out.println(new G.Inner3().x);
		System.out.println(new G.Inner3.Inner().x);
	}
	
}