package mvc.model.Worker;
import java.math.BigDecimal;

public class Handlowiec extends Worker {
	private static final long serialVersionUID = 1L;
	private int provision;
	public Handlowiec(String _pesel,String Name, String LastName, BigDecimal Income, BigDecimal Limit,int Phone, int Provision)
	{
		super(_pesel,Name,LastName,Income, Limit,Phone);
		provision=Provision;
	}
	public String toString()
	{
		String output=super.toString();
		output+="Prowizja (%)\t\t\t:\t"+provision+"\n";
		output+="Limit prowizji/miesi¹c (z³)\t:\t"+limit+"\n";
		return output;
	}
	public int getProvision(){return provision;}
}
