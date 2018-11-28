package mvc.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Authenticate extends Remote{
	public String authenticate(String login, String password, String type) throws RemoteException;
	public boolean authenticateToken(String token) throws RemoteException;
}
