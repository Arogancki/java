package matrix;

class matrixModel {
	private double[][][] matrix=new double[3][][];
	void setMatrix(double[][] input, int index)
	{
		matrix[index-1]=new double[input.length][];
		for (int i = 0; i < input.length; i++)
			matrix[index-1][i]=new double[input[0].length];
		matrix[index-1]=input;
	}
	double[][] getMatrix(int index)
	{
		return(matrix[index-1]);
	}
}
