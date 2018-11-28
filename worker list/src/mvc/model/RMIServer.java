package mvc.model;

import java.rmi.registry.Registry;
import java.nio.channels.AlreadyBoundException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject; 

public class RMIServer extends Bouncer { 
   public RMIServer() {}
   private Bouncer bouncer = null;
   private Registry registry = null;
   public Bouncer getBouncer() {return bouncer;}
   public void start() {
	Authenticate stub=null;
    try {  
    	bouncer = new Bouncer(); 
    	stub = (Authenticate) UnicastRemoteObject.exportObject(bouncer, 0); 
    	LocateRegistry.createRegistry(1099);
    	registry = LocateRegistry.getRegistry(); 
    	registry.bind("Authenticate", stub);
    } catch (AlreadyBoundException e) {
    	if (stub!=null) {
    		try {
    			UnicastRemoteObject.unexportObject(this, true);
				registry.bind("Authenticate", stub);
			} catch (Exception e1) {
				//e1.printStackTrace();
				System.out.println("Nie uda³o siê wystartowaæ serwisu RMI!");
			}
    	}
    } catch (Exception e) {
    	System.out.println("Nie uda³o siê wystartowaæ serwisu RMI!");
    }
   }
   public void finalize() {
	   System.out.println("idzie");
		if (registry!=null) {
			try {
				registry.unbind("Authenticate");
			} catch (RemoteException | NotBoundException e) {}
		}
   }
} 