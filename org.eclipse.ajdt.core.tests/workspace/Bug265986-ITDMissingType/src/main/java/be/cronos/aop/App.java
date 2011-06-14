package be.cronos.aop;

@InterTypeAspectSupport
public class App extends BaseApp {
	public static void main(String[] args) {
		App app = new App();
		app.foo();
		app.getLogger(null);
	}
}