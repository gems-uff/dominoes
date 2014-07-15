package arch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import com.josericardojunior.Native.*;


public class Matrix2DJava implements IMatrix2D {	

	private float[] data;
	
	private MatrixDescriptor matrixDescriptor;
	
	public MatrixDescriptor getMatrixDescriptor() {		
		return matrixDescriptor;
	}

	public Matrix2DJava(MatrixDescriptor _matrixDescriptor) throws Exception{
		
		matrixDescriptor = _matrixDescriptor;
		
		data = new float[matrixDescriptor.getNumRows() * 
				matrixDescriptor.getNumCols()];
	}
	
	public void finalize(){
	}
	
	
	public void setData(float[] data){
		this.data = data;
	}
	
	public float[] getRow(String row){
		int _rowIndex = matrixDescriptor.getRowElementIndex(row);
		
		return Arrays.copyOfRange(data, _rowIndex * matrixDescriptor.getNumCols(),
				matrixDescriptor.getNumCols());
	}
	
	public float getElement(String row, String col){
		
		int _colIndex = matrixDescriptor.getColElementIndex(col);
		
		float[] rowData = getRow(row);
		return rowData[_colIndex];
	}
	
	public IMatrix2D multiply(IMatrix2D other, boolean useGPU) throws Exception{
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
		
		Matrix2DJava result = new Matrix2DJava(resultDesc);
		
		Matrix2DJava otherJava = (Matrix2DJava)other; 
		
		float [] resmat = new float[resultDesc.getNumRows() * resultDesc.getNumCols()];
		
		for (int i = 0; i < resultDesc.getNumRows(); i++){
			for (int j = 0; j < resultDesc.getNumCols(); j++){
				
				float cellSum = 0;
				
				for (int k = 0; k < matrixDescriptor.getNumCols(); k++){
					cellSum += data[i * matrixDescriptor.getNumCols() + k] * 
							otherJava.data[k * other.getMatrixDescriptor().getNumCols() + j];
				}
				
				resmat[i * resultDesc.getNumCols() + j] = cellSum;	
			}
			
		}
		
		result.setData(resmat);
		
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
		
		Matrix2DJava transpose = null;
		
		try {
			transpose = new Matrix2DJava(_newDescriptor);
			
			float []newData = new float[_newDescriptor.getNumRows() * _newDescriptor.getNumCols()];
			
			for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
				for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
					
					newData[j * _newDescriptor.getNumCols()] = data[i * matrixDescriptor.getNumCols() + j];
				}
			}
			
			transpose.setData(newData);
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		return transpose;
	}
	
	
	public void Debug(){
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			
			float[] rowData = getRow(matrixDescriptor.getRowAt(i));
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				System.out.print(rowData[j] + "\t");
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
			float[] rowData = getRow(matrixDescriptor.getRowAt(i));
			out.append(matrixDescriptor.getRowAt(i) + ";");
			
			for (int j = 0; j < matrixDescriptor.getNumCols(); j++){
				out.append(rowData[j] + ";");
			}
			out.append("\n");
		}
		
		return out;
	}
	
	
	
	public float findMinValue(){
		float _min = 0;
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			float[] _row = getRow(matrixDescriptor.getRowAt(i));
			
			for (int k = 0; k < matrixDescriptor.getNumCols(); k++){
				
				if (i == 0){
					_min = _row[0];
				} else {
					if (_row[k] < _min)
						_min = _row[k];
				}
			}
		}
		
		return _min;
	}
	
	public float findMaxValue(){
		float _max = 0;
		
		for (int i = 0; i < matrixDescriptor.getNumRows(); i++){
			float[] _row = getRow(matrixDescriptor.getRowAt(i));
			
			for (int k = 0; k < matrixDescriptor.getNumCols(); k++){
				
				if (i == 0){
					_max = _row[0];
				} else {
					if (_row[k] > _max)
						_max = _row[k];
				}
			}
		}
		
		return _max;
	}		
}