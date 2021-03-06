//ZTPJ I2 14 LAB07
//Artur Ziemba
//za32917@zut.edu.pl

package mvc.model.Worker;
import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Handlowiec")
@XmlAccessorType(XmlAccessType.FIELD)
public class Handlowiec extends Worker {
	private static final long serialVersionUID = 1L;
	private int provision;
	public Handlowiec() {
		super();
		provision=0;
	}
	public Handlowiec(String _pesel,String Name, String LastName, BigDecimal Income, BigDecimal Limit,int Phone, int Provision)
	{
		super(_pesel,Name,LastName,Income, Limit,Phone);
		provision=Provision;
	}
	public String toString()
	{
		String output=super.toString();
		output+="Prowizja (%)\t\t\t:\t"+provision+"\n";
		output+="Limit prowizji/miesi�c (z�)\t:\t"+limit+"\n";
		return output;
	}
	public int getProvision(){return provision;}
}
