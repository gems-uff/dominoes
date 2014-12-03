package com.josericardojunior.gitdataminer;

import java.awt.Menu;
import java.util.Map;
import java.util.Set;

import com.josericardojunior.gitdataminer.Analyzer.InfoType;

public class RateOfMean {
	
	public static String Mean = "Mean";
	public static String StandardDeviation = "StandardDeviation";
	
	public static Matrix2D Process(Matrix2D _mat){
		
		Matrix2D mean = ColumnMean(_mat);
		MatrixDescriptor _matDesc = _mat.getMatrixDescriptor();
		
		// Calculate the standard score
		Matrix2D rateOfMean = new Matrix2D(_matDesc);

		for (int column = 0; column < _matDesc.getNumCols(); column++){

			float colMean = mean.GetElement(0, column);

			for (int row = 0; row < _matDesc.getNumRows(); row++){
				float valrate = _mat.GetElement(row, column) / colMean;
				rateOfMean.SetElement(_matDesc.getRowAt(row), 
						_matDesc.getColumnAt(column), valrate);
			}

		}

		return rateOfMean;
	}
	
	public static Matrix2D ColumnMean(Matrix2D _mat){
		
		MatrixDescriptor _desc = _mat.getMatrixDescriptor();
		
		MatrixDescriptor _meanDesc = new MatrixDescriptor(InfoType.MEAN,
				_desc.getRowType());
		_meanDesc.AddRowDesc("Mean");
	
		for (int i = 0; i < _desc.getNumCols(); i++)
			_meanDesc.AddColDesc(_desc.getColumnAt(i));
		
		Matrix2D _meanMat = new Matrix2D(_meanDesc);
		
		for (int column = 0; column < _desc.getNumCols(); column++){
			
			float sum = 0;
			
			for (int row = 0; row < _desc.getNumRows(); row++)
				sum += _mat.GetElement(row, column);
			
			float mean = sum / _desc.getNumRows();
			
			_meanMat.SetElement("Mean",
					_desc.getColumnAt(column), mean);
			
		}
		
		return _meanMat;
	}
	
public static Matrix2D ColumnDepthMeanAndStandardDeviation(Matrix3D _cube, Definitions.Algorithm algorithm){
		
		
		Matrix2D _first = _cube.getLayer(Database.CUBE_DEPTH_PREFIX + 0);
		
		if (_first == null)
			return null;
		
		MatrixDescriptor _firstDesc = _first.getMatrixDescriptor();
		int _numColumns = _firstDesc.getNumCols();
		int _numRows = _firstDesc.getNumRows();
		
		MatrixDescriptor _meanSDDesc = new MatrixDescriptor(InfoType.MEAN, 
				_firstDesc.getColType());
		
		_meanSDDesc.AddRowDesc(Mean);
		_meanSDDesc.AddRowDesc(StandardDeviation);
		
		for (int i = 0; i < _firstDesc.getNumCols(); i++)
			_meanSDDesc.AddColDesc(_firstDesc.getColumnAt(i));
		
		Matrix2D _meanSDMat = new Matrix2D(_meanSDDesc);
		
		
		float [] sumCols = new float[_numColumns];
		for (Map.Entry<String, Matrix2D> entry : _cube.getIterator()){
			
			Matrix2D _entryMat = entry.getValue();
			MatrixDescriptor _entryDesc = _entryMat.getMatrixDescriptor();
						
			
			for (int col = 0; col < _entryDesc.getNumCols(); col++){
				
				for (int row = 0; row < _entryDesc.getNumRows(); row++){
					sumCols[col] += _entryMat.GetElement(row, col);
				}								
			}
		}
		
		
		for (int col = 0; col < _meanSDDesc.getNumCols(); col++){
						
			_meanSDMat.SetElement(Mean,
					_meanSDDesc.getColumnAt(col), sumCols[col] /
					(_numRows * _cube.getNumLayers()));
		}
		
		
		// Calculates the Standard Deviation	
		float [] variance = new float[_meanSDDesc.getNumCols()];
		for (Map.Entry<String, Matrix2D> entry : _cube.getIterator()){
			
			Matrix2D _entryMat = entry.getValue();
			MatrixDescriptor _entryDesc = _entryMat.getMatrixDescriptor();
									
				for (int col = 0; col < _entryDesc.getNumCols(); col++){
					
					float _colMedian = _meanSDMat.GetElement(Mean, _meanSDDesc.getColumnAt(col));
					
					for (int row = 0; row < _entryDesc.getNumRows(); row++){	
						float _deviate = _entryMat.GetElement(row, col) - _colMedian;						
						variance[col] += _deviate * _deviate;
					}
					
				}
		}
		
		for (int i = 0; i < _meanSDDesc.getNumCols(); i++) 
			_meanSDMat.SetElement(StandardDeviation,
					_meanSDDesc.getColumnAt(i), 
					(float) Math.sqrt(variance[i] / (_numRows * _cube.getNumLayers())));
							
		return _meanSDMat;
	}
	
	public static Matrix2D ColumnDepthMeanAndStandardDeviation(Matrix3D _cube, boolean eliminateZeroRows, 
			Definitions.Algorithm _algorithm){
		
		
		Matrix2D _first = _cube.getLayer(Database.CUBE_DEPTH_PREFIX + 0);
		
		if (_first == null)
			return null;
		
		MatrixDescriptor _firstDesc = _first.getMatrixDescriptor();
		int _numColumns = _firstDesc.getNumCols();
		int _numRows = _firstDesc.getNumRows();
		
		MatrixDescriptor _meanSDDesc = new MatrixDescriptor(InfoType.MEAN, 
				_firstDesc.getColType());
		
		_meanSDDesc.AddRowDesc(Mean);
		_meanSDDesc.AddRowDesc(StandardDeviation);
		
		for (int i = 0; i < _firstDesc.getNumCols(); i++)
			_meanSDDesc.AddColDesc(_firstDesc.getColumnAt(i));
		
		Matrix2D _meanSDMat = new Matrix2D(_meanSDDesc);
		
		float [] sumCols = new float[_numColumns];
		int [] developerWithCommit = new int[_numColumns];
		
		for (Map.Entry<String, Matrix2D> entry : _cube.getIterator()){
			
			Matrix2D _entryMat = entry.getValue();
			MatrixDescriptor _entryDesc = _entryMat.getMatrixDescriptor();
						
			
			for (int col = 0; col < _entryDesc.getNumCols(); col++){
				
				for (int row = 0; row < _entryDesc.getNumRows(); row++){
					float _v = _entryMat.GetElement(row, col);
					
					sumCols[col] += _v;
					
					if (eliminateZeroRows && _v > 0){
						developerWithCommit[col] = 1;
					}					
				}								
			}
		}
		
		int divideBy = _cube.getNumLayers() * _numRows;
		
		if (eliminateZeroRows){
			divideBy = 0;
			for (int i = 0; i < developerWithCommit.length; i++){
				divideBy += developerWithCommit[i];
			}
		}
		
		for (int col = 0; col < _meanSDDesc.getNumCols(); col++){
			
						
			_meanSDMat.SetElement(Mean,
					_meanSDDesc.getColumnAt(col), sumCols[col] / (float) divideBy);
		}
		
		
		// Calculates the Standard Deviation	
		float [] variance = new float[_meanSDDesc.getNumCols()];
		for (Map.Entry<String, Matrix2D> entry : _cube.getIterator()){
			
			Matrix2D _entryMat = entry.getValue();
			MatrixDescriptor _entryDesc = _entryMat.getMatrixDescriptor();
									
				for (int col = 0; col < _entryDesc.getNumCols(); col++){
					
					float _colMedian = _meanSDMat.GetElement(Mean, _meanSDDesc.getColumnAt(col));
					
					for (int row = 0; row < _entryDesc.getNumRows(); row++){	
						float _deviate = _entryMat.GetElement(row, col) - _colMedian;						
						variance[col] += _deviate * _deviate;
					}
					
				}
		}
		
		for (int i = 0; i < _meanSDDesc.getNumCols(); i++) 
			_meanSDMat.SetElement(StandardDeviation,
					_meanSDDesc.getColumnAt(i), 
					(float) Math.sqrt(variance[i] / (_numRows * _cube.getNumLayers())));
							
		return _meanSDMat;
	}
	
	public static Matrix2D ColumnMeanAndStandardDeviation(Matrix2D _mat, boolean removeNonZeros){
		
				
		MatrixDescriptor _matDesc = _mat.getMatrixDescriptor();
		int _numColumns = _matDesc.getNumCols();
		int _numRows = _matDesc.getNumRows();
		
		MatrixDescriptor _meanSDDesc = new MatrixDescriptor(InfoType.MEAN, 
				_matDesc.getColType());
		
		_meanSDDesc.AddRowDesc(Mean);
		_meanSDDesc.AddRowDesc(StandardDeviation);
		
		for (int i = 0; i < _matDesc.getNumCols(); i++)
			_meanSDDesc.AddColDesc(_matDesc.getColumnAt(i));
		
		Matrix2D _meanSDMat = new Matrix2D(_meanSDDesc);
		
		
		float [] sumCols = new float[_numColumns];
		int [] nzRows = new int[_numColumns];
					
			
		for (int col = 0; col < _matDesc.getNumCols(); col++){
				
			for (int row = 0; row < _numRows; row++){
				float _elem = _mat.GetElement(row, col);
				sumCols[col] += _elem;
				
				if (removeNonZeros){
					if (_elem > 0)
						nzRows[col]++;
				} else nzRows[col]++;
			}
			
		}
		
		
		for (int col = 0; col < _meanSDDesc.getNumCols(); col++){
						
			float _mean = sumCols[col] / nzRows[col];
			_meanSDMat.SetElement(Mean,
					_meanSDDesc.getColumnAt(col), _mean);
		}
		
		
		// Calculates the Standard Deviation	
		float [] variance = new float[_meanSDDesc.getNumCols()];

		for (int col = 0; col < _matDesc.getNumCols(); col++){
					
			float _colMedian = _meanSDMat.GetElement(Mean, _meanSDDesc.getColumnAt(col));
					
			for (int row = 0; row < _matDesc.getNumRows(); row++){	
				float _elem = _mat.GetElement(row, col);
				
				float _deviate = _elem > 0 ? _elem- _colMedian : 0;						
				variance[col] += _deviate * _deviate;
			}
		}
		
		for (int i = 0; i < _meanSDDesc.getNumCols(); i++){ 
			float _ssd = (float) Math.sqrt(variance[i] / nzRows[i]);
		
			_meanSDMat.SetElement(StandardDeviation,
					_meanSDDesc.getColumnAt(i), 
						_ssd);
		}
							
		return _meanSDMat;
	}
	
	
	public static void main(String[] args) {
		MatrixDescriptor testDesc = new MatrixDescriptor(InfoType.USER, InfoType.ARTIFACT);
		testDesc.AddColDesc("circumf");
		testDesc.AddColDesc("area_cy");
		testDesc.AddColDesc("area_co");
		testDesc.AddColDesc("draw");
		
		testDesc.AddRowDesc("Alice");
		testDesc.AddRowDesc("Carlos");
		testDesc.AddRowDesc("Bob");
		Matrix2D testMat = new Matrix2D(testDesc);
		
		
		testMat.SetElement("Alice", "circumf", 1);
		testMat.SetElement("Alice", "area_cy", 1);
		testMat.SetElement("Alice", "area_co", 1);
		testMat.SetElement("Alice", "draw", 14);
		
		testMat.SetElement("Carlos", "circumf", 5);
		testMat.SetElement("Carlos", "area_cy", 3);
		testMat.SetElement("Carlos", "area_co", 3);
		testMat.SetElement("Carlos", "draw", 5);
		
		testMat.SetElement("Bob", "circumf", 10);
		testMat.SetElement("Bob", "area_cy", 1);
		testMat.SetElement("Bob", "area_co", 1);
		testMat.SetElement("Bob", "draw", 1);
		
		System.out.println("DM Matrix");
		testMat.Debug();
		
		System.out.println();
		System.out.println("rate of mean Matrix");
		Matrix2D rm = RateOfMean.Process(testMat);
		rm.Debug();
	}

}
