package mvc.controller;
import mvc.model.Model;
import mvc.view.*;

import java.util.Scanner;

public class Controller {
	public static int getInt() {
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		while (true)
		{
			try {
				return Integer.parseInt(reader.nextLine());
			}
			catch (NumberFormatException e) {
				System.out.print("Wartoœæ musi byæ liczb¹!");
			}
		}
	}
	public static String getChoice(String[] options)
	{
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		while (true)
		{
			String choise = reader.nextLine();
			for (String option : options)
			{
				if (option.compareToIgnoreCase(choise)==0)
					return option;
			}
			System.out.print("Bledny wybor. Sprobuj ponownie :");
		}
	}
	public static String getChoice()
	{
		Scanner reader = new Scanner(System.in);  // Reading from System.in
		return reader.nextLine();
	}
	public static void Menu()
	{
		View.loadSettings();
		Model.setup();
		while (true)
		{
			View.printMenu();
			String[] options={"1","2","3","4","5","q"};
			String choice=getChoice(options);
			if (choice.compareTo("1")==0)
			{
				View.printWorkers();
			}
			else if (choice.compareTo("2")==0)
			{
				View.addWorker();
			}
			else if (choice.compareTo("3")==0)
			{
				View.deleteWorker();
			}
			else if (choice.compareTo("4")==0)
			{
				View.Serialize();
			}
			else if (choice.compareTo("5")==0)
			{
				View.Network();
			}
			else
			{
				break;
			}
			View.printStrip();
			View.printStrip();
		}
	}
}
