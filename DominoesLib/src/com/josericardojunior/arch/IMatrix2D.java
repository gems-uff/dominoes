package com.josericardojunior.arch;

import java.util.ArrayList;

public interface IMatrix2D {
	
	
	public void setData(ArrayList<Cell> cells);
	public ArrayList<Cell> getNonZeroData();
	
	public int getMemUsed();
	
	public IMatrix2D transpose();
	
	public MatrixDescriptor getMatrixDescriptor();
	
	public void finalize();
		
	public IMatrix2D multiply(IMatrix2D other, boolean useGPU) throws Exception;
	
	public IMatrix2D reduceRows(boolean useGPU);
	
	public IMatrix2D confidence(boolean useGPU);
	
	public IMatrix2D meanAndSD(boolean useGPU);
	
	public IMatrix2D standardScore(boolean useGPU);
	
	public void Debug();
	
	public void ExportCSV(String filename);
	
	public StringBuffer ExportCSV();
	
	public float findMinValue();
	
	public float findMaxValue();
	
	
}
