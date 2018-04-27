package com.josericardojunior.arch;

import java.util.ArrayList;
import java.util.List;

public class MatrixDescriptor {
	
	enum Equality{
		ROW,
		COL
	}

	private List<String> columnsDesc = new ArrayList<String>();
	private List<String> rowsDesc = new ArrayList<String>();
	private String rowType;
	private String colType;
	
	public MatrixDescriptor(String _rowType, String _colType) {
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
	
	public String getRowType() {
		return rowType;
	}

	public String getColType() {
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
	
	public boolean hasRow(String row){
		return rowsDesc.contains(row);
	}
	
	public boolean hasCol(String col){
		return columnsDesc.contains(col);
	}
}
