package com.josericardojunior.gitdataminer;

public class Analyzer {
	
	public enum Grain {
		FILE,
		CLASS,
		METHOD
	}
	
	public enum InfoType {
		ARTIFACT,
		COMMIT,
		USER,
		MEAN
	}
	
	public static class InvalidMatrix extends Exception {
		public InvalidMatrix(String message){
			super(message);
		}
	}
	
	/*
	 * Commit is always presented in a row, while artifact is presented as a column
	 */
	public static Matrix2D ExtractSupport(Matrix2D _relationalMatrix, 
			int firstCommit, int windowSize, float minimumSupport) throws InvalidMatrix{
		
		MatrixDescriptor _relationalMatrixDesc = _relationalMatrix.getMatrixDescriptor();
		
		if (_relationalMatrixDesc.getRowType() != InfoType.COMMIT ||
				_relationalMatrixDesc.getColType() != InfoType.ARTIFACT)
			throw new InvalidMatrix(
					"Column does not have a COMMIT info or Row does not have a ARTIFACT info");
		
		MatrixDescriptor desc = new MatrixDescriptor(InfoType.ARTIFACT, InfoType.ARTIFACT);
		for (int i = 0; i < _relationalMatrixDesc.getNumCols(); i++){
			desc.AddColDesc(_relationalMatrixDesc.getColumnAt(i));
			desc.AddRowDesc(_relationalMatrixDesc.getColumnAt(i));
		}
		
		
		Matrix2D support = new Matrix2D(desc);
		
        for (int j = 0; j < desc.getNumRows(); j++) {
            
            for (int i = 0; i < desc.getNumCols(); i++) {
                
                float ammount = 0;

                for (int k = 0; k < windowSize; k++) {
                	
                	if (_relationalMatrix.GetElement(firstCommit + k, j) > 0 &&
                			_relationalMatrix.GetElement(firstCommit + k, i) > 0){
                		ammount += 1.0f;
                	}
                }
                
                ammount /= (float) windowSize;
                
                support.SetElement(desc.getRowAt(j), 
        				desc.getColumnAt(i), 
        				ammount > minimumSupport ? ammount : 0);
            }
        }
        
		return support;
	}
	
	public static Matrix2D ExtractConfidence(Matrix2D _support, float minimunConfidence) 
			throws InvalidMatrix{
		
		MatrixDescriptor _supporMatrixDesc = _support.getMatrixDescriptor();
		if (_supporMatrixDesc.getRowType() != InfoType.ARTIFACT ||
				_supporMatrixDesc.getColType() != InfoType.ARTIFACT)
			throw new InvalidMatrix(
					"Column or Row does not have an ARTIFACT info");
		
		Matrix2D confidence = new Matrix2D(_supporMatrixDesc);

        for (int j = 0; j < _supporMatrixDesc.getNumRows(); j++) {
            for (int i = 0; i < _supporMatrixDesc.getNumCols(); i++) {
            	
            	if (_support.GetElement(j, j) > 0){
            				
            		float confidenceValue = _support.GetElement(j, i) /
            				_support.GetElement(j, j);
            		
            		confidence.SetElement(_supporMatrixDesc.getRowAt(j), 
            				_supporMatrixDesc.getRowAt(i), 
            				confidenceValue > minimunConfidence ? confidenceValue : 0);
            	}

            }
        }
        
        return confidence;
	}
	
	
	/*public static Matrix3D ExtractSupportTimeSeries(Matrix2D _relationalMatrix, 
			float minSupport) throws InvalidMatrix {
		
		MatrixDescriptor _relationalMatrixDesc = _relationalMatrix.getMatrixDescriptor();
		
		MatrixDescriptor3D _matDesc3D = new MatrixDescriptor3D(
				InfoType.ARTIFACT, InfoType.ARTIFACT, InfoType.COMMIT);
		
		for (int i = 0; i < _relationalMatrixDesc.getNumRows(); i++)
			_matDesc3D.AddDepthDesc(_relationalMatrixDesc.getRowAt(i));
		
		for (int i = 0; i < _relationalMatrixDesc.getNumCols(); i++){
			_matDesc3D.AddColDesc(_relationalMatrixDesc.getColumnAt(i));
			_matDesc3D.AddRowDesc(_relationalMatrixDesc.getColumnAt(i));
		}
		
		Matrix3D _result = new Matrix3D(_matDesc3D);
		for (int i = 0; i < _relationalMatrixDesc.getNumRows(); i++){
			Matrix2D _layerSupport =  ExtractSupport(_relationalMatrix, 
					0, i+1, minSupport);
			
			_result.SetDepth(_layerSupport, _matDesc3D.getDepthAt(i));
		}
		
		return _result;
	}
	
	
	public static Matrix3D ExtractConfidenceTimeSeries(Matrix3D _support, 
			float minConfidence) throws InvalidMatrix{
		
		MatrixDescriptor3D _supporMatrixDesc = _support.getMatrixDescriptor();
		
		if (_supporMatrixDesc.getRowType() != InfoType.ARTIFACT ||
				_supporMatrixDesc.getColType() != InfoType.ARTIFACT ||
				_supporMatrixDesc.getDepthType() != InfoType.COMMIT)
			throw new InvalidMatrix(
					"Column or Row does not have an ARTIFACT info or "
					+ "depth does not have an DEPTH info");
		
		Matrix3D confidence = new Matrix3D(_supporMatrixDesc);

		
		for (int i = 0; i < _supporMatrixDesc.getNumDepths(); i++){
			
			Matrix2D supportSlice = _support.GetSlice(_supporMatrixDesc.getDepthAt(i));
			Matrix2D confidenceSlice = ExtractConfidence(supportSlice, minConfidence);
			
			confidence.SetDepth(confidenceSlice, _supporMatrixDesc.getDepthAt(i));
		}
		
		
		return confidence;
	}*/
	
	public static Matrix2D ExtractTaskDepedency(Matrix2D _artifactDep, 
			Matrix2D _commits) throws Exception{
		
		MatrixDescriptor _artifactDesc = _artifactDep.getMatrixDescriptor();
		MatrixDescriptor _commitDesc = _commits.getMatrixDescriptor();
		
		if (_artifactDesc.getColType() != InfoType.ARTIFACT ||
				_artifactDesc.getRowType() != InfoType.ARTIFACT)
			throw new InvalidMatrix(
					"Column or Row does not have an ARTIFACT info or "
					+ "depth does not have an DEPTH info");
		
		if (_commitDesc.getRowType() != InfoType.COMMIT ||
				_commitDesc.getColType() != InfoType.ARTIFACT)
			throw new InvalidMatrix(
					"Row does not COMMIT type or Column does "
					+ "not have an ARTIFACT type");
		
		
		
		Matrix2D taskFileDep = _commits.CPUMultiplication(_artifactDep);
		Matrix2D taskDep = taskFileDep.CPUMultiplication(_commits.Transpose());
		
		return taskDep;
	}
	
	public static Matrix2D ExtractCommitSubMatrix(Matrix2D _relationalMatrix,
			int startCommit, int numCommits) throws InvalidMatrix{
		MatrixDescriptor _relationalMatrixDesc = _relationalMatrix.getMatrixDescriptor();
		
		if (_relationalMatrixDesc.getRowType() != InfoType.COMMIT ||
				_relationalMatrixDesc.getColType() != InfoType.ARTIFACT)
			throw new InvalidMatrix(
					"Column does not have a COMMIT info or Row does not have a ARTIFACT info");
		
		
		MatrixDescriptor subDesc = new MatrixDescriptor(InfoType.COMMIT, 
				InfoType.ARTIFACT);
		
		for (int i = 0; i < numCommits; i++){
			subDesc.AddRowDesc(
					_relationalMatrixDesc.getRowAt(startCommit + i));
		}
		
		for (int j = 0; j < _relationalMatrixDesc.getNumCols(); j++){
			
			for (int i = 0; i < numCommits; i++){
				if (_relationalMatrix.GetElement(startCommit + i, j) > 0){
					subDesc.AddColDesc(
							_relationalMatrixDesc.getColumnAt(j));
					break;
				}
			}
		}
		
		
		Matrix2D subMatrix = new Matrix2D(subDesc);
		
		for (int i = 0; i < subDesc.getNumRows(); i++){
			
			String rowName = subDesc.getRowAt(i);
			
			for (int j = 0; j < subDesc.getNumCols(); j++){
				
				String colName = subDesc.getColumnAt(j);
				
				subMatrix.SetElement(rowName, colName,
					_relationalMatrix.GetElement(
						_relationalMatrixDesc.getRowElementIndex(rowName),
						_relationalMatrixDesc.getColElementIndex(colName)));
			}
		}
		
		return subMatrix;
	}
	
}
