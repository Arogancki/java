package mvc.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.TreeMap;

import mvc.model.Worker.Worker;

public class SendViaSocket extends Thread {
    protected Socket socket;
    public SendViaSocket(Socket clientSocket) {
        this.socket = clientSocket;
    }
    public void run() {
    	ObjectOutputStream oos = null;
    	ObjectInputStream ois = null;
    	try {
    		ois = new ObjectInputStream(socket.getInputStream());
			String token = (String) ois.readObject();
			boolean authencited = false;
			try {
				Registry registry = LocateRegistry.getRegistry(null);
				Authenticate stub = (Authenticate) registry.lookup("Authenticate");
				authencited = stub.authenticateToken(token);
			} catch (RemoteException | NotBoundException e1) {
				System.out.println("\nNie uda³o siê po³¹czyæ z serwisem autoryzuj¹cym");
			}
			oos= new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(authencited);
			oos.flush();
			if (authencited) {
				oos.writeObject(Model.persons);
				oos.flush();
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
    	finally {
    		if (oos!=null)
				try {
					oos.close();
				} catch (IOException e) {
				}
    		if (ois!=null)
				try {
					ois.close();
				} catch (IOException e) {
				}
    		try {
				socket.close();
			} catch (IOException e) {
			}
    	}
    }
}