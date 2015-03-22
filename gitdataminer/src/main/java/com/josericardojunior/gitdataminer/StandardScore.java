package com.josericardojunior.gitdataminer;

import java.util.Map.Entry;
import java.util.Map;
import java.util.Set;

import com.josericardojunior.gitdataminer.Analyzer.InfoType;
import com.josericardojunior.gitdataminer.Definitions.Algorithm;

public class StandardScore {
	
	public static Matrix2D Process(Matrix2D _mat, boolean removeNZRows){
		
		MatrixDescriptor desc = _mat.getMatrixDescriptor();
		
		// Calculates the mean
		System.out.println("Calculating Mean and Standar Deviation");
		Matrix2D meanSDMat = RateOfMean.ColumnMeanAndStandardDeviation(_mat, removeNZRows);
		
		
		// Calculate the standard score
		Matrix2D standardScore = new Matrix2D(desc);
		int numColumns = desc.getNumCols();
		
		for (int column = 0; column < numColumns; column++){
			System.out.println("Processing column " + column + " of " + numColumns);
			float _mean = meanSDMat.GetElement(RateOfMean.Mean, desc.getColumnAt(column));
			float _sd = meanSDMat.GetElement(RateOfMean.StandardDeviation, desc.getColumnAt(column));
			
			boolean denZero =  Float.compare(0, _sd) == 0;
			
			for (int row = 0; row < desc.getNumRows(); row++){
				
				float z = 0;
				if (!denZero)					
					z = (_mat.GetElement(row, column) - _mean) /
						_sd;
				
				standardScore.SetElement(desc.getRowAt(row), 
						desc.getColumnAt(column), z);
			}
			
		}
		
		return standardScore;
	}
	
	public static Matrix3D Process(Matrix3D _cube, Definitions.Algorithm _algorithm){
		
		Matrix3D res = new Matrix3D();
		Set<Map.Entry<String,Matrix2D>> records = _cube.getIterator();
		
		// Mean and standard deviation
		System.out.println("Processing Mean and SD");
		Matrix2D _meanMat = RateOfMean.ColumnDepthMeanAndStandardDeviation(_cube, true, _algorithm);
		
		System.out.println("Processing layers...");
		int c = 0;
		boolean print = false;
		boolean print2 = false;
		for (Map.Entry<String, Matrix2D> entry : records){
						
			Matrix2D layer = entry.getValue();
			MatrixDescriptor layerDesc = layer.getMatrixDescriptor();
			Matrix2D layerSS = new Matrix2D(layerDesc);
			//Matrix2D layerSS = layer.DoGPUStandardScore(_meanMat);
						
			for (int col = 0; col < layerDesc.getNumCols(); col++){								
				String colName = layerDesc.getColumnAt(col);
				float _mean = _meanMat.GetElement(RateOfMean.Mean, layerDesc.getColumnAt(col));
				float _sd = _meanMat.GetElement(RateOfMean.StandardDeviation, colName);
				
				for (int row = 0; row < layerDesc.getNumRows(); row++){
					
					
					float _vv = layer.GetElement(row, col);
					if (_vv >= 8 && print == false){
						print = true;
						float _ss = (layer.GetElement(row, col) - _mean) / _sd;
						System.out.println(_ss);
					}
					

					
					layerSS.SetElement(layerDesc.getRowAt(row),
							layerDesc.getColumnAt(col),
							(layer.GetElement(row, col) - _mean) / _sd);
				}
				

			}
			
			if (print == true && print2 == false){
				print2 = true;
				layerSS.Debug();
				
			}
			
			res.AddLayer(entry.getKey(), layerSS);
			
			System.out.println("Standard Score of matrix " + c + " of " + records.size());
			c++;
		}
				
		
		return res;
		
	}
	
	
	
	
	
	private static Matrix2D SquaredVariance(Matrix2D _mat, Matrix2D _mean){
		
		MatrixDescriptor _meanDesc = _mean.getMatrixDescriptor();
		MatrixDescriptor _matDesc = _mat.getMatrixDescriptor();
		
		Matrix2D squaredVariance = new Matrix2D(_matDesc);
		
		
		for (int column = 0; column < _matDesc.getNumCols(); column++){
			
			float columnMean = _mean.GetElement(0, column);
			
			for (int row = 0; row < _matDesc.getNumRows(); row++){
				float res = _mat.GetElement(row, column) - columnMean;
				
				squaredVariance.SetElement(_matDesc.getRowAt(row),
						_matDesc.getColumnAt(column), res * res);
			}
		}
		
		return squaredVariance;
	}
	
	public static void Test2D(){
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
		System.out.println("z-score Matrix");
		Matrix2D ss = StandardScore.Process(testMat, true);
		ss.Debug();
	}
	
	public static void Test3D(){
		MatrixDescriptor testDesc = new MatrixDescriptor(InfoType.USER, InfoType.ARTIFACT);
		testDesc.AddColDesc("circumf");
		testDesc.AddColDesc("area_cy");
		testDesc.AddColDesc("area_co");
		
		testDesc.AddRowDesc("Alice");
		testDesc.AddRowDesc("Carlos");
		Matrix2D testMat = new Matrix2D(testDesc);
		
		
		testMat.SetElement("Alice", "circumf", 2);
		testMat.SetElement("Alice", "area_cy", 4);
		testMat.SetElement("Alice", "area_co", 6);

		
		testMat.SetElement("Carlos", "circumf", 6);
		testMat.SetElement("Carlos", "area_cy", 8);
		testMat.SetElement("Carlos", "area_co", 2);
		
		
		Matrix2D layer2 = new Matrix2D(testDesc);
	
		layer2.SetElement("Alice", "circumf", 7);
		layer2.SetElement("Alice", "area_cy", 4);
		layer2.SetElement("Alice", "area_co", 2);

		
		layer2.SetElement("Carlos", "circumf", 2);
		layer2.SetElement("Carlos", "area_cy", 9);
		layer2.SetElement("Carlos", "area_co", 4);
		
		
		Matrix3D _cube = new Matrix3D();
		_cube.AddLayer(Database.CUBE_DEPTH_PREFIX+0, testMat);
		_cube.AddLayer(Database.CUBE_DEPTH_PREFIX+1, layer2);
	
		System.out.println();
		System.out.println("z-score cube");
		Matrix3D ssc = StandardScore.Process(_cube, Algorithm.CPU);
		ssc.Debug();
	}
	
	
	/// Test
	public static void main(String[] args) {
		
		Test3D();
		
		
		
	}
}
