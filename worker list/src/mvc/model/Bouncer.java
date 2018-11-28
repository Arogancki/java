package mvc.model;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.naming.Context;
import javax.naming.InitialContext;

import mvc.model.Worker.Worker;

public class Bouncer implements Authenticate {
	private static String ADDRESS_WI = "ldap://82.145.72.13:389";
	private static String WI_USERNAME_PREAMBLE = "wipsad\\";
	private static int sleepingTime = 1000*60*2;
	private TreeMap<String, String> db = new TreeMap<String, String>();
	private List<String> tokens = new ArrayList<String>();
	private List<Thread> threads = new ArrayList<Thread>();
	private Random random = ThreadLocalRandom.current();
	public Bouncer() {
		db.put("test", "test");
		db.put("login", "password");
		db.put("admin", "admin");
	}
	@Override
	public boolean authenticateToken(String token) {
		if (tokens.contains(token)) {
			tokens.remove(token);
			return true;
		}
		return false;
	}
	@Override
	public String authenticate(String login, String password, String type) throws RemoteException {
		if (type.compareToIgnoreCase("rmi")==0) {
			if (db.get(login).compareTo(password)==0)
				return generateKey();	
		}
		else if (type.compareToIgnoreCase("jndi")==0) {
			Hashtable<String, String> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, ADDRESS_WI);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, WI_USERNAME_PREAMBLE + login);
			env.put(Context.SECURITY_CREDENTIALS, password);
			try {
				new InitialContext(env);
				return generateKey();
			}
			catch(Exception e) {
				System.out.println("ERROR: "+e);
			}
		}
		else {
			System.out.println("Unsupported auth type!!");
		}
		return "";
	}
	private String generateKey() {
		byte[] randomBytes = new byte[32];
		random.nextBytes(randomBytes);
		String encoded = Base64.getUrlEncoder().encodeToString(randomBytes);
		tokens.add(encoded);
		Thread t = new Thread(() -> {
			try {
				Thread.sleep(sleepingTime);
			} catch (InterruptedException e) {}
			if (tokens.contains(encoded))
				tokens.remove(encoded);
		});
		threads.add(t);
		t.start();
		return encoded;
	}
	@Override
	public void finalize() {
		for (Thread t : threads) {
    		t.interrupt();
    	}
	}
}
