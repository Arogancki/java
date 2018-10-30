package mvc.model.Worker;
import java.math.BigDecimal;

public class Dyrektor extends Worker {
	private static final long serialVersionUID = 1L;
	private BigDecimal addictonal;
	private String card;
	public Dyrektor(String _pesel,String Name, String LastName, BigDecimal Income, BigDecimal Limit,int Phone, String Card, BigDecimal Addictonal)
	{
		super(_pesel, Name, LastName, Income, Limit, Phone);
		card=Card;
		addictonal=Addictonal;
	}
	public String toString()
	{
		String output=super.toString();
		output+="Dodatek sluzbowy (zl)\t\t:\t"+addictonal+"\n";
		output+="Karta sluzbowa numer\t\t:\t"+card+"\n";
		output+="Limit kosztow/miesiac (zl)\t:\t"+limit+"\n";
		return output;
	}
	public BigDecimal getAddictonal(){return addictonal;}
	public String getCard(){return card;}
}
