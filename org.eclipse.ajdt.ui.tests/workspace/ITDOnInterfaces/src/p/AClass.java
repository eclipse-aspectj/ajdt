package p;

public class AClass {
	public static void main(String[] args) {
		AnInterface i = new AnInterface() {
			public boolean isRunning() {
				this.isRunning();
				return running;
			}
			public boolean isRunning2() {
				return running;
			}
		};
		ASubInterface si = new ASubInterface() {
			public boolean isRunning(int x) {
				this.isRunning(4);
				return running;
			}
		};
		i.isRunning();
		si.isRunning(); 
		new AConcrete().isRunning(7);
		new AConcrete().isRunning();
	}

	private static class AConcrete implements AnInterface {
		public boolean isRunning(int x) {
			return true;
		}

		public boolean isRunning() {
			return running;
		}
		public boolean isRunning2() {
			return running;
		}
	}
	
	static class AConcrete3 implements ASubInterface3 {
		public boolean isRunning() {
			return false;
		}
	}
}