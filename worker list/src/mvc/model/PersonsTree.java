//ZTPJ I2 14 LAB07
//Artur Ziemba
//za32917@zut.edu.pl

package mvc.model;

import java.util.TreeMap;
import java.util.Map.Entry;

import mvc.model.Worker.Worker;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
 
@XmlRootElement (name="workers")
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonsTree{
	private TreeMap<String, Worker> personsTree=null;
	public PersonsTree(){
		personsTree = new TreeMap<String, Worker>();
	}
	public void setPersons(TreeMap<String, Worker> tm){
		for(Entry<String, Worker> node : tm.entrySet()) {
			personsTree.put(node.getKey(), node.getValue());
		}
	}
	public TreeMap<String, Worker> getPersonsTree(){
		return personsTree;
	}
}
