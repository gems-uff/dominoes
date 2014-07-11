package com.josericardojunior.gitdataminer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class Matrix3D {
	
	private LinkedHashMap<String, Matrix2D> layers = new LinkedHashMap<String, Matrix2D>();
	
	
	public Set<Map.Entry<String,Matrix2D>> getIterator(){
		return layers.entrySet();
	}
	
	public Matrix2D getLayer(String _depth) {
		return layers.get(_depth);
	}
	
	public int getNumLayers(){
		return layers.size();
	}
	
	public void AddLayer(String _depth, Matrix2D _layer){
		layers.put(_depth, _layer);
	}
	
	public float GetElement(String row, String col, String depth){
		
		Matrix2D _layer = layers.get(depth);
		
		if (_layer == null){
			System.out.println("Matrix3D: Depth not found! - " + depth);
			return -1;
		}
		
		MatrixDescriptor _desc = _layer.getMatrixDescriptor();
		return _layer.GetElement(_desc.getRowElementIndex(row), 
				_desc.getColElementIndex(col));
		
	}
	
	public void ExportCSV(String filename){
		StringBuffer out = new StringBuffer();
		
		for (Map.Entry<String,Matrix2D> layer : layers.entrySet()){
			out.append("Layer: " + layer.getKey() + ";\n");
			out.append(layer.getValue().ExportCSV());						
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
	
	
	
	public void Debug(){
		
		for (Map.Entry<String,Matrix2D> layer : layers.entrySet()){
			System.out.println("Layer: " + layer.getKey());
			
			layer.getValue().Debug();
		}
	}
	
	//public Matrix2D StandardScore(Definitions.Algorithm algorithm){
		
	//}
	
}
