package none;

/**
 * @author Dawid Pytel
 *
 */
public aspect AspectWithThis {
	public interface Server {
		void setClient(Client client);
	}
	
	public class Client {
		
	}
	
	public void Client.use(Server server) {
		server.setClient(this);
	} 
}
