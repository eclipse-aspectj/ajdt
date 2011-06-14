package p;

import java.rmi.RemoteException;


public class TestSoftening {
     public static void main(String[] args) {
         TestSoftening test = new TestSoftening();
         test.perform();
     }
     public void perform() throws RemoteException {
         throw new RemoteException();
     }
}

aspect SofteningTestAspect {
     declare soft : RemoteException : call(void TestSoftening.perform());
}
