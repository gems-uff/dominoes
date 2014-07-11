package com.josericardojunior.gitdataminer;

public class Filter {

	public static Matrix2D Threashold(Matrix2D _mat, float threshold){
		MatrixDescriptor desc = _mat.getMatrixDescriptor();
		Matrix2D filtered = new Matrix2D(desc);
		
		for (int row = 0; row < desc.getNumRows(); row++){
			
			for (int col = 0; col < desc.getNumCols(); col++){
				
				float _v = _mat.GetElement(row, col);
				
				if (_v > threshold){
					filtered.SetElement(desc.getRowAt(row),
							desc.getColumnAt(col), _v);
				}
			}
		}
		
		return filtered;
		
	}
}
