package com.josericardojunior.gitdataminer;

import java.util.ArrayList;
import java.util.List;

import com.josericardojunior.gitdataminer.Analyzer.InfoType;

public class MatrixDescriptor {
	
	enum Equality{
		ROW,
		COL
	}

	private List<String> columnsDesc = new ArrayList<String>();
	private List<String> rowsDesc = new ArrayList<String>();
	private InfoType rowType;
	private InfoType colType;
	
	public MatrixDescriptor(InfoType _rowType, InfoType _colType) {
		rowType = _rowType;
		colType = _colType;
	}
	
	public boolean CheckEquality(Equality desc1, Equality desc2, 
			MatrixDescriptor other){
		
		List<String> thisDesc = 
				desc1 == Equality.ROW ? rowsDesc : columnsDesc;
		List<String> otherDesc = 
				desc2 == Equality.ROW ? other.rowsDesc : other.columnsDesc; 
		
		
		if (thisDesc.size() != otherDesc.size())
			return false;
		
		for (int i = 0; i < thisDesc.size(); i++ ){
			
			if (!thisDesc.get(i).equals(otherDesc.get(i)))
				return false;
		}
		
		return true;
	}
	
	public InfoType getRowType() {
		return rowType;
	}

	public InfoType getColType() {
		return colType;
	}

	public int getNumRows(){ 
		return rowsDesc.size();
	}
	
	public int getNumCols(){
		return columnsDesc.size();
	}
	
	public void AddRowDesc(String _rowDesc){
		rowsDesc.add(_rowDesc);
	}
	
	public void AddColDesc(String _colDesc){
		columnsDesc.add(_colDesc);
	}
	
	public int getRowElementIndex(String _name){
		return rowsDesc.indexOf(_name);
	}
	
	public int getColElementIndex(String _name){
		return columnsDesc.indexOf(_name);
	}
	
	public String getColumnAt(int colIndex){
		return columnsDesc.get(colIndex);
	}
	
	public String getRowAt(int rowIndex){
		return rowsDesc.get(rowIndex);
	}
}
