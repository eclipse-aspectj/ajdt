package snippet;
 
import g.Source;
import g.SourceJar;
import g.G;
public aspect AdvisesLinked {
	public int G.x = 9;
	public int G.Inner.x = 9;
	public int G.Inner3.x = 9;
	public int G.Inner.Inner2.x = 9;
	public int G.Inner3.Inner.x = 9;
	  
	public int Source.x = 9;
	public int Source.Inner.x = 9;
	public int Source.Inner3.x = 9;
	public int Source.Inner.Inner2.x = 9; 
	public int Source.Inner3.Inner.x = 9;
	
	public int SourceJar.x = 9;
	public int SourceJar.Inner.x = 9;
	public int SourceJar.Inner3.x = 9;
	public int SourceJar.Inner.Inner2.x = 9; 
	public int SourceJar.Inner3.Inner.x = 9;
	
	public static void main(String[] args) {
		System.out.println(new Source().x);  
		System.out.println(new G.Inner().x);
		System.out.println(new G.Inner.Inner2().x);
		System.out.println(new G.Inner3().x);
		System.out.println(new G.Inner3.Inner().x);
	} 
	 
} 