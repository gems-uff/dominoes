package com.josericardojunior.gitdataminer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.zip.CRC32;

import org.la4j.matrix.Matrix;
import org.la4j.matrix.functor.MatrixProcedure;
import org.la4j.matrix.sparse.CRSMatrix;

import com.josericardojunior.Native.*;

public class Matrix2D {	

	Matrix elements;
	
	private MatrixDescriptor matrixDescriptor;
	
	public MatrixDescriptor getMatrixDescriptor() {
		return matrixDescriptor;
	}

	public Matrix2D(MatrixDescriptor _matrixDescriptor){
		matrixDescriptor = _matrixDescriptor;
		elements = new CRSMatrix(matrixDescriptor.getNumRows(),
				matrixDescriptor.getNumCols());
		//elements = new float[matrixDescriptor.getNumRows() * matrixDescriptor.getNumCols()];
	}
	
	public void SetElement(String rowName, String colName, float value){
				
		elements.set(matrixDescriptor.getRowElementIndex(rowName),
				matrixDescriptor.getColElementIndex(colName), value);
		
	}
	
	public void AddToElement(String rowName, String colName, float value){
		
		int _rowIdx = matrixDescriptor.getRowElementIndex(rowName);
		int _colIdx = matrixDescriptor.getColElementIndex(colName);
		float v = (float)elements.get(_rowIdx, _colIdx);
		
		elements.set(_rowIdx, _colIdx, v + value);
	}
	
	public float GetElement(int row, int col){
		
		return (float) elements.get(row, col);
	}
	
	public float SumCol(int col){
		float sumCol = 0;
		
		for (int row = 0; row < matrixDescriptor.getNumRows(); row++ ){
			sumCol += GetElement(row, col);
		}
		
		return sumCol;
	}
	
	public float SumRow(int row){
		float sumRow = 0;
		
		for (int col = 0; col < matrixDescriptor.getNumCols(); col++ ){
			sumRow += GetElement(row, col);
		}
		
		return sumRow;
	}
	
	public float SumRow(int row, float threshold){
		float sumRow = 0;
		
		for (int col = 0; col < matrixDescriptor.getNumCols(); col++ ){
			float _elem = GetElement(row, col);
			
			if (_elem > threshold)
				sumRow += _elem; 	
		}
		
		return sumRow;
	}
	
	public float RowCount(int row, float threshold){
		float sumRow = 0;
		
		for (int col = 0; col < matrixDescriptor.getNumCols(); col++ ){
			float _elem = GetElement(row, col);
			
			if (_elem > threshold)
				sumRow += 1; 	
		}
		
		return sumRow;
	}
	
	public float GetElement(String row, String col){
		
		int _rowIndex = matrixDescriptor.getRowElementIndex(row);
		int _colIndex = matrixDescriptor.getColElementIndex(col);
		
		return GetElement(_rowIndex, _colIndex);
	}
	
	public Matrix2D Transpose(){
		MatrixDescriptor _newDescriptor = new MatrixDescriptor(
				this.matrixDescriptor.getColType(), 
				this.matrixDescriptor.getRowType());
		
		for (int i = 0; i < this.matrixDescriptor.getNumCols(); i++)
			_newDescriptor.AddRowDesc(this.matrixDescriptor.getColumnAt(i));
		
		for (int i = 0; i < this.matrixDescriptor.getNumRows(); i++)
			_newDescriptor.AddColDesc(this.matrixDescriptor.getRowAt(i));
		
		Matrix2D transpose = new Matrix2D(_newDescriptor);
		transpose.elements = elements.transpose();
		
		return transpose;
	}
	
	public Matrix2D DoGPUStandardScore(Matrix2D _meanSD){
		float [] thisLinearArray = LinearArray();
		float [] _meanSDLinearArray = _meanSD.LinearArray();
		
		float [] res = MatrixProcessor.GPUStandardScore(thisLinearArray, 
				matrixDescriptor.getNumRows(), matrixDescriptor.getNumCols(), 1,
				_meanSDLinearArray);
		
		Matrix2D result = new Matrix2D(matrixDescriptor);
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				result.elements.set(i, j, res[i * matrixDescriptor.getNumCols() + j]);
			}
		}
		
		return result;
	}
	
	public Matrix2D GPUMultiplication(Matrix2D other) throws Exception{
		
		MatrixDescriptor otherDescriptor = other.getMatrixDescriptor();
		
		if (matrixDescriptor.getNumCols() != otherDescriptor.getNumRows())
			throw new Exception("Matrix cannot be multiplied!");
		
		MatrixDescriptor resultDesc = new MatrixDescriptor(
				matrixDescriptor.getRowType(), 
				otherDescriptor.getRowType());
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++)
			resultDesc.AddRowDesc(
					matrixDescriptor.getRowAt(i));
		
		for (int i = 0; i < otherDescriptor.getNumCols(); i++)
			resultDesc.AddColDesc(
					otherDescriptor.getColumnAt(i));
		
		Matrix2D result = new Matrix2D(resultDesc);
		

		float [] linearArray = LinearArray();
		float [] linearArrayOther = other.LinearArray();
		float [] _multRes =  MatrixProcessor.GPUMatMult(linearArray, linearArrayOther,
				matrixDescriptor.getNumRows(), matrixDescriptor.getNumCols(),
				otherDescriptor.getNumCols());
		
		
		for (int i = 0; i < resultDesc.getNumRows(); i++){
			for (int j = 0; j < resultDesc.getNumCols(); j++){
				result.elements.set(i, j, _multRes[i * resultDesc.getNumCols() + j]);
			}
		}		
		return result;
	}
	
	public float[] LinearArray(){
		int numRows = matrixDescriptor.getNumRows();
		int numCols = matrixDescriptor.getNumCols();
		float[] tempData = new float[numRows * numCols];
		for (int i = 0; i < numRows; i++){
			for (int j = 0; j < numCols; j++){
				tempData[i * numCols + j] = (float)elements.get(i, j);
			}
		}
		
		return tempData;
	}
	
	public Matrix2D CPUMultiplication(Matrix2D other) throws Exception{
		
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
		
		Matrix2D result = new Matrix2D(resultDesc);		
				
		float [] linearArray = LinearArray();
		float [] linearArrayOther = other.LinearArray();
		float [] _multRes =  MatrixProcessor.CPUMatMult(linearArray, linearArrayOther,
				matrixDescriptor.getNumRows(), matrixDescriptor.getNumCols(),
				otherDescriptor.getNumCols());
		
		
		for (int i = 0; i < resultDesc.getNumRows(); i++){
			for (int j = 0; j < resultDesc.getNumCols(); j++){
				result.elements.set(i, j, _multRes[(i * resultDesc.getNumCols()) + j]);
			}
		}
		
		//Matrix2D result = new Matrix2D(resultDesc);
		//result.elements = elements.multiply(other.elements);
		return result;
			
		
		/*
		
		MatrixDescriptor otherDescriptor = other.getMatrixDescriptor();
		
		if (matrixDescriptor.getNumCols() != otherDescriptor.getNumRows())
			throw new Exception("Matrix cannot be multiplied!");
		
		MatrixDescriptor resultDesc = new MatrixDescriptor(
				matrixDescriptor.getRowType(), 
				otherDescriptor.getRowType());
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++)
			resultDesc.AddRowDesc(
					matrixDescriptor.getRowAt(i));
		
		for (int i = 0; i < otherDescriptor.getNumCols(); i++)
			resultDesc.AddColDesc(
					otherDescriptor.getColumnAt(i));
		
		Matrix2D result = new Matrix2D(resultDesc);
		
		for (int i = 0; i < resultDesc.getNumRows(); i++){
			for (int j = 0; j < resultDesc.getNumCols(); j++){
				
				float cellSum = 0;
				
				for (int k = 0; k < matrixDescriptor.getNumCols(); k++){
					cellSum += GetElement(i, k) * 
							other.GetElement(k, j);
				}
				
				result.SetElement(resultDesc.getRowAt(i),
						resultDesc.getColumnAt(j), cellSum);	
			}
		}
		
		return result;*/
	}
	
	public Matrix2D subMatrix(int startLine, int startColumn, 
			int numLines, int numColumns) throws Exception{
		
		if ( (numLines - startLine) > matrixDescriptor.getNumRows() ||
			 (numColumns - startColumn) > matrixDescriptor.getNumCols() )
			throw new Exception("subMatrix: out of bounds");
		
		
		MatrixDescriptor subMatrixDescriptor = 
				new MatrixDescriptor(matrixDescriptor.getRowType(), 
						matrixDescriptor.getColType());
		
		for (int i = 0; i < numLines; i++){
			subMatrixDescriptor.AddRowDesc(
					matrixDescriptor.getRowAt(startLine + i));
		}
		
		for (int i = 0; i < numColumns; i++){
			subMatrixDescriptor.AddColDesc(
					matrixDescriptor.getColumnAt(startColumn + i));
		}
		
		Matrix2D subMatrix = new Matrix2D(subMatrixDescriptor);
		
		for (int i = 0; i < numLines; i++){
			for (int j = 0; j < numColumns; j++){
				subMatrix.SetElement(
						subMatrixDescriptor.getRowAt(i),
						subMatrixDescriptor.getColumnAt(j),
						GetElement(startLine + i, startColumn + j));
			}
		}
		
		return subMatrix;
		
	}
	
	
	
	public void Debug(){
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			
			//matrixDescriptor.getRowAt(i) + "\t");
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				System.out.print(GetElement(i, j) + "\t");
			}
			System.out.println();
		}
	}
	
	public void ExportCSV(String filename){
		
		StringBuffer out = new StringBuffer();
		
		for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
			out.append(";");
			out.append(matrixDescriptor.getColumnAt(j));
		}
		out.append("\n");
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			
			out.append(matrixDescriptor.getRowAt(i) + ";");
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				out.append(GetElement(i, j) + ";");
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
		}	
	}
	
	public StringBuffer ExportCSV(){
		
		StringBuffer out = new StringBuffer();
		
		for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
			out.append(";");
			out.append(matrixDescriptor.getColumnAt(j));
		}
		out.append("\n");
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			
			out.append(matrixDescriptor.getRowAt(i) + ";");
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				out.append(GetElement(i, j) + ";");
			}
			out.append("\n");
		}
		
		return out;
	}
	
	public void SaveToDatabase(String dbName, String tableName){
		Connection conn;
					
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:" + dbName + ".sqlite");
			
			
			Statement smt = conn.createStatement();
			
			String sql = "CREATE TABLE " + tableName + "(" +
			"row STRING," + 
			"col String, value REAL);"; 
			
			smt.execute(sql);						
									
			for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
							
				StringBuffer out = new StringBuffer();				
				String rowName = matrixDescriptor.getRowAt(i);
				
			
				smt.execute("BEGIN TRANSACTION");
				for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
										
					smt.execute("INSERT INTO " + tableName + " (row, col, value) VALUES ('" + 
							rowName + "', '" + matrixDescriptor.getColumnAt(j) + "', " + 
							GetElement(i, j) + ");");
																																																		}
				System.out.println("Line " + i + " of " + matrixDescriptor.getNumRows() + ".");
				smt.execute("COMMIT");
			}
						

			smt.close();
			conn.close();
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		float [] m1 = {1, 0, 5,
		                0, 8, 9};
		float [] m2 = {1, 7,
                7, 2,
                10, 0};
		
		float []res = MatrixProcessor.CPUMatMult(m1, m2, 2, 3, 2);
		
		for (int i = 0; i < res.length; i++)
			System.out.println(res[i]);
	}
		
}