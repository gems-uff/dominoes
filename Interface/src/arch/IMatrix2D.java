package arch;

public interface IMatrix2D {
	
	public IMatrix2D transpose();
	
	public MatrixDescriptor getMatrixDescriptor();
	
	public void finalize();
	
	public void setData(float[] data);
	
	public float[] getRow(String row);
	
	public float getElement(String row, String col);
	
	public IMatrix2D multiply(IMatrix2D other, boolean useGPU) throws Exception;
	
	public void Debug();
	
	public void ExportCSV(String filename);
	
	public StringBuffer ExportCSV();
	
	public float findMinValue();
	
	public float findMaxValue();
	
	
}
