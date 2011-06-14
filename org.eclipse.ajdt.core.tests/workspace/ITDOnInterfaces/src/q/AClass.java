package q;

import p.ASubInterface;
import p.AnInterface;

public class AClass {
	public static void main(String[] args) {
		AnInterface i = new AnInterface() {
			public boolean isRunning() {
				this.isRunning();
				return true;
			}
			public boolean isRunning2() {
				return true;
			}
		};
		ASubInterface si = new ASubInterface() {
			public boolean isRunning(int x) {
				this.isRunning(4);
				return true;
			}
		};
		si = new ASubInterface() {
			public boolean isRunning(int x) {
				this.isRunning(4);
				return true;
			}
			public boolean isRunning() {
				this.isRunning();
				return true;
			}
			public boolean isRunning2() {
				return true;
			}
		};
		i.isRunning();
		si.isRunning(); 
		new AConcrete2().isRunning(7);
		new AConcrete2().isRunning();
	}
	static class AConcrete2 implements AnInterface {
		public boolean isRunning(int x) {
			return true;
		}

		public boolean isRunning2() {
			return true;
		}
	}
}

class AConcrete implements AnInterface {
	public boolean isRunning(int x) {
		return true;
	}

	public boolean isRunning2() {
		return true;
	}
}