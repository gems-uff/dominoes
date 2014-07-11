package com.josericardojunior.gitdataminer;

import java.util.ArrayList;
import java.util.List;

import com.josericardojunior.gitdataminer.Analyzer.InfoType;

public class MatrixDescriptor3D extends MatrixDescriptor {

	private InfoType depthType;
	private List<String> depthDesc = new ArrayList<String>();
	
	public MatrixDescriptor3D(InfoType _rowType, InfoType 
			_colType, InfoType _depthType){
		super(_rowType, _colType);
		
		depthType = _depthType;	
	}
	
	public int getNumDepths(){
		return depthDesc.size();
	}
	
	public InfoType getDepthType(){
		return depthType;
	}
	
	public void AddDepthDesc(String _depthDesc){
		depthDesc.add(_depthDesc);
	}
	
	public int getDepthElementIndex(String _name){
		return depthDesc.indexOf(_name);
	}
	
	public String getDepthAt(int depthIndex){
		return depthDesc.get(depthIndex);
	}
	
}
