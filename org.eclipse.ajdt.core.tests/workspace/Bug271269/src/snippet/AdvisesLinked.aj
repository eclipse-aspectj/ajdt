package snippet;
 
import g.Source;
import g.G;
public aspect AdvisesLinked {
	int g.G.x = 9;
	int g.G.Inner.x = 9;
	int g.G.Inner3.x = 9;
	int g.G.Inner.Inner2.x = 9;
	int g.G.Inner3.Inner.x = 9;
	  
	int g.Source.x = 9;
	int g.Source.Inner.x = 9;
	int g.Source.Inner3.x = 9;
	int g.Source.Inner.Inner2.x = 9; 
	int g.Source.Inner3.Inner.x = 9;
	
	public static void main(String[] args) {
		System.out.println(new Source().x);  
		System.out.println(new G.Inner().x);
		System.out.println(new G.Inner.Inner2().x);
		System.out.println(new G.Inner3().x);
		System.out.println(new G.Inner3.Inner().x);
	}
	
}