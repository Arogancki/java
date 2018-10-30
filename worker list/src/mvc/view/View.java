package mvc.view;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.math.BigDecimal;
import java.sql.SQLException;

import mvc.model.*;
import mvc.model.Worker.*;
import mvc.controller.*;
import mvc.external.*;

public class View {
	public static void printMenu()
	{
		System.out.println("MENU");
		System.out.println("\t1.Lista pracownikow");
		System.out.println("\t2.Dodaj pracownika");
		System.out.println("\t3.Usun pracownika");
		System.out.println("\t4.Kopia zapasowa");
		System.out.print("\nWybor>");
	}
	public static void printWorkers()
	{
		System.out.println("1.Lista pracownikow:");
		int licznik=1;
		for(  Entry<String, Worker> node : Model.persons.entrySet()) 
		{
			Worker person= node.getValue();
			System.out.print(person);
			System.out.println("\n[Pozycja:"+ licznik++ +"/"+Model.persons.size()+"]");
			System.out.println("[Enter]-nastepny");
			System.out.println("[q]-powrot");
			String[] options={"q",""};
			if (Controller.getChoice(options).compareToIgnoreCase("q")==0)
				break;
		}
	}
	public static void printStrip()
	{
		System.out.println("-----------------------------");
	}
	public static void deleteWorker()
	{
		System.out.println("3.Usun pracownika:\n");
		System.out.print("Podaj pesel\t: ");
		try
		{
			String pesel=Controller.getChoice();
			printStrip();
			if (!Model.persons.containsKey(pesel))
				throw new NumberFormatException();
			System.out.println(Model.persons.get(pesel));
			printStrip();
			System.out.println("[Enter]-potwierdz");
			System.out.println("[q]-powrot");
			String[] options={"","q"};
				if (Controller.getChoice(options).compareToIgnoreCase("")==0)
					Model.persons.remove(pesel);
		}
		catch(Exception e)
		{
			printStrip();
			System.out.println("Nie ma takiego pracownika");
		}
	}
	public static void addWorker()
	{
		try
		{
			System.out.println("2.Dodaj pracownika:\n");
			System.out.print("[D]yrektor/[H]andlowiec\t:\t");
			String[] options={"D","H"};
			String type=Controller.getChoice(options);
			printStrip();
			System.out.print("Identyfikator pesel\t\t:\t");
			String pesel = Controller.getChoice();

			if (!PeselValidation.byString(pesel) || Model.persons.containsKey(pesel))
				throw new NumberFormatException();
			System.out.print("Imie\t\t\t\t:\t"); 
			String name=Controller.getChoice();
			System.out.print("Nazwisko\t\t\t:\t");
			String lastName=Controller.getChoice();
			System.out.print("Wynagrodzenie (zl)\t\t:\t");
			BigDecimal income=new BigDecimal(Controller.getChoice());
			System.out.print("Telefon\t\t\t\t:\t");
			String sphone=Controller.getChoice();
			int phone;
			if (sphone.compareToIgnoreCase("")==0)
				phone=-1;
			else
				phone=Integer.parseInt(sphone); 
			if (type.compareToIgnoreCase("D")==0)
			{
				System.out.print("Dodatek sluzbowy (zl)\t\t:\t");
				BigDecimal addictonal=new BigDecimal(Controller.getChoice());
				System.out.print("Karta sluzbowa\t\t\t:\t");
				String card=Controller.getChoice();
				System.out.print("Limit kosztow (zl)\t\t:\t");
				BigDecimal limit= new BigDecimal(Controller.getChoice());
				printStrip();
				System.out.println("[Enter]-zapisz");
				System.out.println("[q]-odrzuc");
				String[] options2={"","q"};
				if (Controller.getChoice(options2).compareToIgnoreCase("")==0)
				{
					Model.persons.put(pesel, new Dyrektor(pesel,name, lastName, income, 
							limit, phone, card, addictonal)
					);
				}
			}
			else
			{
				System.out.print("Prowizja (%)\t\t\t:\t");
				int provision=Integer.parseInt(Controller.getChoice());
				System.out.print("Limit prowizji/miesi¹c (z³)\t:\t");
				BigDecimal limit= new BigDecimal(Controller.getChoice());
				printStrip();
				System.out.println("[Enter]-zapisz");
				System.out.println("[q]-odrzuc");
				String[] options2={"","q"};
				if (Controller.getChoice(options2).compareToIgnoreCase("")==0)
				{
					Model.persons.put(pesel, new Handlowiec(pesel,name, lastName, income, limit, phone, provision));
				}
			}
		}
		catch(NumberFormatException e)
		{
			printStrip();
			System.out.println("Bledne dane");
		}
	}
	public static void Serialize()
	{
		System.out.println("4.Kopia zapasowa:\n");
		System.out.print("[Z]achowaj/[O]dtworz\t:\t");
		String[] options={"O","Z"};
		String type=Controller.getChoice(options);
		printStrip();
		if (type.compareToIgnoreCase("Z")==0)
		{
			System.out.print("[B]aza danych/[P]lik\t:\t");
			String[] options15={"B","P"};
			String where=Controller.getChoice(options15);
			if (where.compareToIgnoreCase("B")==0){
				try {
					Model.dao.saveWorkers(Model.persons);
					System.out.println("Zapisano pracowników w bazie");
				} catch (SQLException e) {
					System.out.println("\nBlad bazy danych - nie udalo sie zapisac");
					System.out.println("Spróbuj najpierw pobraæ dane z bazy");
				}
			}
			else {
				System.out.print("Kompresja [G]zip/[Z]zip\t:\t");
				String[] options2={"G","Z"};
				String compresion=Controller.getChoice(options2);
				System.out.print("Nazwa pliku\t:\t");
				String filePath = Controller.getChoice();
				printStrip();
				System.out.println("[Enter]-zapisz");
				System.out.println("[q]-odrzuc");
				String[] options3={"","q"};
				if (Controller.getChoice(options3).compareToIgnoreCase("")==0)
				{
					if (!Model.Compress(filePath, compresion.charAt(0)))
						System.out.println("Blad pliku");
				}	
			}
		}
		else
		{
			System.out.print("[B]aza danych/[P]lik\t:\t");
			String[] options15={"B","P"};
			String where=Controller.getChoice(options15);
			if (where.compareToIgnoreCase("B")==0){
				try {
					int before = Model.persons.size();
					int zmienieni = 0;
					TreeMap<String, Worker> duplicates = Model.mergeWorkers(Model.dao.getWorkers());
					for(Map.Entry<String, Worker> entry : duplicates.entrySet()) {
						Worker newWorker = entry.getValue();
						String newWorkerPesel = newWorker.getPesel();
						System.out.println("\nZnaleziono duplikaty. Czy nadpisaæ pracownika:\n");
						System.out.println(Model.persons.get(newWorkerPesel));
						System.out.println("\n"+"pracownikiem:" +"\n");
						System.out.println(newWorker);
						System.out.println("[T]ak, [N]ie");
						String[] options2={"T","N"};
						if (Controller.getChoice(options2).compareToIgnoreCase("T")==0){
							Model.persons.remove(newWorkerPesel);
							Model.persons.put(newWorkerPesel, newWorker);
							zmienieni++;
						}
					}
					System.out.println("\nPobrano " + (Model.persons.size()-before+zmienieni) + " pracowników z bazy danych");
				} catch (SQLException e) {
					System.out.println("\nBlad bazy danych - nie udalo sie pobrac");
				}
			}
			else {
				System.out.print("Nazwa pliku\t:\t");
				String filePath = Controller.getChoice();
				printStrip();
				System.out.println("[Enter]-odzyskaj");
				System.out.println("[q]-odrzuc");
				String[] options3={"","q"};
				if (Controller.getChoice(options3).compareToIgnoreCase("")==0)
				{
					if (!Model.Decompress(filePath))
						System.out.println("Blad pliku");
				}
			}
		}
	}
}
