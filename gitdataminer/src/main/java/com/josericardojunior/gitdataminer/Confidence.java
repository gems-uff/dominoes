package com.josericardojunior.gitdataminer;

public class Confidence {

	public static Matrix2D Process(Matrix2D _support){
		MatrixDescriptor _supportMatrixDesc = _support.getMatrixDescriptor();
		
		Matrix2D confidence = new Matrix2D(_supportMatrixDesc);

        for (int j = 0; j < _supportMatrixDesc.getNumRows(); j++) {
            for (int i = 0; i < _supportMatrixDesc.getNumCols(); i++) {
            	
            	if (_support.GetElement(j, j) > 0){
            				
            		float confidenceValue = _support.GetElement(j, i) /
            				_support.GetElement(j, j);
            		
            		confidence.SetElement(_supportMatrixDesc.getRowAt(j), 
            				_supportMatrixDesc.getRowAt(i), confidenceValue);
            	}

            }
        }
        
        return confidence;
	}
}
