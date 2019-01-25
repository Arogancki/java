//ZTPJ I2 14 LAB07
//Artur Ziemba
//za32917@zut.edu.pl

package mvc.model.webService;

import javax.xml.ws.Endpoint;

public class Publisher {
	private Endpoint endpoint;
	public Publisher(int port) {
		endpoint = Endpoint.create(new webServiceC());
		endpoint.setExecutor(new MyThreadPool());
		String url = "http://localhost:" + port + "/webServiceC";
		try {
			endpoint.publish(url);
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
		}
		System.out.println("webService on port " + port + " ");
	}
}