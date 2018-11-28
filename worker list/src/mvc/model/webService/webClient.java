package mvc.model.webService;

import javax.xml.ws.WebServiceRef;

public class webClient {
	@WebServiceRef(wsdlLocation="http://localhost:8080/webServiceC/getPersons?wsdl")
	static webServiceC service;
	public static String request(String token) {
		service
		service.getPersons(token);
        return service.getPersons(token);
	}
}
