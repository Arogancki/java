//ZTPJ I2 14 LAB07
//Artur Ziemba
//za32917@zut.edu.pl

package mvc.model.webService;

import javax.xml.ws.WebServiceRef;

import mvc.model.webService.client.WebServiceC;
import mvc.model.webService.client.WebServiceCService;

public class webClient {
	public static String request(String token) {
		try {
			String g = new WebServiceCService().getWebServiceCPort().getPersons(token);
			return g;
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return "";
	}
}
