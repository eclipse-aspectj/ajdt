/**
 * 
 */
package none;


/**
 * @author Dawid Pytel
 *
 */
public aspect AspectWithThisJava {
	public interface Server {
		void setClient(Client client);
	}
	
	public class Client {
		
	}
	
	public void Client.use(Server server) {
		server.setClient(this);
	}
}