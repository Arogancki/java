//ZTPJ I2 14 LAB07
//Artur Ziemba
//za32917@zut.edu.pl

package mvc.model.Worker;
import java.io.Serializable;
import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlSeeAlso({Dyrektor.class, Handlowiec.class})
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Worker implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String pesel;
	private String name;
	private String lastName;
	private BigDecimal income;
	private int phone;
	protected BigDecimal limit;
	Worker() {
		pesel="0";
		name="";
		lastName="";
		income=new BigDecimal(0);
		phone=0;
		limit=new BigDecimal(0);
	}
	
	Worker(String _pesel,String Name, String LastName, BigDecimal Income, BigDecimal Limit,int Phone){
		pesel=_pesel;
		name=Name;
		lastName=LastName;
		income=Income;
		phone=Phone;
		limit=Limit;
	}
	public String toString()
	{
		String output="";
		output+="Identyfikator Pesel\t\t:\t"+pesel+"\n";
		output+="Imie\t\t\t\t:\t"+name+"\n";
		output+="Nazwisko\t\t\t:\t"+lastName+"\n";
		output+="Stanowisko\t\t\t:\t"+this.getClass().getSimpleName()+"\n";
		output+="Wynagrodzenie (zl)\t\t:\t"+income+"\n";
		output+="Telefon sluzbowy numer\t\t:\t";
			if (phone!=-1)
				output+=phone+"\n";
			else
				output+="-brak-\n";
		return output;
	}
	public static Worker create(String _pesel,String Name, String LastName, 
			BigDecimal Income, BigDecimal Limit,int Phone, 
			Integer Provision, String Card, BigDecimal Addictonal) {
		if (Provision==null && Card!=null && Addictonal!=null) {
			return new Dyrektor(_pesel, Name, LastName, Income, Limit, Phone, Card, Addictonal);
		}
		if (Provision!=null && Card==null && Addictonal==null) {
			return new Handlowiec(_pesel, Name, LastName, Income, Limit, Phone, Provision);
		}
		throw new Error("Coudn't resolve any worker");
	}
	@Override
    public boolean equals(Object object)
    {
		if (object.getClass()==this.getClass() && 
			object.toString().compareTo(this.toString())==0)
			return true;
		return false;
    }
	public String getPesel(){return pesel;}
	public String getName(){return name;}
	public String getLastName(){return lastName;}
	public BigDecimal getIncome(){return income;}
	public int getPhone(){return phone;}
	public BigDecimal getLimit(){return limit;}
}
