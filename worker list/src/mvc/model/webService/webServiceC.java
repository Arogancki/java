//ZTPJ I2 14 LAB07
//Artur Ziemba
//za32917@zut.edu.pl

package mvc.model.webService;

import java.io.ObjectInputStream;
import java.io.StringWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import mvc.model.Authenticate;
import mvc.model.Model;
import mvc.model.PersonsTree;

@WebService
public class webServiceC {
	@WebMethod
	public String getPersons(String token) {
		try {
			try {
				Registry registry = LocateRegistry.getRegistry(null);
				Authenticate stub = (Authenticate) registry.lookup("Authenticate");
				boolean authencited = true || stub.authenticateToken(token);
				if (!authencited) {
					return "1";
				}
				PersonsTree temp = new PersonsTree();
				temp.setPersons(Model.persons);
				JAXBContext jaxbContext;
				jaxbContext = JAXBContext.newInstance(PersonsTree.class);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
	    		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	    		StringWriter sw = new StringWriter();
	    		jaxbMarshaller.marshal(temp, sw);
	    		return sw.toString();
		} catch (RemoteException | NotBoundException e1) {
			System.out.println("\nNie uda³o siê po³¹czyæ z serwisem autoryzuj¹cym");
		}
		} catch (JAXBException e) {
		}
		return "2";
	}
}
