//ZTPJ I2 14 LAB07
//Artur Ziemba
//za32917@zut.edu.pl

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
		System.out.println("\t5.Pobierz dane z sieci");
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
	public static void Network() {
		System.out.println("5.Pobierz dane z sieci:\n");
		System.out.print("Zrodlo [W]ebService/[P]eer\t:\t");
		String[] optionsZ={"W","P"};
		String source=Controller.getChoice(optionsZ);
		if (source.compareToIgnoreCase("W")==0) {
			source = "web";
		}
		else {
			source = "peer";
		};
		System.out.print("Autentykacja [S]erwer wi.zut/[R]mi\t:\t");
		String[] options={"R","S"};
		String type=Controller.getChoice(options);
		String auth="";
		if (type.compareToIgnoreCase("R")==0) {
			auth = "rmi";
		}
		else {
			auth = "jndi";
		};
		System.out.print("Podaj login\t:\t");
		String username=Controller.getChoice();
		System.out.print("Podaj haslo\t:\t");
		String password=Controller.getChoice();
		TreeMap<String,Worker> out=null;
		if (source.compareToIgnoreCase("peer")==0) {
			System.out.print("Podaj adres serwera\t:\t");
			String address=Controller.getChoice();
			System.out.print("Podaj numer portu serwera\t:\t");
			int port = Controller.getInt();
			out=Model.ReceiveFromSocket(address, port, username, password, auth);
		}
		else {
			out= Model.ReceiveFromWebService(username, password, auth);
		}
		int newWorkers = Model.mergeAndResolveDuplicates(out);
		System.out.println("\nPobrano " + newWorkers + " nowych pracowników z bazy danych");
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
				System.out.print("Typ [X]ml/[G]zip/[Z]zip\t:\t");
				String[] options2={"G","Z","X"};
				String compresion=Controller.getChoice(options2);
				System.out.print("Nazwa pliku\t:\t");
				String filePath = Controller.getChoice();
				printStrip();
				System.out.println("[Enter]-zapisz");
				System.out.println("[q]-odrzuc");
				String[] options3={"","q"};
				if (Controller.getChoice(options3).compareToIgnoreCase("")==0)
				{
					if (compresion.compareToIgnoreCase("X")==0) {
						if (!Model.saveToXml(filePath))
							System.out.println("Blad pliku");
					}
					else if (!Model.Compress(filePath, compresion.charAt(0)))
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
					int newWorkers = Model.mergeAndResolveDuplicates(Model.dao.getWorkers());
					System.out.println("\nPobrano " + newWorkers + " nowych pracowników z bazy danych");
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
					String[] temp = filePath.split("\\.");
					if (temp[temp.length - 1].charAt(0) == 'X' 
							|| temp[temp.length - 1].charAt(0) == 'x') {
						System.out.println("Wczytano " + Model.readFromXml(filePath) + " nowych rekordów!");
					}
					else if (!Model.Decompress(filePath)) {
						System.out.println("Blad pliku");
					}
				}
			}
		}
	}
	public static void loadSettings() {
		System.out.print("Podaj port dla po³¹czenia tcp/ip\t:\t");
		Model.socketPort = Controller.getInt();
		
	}
}
