package mvc.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.net.ServerSocketFactory;

import mvc.controller.Controller;
import mvc.model.Worker.*;

public class Model {
	public static DAO dao = new DAO();
	public static Thread socketDemonThread = null;
	public static int socketPort;
	public static RMIServer rmiServer = null;

	public static void setup() {
		runSocketServer();
		rmiServer = new RMIServer();
		rmiServer.start();
	}

	public static Thread runSocketServer() {
		if (socketDemonThread != null)
			return socketDemonThread;
		socketDemonThread = new Thread(() -> {
			ServerSocket serverSocket = null;
			Socket socket = null;
			List<Thread> threads = new ArrayList<Thread>();
			try {
				serverSocket = new ServerSocket(socketPort);
				while (true) {
					Thread t = null;
					try {
						t = new SendViaSocket(serverSocket.accept());
					} catch (IOException e) {
						System.out.println("Demon server error during client handling " + e);
					}
					t.start();
					threads.add(t);
				}

			} catch (Exception e) {
				System.out.println("Demon server error " + e);
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (IOException e) {
				}
				for (Thread t : threads) {
					t.interrupt();
				}
			}
		});
		socketDemonThread.start();
		return socketDemonThread;
	}

	@Override
	public void finalize() {
		socketDemonThread.interrupt();
	}

	public static TreeMap<String, Worker> persons = new TreeMap<String, Worker>();

	public static Boolean Compress(String filePath, char compresion) {
		Boolean output = false;
		ObjectOutputStream oos = null;
		ZipOutputStream zos = null;
		try {
			if (compresion == 'G') {
				oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(new File(filePath))));
			} else {
				zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));
				zos.putNextEntry(new ZipEntry("Object"));
				oos = new ObjectOutputStream(zos);
			}
			oos.writeObject(Model.persons);
			oos.flush();
			oos.close();
			output = true;
		} catch (Exception ex) {
		} finally {
			try {
				if (oos != null)
					oos.close();
				return output;
			} catch (IOException e) {
				return output;
			}
		}
	}

	public static Boolean Decompress(String filePath) {
		Boolean output = false;
		ObjectInputStream ois = null;
		ZipInputStream zis = null;
		try {
			String[] temp = filePath.split("\\.");
			char compresion = temp[temp.length - 1].charAt(0);
			if (compresion == 'g' || compresion == 'G') {
				ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(new File(filePath))));
			} else if (compresion == 'z' || compresion == 'Z') {
				zis = new ZipInputStream(new FileInputStream(filePath));
				zis.getNextEntry();
				ois = new ObjectInputStream(zis);
			} else
				throw new ArrayIndexOutOfBoundsException();
			Model.persons = (TreeMap<String, Worker>) ois.readObject();
			output = true;
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Brak rozszerzenia");
		} catch (Exception ex) {
		} finally {
			try {
				if (ois != null)
					ois.close();
				if (zis != null)
					zis.close();
				return output;
			} catch (IOException e) {
				return output;
			}
		}
	}

	public static TreeMap<String, Worker> mergeWorkers(TreeMap<String, Worker> workers) {
		TreeMap<String, Worker> duplicates = new TreeMap<String, Worker>();
		for (Map.Entry<String, Worker> entry : workers.entrySet()) {
			Worker newWorker = entry.getValue();
			String newWorkerPesel = newWorker.getPesel();
			Worker duplicate = persons.get(newWorkerPesel);
			if (duplicate != null) {
				if (!newWorker.equals(duplicate))
					duplicates.put(newWorkerPesel, newWorker);
			} else {
				persons.put(newWorkerPesel, newWorker);
			}
		}
		return duplicates;
	}

	public static int mergeAndResolveDuplicates(TreeMap<String, Worker> newWorkers) {
		int before = Model.persons.size();
		int zmienieni = 0;
		TreeMap<String, Worker> duplicates = Model.mergeWorkers(newWorkers);
		for (Map.Entry<String, Worker> entry : duplicates.entrySet()) {
			Worker newWorker = entry.getValue();
			String newWorkerPesel = newWorker.getPesel();
			System.out.println("\nZnaleziono duplikaty. Czy nadpisaæ pracownika:\n");
			System.out.println(Model.persons.get(newWorkerPesel));
			System.out.println("\n" + "pracownikiem:" + "\n");
			System.out.println(newWorker);
			System.out.println("[T]ak, [N]ie");
			String[] options2 = { "T", "N" };
			if (Controller.getChoice(options2).compareToIgnoreCase("T") == 0) {
				Model.persons.remove(newWorkerPesel);
				Model.persons.put(newWorkerPesel, newWorker);
				zmienieni++;
			}
		}
		return (Model.persons.size() - before + zmienieni);
	}

	public static TreeMap<String, Worker> ReceiveFromSocket(String address, int port, String login, String password) {
		TreeMap<String, Worker> data = null;
		String key = "";
		try {
			System.out.print("\nAutentykacja...");
			Registry registry = LocateRegistry.getRegistry(null);
			Authenticate stub = (Authenticate) registry.lookup("Authenticate");
			key = stub.authenticate(login, password);
			if (key.compareTo("") == 0) {
				System.out.print(" Nieprawid³owe dane\n");
				return new TreeMap<String, Worker>();
			}
			System.out.print(" Sukces!\n");
		} catch (RemoteException | NotBoundException e1) {
			System.out.println("\nNie uda³o siê po³¹czyæ z serwisem autoryzuj¹cym");
			return new TreeMap<String, Worker>();
		}
		Socket socket = null;
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		try {
			System.out.print("\nUstanawianie po³¹czenia...");
			socket = new Socket(address, port);
			System.out.print(" Sukces!");
			socket.setSoTimeout(10000);
			System.out.print("\nPobieranie...");
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(key);
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			boolean isAuthenticated = (boolean) ois.readObject();
			if (isAuthenticated) {
				data = (TreeMap<String, Worker>) ois.readObject();
				System.out.print(" Sukces!\n");
			} else {
				System.out.print(" Token wygasl!\n");
				throw new IOException();
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("\nNie uda³o siê pobraæ danych z serwera tcp");
		} finally {
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
				}
			if (ois != null)
				try {
					ois.close();
				} catch (IOException e) {
				}
			if (oos != null)
				try {
					oos.close();
				} catch (IOException e) {
				}
		}
		return data != null ? data : new TreeMap<String, Worker>();
	}
}
