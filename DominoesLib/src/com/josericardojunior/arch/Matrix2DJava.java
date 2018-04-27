package com.josericardojunior.arch;

import java.util.ArrayList;
import java.util.List;

import org.la4j.matrix.functor.MatrixProcedure;
import org.la4j.matrix.sparse.CRSMatrix;


public class Matrix2DJava implements IMatrix2D {	

	private CRSMatrix data;
	
	private MatrixDescriptor matrixDescriptor;
	
	public MatrixDescriptor getMatrixDescriptor() {		
		return matrixDescriptor;
	}

	public Matrix2DJava(MatrixDescriptor _matrixDescriptor){
		
		matrixDescriptor = _matrixDescriptor;
		
		data = new CRSMatrix(matrixDescriptor.getNumRows(), 
				matrixDescriptor.getNumCols());
	}
	
	@Override
	public int getMemUsed() {
		return matrixDescriptor.getNumCols() * matrixDescriptor.getNumRows() * (Float.SIZE / 8);
	}	
	
	public void finalize(){
	}
	
	
	/*public float getElement(String row, String col){
		
		int _colIndex = matrixDescriptor.getColElementIndex(col);
		
		float[] rowData = getRow(row);
		return rowData[_colIndex];
	}*/
	
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
		
		Matrix2DJava result = new Matrix2DJava(resultDesc);
		
		Matrix2DJava otherJava = (Matrix2DJava)other; 
		
		result.data = (CRSMatrix) data.multiply(otherJava.data);
		
		
		return result;
	}
	
	public Matrix2DJava transpose(){
		MatrixDescriptor _newDescriptor = new MatrixDescriptor(
				this.matrixDescriptor.getColType(), 
				this.matrixDescriptor.getRowType());
		
		for (int i = 0; i < this.matrixDescriptor.getNumCols(); i++)
			_newDescriptor.AddRowDesc(this.matrixDescriptor.getColumnAt(i));
		
		for (int i = 0; i < this.matrixDescriptor.getNumRows(); i++)
			_newDescriptor.AddColDesc(this.matrixDescriptor.getRowAt(i));
		
		Matrix2DJava transpose = new Matrix2DJava(_newDescriptor);
		transpose.data = (CRSMatrix) data.transpose();
		
		return transpose;
	}
	
	
	public void Debug(){
		
		ArrayList<Cell> cells = getNonZeroData();
		
		int currentLine = -1;
		
		for (int i = 0; i < cells.size(); i++){
			Cell cell = cells.get(i);
			
			if (currentLine != cell.row ){
				System.out.println();
				currentLine = cell.row;
			}
			
			System.out.print(cell.value + "\t");
		}
	}
	
	public void ExportCSV(String filename){
		
		StringBuffer out = new StringBuffer();
		
		for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
			out.append(";");
			out.append(matrixDescriptor.getColumnAt(j));
		}
		out.append("\n");
		
	/*	for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			
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
		
		for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
			out.append(";");
			out.append(matrixDescriptor.getColumnAt(j));
		}
		out.append("\n");
		
		/*for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			float[] rowData = getRow(matrixDescriptor.getRowAt(i));
			out.append(matrixDescriptor.getRowAt(i) + ";");
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				out.append(rowData[j] + ";");
			}
			out.append("\n");
		}*/
		
		return out;
	}
	
	
	
	public float findMinValue(){
		
		return (float) data.min();
	}
	
	public float findMaxValue(){
		
		return (float) data.max();
	}

	@Override
	public void setData(ArrayList<Cell> cells) {
		for (Cell cell : cells){
			data.set(cell.row, cell.col, cell.value);;
		}
		
	}

	@Override
	public ArrayList<Cell> getNonZeroData() {
		
		ArrayList<Cell> cells = new ArrayList<Cell>();
		
		data.eachNonZero(new MatrixProcedure() {
			
			@Override
			public void apply(int row, int col, double value) {
				Cell cell = new Cell();
				cell.row = row;
				cell.col = col;
				cell.value = (float)value;
				cells.add(cell);
			}
		});
		
		return cells;
	}

	@Override
	public IMatrix2D reduceRows(boolean useGPU) {
		MatrixDescriptor _newDescriptor = new MatrixDescriptor(
				this.matrixDescriptor.getColType(), 
				this.matrixDescriptor.getRowType());
		

		_newDescriptor.AddRowDesc("SUM");
		
		for (int i = 0; i < this.matrixDescriptor.getNumCols(); i++)
			_newDescriptor.AddColDesc(this.matrixDescriptor.getColumnAt(i));
		
		Matrix2DJava reduced = new Matrix2DJava(_newDescriptor);
		
		float []rowSum = new float[this.matrixDescriptor.getNumCols()];
		ArrayList<Cell> nz = getNonZeroData();
		
		for (Cell c : nz){
			rowSum[c.col] += c.value;
		}
		
		ArrayList<Cell> resCells = new ArrayList<Cell>();
		
		for (int i = 0; i < rowSum.length; i++){
			if (Math.abs(rowSum[i]) > 0){
				resCells.add(new Cell(0, i, rowSum[i]));
			}
		}
		reduced.setData(resCells);
		
		return reduced;
	}

	@Override
	public IMatrix2D confidence(boolean useGPU) {
		List<Cell> nonZeros = getNonZeroData();
		
		ArrayList<Cell> newValues = new ArrayList<Cell>();
		
		for (Cell cell : nonZeros){
			Cell c = new Cell();
			c.row = cell.row;
			c.col = cell.col;
			
			float diagonal = (float) data.get(c.row, c.row);
			
			if (diagonal > 0)
				c.value = cell.value / diagonal;
			
			newValues.add(c);
		}
		
		Matrix2DJava confidenceM = new Matrix2DJava(getMatrixDescriptor());
		confidenceM.setData(newValues);
		
		return confidenceM;
	}	
}