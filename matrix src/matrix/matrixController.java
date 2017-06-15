package matrix;
import java.io.FileNotFoundException;
import java.util.Scanner;
import matrix.matrixView.Options;

public class matrixController{
	static public void doStep()
	{
		view.showMenu();
		readOption();
	}
	static void readOption()
	{
		Scanner input = new Scanner(System.in);
		try
		{
			checkOption(input.next("^[1-9]{1,}(\\.[1-9]{1,})*$"));
		}
		catch(java.util.InputMismatchException exc)
		{
			matrixView.showError("Type option number");
			readOption();
		}
	}
	static void checkOption(String input)
	{
		matrixView.showMsg(""); // insert empty line
		input+="."; // add dot becoz single digits is found by [number]. pattern
		int beginIndex=0, endIndex;
		Options actual=view.options; // start search option in tree
		try
		{
			while (beginIndex<input.length())
			{
				endIndex = input.indexOf('.', beginIndex+1);
				actual=actual.subOptions[Integer.parseInt(input.substring(beginIndex, endIndex))-1];
				beginIndex=endIndex+1;
			}
			if (actual.subOptions!=null) // this is't tree leaf - this is't option
			{
				view.showMenu(actual, input); // show more menus1
				readOption(); 					// and read option again 
			}
			else
				handleOption(input); // function number is correct
				
		}
		catch (IndexOutOfBoundsException e) //NullPointerException|IndexOutOfBoundsExceptionn
		{
			matrixView.showError("Invalid option number");
			readOption();
		}
		catch (NumberFormatException e) //NullPointerException|IndexOutOfBoundsExceptionn
		{
			matrixView.showError("Invalid option number");
			readOption();
		}
}
	static void handleOption(String input)
	{
		try 
		{
			switch (input)
			{
			case "1.":
				setMatrixes();
				break;
			case "2.1.":
				transpoze(1);
				transpoze(2);
				break;
			case "2.2.":
				multiply(1,2);
				break;
			case "2.3.":
				transpoze(3);
				break;
			case "3.1.":
				matrixView.showMatrix(model.getMatrix(1));
				matrixView.showMsg("");
				matrixView.showMatrix(model.getMatrix(2));
				break;
			case "3.2.":
				matrixView.showMatrix(model.getMatrix(3));
	        	break;
			}
		}
		catch (NullPointerException e)
		{
			matrixView.showError("Matrix non-exist");
			doStep();
		}
	}
	static private void setMatrixes()
	{
		int R1,R2,C1,C2;
		try
		{
			matrixView.showMsg("How many rows first matrix will be?");
			R1=Integer.parseInt((new Scanner(System.in).next("^[1-9][0-9]*$")));
			matrixView.showMsg("How many columns first matrix will be?");
			C1=Integer.parseInt((new Scanner(System.in).next("^[1-9][0-9]*$")));
			matrixView.showMsg("How many rows second matrix will be?");
			R2=Integer.parseInt((new Scanner(System.in).next("^[1-9][0-9]*$")));
			matrixView.showMsg("How many columns second matrix will be?");
			C2=Integer.parseInt((new Scanner(System.in).next("^[1-9][0-9]*$")));
		}
		catch(java.util.InputMismatchException exc)
		{
			matrixView.showError("Size must be integer and at least 1 " + exc.getMessage());
			setMatrixes();
			return;
		}		
		double[][] matrix1=new double[R1][];	
		double[][] matrix2=new double[R2][];
		for (int i=0; i<R1; i++)
			matrix1[i]=new double[C1];
		for (int i=0; i<R2; i++)
			matrix2[i]=new double[C2];
		String numbers2,numbers1;
		try
		{
			String rgx;			
			rgx="^[0-9]{1,}(\\.[0-9]{1,})?(;[0-9]{1,}(\\.[0-9]{1,})?){"+((R1*C1)-1)+"}$";
			matrixView.showMsg("Type first matrix numbers");
			numbers1=(new Scanner(System.in).next(rgx))+";";			
			rgx="^[0-9]{1,}(\\.[0-9]{1,})?(;[0-9]{1,}(\\.[0-9]{1,})?){"+((R2*C2)-1)+"}$";
			matrixView.showMsg("Type second matrix numbers");
			numbers2=(new Scanner(System.in).next(rgx))+";";
		}
		catch(java.util.InputMismatchException exc)
		{
			matrixView.showError("Numbers must be separated by semicolon and their amount must be correct");
			setMatrixes();
			return;
		}
		int beginIndex, endIndex;
		beginIndex=0;
		for (int i=0;i<R1;i++)
			for (int j=0;j<C1;j++)
			{
				endIndex = numbers1.indexOf(';', beginIndex+1);
				matrix1[i][j]=Double.parseDouble(numbers1.substring(beginIndex, endIndex));
				beginIndex=endIndex+1;
			}
		beginIndex=0;
		for (int i=0;i<R2;i++)
			for (int j=0;j<C2;j++)
			{
				endIndex = numbers2.indexOf(';', beginIndex+1);
				matrix2[i][j]=Double.parseDouble(numbers2.substring(beginIndex, endIndex));
				beginIndex=endIndex+1;
			}
		model.setMatrix(matrix1, 1);
		model.setMatrix(matrix2, 2);
	}
	static private void transpoze(int index)
	{
		double[][] matrix=model.getMatrix(index);
		double[][] temp=new double[matrix[0].length][];
		for (int i=0; i<matrix[0].length; i++)
		{
			temp[i]=new double[matrix.length];
			for (int j = 0; j < matrix.length; j++)
					temp[i][j]=matrix[j][i];
		}
		model.setMatrix(temp, index);
	}
	static private void multiply(int index1,int index2)
	{
		double[][] matrix1=model.getMatrix(index1);
		double[][] matrix2=model.getMatrix(index2);
		if (matrix1.length != matrix2[0].length)
		{
			matrixView.showError("First matrix row's size must be equal second matrix column size");
			doStep();
			return;
		}
		
		//create new matrix
		double[][] temp = new double[matrix1.length][];
		for (int i=0; i<matrix1.length; i++)
			temp[i]=new double[matrix2[0].length];
		
		//calculate new matrix values
		for (int count = 0; count < matrix1.length; count++)
			for (int count2 = 0; count2 < matrix2[0].length; count2++)
				for (int count3 = 0; count3 < matrix1[0].length; count3++)
					temp[count][count2] += matrix1[count][count3] * matrix2[count3][count2];
		
		model.setMatrix(temp, 3);		
	}
	static private matrixView view=new matrixView();		
	static private matrixModel model=new matrixModel();
}
