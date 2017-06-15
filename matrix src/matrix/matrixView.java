package matrix;

class matrixView {
	class Options
	{
		Options[] subOptions;
		String name;
	}
	Options options;
	matrixView()
	{
		options = new Options();
		options.name="Main Menu";
		options.subOptions=new Options[3];
		
		options.subOptions[0]=new Options();
		options.subOptions[0].name="Set input matrixes";
		
		options.subOptions[1]=new Options();
		options.subOptions[1].name="Arithmetic Options";
		options.subOptions[1].subOptions=new Options[3];
		options.subOptions[1].subOptions[0]=new Options();
		options.subOptions[1].subOptions[0].name="Transpose input matrixes";
		options.subOptions[1].subOptions[1]=new Options();
		options.subOptions[1].subOptions[1].name="Multiply input matrixes";
		options.subOptions[1].subOptions[2]=new Options();
		options.subOptions[1].subOptions[2].name="Transpose result matrix";
		
		options.subOptions[2]=new Options();
		options.subOptions[2].name="Show matrixes";
		options.subOptions[2].subOptions=new Options[2];
		options.subOptions[2].subOptions[0]=new Options();
		options.subOptions[2].subOptions[0].name="Show input matrixes";
		options.subOptions[2].subOptions[1]=new Options();
		options.subOptions[2].subOptions[1].name="Show result matrix";
	}
	private String showOptions(Options option, String count, int tabs)
	{
		String out="";
		for (int i=0; i<tabs; i++)
			out+="\t";
		out+=count+option.name+"\n";
		if (option.subOptions!=null)
			for (int i=0; i<option.subOptions.length; i++ )
				out+=showOptions(option.subOptions[i],count+Integer.toString(i+1)+".",tabs+1);
		return out;
	}
	void showMenu()
	{
		System.out.print(showOptions(options,"",0));
	}
	void showMenu(Options option, String count)
	{
		System.out.print(showOptions(option,count,0));
	}
	static void showMsg(String msg)
	{
		System.out.println(msg);
	}
	static void showError(String er)
	{
		System.err.println(er);
	}
	static void showMatrix(double[][] input)
	{
		for (int i = 0; i < input.length; i++)
		{
			for (int j = 0; j < input[i].length; j++)
					System.out.print(input[i][j]+"\t");
			System.out.print("\n");
		}
	}
}
