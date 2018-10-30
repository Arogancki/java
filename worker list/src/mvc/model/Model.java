package mvc.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import mvc.model.Worker.*;

public class Model {
	public static DAO dao = new DAO();
	public static TreeMap<String, Worker> persons = new TreeMap<String, Worker>();
	public static Boolean Compress(String filePath, char compresion)
	{
		Boolean output=false;
		ObjectOutputStream oos =null;
		ZipOutputStream zos=null;
		try  
		{
            if (compresion=='G')
            {
            	oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(new File(filePath))));
            }
            else
            {
                 zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));
                 zos.putNextEntry(new ZipEntry("Object"));
                 oos= new ObjectOutputStream(zos);
            }
            oos.writeObject(Model.persons);
			oos.flush();
			oos.close();
			output=true;
		} 
		catch (Exception ex) {}
		finally 
		{ 
			try {
					if (oos!=null)oos.close();
					return output;
				}
			catch (IOException e) {return output;}
		}	
	}
	
	public static Boolean Decompress(String filePath)
	{
		Boolean output=false;
		ObjectInputStream ois=null;
		ZipInputStream zis=null;
		try{
			String[] temp=filePath.split("\\.");
			char compresion=temp[temp.length-1].charAt(0);
			if (compresion=='g'||compresion=='G')
			{
				 ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(new File(filePath))));
			}
			else if (compresion=='z'||compresion=='Z')
			{
		            zis = new ZipInputStream(new FileInputStream(filePath));
		            zis.getNextEntry();
		            ois= new ObjectInputStream(zis);
			}
			else
				throw new ArrayIndexOutOfBoundsException();
			Model.persons= (TreeMap<String, Worker>) ois.readObject();
			output=true;
		}
		catch (ArrayIndexOutOfBoundsException e) {System.out.println("Brak rozszerzenia");}
		catch (Exception ex) {}
		finally 
		{ 
			try {
				if (ois!=null)ois.close();
				if (zis!=null)zis.close();
					return output;
				}
			catch (IOException e) {return output;}
		}				
	}
	public static TreeMap<String, Worker> mergeWorkers(TreeMap<String, Worker> workers) {
		TreeMap<String, Worker> duplicates = new TreeMap<String, Worker>();
		for(Map.Entry<String, Worker> entry : workers.entrySet()) {
			Worker newWorker = entry.getValue();
			String newWorkerPesel = newWorker.getPesel();
			Worker duplicate = persons.get(newWorkerPesel);
			if (duplicate!=null) {
				if (!newWorker.equals(duplicate))
					duplicates.put(newWorkerPesel, newWorker);
			}
			else {
				persons.put(newWorkerPesel, newWorker);
			}
		}
		return duplicates;
	}
}
