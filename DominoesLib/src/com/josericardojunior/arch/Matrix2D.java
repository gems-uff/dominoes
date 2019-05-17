package com.josericardojunior.arch;

import java.util.ArrayList;

import com.josericardojunior.Native.*;


public class Matrix2D implements IMatrix2D {	

	private long matPointer = 0;
	
	private MatrixDescriptor matrixDescriptor;
	
	public MatrixDescriptor getMatrixDescriptor() {		
		return matrixDescriptor;
	}
	
	@Override
	public int getMemUsed(){
		return matrixDescriptor.getNumCols() * matrixDescriptor.getNumRows() * (Float.SIZE / 8);
	}

	public Matrix2D(MatrixDescriptor _matrixDescriptor) throws Exception{
		
		if (!Session.isSessionStarted())
			throw new Exception("Session is not started");
		
		System.out.println("Creating matrix. Rows: " + _matrixDescriptor.getNumRows() + 
				" Cols: " + _matrixDescriptor.getNumCols());
		
		matrixDescriptor = _matrixDescriptor;
		
		matPointer = MatrixProcessor.createMatrixData(matrixDescriptor.getNumRows(),
				matrixDescriptor.getNumCols());
		
		Session.register2DMatrix(this);
	}
	
	public void finalize(){
		MatrixProcessor.deleteMatrixData(matPointer);
	}
	
	
	
	public IMatrix2D multiply(IMatrix2D other, boolean useGPU) throws Exception{
		MatrixDescriptor otherDescriptor = other.getMatrixDescriptor();
		
		if (matrixDescriptor.getNumCols() != otherDescriptor.getNumRows())
			throw new Exception("Matrix cannot be multiplied!");
		
		MatrixDescriptor resultDesc = new MatrixDescriptor(
				matrixDescriptor.getRowType(), 
				otherDescriptor.getColType());
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++)
			resultDesc.AddRowDesc(
					matrixDescriptor.getRowAt(i));
		
		for (int i = 0; i < otherDescriptor.getNumCols(); i++)
			resultDesc.AddColDesc(
					otherDescriptor.getColumnAt(i));
		
		
		System.out.println("Matrix 1: Rows: " + matrixDescriptor.getNumRows() + " Cols: " + matrixDescriptor.getNumCols() +
				" Size: " + getMemUsed());
		System.out.println("Matrix 2: Rows: " + otherDescriptor.getNumRows() + " Cols: " + otherDescriptor.getNumCols() +
				" Size: " + other.getMemUsed());
		
		System.out.println("1) Operation: Multiplication - Using " + getMemUsed() + other.getMemUsed() + " KB of GPU Memory.");
		
		
		Matrix2D result = new Matrix2D(resultDesc);
	
		
		System.out.println("2) Operation: Multiplication - Using " + getMemUsed() + other.getMemUsed() + " KB of GPU Memory.");
		
		MatrixProcessor.multiply(matPointer, ((Matrix2D)other).matPointer,
				result.matPointer, useGPU);
		System.out.println("Releasing " + getMemUsed() + other.getMemUsed() + " KB of GPU Memory.");
		
		return result;
	}
	
	public Matrix2D transpose(){
		MatrixDescriptor _newDescriptor = new MatrixDescriptor(
				this.matrixDescriptor.getColType(), 
				this.matrixDescriptor.getRowType());
		
		for (int i = 0; i < this.matrixDescriptor.getNumCols(); i++)
			_newDescriptor.AddRowDesc(this.matrixDescriptor.getColumnAt(i));
		
		for (int i = 0; i < this.matrixDescriptor.getNumRows(); i++)
			_newDescriptor.AddColDesc(this.matrixDescriptor.getRowAt(i));
		
		Matrix2D transpose = null;
		
		try {
			transpose = new Matrix2D(_newDescriptor);
			System.out.println("Operation: Transposing - Using " + getMemUsed() + " KB of GPU Memory.");
			MatrixProcessor.transpose(matPointer, transpose.matPointer);
			System.out.println("Releasing " + getMemUsed() + " KB of GPU Memory.");
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		return transpose;
	}
	
	
	public void Debug(){
		
		/*for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			
			float[] rowData = getRow(matrixDescriptor.getRowAt(i));
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				System.out.print(rowData[j] + "\t");
			}
			System.out.println();
		}*/
	}
	
	public void ExportCSV(String filename){
		
		/*StringBuffer out = new StringBuffer();
		
		for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
			out.append(";");
			out.append(matrixDescriptor.getColumnAt(j));
		}
		out.append("\n");
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			
			float[] rowData = getRow(matrixDescriptor.getRowAt(i));
			
			out.append(matrixDescriptor.getRowAt(i) + ";");
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				out.append(rowData[j] + ";");
			}
			out.append("\n");
		}
		
		File f = new File(filename);
		try {
			f.createNewFile();
			
			FileWriter fw = new FileWriter(f.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(out.toString());
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	*/
	}
	
	public StringBuffer ExportCSV(){
		
		StringBuffer out = new StringBuffer();
		/*
		for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
			out.append(";");
			out.append(matrixDescriptor.getColumnAt(j));
		}
		out.append("\n");
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			float[] rowData = getRow(matrixDescriptor.getRowAt(i));
			out.append(matrixDescriptor.getRowAt(i) + ";");
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				out.append(rowData[j] + ";");
			}
			out.append("\n");
		}*/
		
		return out;
	}
	
	
	
	
	/*public static void main(String[] args) {
		float [] m1 = {1, 0, 5,
		                0, 8, 9};
		float [] m2 = {1, 7,
                7, 2,
                10, 0};
		
		Session.startSession();
		
		MatrixDescriptor desc1 = new MatrixDescriptor("T1", "T2");
		desc1.AddRowDesc("R1");
		desc1.AddRowDesc("R2");
		desc1.AddColDesc("C1");
		desc1.AddColDesc("C2");
		desc1.AddColDesc("C3");
		
		try {
			Matrix2D mat1 = new Matrix2D(desc1);
			
		
		mat1.setData(m1);
		mat1.Debug();
		
		MatrixDescriptor desc2 = new MatrixDescriptor("T1", "T2");
		desc2.AddRowDesc("R1");
		desc2.AddRowDesc("R2");
		desc2.AddRowDesc("R3");
		desc2.AddColDesc("C1");
		desc2.AddColDesc("C2");
		Matrix2D mat2 = new Matrix2D(desc2);
		mat2.setData(m2);
		mat2.Debug();

		
		Matrix2D res = mat1.multiply(mat2, false);
		res.Debug();
		
		
		//res.Debug();
		} catch (Exception ex){
		}
		
		
		
		Session.closeSection();
	}*/
	
	public float findMinValue(){
		return MatrixProcessor.getMin(matPointer);
	}
	
	public float findMaxValue(){
		return MatrixProcessor.getMax(matPointer);
	}

	@Override
	public void setData(ArrayList<Cell> cells) {
		
		int[] rows = new int[cells.size()];
		int[] cols = new int[cells.size()];
		float[] data = new float[cells.size()];
		
		for (int i = 0; i < cells.size(); i++){
			Cell cell = cells.get(i);
			
			rows[i] = cell.row;
			cols[i] = cell.col;
			data[i] = cell.value;		
		}
		
		MatrixProcessor.setData(matPointer, rows, cols, data);
	}

	@Override
	public ArrayList<Cell> getNonZeroData() {
		
		java_to_c_info[] nzList = MatrixProcessor.getNonZeroData(matPointer);
		ArrayList<Cell> cellList = new ArrayList<Cell>();
		
		for (java_to_c_info nz : nzList){
			cellList.add(new Cell(nz.row, nz.col, nz.value));
		}
		
		return cellList;
	}

	@Override
	public IMatrix2D reduceRows(boolean useGPU) {
		MatrixDescriptor _newDescriptor = new MatrixDescriptor(
				this.matrixDescriptor.getColType(), 
				this.matrixDescriptor.getRowType());
		
		_newDescriptor.AddRowDesc("SUM");
		
		for (int i = 0; i < this.matrixDescriptor.getNumCols(); i++)
			_newDescriptor.AddColDesc(this.matrixDescriptor.getColumnAt(i));
		
		Matrix2D reduced = null;
		
		try {
			reduced = new Matrix2D(_newDescriptor);
			System.out.println("Operation: Reduction - Using " + getMemUsed() + " KB of GPU Memory.");
			MatrixProcessor.reduceRow(matPointer, reduced.matPointer, useGPU);
			System.out.println("Releasing " + getMemUsed() + " KB of GPU Memory.");
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		return reduced;
	}

	@Override
	public IMatrix2D confidence(boolean useGPU) {
		MatrixDescriptor _newDescriptor = this.matrixDescriptor;
		
		Matrix2D confidence = null;
		
		try {
			System.out.println("Operation: Confidence - Using " + getMemUsed() + " KB of GPU Memory.");
			confidence = new Matrix2D(_newDescriptor);
			MatrixProcessor.confidence(matPointer, confidence.matPointer, useGPU);
			System.out.println("Releasing " + getMemUsed() + " KB of GPU Memory.");
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		return confidence;
	}

	@Override
	public IMatrix2D meanAndSD(boolean useGPU) {
		MatrixDescriptor _newDescriptor = new MatrixDescriptor(
				this.matrixDescriptor.getColType(), 
				this.matrixDescriptor.getRowType());
		
		_newDescriptor.AddRowDesc("MEAN");
		_newDescriptor.AddRowDesc("SD");
		
		for (int i = 0; i < this.matrixDescriptor.getNumCols(); i++)
			_newDescriptor.AddColDesc(this.matrixDescriptor.getColumnAt(i));
		
		Matrix2D meanSD = null;
		
	//	try {
			// T
	//		meanSD = new Matrix2D(_newDescriptor);
	//		MatrixProcessor
	//	}
		
		
		// TODO Implement meanAndSD
		return null;
	}

	@Override
	public IMatrix2D standardScore(boolean useGPU) {
		// TODO Implement standardScore
		return null;
	}		
}