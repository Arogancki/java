package mvc.model;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SendViaSocket extends Thread {
    protected Socket socket;
    public SendViaSocket(Socket clientSocket) {
        this.socket = clientSocket;
    }
    public void run() {
    	ObjectOutputStream oos =null;
    	try {
			oos= new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(Model.persons);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	finally {
    		if (oos!=null)
				try {
					oos.close();
				} catch (IOException e) {
				}
    		try {
				socket.close();
			} catch (IOException e) {
			}
    	}
    }
}