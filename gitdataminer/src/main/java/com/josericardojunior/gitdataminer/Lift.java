package com.josericardojunior.gitdataminer;

import com.josericardojunior.gitdataminer.Analyzer.InfoType;

public class Lift {

	public static Matrix2D Process(int _order, Matrix2D _support, 
			Matrix2D _confidence){
		
		MatrixDescriptor _confDesc = _confidence.getMatrixDescriptor();		
		
		Matrix2D lift = new Matrix2D(_confDesc);
		
		for (int row = 0; row < _confDesc.getNumRows(); row++){
			
			for (int col = 0; col < _confDesc.getNumCols(); col++){
				
				//0.75 * 7578 / 4 
				float l = _confidence.GetElement(row, col) *
						(float) _order / _support.GetElement(col, col);
				
				lift.SetElement(_confDesc.getRowAt(row),
						_confDesc.getColumnAt(col), l);
			}
		}
		
		return lift;
	}
	
	public static void main(String[] args) {
		MatrixDescriptor _desc = new MatrixDescriptor(InfoType.COMMIT, InfoType.ARTIFACT);
		_desc.AddRowDesc("C1");
		_desc.AddRowDesc("C2");
		_desc.AddRowDesc("C3");
		_desc.AddRowDesc("C4");
		_desc.AddRowDesc("C5");
		
		_desc.AddColDesc("circumf");
		_desc.AddColDesc("area_cy");
		_desc.AddColDesc("area_co");
		_desc.AddColDesc("draw");
		
		Matrix2D m = new Matrix2D(_desc);
		m.SetElement("C1", "circumf", 1);
		m.SetElement("C1", "area_cy", 1);
		m.SetElement("C1", "area_co", 0);
		m.SetElement("C1", "draw", 1);
		
		m.SetElement("C2", "circumf", 0);
		m.SetElement("C2", "area_cy", 0);
		m.SetElement("C2", "area_co", 1);
		m.SetElement("C2", "draw", 1);
		
		m.SetElement("C3", "circumf", 0);
		m.SetElement("C3", "area_cy", 0);
		m.SetElement("C3", "area_co", 0);
		m.SetElement("C3", "draw", 1);
		
		m.SetElement("C4", "circumf", 1);
		m.SetElement("C4", "area_cy", 1);
		m.SetElement("C4", "area_co", 1);
		m.SetElement("C4", "draw", 1);
		
		m.SetElement("C5", "circumf", 0);
		m.SetElement("C5", "area_cy", 1);
		m.SetElement("C5", "area_co", 0);
		m.SetElement("C5", "draw", 1);
		
		try {
			Matrix2D support = m.Transpose().CPUMultiplication(m);
			
			System.out.println("Support Matrix");
			support.Debug();
			
			System.out.println();
			System.out.println("Confidence");
			Matrix2D confidence = Confidence.Process(support);
			confidence.Debug();
			
			System.out.println();
			System.out.println("Lift");
			Matrix2D lift = Lift.Process(5, support, confidence);
			lift.Debug();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
		
		

	}
}